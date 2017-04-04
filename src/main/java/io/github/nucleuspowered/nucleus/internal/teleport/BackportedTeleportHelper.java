/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
/*
 * Code from this file is from Sponge. Copyright reproduced below.
 *
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.nucleuspowered.nucleus.internal.teleport;

import com.flowpowered.math.vector.Vector3i;
import com.google.inject.Singleton;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class BackportedTeleportHelper {

    // Reflection! Yaaaayyyyyy
    private static BackportedTeleportHelper INSTANCE;

    public static BackportedTeleportHelper getInstance() {
        if (INSTANCE == null) {
            try {
                INSTANCE = new BackportedTeleportHelper();
            } catch (Exception e) {}
        }

        return INSTANCE;
    }

    private final Method getBlockLocations;
    private final Method getBlockData;
    private final Method isFloorSafe;

    private Field isSafeBody;
    private Field isSafeFloor;

    private final TeleportHelper helper = Sponge.getGame().getTeleportHelper();

    @SuppressWarnings("JavaReflectionMemberAccess")
    private BackportedTeleportHelper() throws Exception {
        this.getBlockLocations = this.helper.getClass().getDeclaredMethod("getBlockLocations", Location.class, int.class, int.class);
        this.getBlockLocations.setAccessible(true);

        this.getBlockData = this.helper.getClass().getDeclaredMethod("getBlockData", Vector3i.class, World.class, Map.class);
        this.getBlockData.setAccessible(true);

        this.isFloorSafe = this.helper.getClass().getDeclaredMethod("isFloorSafe", Vector3i.class, World.class, Map.class);
        this.isFloorSafe.setAccessible(true);

    }

    @SuppressWarnings("ALL")
    public Optional<Location<World>> getSafeLocation(Location<World> location, int height, int width) {
        final World world = location.getExtent();

        // We cache the various block lookup results so we don't check a block twice.
        final Map<Vector3i, Object> blockCache = new HashMap<>();

        // Get the vectors to check, and get the block types with them.
        // The vectors should be sorted by distance from the centre of the checking region, so
        // this makes it easier to try to get close, because we can just iterate and get progressively further out.
        try {
            Stream<Vector3i> svi = (Stream<Vector3i>) this.getBlockLocations.invoke(this.helper, location, height, width);
            svi = svi.filter(x ->
                    !world.getBlock(x).getType().equals(BlockTypes.PORTAL) &&
                    !world.getBlock(x).getType().equals(BlockTypes.END_PORTAL));

            Optional<Vector3i> result = svi.filter(currentTarget -> {
                // Get the block, add it to the cache.
                try {
                    Object block = this.getBlockData.invoke(this.helper, currentTarget, world, blockCache);

                    if (this.isSafeBody == null) {
                        this.isSafeBody = block.getClass().getDeclaredField("isSafeBody");
                        this.isSafeBody.setAccessible(true);

                        this.isSafeFloor = block.getClass().getDeclaredField("isSafeFloor");
                        this.isSafeFloor.setAccessible(true);
                    }

                    // If the block isn't safe, no point in continuing on this run.
                    if (this.isSafeBody.getBoolean(block)) {

                        // Check the block ABOVE is safe for the body, and the two BELOW are safe too.
                        if (this.isSafeBody.getBoolean(this.getBlockData.invoke(this.helper, currentTarget.add(0, 1, 0), world, blockCache))
                                && (boolean) this.isFloorSafe.invoke(this.helper, currentTarget, world, blockCache)) {

                            if (world.getBlock(currentTarget.sub(0, 1, 0)).getType() != BlockTypes.PORTAL
                                    && world.getBlock(currentTarget.sub(0, 1, 0)).getType() != BlockTypes.END_PORTAL
                                    && world.getBlock(currentTarget.sub(0, 2, 0)).getType() != BlockTypes.PORTAL
                                    && world.getBlock(currentTarget.sub(0, 2, 0)).getType() != BlockTypes.END_PORTAL) {
                                return true;
                            }

                            // This position should be safe. Get the center of the block to spawn into.
                            return false;
                        }
                    }

                    return false;
                } catch (Exception e) {
                    return false;
                }

            }).findFirst();

            if (result.isPresent()) {
                return Optional.of(new Location<>(world, result.get().toDouble().add(0.5, 0, 0.5)));
            }

            // No vectors matched, so return an empty optional.
            return Optional.empty();

        } catch (Exception e) {
            return Optional.empty();
        }

    }

}
