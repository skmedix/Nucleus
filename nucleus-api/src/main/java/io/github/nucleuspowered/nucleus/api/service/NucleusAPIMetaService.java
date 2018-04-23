/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.service;

/**
 * This service provides information about the API itself.
 *
 * <p>
 *     This service is available from PRE INIT - Late order.
 * </p>
 */
public final class NucleusAPIMetaService {

    private final String version;
    private final String semver;
    private final int majorVersion;
    private final int minorVersion;
    private final int patchVersion;
    private final boolean release;

    public NucleusAPIMetaService(String version, String semver, boolean isRelease) {
        this.version = version;
        this.semver = semver;

        String[] sp = semver.split("\\.", 3);
        this.majorVersion = parse(sp[0]);
        this.minorVersion = parse(sp[1]);
        this.patchVersion = parse(sp[2]);
        this.release = isRelease;
    }

    /**
     * The version of the Nucleus API this is.
     *
     * @return The version. This might include specifiers like "-SNAPSHOT" or "-PR1".
     */
    public String version() {
        return this.version;
    }

    /**
     * The version of the Nucleus API this is, or will be, if this is a snapshot.
     *
     * @return The semantic version.
     */
    public String semanticVersion() {
        return this.semver;
    }

    /**
     * The major component of the version number. Note that there may be breakages between major versions.
     *
     * @return The major version.
     */
    public int major() {
        return this.majorVersion;
    }

    /**
     * The minor component of the version number. Breakages should not occur between minor versions, but there may be new features included.
     *
     * @return The minor version.
     */
    public int minor() {
        return this.minorVersion;
    }

    /**
     * The patch component of the version number. These are bug fixes only.
     *
     * @return The patch version.
     */
    public int patch() {
        return this.patchVersion;
    }

    /**
     * Returns whether this is a release version.
     *
     * @return <code>true</code> if so.
     */
    public boolean isRelease() {
        return this.release;
    }

    private int parse(String string) {
        try {
            return Integer.parseUnsignedInt(string);
        } catch (Exception e) {
            return -1;
        }
    }
}
