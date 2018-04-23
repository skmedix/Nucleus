/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.tests;

import com.flowpowered.math.vector.Vector3d;
import io.github.nucleuspowered.nucleus.Util;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.spongepowered.api.world.ChunkPreGenerate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;

import java.util.Arrays;

public class UtilTests {

    @SuppressWarnings("CanBeFinal")
    @RunWith(Parameterized.class)
    public static class WorldBorderTests {
        @Parameterized.Parameters(name = "{index}: Co-ords ({0}, {1}, {2}), border centre ({3}, {4}, {5}), diameter: {6}, expecting {7}")
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {0, 0, 0, 0, 0, 0, 10, true},
                    {20, 0, 0, 0, 0, 0, 10, false},
                    {0, 20, 0, 0, 0, 0, 10, true},
                    {20, 0, 20, 0, 0, 0, 10, false},
                    {0, 0, 20, 0, 0, 0, 10, false},
                    {4, 0, 4, 0, 0, 0, 10, true},
                    {5, 0, 5, 0, 0, 0, 10, true},
                    {5, 0, 5, 0, 20, 0, 10, true},
                    {6, 0, 5, 0, 20, 0, 10, false},
                    {5, 0, 5, 500, 0, 0, 10, false},
                    {499, 0, 5, 500, 0, 0, 10, true},
                    {499, 0, 500, 500, 0, 0, 10, false}
            });
        }

        @Parameterized.Parameter()
        public double x;

        @Parameterized.Parameter(1)
        public double y;

        @Parameterized.Parameter(2)
        public double z;

        @Parameterized.Parameter(3)
        public double borderX;

        @Parameterized.Parameter(4)
        public double borderY;

        @Parameterized.Parameter(5)
        public double borderZ;

        @Parameterized.Parameter(6)
        public double dia;

        @Parameterized.Parameter(7)
        public boolean result;

        private WorldBorder getBorder() {
            return new WorldBorder() {
                @Override
                public double getNewDiameter() {
                    return WorldBorderTests.this.dia;
                }

                @Override
                public double getDiameter() {
                    return WorldBorderTests.this.dia;
                }

                @Override
                public void setDiameter(double diameter) {

                }

                @Override
                public void setDiameter(double diameter, long time) {

                }

                @Override
                public void setDiameter(double startDiameter, double endDiameter, long time) {

                }

                @Override
                public long getTimeRemaining() {
                    return 0;
                }

                @Override
                public void setCenter(double x, double z) {

                }

                @Override
                public Vector3d getCenter() {
                    return new Vector3d(WorldBorderTests.this.borderX, WorldBorderTests.this.borderY, WorldBorderTests.this.borderZ);
                }

                @Override
                public int getWarningTime() {
                    return 0;
                }

                @Override
                public void setWarningTime(int time) {

                }

                @Override
                public int getWarningDistance() {
                    return 0;
                }

                @Override
                public void setWarningDistance(int distance) {

                }

                @Override
                public double getDamageThreshold() {
                    return 0;
                }

                @Override
                public void setDamageThreshold(double distance) {

                }

                @Override
                public double getDamageAmount() {
                    return 0;
                }

                @Override
                public void setDamageAmount(double damage) {

                }

                @Override
                public ChunkPreGenerate.Builder newChunkPreGenerate(World world) {
                    return null;
                }
            };
        }

        @Test
        public void testInWorldBorder() {
            WorldBorder wb = getBorder();
            World world = Mockito.mock(World.class);
            Mockito.when(world.getWorldBorder()).thenReturn(wb);

            Location<World> lw = new Location<>(world, new Vector3d(this.x, this.y, this.z));
            Assert.assertEquals(this.result, Util.isLocationInWorldBorder(lw));
        }
    }
}
