/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.geoip.config;

import io.github.nucleuspowered.neutrino.annotations.DoNotGenerate;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class GeoIpConfig {

    // Not localised, this is a legal statement that should be adhered to.
    @Setting(value = "accept-licences", comment = "By setting this to true, you agree to the licences for the MaxMind GeoLite 2 databases, and the information as displayed at http://nucleuspowered.org/thirdparty/geoip.html \nor in the geoip.txt file in the plugin JAR (you can open the Nucleus JAR with any zip program)")
    private boolean acceptLicence = false;

    @Setting(value = "alert-on-login", comment = "config.geoip.onlogin")
    private boolean alertOnLogin = false;

    // Not generated, only should be added if there is a problem to redress.
    @DoNotGenerate
    @Setting(value = "country-data")
    private String countryUrl = "http://geolite.maxmind.com/download/geoip/database/GeoLite2-Country.mmdb.gz";

    public boolean isAcceptLicence() {
        return this.acceptLicence;
    }

    public boolean isAlertOnLogin() {
        return this.alertOnLogin;
    }

    public String getCountryData() {
        return this.countryUrl;
    }
}
