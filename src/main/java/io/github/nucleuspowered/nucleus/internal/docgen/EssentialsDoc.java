/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.docgen;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class EssentialsDoc {

    @Setting
    private List<String> essentialsCommands;

    @Setting
    private List<String> nucleusEquiv;

    @Setting
    private boolean isExact;

    @Setting
    private String notes;

    public List<String> getEssentialsCommands() {
        return this.essentialsCommands;
    }

    public void setEssentialsCommands(List<String> essentialsCommands) {
        this.essentialsCommands = essentialsCommands;
    }

    public List<String> getNucleusEquiv() {
        return this.nucleusEquiv;
    }

    public void setNucleusEquiv(List<String> nucleusEquiv) {
        this.nucleusEquiv = nucleusEquiv;
    }

    public boolean isExact() {
        return this.isExact;
    }

    public void setExact(boolean exact) {
        this.isExact = exact;
    }

    public String getNotes() {
        return this.notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
