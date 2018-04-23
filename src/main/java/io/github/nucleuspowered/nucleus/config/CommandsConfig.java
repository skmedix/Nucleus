/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config;

import io.github.nucleuspowered.nucleus.config.bases.AbstractStandardNodeConfig;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.nio.file.Path;

public class CommandsConfig extends AbstractStandardNodeConfig<CommentedConfigurationNode, HoconConfigurationLoader> {

    public CommandsConfig(Path file) throws Exception {
        super(file);
    }

    @Override
    protected HoconConfigurationLoader getLoader(Path file) {
        return HoconConfigurationLoader.builder().setPath(file).build();
    }

    @Override
    protected CommentedConfigurationNode getDefaults() {
        return SimpleCommentedConfigurationNode.root();
    }

    public CommentedConfigurationNode getCommandNode(String command) {
        return this.node.getNode(command.toLowerCase());
    }

    public void mergeDefaults(CommentedConfigurationNode node) {
        this.node.mergeValuesFrom(node);
    }
}
