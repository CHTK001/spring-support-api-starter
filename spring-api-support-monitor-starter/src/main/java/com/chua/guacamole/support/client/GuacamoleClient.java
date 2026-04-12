package com.chua.guacamole.support.client;

public class GuacamoleClient implements AutoCloseable {
    private final Object configuration;

    public GuacamoleClient(Object configuration) {
        this.configuration = configuration;
    }

    public Object getConfiguration() {
        return configuration;
    }

    @Override
    public void close() {
        // Compatibility stub for projects where guacamole client module is absent.
    }
}
