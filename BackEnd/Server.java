package BackEnd;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Server {

  private static final int PORT = 8080;
  private static final Path FRONTEND_DIR = Paths.get("FrontEnd");

  public static void main(String[] args) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

    // ---- Register API endpoints here ----
    // To add a new endpoint, write a method that takes a Request and returns a JSON string,
    // then register it below with route(server, path, handlerMethod).
    route(server, "/api/code", Server::handleCode);
    // route(server, "/api/yourThing", Server::handleYourThing);

    // Static file serving for the FrontEnd directory
    server.createContext("/", new StaticHandler());

    server.setExecutor(null);
    System.out.println("Server running at http://localhost:" + PORT);
    server.start();
  }

  // =========================================================================
  // Endpoint handlers — each takes a Request, returns JSON string.
  // Add new ones here.
  // =========================================================================

  private static String handleCode(Request req) {
    String name = req.param("name", "Player");
    JoinCode jc = new JoinCode(name);
    jc.genCode();
    return "{\"name\":\"" + escape(name) + "\",\"code\":\"" + jc.getCode() + "\"}";
  }

  // =========================================================================
  // Framework below — you usually don't need to touch this.
  // =========================================================================

  @FunctionalInterface
  interface JsonHandler {
    String handle(Request req) throws Exception;
  }

  static class Request {
    final String method;
    final Map<String, String> query;
    final String body;

    Request(String method, Map<String, String> query, String body) {
      this.method = method;
      this.query = query;
      this.body = body;
    }

    String param(String key, String defaultValue) {
      return query.getOrDefault(key, defaultValue);
    }
  }

  private static void route(HttpServer server, String path, JsonHandler handler) {
    server.createContext(path, exchange -> {
      try {
        Request req = new Request(
            exchange.getRequestMethod(),
            queryParams(exchange.getRequestURI()),
            readBody(exchange));
        String json = handler.handle(req);
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(body);
        }
      } catch (Exception e) {
        String err = "{\"error\":\"" + escape(e.getMessage() == null ? "internal" : e.getMessage()) + "\"}";
        byte[] body = err.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(500, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(body);
        }
      }
    });
  }

  static class StaticHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      String requestPath = exchange.getRequestURI().getPath();
      if (requestPath.equals("/")) requestPath = "/index.html";

      Path file = FRONTEND_DIR.resolve(requestPath.substring(1)).normalize();
      if (!file.startsWith(FRONTEND_DIR) || !Files.exists(file) || Files.isDirectory(file)) {
        byte[] notFound = "404 Not Found".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(404, notFound.length);
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(notFound);
        }
        return;
      }

      byte[] body = Files.readAllBytes(file);
      exchange.getResponseHeaders().set("Content-Type", contentType(file));
      exchange.sendResponseHeaders(200, body.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(body);
      }
    }
  }

  private static Map<String, String> queryParams(URI uri) {
    Map<String, String> map = new HashMap<>();
    String q = uri.getRawQuery();
    if (q == null) return map;
    for (String pair : q.split("&")) {
      int eq = pair.indexOf('=');
      if (eq < 0) continue;
      String k = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
      String v = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
      map.put(k, v);
    }
    return map;
  }

  private static String readBody(HttpExchange exchange) throws IOException {
    try (InputStream is = exchange.getRequestBody()) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static String contentType(Path file) {
    String name = file.getFileName().toString().toLowerCase();
    if (name.endsWith(".html")) return "text/html; charset=utf-8";
    if (name.endsWith(".js"))   return "application/javascript; charset=utf-8";
    if (name.endsWith(".css"))  return "text/css; charset=utf-8";
    if (name.endsWith(".json")) return "application/json; charset=utf-8";
    if (name.endsWith(".png"))  return "image/png";
    if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
    return "application/octet-stream";
  }

  private static String escape(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
