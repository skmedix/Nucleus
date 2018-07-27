/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rtp.config;

import com.flowpowered.math.GenericMath;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.github.nucleuspowered.neutrino.annotations.ProcessSetting;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.rtp.RTPKernel;
import io.github.nucleuspowered.nucleus.api.rtp.RTPKernels;
import io.github.nucleuspowered.nucleus.configurate.settingprocessor.LowercaseMapKeySettingProcessor;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.storage.WorldProperties;
import uk.co.drnaylor.quickstart.config.NoMergeIfPresent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@ConfigSerializable
public class RTPConfig {

    @Setting(value = "attempts", comment = "config.rtp.attempts")
    private int noOfAttempts = 10;

    @Setting(value = "radius", comment = "config.rtp.radius")
    private int radius = 30000;

    @Setting(value = "min-radius", comment = "config.rtp.minradius")
    private int minRadius = 0;

    @Setting(value = "minimum-y", comment = "config.rtp.min-y")
    private int minY = 0;

    @Setting(value = "maximum-y", comment = "config.rtp.max-y")
    private int maxY = 255;

    @Setting(value = "default-method", comment = "config.rtp.defaultmethod")
    private String defaultRTPKernel = "nucleus:default";

    private RTPKernel lazyLoadedKernel;

    @Setting(value = "per-world-permissions", comment = "config.rtp.perworldperms")
    private boolean perWorldPermissions = false;

    @Setting(value = "world-overrides", comment = "config.rtp.perworldsect")
    @ProcessSetting(LowercaseMapKeySettingProcessor.class)
    private Map<String, PerWorldRTPConfig> perWorldRTPConfigList = new HashMap<String, PerWorldRTPConfig>() {{
        put("example", new PerWorldRTPConfig());
    }};

    @Setting(value = "default-world", comment = "config.rtp.defaultworld")
    private String defaultWorld = "";

    @NoMergeIfPresent
    @Setting(value = "prohibited-biomes", comment = "config.rtp.prohibitedbiomes")
    private Set<String> prohibitedBiomes = Sets.newHashSet(
            BiomeTypes.OCEAN.getId(),
            BiomeTypes.DEEP_OCEAN.getId(),
            BiomeTypes.FROZEN_OCEAN.getId()
    );

    private ImmutableSet<BiomeType> lazyLoadProhbitedBiomes;

    public int getNoOfAttempts() {
        return this.noOfAttempts;
    }

    private Optional<PerWorldRTPConfig> get(@Nullable String worldName) {
        if (worldName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.perWorldRTPConfigList.get(worldName.toLowerCase()));
    }

    public int getMinRadius(String worldName) {
        return get(worldName).map(x -> x.minRadius).orElse(this.minRadius);
    }

    public int getRadius(String worldName) {
        return get(worldName).map(x -> x.radius).orElse(this.radius);
    }

    public int getMinY(String worldName) {
        return get(worldName).map(x -> GenericMath.clamp(x.minY, 0, Math.min(255, x.maxY)))
                .orElseGet(() -> GenericMath.clamp(this.minY, 0, Math.min(255, this.maxY)));
    }

    public int getMaxY(String worldName) {
        return get(worldName).map(x -> GenericMath.clamp(x.maxY, Math.max(0, x.minY), 255))
                .orElseGet(() -> GenericMath.clamp(this.maxY, Math.max(0, this.minY), 255));
    }

    public boolean isPerWorldPermissions() {
        return this.perWorldPermissions;
    }

    public Optional<WorldProperties> getDefaultWorld() {
        if (this.defaultWorld == null || this.defaultWorld.equalsIgnoreCase("")) {
            return Optional.empty();
        }

        return Sponge.getServer().getWorldProperties(this.defaultWorld).filter(WorldProperties::isEnabled);
    }

    public ImmutableSet<BiomeType> getProhibitedBiomes() {
        if (this.lazyLoadProhbitedBiomes == null) {
            this.lazyLoadProhbitedBiomes = this.prohibitedBiomes.stream()
                    .map(x -> x.contains(":") ? x : "minecraft:" + x)
                    .map(x -> Sponge.getRegistry().getType(BiomeType.class, x).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(ImmutableSet.toImmutableSet());
        }

        return this.lazyLoadProhbitedBiomes;
    }

    public RTPKernel getKernel() {
        if (this.lazyLoadedKernel == null) {
            // does the kernel exist?
            String kernelId = this.defaultRTPKernel;
            kernelId = kernelId.contains(":") ? kernelId : "nucleus:" + kernelId;
            Optional<RTPKernel> rtpKernel = Sponge.getRegistry().getType(RTPKernel.class, kernelId);
            if (!rtpKernel.isPresent()) {
                Nucleus.getNucleus().getLogger().warn("Kernel with ID {} could not be found. Falling back to the default.", this.defaultRTPKernel);
                this.lazyLoadedKernel = RTPKernels.DEFAULT;
            } else {
                this.lazyLoadedKernel = rtpKernel.get();
            }
        }

        return this.lazyLoadedKernel;
    }

    public RTPKernel getKernel(String world) {
        return get(world).map(x -> {
            if (x.lazyLoadedKernel == null) {
                // does the kernel exist?
                String kernelId = x.defaultRTPKernel;
                kernelId = kernelId.contains(":") ? kernelId : "nucleus:" + kernelId;
                Optional<RTPKernel> rtpKernel = Sponge.getRegistry().getType(RTPKernel.class, kernelId);
                if (!rtpKernel.isPresent()) {
                    Nucleus.getNucleus().getLogger().warn("Kernel with ID {} for world {} could not be found. Falling back to the default.",
                            x.defaultRTPKernel, world);
                    x.lazyLoadedKernel = RTPKernels.DEFAULT;
                } else {
                    x.lazyLoadedKernel = rtpKernel.get();
                }
            }

            return x.lazyLoadedKernel;
        }).orElseGet(this::getKernel);
    }

    @ConfigSerializable
    public static class PerWorldRTPConfig {
        @Setting(value = "radius")
        private int radius = 30000;

        @Setting(value = "min-radius")
        private int minRadius = 30000;

        @Setting(value = "minimum-y")
        private int minY = 0;

        @Setting(value = "maximum-y")
        private int maxY = 255;

        @Setting(value = "default-method", comment = "config.rtp.defaultmethod")
        private String defaultRTPKernel = "nucleus:default";

        private RTPKernel lazyLoadedKernel;
    }
}
