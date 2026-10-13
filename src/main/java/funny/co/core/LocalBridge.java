package funny.co.core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Authenticated loopback-only bridge; all position access runs on the FX thread. */
public final class LocalBridge implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread thread = new Thread(r, "chess-bridge"); thread.setDaemon(true); return thread;
    });
    private final String token = UUID.randomUUID().toString();
    private volatile boolean closed;

    public LocalBridge(GameplayHandler game) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 8);
        server.setExecutor(executor);
        server.createContext("/state", exchange -> handle(exchange, game, false));
        server.createContext("/move", exchange -> handle(exchange, game, true));
        Path session = Paths.get(System.getProperty("chess.home", "."), "target", "codex-session.properties");
        try {
            Files.createDirectories(session.getParent());
            Files.writeString(session, "port=" + server.getAddress().getPort() + "\ntoken=" + token + "\n", StandardCharsets.UTF_8);
            server.start();
        } catch (IOException e) { close(); throw e; }
    }
    private void handle(HttpExchange exchange, GameplayHandler game, boolean move) throws IOException {
        try {
            if (!Objects.equals(exchange.getRequestHeaders().getFirst("Authorization"), "Bearer " + token)) {
                reply(exchange, 401, "{\"error\":\"Session token required\"}"); return;
            }
            if (exchange.getRequestHeaders().getFirst("Origin") != null) {
                reply(exchange, 403, "{\"error\":\"Browser requests are not allowed\"}"); return;
            }
            if (!exchange.getRequestMethod().equals(move ? "POST" : "GET") || !exchange.getRequestURI().getPath().equals(move ? "/move" : "/state")) {
                reply(exchange, 405, "{\"error\":\"Unsupported request\"}"); return;
            }
            Map<String, String> fields = new HashMap<>();
            if (move) {
                byte[] bytes = exchange.getRequestBody().readNBytes(257);
                if (bytes.length > 256) { reply(exchange, 413, "{\"error\":\"Request too large\"}"); return; }
                for (String pair : new String(bytes, StandardCharsets.UTF_8).split("&")) {
                    String[] parts = pair.split("=", 2);
                    if (parts.length == 2) fields.put(parts[0], URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
                }
            }
            FutureTask<String> task = new FutureTask<>(() -> {
                if (closed) throw new IllegalStateException("Game closed");
                return move ? game.remoteMove(fields.getOrDefault("move", ""), Integer.parseInt(fields.getOrDefault("version", "-1"))) : game.stateJson();
            });
            Platform.runLater(task);
            try { reply(exchange, 200, task.get(5, TimeUnit.SECONDS)); }
            catch (TimeoutException e) { task.cancel(false); reply(exchange, 503, "{\"error\":\"UI busy. Read state before retrying.\"}"); }
            catch (ExecutionException e) { reply(exchange, 409, "{\"error\":\"" + escape(e.getCause().getMessage()) + "\"}"); }
            catch (InterruptedException e) { task.cancel(false); Thread.currentThread().interrupt(); }
        } catch (IllegalArgumentException e) { reply(exchange, 400, "{\"error\":\"Malformed request\"}"); }
        finally { exchange.close(); }
    }
    private static String escape(String value) { return String.valueOf(value).replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " "); }
    private static void reply(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(code, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
    @Override public void close() { closed = true; server.stop(0); executor.shutdownNow(); }
}
