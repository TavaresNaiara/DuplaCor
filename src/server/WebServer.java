package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import controller.*;
import dao.Conexao;
import model.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Servidor Web HTTP nativo da JDK (porta 8080).
 * Serve a interface Web (HTML, CSS, JS) e expõe endpoints RESTful JSON conectados aos Controllers.
 */
public class WebServer {

    private static final int PORT = getPortOrDefault(8080);
    private static final String WEB_DIR = "web";

    private final ProdutoController produtoController;
    private final CategoriaController categoriaController;
    private final LoteController loteController;
    private final PedidoController pedidoController;
    private final PerdaController perdaController;
    private final UsuarioController usuarioController;
    private final UsuarioCarrinhoController carrinhoController;

    public WebServer() {
        this.produtoController = new ProdutoController();
        this.categoriaController = new CategoriaController();
        this.loteController = new LoteController();
        this.pedidoController = new PedidoController();
        this.perdaController = new PerdaController();
        this.usuarioController = new UsuarioController();
        this.carrinhoController = new UsuarioCarrinhoController();
    }

    public static void main(String[] args) {
        new WebServer().iniciar();
    }

    public void iniciar() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // Roteamento de API REST
            server.createContext("/api/status", new StatusHandler());
            server.createContext("/api/produtos", new ProdutosHandler());
            server.createContext("/api/categorias", new CategoriasHandler());
            server.createContext("/api/lotes", new LotesHandler());
            server.createContext("/api/pedidos", new PedidosHandler());
            server.createContext("/api/carrinho", new CarrinhoHandler());
            server.createContext("/api/perdas", new PerdasHandler());

            // Roteamento de Arquivos Estáticos (HTML, CSS, JS)
            server.createContext("/", new StaticFileHandler(WEB_DIR));

            server.setExecutor(null); // Executor padrão
            server.start();

