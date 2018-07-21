/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.util;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfig;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatTemplateConfig;
import io.github.nucleuspowered.nucleus.modules.chat.config.WeightedChatTemplateConfig;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.context.Contextual;
import org.spongepowered.api.service.permission.Subject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Contains the logic for caching templates and the template selection logic.
 */
public class TemplateUtil implements Reloadable {

    private final AtomicBoolean currentlyReloading = new AtomicBoolean(false);
    private LinkedHashMap<String, WeightedChatTemplateConfig> cachedTemplates = null;
    private ChatConfig config
            = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(ChatConfigAdapter.class).getNodeOrDefault();

    public ChatTemplateConfig getDefaultTemplate() {
        return this.config.getDefaultTemplate();
    }

    public ChatTemplateConfig getTemplateNow(Subject subject) {
        if (!this.config.isUseGroupTemplates()) {
            return this.config.getDefaultTemplate();
        }

        Optional<String> groupString = subject.getOption("nucleus.chat.group");
        List<String> groups = new ArrayList<>();
        if (groupString.isPresent()) {
            groups.add(groupString.get());
        } else if (this.config.isCheckPermissionGroups()) {
            // Expensive, should hide behind a switch.
            try {
                groups = Util.getParentSubjects(subject)
                    .get(100, TimeUnit.MILLISECONDS)
                    .stream()
                    .map(Contextual::getIdentifier)
                    .collect(Collectors.toList());
            } catch (Exception e) {
                Nucleus.getNucleus().getLogger().error(
                        Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("chat.templates.timeout", subject.getIdentifier())
                );
                return this.config.getDefaultTemplate();
            }

            if (groups == null || groups.isEmpty()) {
                return this.config.getDefaultTemplate();
            }
        } else {
            // Nothin'. Return
            return this.config.getDefaultTemplate();
        }

        // For each weight...
        for (Map.Entry<String, WeightedChatTemplateConfig> templates : this.cachedTemplates.entrySet()) {
            // Iterate through all groups the subject is in.
            for (String group : groups) {
                if (templates.getKey().equalsIgnoreCase(group)) {
                    return templates.getValue();
                }
            }
        }

        // Return the default.
        return this.config.getDefaultTemplate();
    }

    @Override
    public void onReload() {
        try {
            this.config = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(ChatConfigAdapter.class).getNodeOrDefault();
            if (!this.currentlyReloading.get()) {
                this.currentlyReloading.set(true);
                // Do this off the main thread to not cause a lockup
                Task.builder().async().execute(() -> {
                    try {
                        if (this.config.isUseGroupTemplates()) {
                            LinkedHashMap<String, WeightedChatTemplateConfig> sw = new LinkedHashMap<>();
                            SortedMap<Integer, Set<Map.Entry<String, WeightedChatTemplateConfig>>> firstStage =
                                    new TreeMap<>(Comparator.reverseOrder());
                            for (Map.Entry<String, WeightedChatTemplateConfig> me : this.config.getGroupTemplates().entrySet()) {
                                // For each weight, get the set.
                                Set<Map.Entry<String, WeightedChatTemplateConfig>> sme = firstStage
                                        .computeIfAbsent(me.getValue().getWeight(), s -> new HashSet<>());

                                sme.add(me);
                            }

                            // keySet is in order.
                            for (int i : firstStage.keySet()) {
                                firstStage.get(i).forEach(x -> sw.put(x.getKey(), x.getValue()));
                            }

                            this.cachedTemplates = sw;
                        } else {
                            this.cachedTemplates = new LinkedHashMap<>();
                        }
                    } finally {
                        this.currentlyReloading.set(false);
                    }
                }).submit(Nucleus.getNucleus());

            }
        } catch (Exception e) {
            this.currentlyReloading.set(false);
        }
    }
}
