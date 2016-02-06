package uk.co.drnaylor.minecraft.quickstart.argumentparsers;

import com.google.common.collect.Lists;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.minecraft.quickstart.QuickStart;
import uk.co.drnaylor.minecraft.quickstart.Util;
import uk.co.drnaylor.minecraft.quickstart.api.data.WarpLocation;
import uk.co.drnaylor.minecraft.quickstart.api.service.QuickStartWarpService;
import uk.co.drnaylor.minecraft.quickstart.internal.ConfigMap;
import uk.co.drnaylor.minecraft.quickstart.internal.PermissionUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns a {@link WarpData}
 */
public class WarpParser extends CommandElement {
    private final boolean includeWarpData;
    private QuickStartWarpService service;
    private final QuickStart plugin;
    private final boolean permissionCheck;

    public WarpParser(@Nullable Text key, QuickStart plugin, boolean permissionCheck) {
        this(key, plugin, permissionCheck, true);
    }

    public WarpParser(@Nullable Text key, QuickStart plugin, boolean permissionCheck, boolean includeWarpData) {
        super(key);
        this.plugin = plugin;
        this.permissionCheck = permissionCheck;
        this.includeWarpData = includeWarpData;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        getService();

        String warpName = args.next();
        String warp = warpName.toLowerCase();
        if (!service.warpExists(warp)) {
            throw args.createError(Text.of(QuickStart.ERROR_MESSAGE_PREFIX, TextColors.RED, Util.messageBundle.getString("args.warps.noexist")));
        }

        if (!checkPermission(source, warpName)) {
            throw args.createError(Text.of(QuickStart.ERROR_MESSAGE_PREFIX, TextColors.RED, Util.messageBundle.getString("args.warps.noperms")));
        }

        if (includeWarpData) {
            return new WarpData(warpName, service.getWarp(warp).orElseThrow(() -> args.createError(Text.of(Util.messageBundle.getString("args.warps.notavailable")))));
        } else {
            return new WarpData(warpName, null);
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        getService();

        try {
            String name = args.peek().toLowerCase();
            return service.getWarpNames().stream().filter(s -> s.startsWith(name)).filter(s -> !includeWarpData || service.getWarp(s).isPresent())
                    .filter(x -> checkPermission(src, name)).collect(Collectors.toList());
        } catch (ArgumentParseException e) {
            return Lists.newArrayList();
        }
    }

    private boolean checkPermission(CommandSource src, String name) {
        if (!permissionCheck || !plugin.getConfig(ConfigMap.MAIN_CONFIG).get().useSeparatePermissionsForWarp()) {
            return true;
        }

        // No permissions, no entry!
        return (src.hasPermission(PermissionUtil.PERMISSIONS_PREFIX + "warp." + name.toLowerCase()) || src.hasPermission(PermissionUtil.PERMISSIONS_ADMIN));
    }

    private void getService() {
        if (service == null) {
            service = Sponge.getServiceManager().provideUnchecked(QuickStartWarpService.class);
        }
    }

    public class WarpData {
        public final String warp;
        public final WarpLocation loc;

        private WarpData(String warp, WarpLocation loc) {
            this.warp = warp;
            this.loc = loc;
        }
    }
}
