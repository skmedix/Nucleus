/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.geoip.handlers;

import com.google.common.base.Preconditions;
import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.record.Country;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.modules.geoip.GeoIpModule;
import io.github.nucleuspowered.nucleus.modules.geoip.config.GeoIpConfigAdapter;
import org.spongepowered.api.Sponge;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;

public class GeoIpDatabaseHandler implements Closeable {

    private GeoIpConfigAdapter geoIpConfigAdapter = null;
    private final Path downloadDirectory;
    private final Path countries;
    private DatabaseReader databaseReader;
    private boolean isLoading = false;

    public GeoIpDatabaseHandler() {
        this.downloadDirectory = Nucleus.getNucleus().getDataPath().resolve("geoip");
        this.countries = this.downloadDirectory.resolve("countries.mmdb");
    }

    /**
     * Loads the IP database.
     *
     * <p>This method will respond in one of three ways, dependent on what {@link LoadType} is provided:</p>
     * <ul>
     *     <li>
     *         {@link LoadType#IF_REQUIRED} will only load the database if it isn't already loaded.
     *     </li>
     *     <li>
     *         {@link LoadType#RELOAD} will reload the database, but it won't re-download it.
     *     </li>
     *     <li>
     *         {@link LoadType#DOWNLOAD} will re-download the database, even if it exists, and reload it.
     *     </li>
     * </ul>
     *
     * @param type The {@link LoadType}
     * @throws Exception If there is a problem. {@link IllegalStateException} is thrown if the licence has not been accepted.
     */
    public void load(LoadType type) throws Exception {
        if (this.isLoading) {
            return;
        }

        Preconditions.checkNotNull(type);
        init();

        if (type == LoadType.IF_REQUIRED && this.databaseReader != null) {
            return;
        }

        onRun(type);
    }

    private void onRun(LoadType type) {
        try {
            this.isLoading = true;

            // Check in case we need it.
            downloadUpdate(type == LoadType.DOWNLOAD);

            if (this.databaseReader != null) {
                this.databaseReader.close();
            }

            InputStream inputStream = new FileInputStream(this.countries.toFile());
            this.databaseReader = new DatabaseReader.Builder(inputStream).withCache(new CHMCache()).fileMode(Reader.FileMode.MEMORY).build();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.isLoading = false;
        }
    }

    public CompletableFuture<Optional<Country>> getDetails(InetAddress address) throws Exception {
        init();

        final CompletableFuture<Optional<Country>> completableFuture = new CompletableFuture<>();

        Sponge.getScheduler().createAsyncExecutor(Nucleus.getNucleus()).execute(() -> {
            try {
                load(LoadType.IF_REQUIRED);
                int counter = 0;

                // Load check.
                if (this.isLoading) {
                    while (counter < 30) {
                        Thread.sleep(500);
                        if (!this.isLoading) {
                            break;
                        }

                        counter++;
                    }

                    if (counter == 30) {
                        completableFuture.completeExceptionally(new TimeoutException("loading"));
                        return;
                    }
                }

                completableFuture.complete(Optional.ofNullable(this.databaseReader.country(address).getCountry()));
            } catch (AddressNotFoundException ex) {
                completableFuture.complete(Optional.empty());
            } catch (Exception e) {
                completableFuture.completeExceptionally(e);
            }
        });

        return completableFuture;
    }

    private void init() throws Exception {
        if (this.geoIpConfigAdapter == null) {
            this.geoIpConfigAdapter = Nucleus.getNucleus().getModuleContainer().getConfigAdapterForModule(GeoIpModule.ID, GeoIpConfigAdapter.class);
        }

        if (!this.geoIpConfigAdapter.getNodeOrDefault().isAcceptLicence()) {
            throw new IllegalStateException("licence");
        }
    }

    private void downloadUpdate(boolean loadAnyway) throws Exception {
        init();

        Files.createDirectories(this.downloadDirectory);

        if (loadAnyway || !Files.exists(this.countries)) {
            downloadFile(this.geoIpConfigAdapter.getNodeOrDefault().getCountryData(), this.countries);
        }
    }

    private void downloadFile(String url, Path path) throws IOException {
        URL downloadUrl = new URL(url);
        URLConnection conn = downloadUrl.openConnection();
        conn.setConnectTimeout(10000);
        conn.connect();

        try(InputStream input = new GZIPInputStream(conn.getInputStream());
            OutputStream output = new FileOutputStream(path.toFile())) {
            byte[] buffer = new byte[2048];
            int length = input.read(buffer);
            while (length >= 0) {
                output.write(buffer, 0, length);
                length = input.read(buffer);
            }

            output.flush();
        }
    }

    @Override public void close() throws IOException {
        if (this.databaseReader != null) {
            this.databaseReader.close();
            this.databaseReader = null;
        }
    }

    public enum LoadType {
        IF_REQUIRED,
        RELOAD,
        DOWNLOAD
    }
}