            System.out.println("===============================================================");
            System.out.println("       SERVIDOR WEB DUPLA COR INICIADO COM SUCESSO!            ");
            System.out.println("===============================================================");
            System.out.println(" Acesse no navegador: http://localhost:" + PORT);
            System.out.println(" Banco de dados MySQL: " + (Conexao.testarConexao() ? "CONECTADO" : "OFFLINE (usando mock frontend)"));
            System.out.println("===============================================================");

        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor HTTP: " + e.getMessage());
        }
    }

    private static int getPortOrDefault(int defaultPort) {
        String envPort = System.getenv("PORT");
        if (envPort == null || envPort.trim().isEmpty()) {
            envPort = System.getenv("WEB_PORT");
        }
        if (envPort != null) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }
        return defaultPort;
    }

    // =========================================================================
    // HANDLER DE ARQUIVOS ESTÁTICOS (HTML, CSS, JS)
    // =========================================================================
    private static class StaticFileHandler implements HttpHandler {
        private final String baseDir;

        public StaticFileHandler(String baseDir) {
            this.baseDir = baseDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String uriPath = exchange.getRequestURI().getPath();
            if (uriPath.equals("/") || uriPath.isEmpty()) {
                uriPath = "/index.html";
            }

            Path filePath = Paths.get(baseDir, uriPath.replaceFirst("^/", ""));

            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                byte[] bytes = Files.readAllBytes(filePath);
                String mimeType = obterMimeType(filePath.toString());

                exchange.getResponseHeaders().set("Content-Type", mimeType + "; charset=UTF-8");
                exchange.getResponseHeaders().set("Cache-Control", "no-cache");
                exchange.sendResponseHeaders(200, bytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                // Fallback para SPA (serve index.html para rotas do navegador)
                Path indexPath = Paths.get(baseDir, "index.html");
                if (Files.exists(indexPath)) {
                    byte[] bytes = Files.readAllBytes(indexPath);
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } else {
                    String msg = "404 Not Found";
                    exchange.sendResponseHeaders(404, msg.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(msg.getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        }

        private String obterMimeType(String filename) {
            if (filename.endsWith(".html")) return "text/html";
            if (filename.endsWith(".css")) return "text/css";
            if (filename.endsWith(".js")) return "application/javascript";
            if (filename.endsWith(".json")) return "application/json";
            if (filename.endsWith(".svg")) return "image/svg+xml";
            if (filename.endsWith(".png")) return "image/png";
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
            if (filename.endsWith(".ico")) return "image/x-icon";
            return "text/plain";
        }
    }

    // =========================================================================
    // HANDLERS RESTful JSON DA API
    // =========================================================================

    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            boolean dbOk = Conexao.testarConexao();
            String json = String.format("{\"status\":\"UP\",\"mysql\":\"%s\",\"port\":%d}", (dbOk ? "CONNECTED" : "DISCONNECTED"), PORT);
            enviarRespostaJson(exchange, 200, json);
        }
    }

    private class ProdutosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            List<Produto> produtos = (query != null && query.contains("vitrine=true"))
                    ? produtoController.listarVitrine()
                    : produtoController.listarTodos();

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < produtos.size(); i++) {
                Produto p = produtos.get(i);
                sb.append(String.format("{\"idProduto\":%d,\"nome\":\"%s\",\"marca\":\"%s\",\"precoBase\":%s,\"status\":\"%s\"}",
                        p.getIdProduto(), escapeJson(p.getNome()), escapeJson(p.getMarca()),
                        p.getPrecoBase().toString(), p.getStatus()));
                if (i < produtos.size() - 1) sb.append(",");
            }
            sb.append("]");

            enviarRespostaJson(exchange, 200, sb.toString());
        }
    }

    private class CategoriasHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Categoria> lista = categoriaController.listarTodos();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Categoria c = lista.get(i);
                sb.append(String.format("{\"idCategoria\":%d,\"nome\":\"%s\",\"descricao\":\"%s\"}",
                        c.getIdCategoria(), escapeJson(c.getNome()), escapeJson(c.getDescricao())));
                if (i < lista.size() - 1) sb.append(",");
            }
            sb.append("]");

            enviarRespostaJson(exchange, 200, sb.toString());
        }
    }

    private class LotesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Lote> lotes = loteController.listarTodos();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lotes.size(); i++) {
                Lote l = lotes.get(i);
                sb.append(String.format("{\"idLote\":%d,\"quantInicial\":%d,\"quantAtual\":%d,\"dataValidade\":\"%s\",\"dataEntrada\":\"%s\",\"status\":\"%s\",\"Produto_idProduto\":%d}",
                        l.getIdLote(), l.getQuantInicial(), l.getQuantAtual(),
                        l.getDataValidade().toString(), l.getDataEntrada().toString(),
                        l.getStatus(), l.getProdutoId()));
                if (i < lotes.size() - 1) sb.append(",");
            }
            sb.append("]");

            enviarRespostaJson(exchange, 200, sb.toString());
        }
    }

    private class PedidosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Pedido> pedidos = pedidoController.listarTodos();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < pedidos.size(); i++) {
                Pedido p = pedidos.get(i);
                sb.append(String.format("{\"idPedido\":%d,\"dataVenda\":\"%s\",\"total\":%s,\"statusPagamento\":\"%s\",\"Usuario_idUsuario\":%d}",
                        p.getIdPedido(), (p.getDataVenda() != null ? p.getDataVenda().toString() : ""),
                        p.getTotal().toString(), p.getStatusPagamento(), p.getUsuarioId()));
                if (i < pedidos.size() - 1) sb.append(",");
            }
            sb.append("]");

            enviarRespostaJson(exchange, 200, sb.toString());
        }
    }

    private class CarrinhoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int userId = 2; // Usuário padrão para a requisição
            List<UsuarioCarrinho> itens = carrinhoController.listarPorUsuario(userId);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < itens.size(); i++) {
                UsuarioCarrinho c = itens.get(i);
                sb.append(String.format("{\"idUsuarioCarrinho\":%d,\"quantidade\":%d,\"Usuario_idUsuario\":%d,\"Produto_idProduto\":%d}",
                        c.getIdUsuarioCarrinho(), c.getQuantidade(), c.getUsuarioId(), c.getProdutoId()));
                if (i < itens.size() - 1) sb.append(",");
            }
            sb.append("]");

            enviarRespostaJson(exchange, 200, sb.toString());
        }
    }

    private class PerdasHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Perda> perdas = perdaController.listarTodos();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < perdas.size(); i++) {
                Perda p = perdas.get(i);
                sb.append(String.format("{\"idPerda\":%d,\"quantidade\":%d,\"motivo\":\"%s\",\"dataRegistro\":\"%s\",\"Lote_idLote\":%d}",
                        p.getIdPerda(), (p.getQuantidade() != null ? p.getQuantidade() : 0),
                        escapeJson(p.getMotivo()), (p.getDataRegistro() != null ? p.getDataRegistro().toString() : ""),
                        p.getLoteId()));
                if (i < perdas.size() - 1) sb.append(",");
            }
            sb.append("]");

            enviarRespostaJson(exchange, 200, sb.toString());
        }
    }

    private static void enviarRespostaJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escapeJson(String valor) {
        if (valor == null) return "";
        return valor.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
