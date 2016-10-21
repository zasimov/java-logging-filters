package com.cyberdyber.filters;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Creates additional in-memory copy for stream.
 *
 * @author zasimov
 *
 */
public class StreamDuplicator extends FilterOutputStream {
    private final Object context;
    private final ByteArrayOutputStream dupStream =
            new ByteArrayOutputStream();

    public StreamDuplicator(OutputStream output,
                            Object context) {
        super(output);
        this.context = context;
    }

    public String getEntityCopy() {
        return new String(dupStream.toByteArray(), StandardCharsets.UTF_8);
    }

    public Object getContext() {
        return this.context;
    }

    @Override
    public void write(int i) throws IOException {
        dupStream.write(i);
        out.write(i);
    }

    @Override
    public void close() throws IOException {
        dupStream.close();
        super.close();
    }
}
