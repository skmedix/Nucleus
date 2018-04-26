/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp;

import io.github.nucleuspowered.nucleus.argumentparsers.WarpArgument;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

public final class WarpParameters {

    private WarpParameters() {}

    public static final String WARP_KEY = "warp";

    public static final CommandElement WARP_PERM = new WarpArgument(Text.of(WARP_KEY), true);

    public static final CommandElement WARP_NO_PERM = new WarpArgument(Text.of(WARP_KEY), false);
}
