/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rtp.kernels;

import com.flowpowered.math.vector.Vector3i;
import io.github.nucleuspowered.nucleus.api.service.NucleusRTPService;

import java.util.Random;

class KernelHelper {

    private KernelHelper() {}

    private static final Random random = new Random();

    static int getRandomBetween(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    static int randomSign(int in) {
        return random.nextInt(2) == 0 ? -in : in;
    }

    static Vector3i getLocationWithOffset(Vector3i centre, NucleusRTPService.RTPOptions options) {
        return new Vector3i(
                randomSign(getRandomBetween(options.minRadius(), options.maxRadius()) + centre.getX()),
                getRandomBetween(options.minHeight(), options.maxHeight()),
                randomSign(getRandomBetween(options.minRadius(), options.maxRadius())  + centre.getZ())
        );
    }

}
