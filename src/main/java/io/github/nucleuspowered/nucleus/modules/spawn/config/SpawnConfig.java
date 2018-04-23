/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class SpawnConfig {

    @Setting(value = "spawn-on-login", comment = "config.spawn.onlogin")
    private boolean spawnOnLogin = false;

    @Setting(value = "use-safe-spawn", comment = "config.spawn.safe")
    private boolean safeTeleport = true;

    @Setting(value = "force-first-spawn", comment = "config.spawn.forcefirstspawn")
    private boolean forceFirstSpawn = false;

    @Setting(value = "global-spawn", comment = "config.spawn.global.base")
    private GlobalSpawnConfig globalSpawn = new GlobalSpawnConfig();

    @Setting(value = "per-world-permissions", comment = "config.spawn.worlds")
    private boolean perWorldPerms = false;

    @Setting(value = "affect-bed-spawn", comment = "config.spawn.bedspawn")
    private boolean redirectBedSpawn = true;

    public boolean isSpawnOnLogin() {
        return this.spawnOnLogin;
    }

    public boolean isSafeTeleport() {
        return this.safeTeleport;
    }

    public boolean isForceFirstSpawn() {
        return this.forceFirstSpawn;
    }

    public GlobalSpawnConfig getGlobalSpawn() {
        return this.globalSpawn;
    }

    public boolean isPerWorldPerms() {
        return this.perWorldPerms;
    }

    public boolean isRedirectBedSpawn() {
        return this.redirectBedSpawn;
    }
}
