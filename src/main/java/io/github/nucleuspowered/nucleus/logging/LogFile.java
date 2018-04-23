/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.logging;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.Util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

class LogFile implements Closeable {

    private final Path location;
    private final Function<String, String> formatter;
    private final BufferedWriter outputStream;
    private boolean isClosed = false;

    LogFile(final Path location, Function<String, String> stringFormatter) throws IOException {
        Preconditions.checkNotNull(location);
        Preconditions.checkNotNull(stringFormatter);

        this.location = location;
        this.outputStream = Files.newBufferedWriter(location);
        this.formatter = stringFormatter;
    }

    void writeLine(String line) throws IOException {
        try {
            this.outputStream.write(this.formatter.apply(line));
            this.outputStream.newLine();
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    void flush() throws IOException {
        this.outputStream.flush();
    }

    boolean isClosed() {
        return this.isClosed;
    }

    @Override
    public void close() throws IOException {
        if (this.isClosed) {
            return;
        }

        try {
            this.outputStream.close();
        } finally {
            this.isClosed = true;

            Util.compressAndDeleteFile(this.location);
        }
    }
}
