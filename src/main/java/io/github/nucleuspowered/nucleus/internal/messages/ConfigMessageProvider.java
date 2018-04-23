/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.messages;

import io.github.nucleuspowered.nucleus.config.MessageConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ConfigMessageProvider extends ResourceMessageProvider {

    private final MessageConfig mc;

    public ConfigMessageProvider(Path file, String fallbackResource) throws Exception {
        super(fallbackResource);
        this.mc = new MessageConfig(file, new ResourceMessageProvider(this.rb));
    }

    @Override
    public Optional<String> getMessageFromKey(String key) {
        Optional<String> s = this.mc.getKey(key);
        if (s.isPresent()) {
            return s;
        }

        return super.getMessageFromKey(key);
    }

    public List<String> checkForMigration() {
        return this.mc.walkThroughForMismatched();
    }

    public void reset(List<String> keys) throws IOException {
        this.mc.fixMistmatched(keys);
    }
}
