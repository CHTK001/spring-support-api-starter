package com.chua.starter.devops.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Minimal HTTP app for validating DevOps shell scripts.
 */
public final class DevopsScriptSmokeApp {

    private DevopsScriptSmokeApp() {
    }

    public static void main(String[] args) throws Exception {
        int port = resolvePort();
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/hello", new TextHandler("ok"));
        server.createContext("/", new TextHandler("devops-smoke-app"));
        server.setExecutor(Executors.newFixedThreadPool(2));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0), "devops-smoke-shutdown"));

        server.start();
        System.out.printf("DevopsScriptSmokeApp started on port %d%n", port);
        Thread.currentThread().join();
    }

    private static int resolvePort() {
        String fromEnv = System.getenv("SERVER_PORT");
        if (fromEnv == null || fromEnv.trim().isEmpty()) {
            return 18080;
        }
        try {
            int value = Integer.parseInt(fromEnv.trim());
            if (value < 1 || value > 65535) {
                return 18080;
            }
            return value;
        } catch (NumberFormatException ex) {
            return 18080;
        }
    }

    private static final class TextHandler implements HttpHandler {
        private final byte[] payload;

        private TextHandler(String body) {
            this.payload = body.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(payload);
            }
        }
    }
}
