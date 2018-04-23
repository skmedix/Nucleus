/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.docgen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.configurate.ConfigurateHelper;
import io.github.nucleuspowered.nucleus.internal.annotations.Since;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoDocumentation;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoPermissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DocGenCache {

    private final List<CommandDoc> commandDocs = Lists.newArrayList();
    private final Map<String, PermissionDoc> permissionDocs = Maps.newHashMap();
    private final List<TokenDoc> tokenDocs = Lists.newArrayList();
    private final List<EssentialsDoc> essentialsDocs = Lists.newArrayList();
    private final Map<String, String> configDocs = Maps.newHashMap();

    private final org.slf4j.Logger logger;

    public DocGenCache(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    public List<CommandDoc> getCommandDocs() {
        return this.commandDocs;
    }

    public Collection<PermissionDoc> getPermissionDocs() {
        return this.permissionDocs.values();
    }

    public List<TokenDoc> getTokenDocs() {
        return this.tokenDocs;
    }

    public List<EssentialsDoc> getEssentialsDocs() {
        return this.essentialsDocs;
    }

    public Map<String, String> getConfigDocs() {
        return this.configDocs;
    }

    public void addConfigurableModule(String name, ConfigurableModule<?> module) {
        try (StringWriter sw = new StringWriter(); BufferedWriter writer = new BufferedWriter(sw)) {
            HoconConfigurationLoader hcl = HoconConfigurationLoader.builder()
                    .setDefaultOptions(ConfigurateHelper.setOptions(ConfigurationOptions.defaults()))
                    .setSink(() -> writer)
                    .build();
            CommentedConfigurationNode cn = hcl.createEmptyNode(ConfigurateHelper.setOptions(hcl.getDefaultOptions()));
            AbstractConfigAdapter<?> aca = module.getConfigAdapter().get();
            if (aca instanceof NucleusConfigAdapter.StandardWithSimpleDefault) {
                NucleusConfigAdapter.StandardWithSimpleDefault<?> nca = (NucleusConfigAdapter.StandardWithSimpleDefault<?>) aca;
                Object o = nca.getDefaultObject();
                applyToNode(o.getClass(), o, cn);
            } else {
                cn.setValue(module.getConfigAdapter().get().getDefaults());
            }

            hcl.save(cn);
            this.configDocs.put(name, sw.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private <T> void applyToNode(Class<T> c, Object object, ConfigurationNode node) {
        try {
            node.setValue(TypeToken.of(c), c.cast(object));
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public void addCommand(final String moduleID, final AbstractCommand<?> abstractCommand) {
        if (abstractCommand.getClass().isAnnotationPresent(NoDocumentation.class)) {
            return;
        }

        CommandDoc cmd = new CommandDoc();

        String cmdPath = abstractCommand.getCommandPath().replaceAll("\\.", " ");
        cmd.setCommandName(cmdPath);

        cmd.setAliases(String.join(", ", Lists.newArrayList(abstractCommand.getAliases())));

        if (abstractCommand.getRootCommandAliases().length > 0) {
            cmd.setRootAliases(String.join(", ", Arrays.asList(abstractCommand.getRootCommandAliases())));
        }

        cmd.setPermissionbase(abstractCommand.getPermissionHandler().getBase());

        Class<? extends AbstractCommand> cac = abstractCommand.getClass();
        Permissions s = cac.getAnnotation(Permissions.class);
        if (s == null) {
            cmd.setDefaultLevel(cac.isAnnotationPresent(NoPermissions.class) ? SuggestedLevel.USER.name() : SuggestedLevel.ADMIN.name());
        } else {
            cmd.setDefaultLevel(s.suggestedLevel().name());
        }

        cmd.setModule(moduleID);
        if (!cac.isAnnotationPresent(NoModifiers.class)) {
            cmd.setCooldown(!cac.isAnnotationPresent(NoCooldown.class));
            cmd.setCost(!cac.isAnnotationPresent(NoCost.class));
            cmd.setWarmup(!cac.isAnnotationPresent(NoWarmup.class));
        }
        cmd.setSince(cac.getAnnotation(Since.class));

        String desc = abstractCommand.getDescription();
        if (!desc.contains(" ")) {
            this.logger.warn("Cannot generate description for: " + abstractCommand.getCommandPath() + ": " + desc);
        }
        cmd.setOneLineDescription(desc);

        String extendedDescription = abstractCommand.getExtendedDescription().replace("\n", "|br|").replace("\"", "&quot;");
        if (!extendedDescription.isEmpty()) {
            cmd.setExtendedDescription(extendedDescription);
        }

        List<PermissionDoc> lp = new ArrayList<>();
        abstractCommand.getPermissionHandler().getSuggestedPermissions().forEach((k, v) -> lp.add(addPermissionDocs(moduleID, k, v)));

        cmd.setPermissions(lp);
        cmd.setUsageString(abstractCommand.getUsageString(Sponge.getServer().getConsole()));
        cmd.setSubcommands(abstractCommand.getChildrenUsage(Sponge.getServer().getConsole()).map(Text::toPlain).orElse(""));
        cmd.setSimpleUsage(abstractCommand.getSimpleUsage(Sponge.getServer().getConsole()));

        // Essentials
        EssentialsEquivalent ee = abstractCommand.getClass().getAnnotation(EssentialsEquivalent.class);
        if (ee != null) {
            List<String> ss = Arrays.asList(ee.value());
            cmd.setEssentialsEquivalents(ss);
            cmd.setEssNotes(ee.notes());
            cmd.setExactEssEquiv(ee.isExact());

            EssentialsDoc doc = new EssentialsDoc();
            doc.setEssentialsCommands(ss);

            int i = cmdPath.lastIndexOf(" ");
            String c;
            if (i > -1) {
                c = cmdPath.substring(0, i) + " ";
            } else {
                c = "";
            }

            List<String> a = Lists.newArrayList(abstractCommand.getAliases()).stream().map(x -> c + x).collect(Collectors.toList());
            if (abstractCommand.getRootCommandAliases().length > 0) {
                a.addAll(Arrays.asList(abstractCommand.getRootCommandAliases()));
            }

            doc.setNucleusEquiv(a);
            doc.setExact(ee.isExact());
            doc.setNotes(ee.notes());
            this.essentialsDocs.add(doc);
        }

        this.commandDocs.add(cmd);
    }

    public void addPermissionDocs(final String moduleID, Map<String, PermissionInformation> msp) {
        msp.forEach((k, v) -> addPermissionDocs(moduleID, k, v));
    }

    private PermissionDoc addPermissionDocs(final String moduleID, String k, PermissionInformation v) {
        PermissionDoc pd;
        if (!this.permissionDocs.containsKey(k)) {
            pd = getPermissionFrom(moduleID, k, v);
            this.permissionDocs.put(k, pd);
        } else {
            pd = this.permissionDocs.get(k);
            if (!pd.getDescription().contains(v.plainDescription)) {
                pd.setDescription(pd.getDescription().replaceAll("\\.$", "") + ", " + v.plainDescription);
            }
        }

        return pd;
    }

    public void addTokenDocs(Set<String> tokens) {
        tokens.forEach(x -> Nucleus.getNucleus().getMessageProvider().getMessageFromKey("nucleus.token." + x.toLowerCase()).ifPresent(y ->
                this.tokenDocs.add(new TokenDoc().setName(x.toLowerCase()).setDescription(y))
        ));
    }

    private PermissionDoc getPermissionFrom(String module, String k, PermissionInformation v) {
        PermissionDoc perm = new PermissionDoc();
        perm.setModule(module);
        perm.setDescription(v.plainDescription);
        perm.setPermission(k);
        perm.setDefaultLevel(v.level.name());
        perm.setOre(v.isOre).setNormal(v.isNormal);
        return perm;
    }
}
