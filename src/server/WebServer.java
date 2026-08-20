package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import controller.*;
import dao.Conexao;
import model.*;
import util.SimpleJson;

import java.io.*;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Servidor Web HTTP nativo da JDK (porta 8080).
 * Serve a interface Web (HTML, CSS, JS) e expõe endpoints RESTful JSON conectados aos Controllers.
 */
public class WebServer {

    private static final int PORT = getPortOrDefault(8081);
    private static final String WEB_DIR = "web";
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProdutoController produtoController;
    private final CategoriaController categoriaController;
    private final LoteController loteController;
    private final PedidoController pedidoController;
    private final PerdaController perdaController;
    private final UsuarioController usuarioController;
    private final UsuarioCarrinhoController carrinhoController;
    private final RelatorioController relatorioController;

    public WebServer() {
        this.produtoController = new ProdutoController();
        this.categoriaController = new CategoriaController();
        this.loteController = new LoteController();
        this.pedidoController = new PedidoController();
        this.perdaController = new PerdaController();
        this.usuarioController = new UsuarioController();
        this.carrinhoController = new UsuarioCarrinhoController();
        this.relatorioController = new RelatorioController();
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
            server.createContext("/api/usuarios", new UsuariosHandler());
            server.createContext("/api/relatorios", new RelatoriosHandler());

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

    // -------------------------------------------------------------------
    // PRODUTOS: GET /api/produtos (?vitrine=true) | GET /api/produtos/{id}
    //           POST /api/produtos | PUT /api/produtos/{id} | DELETE /api/produtos/{id}
    // -------------------------------------------------------------------
    private class ProdutosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String metodo = exchange.getRequestMethod();
                String[] segmentos = extrairSegmentos(exchange, "/api/produtos");
                Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());

                if (segmentos.length == 0) {
                    if ("GET".equalsIgnoreCase(metodo)) {
                        List<Produto> produtos = query.containsKey("vitrine")
                                ? produtoController.listarVitrine()
                                : produtoController.listarTodos();
                        enviarRespostaJson(exchange, 200, listaParaJson(produtos, this::produtoParaJson));
                        return;
                    }
                    if ("POST".equalsIgnoreCase(metodo)) {
                        Map<String, Object> body = SimpleJson.parseObject(lerCorpo(exchange));
                        String nome = SimpleJson.getString(body, "nome");
                        String marca = SimpleJson.getString(body, "marca");
                        Double preco = SimpleJson.getDouble(body, "precoBase");
                        List<Integer> catIds = new ArrayList<>();
                        for (Object o : SimpleJson.getList(body, "categorias")) {
                            if (o instanceof Number) catIds.add(((Number) o).intValue());
                        }
                        boolean ok = produtoController.cadastrar(nome, marca,
                                preco != null ? BigDecimal.valueOf(preco) : null, catIds);
                        if (ok) {
                            enviarRespostaJson(exchange, 201, "{\"sucesso\":true}");
                        } else {
                            enviarErro(exchange, 400, "Não foi possível cadastrar o produto. Verifique os dados.");
                        }
                        return;
                    }
                    enviarErro(exchange, 405, "Método não suportado.");
                    return;
                }

                // /api/produtos/{id}
                int id = parseIdOuMenosUm(segmentos[0]);
                if (id <= 0) {
                    enviarErro(exchange, 400, "ID de produto inválido.");
                    return;
                }

                if ("GET".equalsIgnoreCase(metodo)) {
                    Produto p = produtoController.buscarPorId(id);
                    if (p == null) {
                        enviarErro(exchange, 404, "Produto não encontrado.");
                    } else {
                        enviarRespostaJson(exchange, 200, produtoParaJson(p));
                    }
                    return;
                }
                if ("PUT".equalsIgnoreCase(metodo)) {
                    Map<String, Object> body = SimpleJson.parseObject(lerCorpo(exchange));
                    String nome = SimpleJson.getString(body, "nome");
                    String marca = SimpleJson.getString(body, "marca");
                    Double preco = SimpleJson.getDouble(body, "precoBase");
                    String status = SimpleJson.getString(body, "status");
                    List<Integer> catIds = null;
                    if (body.containsKey("categorias")) {
                        catIds = new ArrayList<>();
                        for (Object o : SimpleJson.getList(body, "categorias")) {
                            if (o instanceof Number) catIds.add(((Number) o).intValue());
                        }
                    }
                    boolean ok = produtoController.atualizar(id, nome, marca,
                            preco != null ? BigDecimal.valueOf(preco) : null, status, catIds);
                    if (ok) {
                        enviarRespostaJson(exchange, 200, "{\"sucesso\":true}");
                    } else {
                        enviarErro(exchange, 400, "Não foi possível atualizar o produto.");
                    }
                    return;
                }
                if ("DELETE".equalsIgnoreCase(metodo)) {
                    boolean ok = produtoController.excluir(id);
                    if (ok) {
                        enviarRespostaJson(exchange, 200, "{\"sucesso\":true}");
                    } else {
                        enviarErro(exchange, 400, "Não foi possível excluir o produto.");
                    }
                    return;
                }
                enviarErro(exchange, 405, "Método não suportado.");
            } catch (Exception e) {
                enviarErro(exchange, 500, "Erro interno: " + e.getMessage());
            }
        }

        private String produtoParaJson(Produto p) {
            StringBuilder catsSb = new StringBuilder("[");
            List<Categoria> cats = p.getCategorias();
            for (int i = 0; i < cats.size(); i++) {
                catsSb.append(cats.get(i).getIdCategoria());
                if (i < cats.size() - 1) catsSb.append(",");
            }
            catsSb.append("]");

            return String.format(
                    "{\"idProduto\":%d,\"nome\":\"%s\",\"marca\":\"%s\",\"precoBase\":%s,\"status\":\"%s\",\"categorias\":%s}",
                    p.getIdProduto(), SimpleJson.escape(p.getNome()), SimpleJson.escape(p.getMarca()),
                    p.getPrecoBase() != null ? p.getPrecoBase().toString() : "0",
                    SimpleJson.escape(p.getStatus()), catsSb.toString());
        }
    }

    // -------------------------------------------------------------------
    // CATEGORIAS: GET /api/categorias | GET /api/categorias/{id}
    //             POST /api/categorias | PUT /api/categorias/{id} | DELETE /api/categorias/{id}
    // -------------------------------------------------------------------
    private class CategoriasHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String metodo = exchange.getRequestMethod();
                String[] segmentos = extrairSegmentos(exchange, "/api/categorias");

                if (segmentos.length == 0) {
                    if ("GET".equalsIgnoreCase(metodo)) {
                        List<Categoria> lista = categoriaController.listarTodos();
                        enviarRespostaJson(exchange, 200, listaParaJson(lista, this::categoriaParaJson));
                        return;
                    }
                    if ("POST".equalsIgnoreCase(metodo)) {
                        Map<String, Object> body = SimpleJson.parseObject(lerCorpo(exchange));
                        String nome = SimpleJson.getString(body, "nome");
                        String descricao = SimpleJson.getString(body, "descricao");
                        boolean ok = categoriaController.cadastrar(nome, descricao);
                        if (ok) {
                            enviarRespostaJson(exchange, 201, "{\"sucesso\":true}");
                        } else {
                            enviarErro(exchange, 400, "Não foi possível cadastrar a categoria.");
                        }
                        return;
                    }
                    enviarErro(exchange, 405, "Método não suportado.");
                    return;
                }

                int id = parseIdOuMenosUm(segmentos[0]);
                if (id <= 0) {
                    enviarErro(exchange, 400, "ID de categoria inválido.");
                    return;
                }
                if ("PUT".equalsIgnoreCase(metodo)) {
                    Map<String, Object> body = SimpleJson.parseObject(lerCorpo(exchange));
                    boolean ok = categoriaController.atualizar(id, SimpleJson.getString(body, "nome"), SimpleJson.getString(body, "descricao"));
                    if (ok) {
                        enviarRespostaJson(exchange, 200, "{\"sucesso\":true}");
                    } else {
                        enviarErro(exchange, 400, "Não foi possível atualizar a categoria.");
                    }
                    return;
                }
                if ("DELETE".equalsIgnoreCase(metodo)) {
                    boolean ok = categoriaController.excluir(id);
                    if (ok) {
                        enviarRespostaJson(exchange, 200, "{\"sucesso\":true}");
                    } else {
                        enviarErro(exchange, 400, "Não foi possível excluir a categoria.");
                    }
                    return;
                }
                enviarErro(exchange, 405, "Método não suportado.");
            } catch (Exception e) {
                enviarErro(exchange, 500, "Erro interno: " + e.getMessage());
            }
        }

        private String categoriaParaJson(Categoria c) {
            return String.format("{\"idCategoria\":%d,\"nome\":\"%s\",\"descricao\":\"%s\"}",
                    c.getIdCategoria(), SimpleJson.escape(c.getNome()), SimpleJson.escape(c.getDescricao()));
        }
    }

    // -------------------------------------------------------------------
    // LOTES: GET /api/lotes (?produtoId=X&fefo=true) | GET /api/lotes/{id}
    //        POST /api/lotes | PUT /api/lotes/{id} | DELETE /api/lotes/{id}
    //        POST /api/lotes/monitorar
    // -------------------------------------------------------------------
    private class LotesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String metodo = exchange.getRequestMethod();
                String[] segmentos = extrairSegmentos(exchange, "/api/lotes");
                Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());

                if (segmentos.length == 1 && "monitorar".equalsIgnoreCase(segmentos[0]) && "POST".equalsIgnoreCase(metodo)) {
                    int bloqueados = loteController.monitorarEBloquearVencidos();
                    enviarRespostaJson(exchange, 200, String.format("{\"bloqueados\":%d}", bloqueados));
                    return;
                }

                if (segmentos.length == 0) {
                    if ("GET".equalsIgnoreCase(metodo)) {
                        List<Lote> lotes;
                        if (query.containsKey("produtoId")) {
                            int produtoId = parseIdOuMenosUm(query.get("produtoId"));
                            lotes = query.containsKey("fefo")
                                    ? loteController.listarDisponiveisFEFO(produtoId)
                                    : loteController.listarPorProduto(produtoId);
                        } else {
                            lotes = loteController.listarTodos();
                        }
                        enviarRespostaJson(exchange, 200, listaParaJson(lotes, this::loteParaJson));
                        return;
                    }
                    if ("POST".equalsIgnoreCase(metodo)) {
                        Map<String, Object> body = SimpleJson.parseObject(lerCorpo(exchange));
                        Integer qtdInicial = SimpleJson.getInt(body, "quantInicial");
                        String dataValidadeStr = SimpleJson.getString(body, "dataValidade");
                        String dataEntradaStr = SimpleJson.getString(body, "dataEntrada");
                        Integer produtoId = SimpleJson.getInt(body, "Produto_idProduto");

                        LocalDate dataValidade = parseDataOuNull(dataValidadeStr);
                        LocalDate dataEntrada = parseDataOuNull(dataEntradaStr);

                        boolean ok = loteController.cadastrar(
                                qtdInicial != null ? qtdInicial : 0,
                                dataValidade, dataEntrada,
                                produtoId != null ? produtoId : 0);

                        if (ok) {
                            enviarRespostaJson(exchange, 201, "{\"sucesso\":true}");
                        } else {
                            enviarErro(exchange, 400, "Não foi possível cadastrar o lote. Verifique os dados.");
                        }
                        return;
                    }
                    enviarErro(exchange, 405, "Método não suportado.");
                    return;
                }

                int id = parseIdOuMenosUm(segmentos[0]);
                if (id <= 0) {
                    enviarErro(exchange, 400, "ID de lote inválido.");
                    return;
                }
                if ("GET".equalsIgnoreCase(metodo)) {
                    Lote lote = loteController.buscarPorId(id);
                    if (lote == null) {
                        enviarErro(exchange, 404, "Lote não encontrado.");
                    } else {
                        enviarRespostaJson(exchange, 200, loteParaJson(lote));
                    }
                    return;
                }
                if ("PUT".equalsIgnoreCase(metodo)) {
                    Map<String, Object> body = SimpleJson.parseObject(lerCorpo(exchange));
                    Integer quantAtual = SimpleJson.getInt(body, "quantAtual");
                    LocalDate dataValidade = parseDataOuNull(SimpleJson.getString(body, "dataValidade"));
                    LocalDate dataEntrada = parseDataOuNull(SimpleJson.getString(body, "dataEntrada"));
                    String status = SimpleJson.getString(body, "status");

                    boolean ok = loteController.atualizar(id, quantAtual != null ? quantAtual : -1,
                            dataValidade, dataEntrada, status);
                    if (ok) {
                        enviarRespostaJson(exchange, 200, "{\"sucesso\":true}");
                    } else {
                        enviarErro(exchange, 400, "Não foi possível atualizar o lote.");
                    }
                    return;
                }
                if ("DELETE".equalsIgnoreCase(metodo)) {
                    boolean ok = loteController.excluir(id);
                    if (ok) {
                        enviarRespostaJson(exchange, 200, "{\"sucesso\":true}");
                    } else {
                        enviarErro(exchange, 400, "Não foi possível excluir o lote.");
                    }
                    return;
                }
                enviarErro(exchange, 405, "Método não suportado.");
            } catch (Exception e) {
                enviarErro(exchange, 500, "Erro interno: " + e.getMessage());
            }
        }

        private String loteParaJson(Lote l) {
            String nomeProduto = (l.getProduto() != null) ? l.getProduto().getNome() : "";
            return String.format(
                    "{\"idLote\":%d,\"quantInicial\":%d,\"quantAtual\":%d,\"dataValidade\":\"%s\",\"dataEntrada\":\"%s\",\"status\":\"%s\",\"Produto_idProduto\":%d,\"produtoNome\":\"%s\"}",
                    l.getIdLote(), l.getQuantInicial(), l.getQuantAtual(),
                    l.getDataValidade() != null ? l.getDataValidade().toString() : "",
                    l.getDataEntrada() != null ? l.getDataEntrada().toString() : "",
                    SimpleJson.escape(l.getStatus()), l.getProdutoId(), SimpleJson.escape(nomeProduto));
        }
    }

    // -------------------------------------------------------------------
    // CARRINHO: GET /api/carrinho?usuarioId=X | POST /api/carrinho
    //           PUT /api/carrinho/{id} | DELETE /api/carrinho/{id}
    // -------------------------------------------------------------------
    private class CarrinhoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String metodo = exchange.getRequestMethod();
                String[] segmentos = extrairSegmentos(exchange, "/api/carrinho");
                Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());

                if (segmentos.length == 0) {
                    if ("GET".equalsIgnoreCase(metodo)) {
                        int usuarioId = query.containsKey("usuarioId") ? parseIdOuMenosUm(query.get("usuarioId")) : -1;
                        if (usuarioId <= 0) {
                            enviarErro(exchange, 400, "Parâmetro usuarioId é obrigatório.");
                            return;
                        }
                        List<UsuarioCarrinho> itens = carrinhoController.listarPorUsuario(usuarioId);
                        enviarRespostaJson(exchange, 200, listaParaJson(itens, this::itemCarrinhoParaJson));
                        return;
                    }
                    if ("POST".equalsIgnoreCase(metodo)) {
                        Map<String, Object> body = SimpleJson.parseObject(lerCorpo(exchange));
                        Integer usuarioId = SimpleJson.getInt(body, "usuarioId");
                        Integer produtoId = SimpleJson.getInt(body, "produtoId");
                        Integer quantidade = SimpleJson.getInt(body, "quantidade");
                        boolean ok = carrinhoController.adicionarItem(
                                usuarioId != null ? usuarioId : 0,
                                produtoId != null ? produtoId : 0,
                                quantidade != null ? quantidade : 1);
                        if (ok) {
                            enviarRespostaJson(exchange, 201, "{\"sucesso\":true}");
                        } else {
                            enviarErro(exchange, 400, "Não foi possível adicionar o item ao carrinho.");
                        }
                        return;
                    }
                    enviarErro(exchange, 405, "Método não suportado.");
                    return;
                }

                int idItem = parseIdOuMenosUm(segmentos[0]);
                if (idItem <= 0) {
                    enviarErro(exchange, 400, "ID de item de carrinho inválido.");
                    return;
                }
                if ("PUT".equalsIgnoreCase(metodo)) {
                    Map<String, Object> body = SimpleJson.parseObject(lerCorpo(exchange));
                    Integer novaQtd = SimpleJson.getInt(body, "quantidade");
                    boolean ok = carrinhoController.atualizarQuantidade(idItem, novaQtd != null ? novaQtd : 0);
                    if (ok) {
                        enviarRespostaJson(exchange, 200, "{\"sucesso\":true}");
                    } else {
                        enviarErro(exchange, 400, "Não foi possível atualizar a quantidade.");
                    }
                    return;
                }
                if ("DELETE".equalsIgnoreCase(metodo)) {
                    boolean ok = carrinhoController.removerItem(idItem);
                    if (ok) {
                        enviarRespostaJson(exchange, 200, "{\"sucesso\":true}");
                    } else {
                        enviarErro(exchange, 400, "Não foi possível remover o item.");
                    }
                    return;
                }
                enviarErro(exchange, 405, "Método não suportado.");
            } catch (Exception e) {
                enviarErro(exchange, 500, "Erro interno: " + e.getMessage());
            }
        }

        private String itemCarrinhoParaJson(UsuarioCarrinho c) {
            Produto prod = c.getProduto();
            String produtoJson = "null";
            if (prod != null) {
                produtoJson = String.format(
                        "{\"idProduto\":%d,\"nome\":\"%s\",\"marca\":\"%s\",\"precoBase\":%s,\"status\":\"%s\"}",
                        prod.getIdProduto(), SimpleJson.escape(prod.getNome()), SimpleJson.escape(prod.getMarca()),
                        prod.getPrecoBase() != null ? prod.getPrecoBase().toString() : "0",
                        SimpleJson.escape(prod.getStatus()));
            }
            return String.format(
                    "{\"idUsuarioCarrinho\":%d,\"quantidade\":%d,\"Usuario_idUsuario\":%d,\"Produto_idProduto\":%d,\"produto\":%s}",
                    c.getIdUsuarioCarrinho(), c.getQuantidade(), c.getUsuarioId(), c.getProdutoId(), produtoJson);
        }
    }

    // -------------------------------------------------------------------
    // PEDIDOS: GET /api/pedidos (?usuarioId=X) | GET /api/pedidos/{id}
    //          POST /api/pedidos (finaliza carrinho) | PUT /api/pedidos/{id}
    // -------------------------------------------------------------------
    private class PedidosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String metodo = exchange.getRequestMethod();
                String[] segmentos = extrairSegmentos(exchange, "/api/pedidos");
                Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());

                if (segmentos.length == 0) {
                    if ("GET".equalsIgnoreCase(metodo)) {
                        List<Pedido> pedidos;
                        if (query.containsKey("usuarioId")) {
                            pedidos = pedidoController.listarPorUsuario(parseIdOuMenosUm(query.get("usuarioId")));
                        } else {
                            pedidos = pedidoController.listarTodos();
                        }
                        enviarRespostaJson(exchange, 200, listaParaJson(pedidos, this::pedidoParaJson));
                        return;
                    }
                    if ("POST".equalsIgnoreCase(metodo)) {
                        Map<String, Object> body = SimpleJson.parseObject(lerCorpo(exchange));
                        Integer usuarioId = SimpleJson.getInt(body, "usuarioId");
                        String statusPagamento = SimpleJson.getString(body, "statusPagamento");

                        Pedido pedido = pedidoController.finalizarCompraDoCarrinho(
                                usuarioId != null ? usuarioId : 0, statusPagamento);

                        if (pedido != null && pedido.getIdPedido() > 0) {
                            Pedido completo = pedidoController.buscarPorId(pedido.getIdPedido());
                            enviarRespostaJson(exchange, 201, pedidoParaJson(completo != null ? completo : pedido));
                        } else {
                            enviarErro(exchange, 400, "Não foi possível finalizar o pedido. Verifique o carrinho e o estoque dos lotes.");
                        }
                        return;
                    }
                    enviarErro(exchange, 405, "Método não suportado.");
                    return;
                }

                int id = parseIdOuMenosUm(segmentos[0]);
                if (id <= 0) {
                    enviarErro(exchange, 400, "ID de pedido inválido.");
                    return;
                }
                if ("GET".equalsIgnoreCase(metodo)) {
                    Pedido pedido = pedidoController.buscarPorId(id);
                    if (pedido == null) {
                        enviarErro(exchange, 404, "Pedido não encontrado.");
                    } else {
                        enviarRespostaJson(exchange, 200, pedidoParaJson(pedido));
                    }
                    return;
                }
                if ("PUT".equalsIgnoreCase(metodo)) {
                    Map<String, Object> body = SimpleJson.parseObject(lerCorpo(exchange));
                    String novoStatus = SimpleJson.getString(body, "statusPagamento");
                    boolean ok = pedidoController.atualizarStatusPagamento(id, novoStatus);
                    if (ok) {
                        enviarRespostaJson(exchange, 200, "{\"sucesso\":true}");
                    } else {
                        enviarErro(exchange, 400, "Não foi possível atualizar o status do pedido.");
                    }
                    return;
                }
                enviarErro(exchange, 405, "Método não suportado.");
            } catch (Exception e) {
                enviarErro(exchange, 500, "Erro interno: " + e.getMessage());
            }
        }

        private String pedidoParaJson(Pedido p) {
            StringBuilder itensSb = new StringBuilder("[");
            List<ItemPedido> itens = p.getItensPedido();
            for (int i = 0; i < itens.size(); i++) {
                ItemPedido it = itens.get(i);
                String produtoNome = "";
                if (it.getLote() != null && it.getLote().getProduto() != null) {
                    produtoNome = it.getLote().getProduto().getNome();
                } else {
                    // Fallback: alguns métodos do controller não populam o objeto Produto aninhado no Lote
                    Lote loteCompleto = loteController.buscarPorId(it.getLoteId());
                    if (loteCompleto != null && loteCompleto.getProduto() != null) {
                        produtoNome = loteCompleto.getProduto().getNome();
                    }
                }
                itensSb.append(String.format(
                        "{\"idItemPedido\":%d,\"quantidade\":%d,\"precoAplicado\":%s,\"Lote_idLote\":%d,\"Pedido_idPedido\":%d,\"produtoNome\":\"%s\"}",
                        it.getIdItemPedido(), it.getQuantidade(),
                        it.getPrecoAplicado() != null ? it.getPrecoAplicado().toString() : "0",
                        it.getLoteId(), it.getPedidoId(), SimpleJson.escape(produtoNome)));
                if (i < itens.size() - 1) itensSb.append(",");
            }
            itensSb.append("]");

            return String.format(
                    "{\"idPedido\":%d,\"dataVenda\":\"%s\",\"total\":%s,\"statusPagamento\":\"%s\",\"Usuario_idUsuario\":%d,\"itens\":%s}",
                    p.getIdPedido(),
                    p.getDataVenda() != null ? p.getDataVenda().format(DT_FORMATTER) : "",
                    p.getTotal() != null ? p.getTotal().toString() : "0",
                    SimpleJson.escape(p.getStatusPagamento()), p.getUsuarioId(), itensSb.toString());
        }
    }

    // -------------------------------------------------------------------
    // PERDAS: GET /api/perdas (?loteId=X) | POST /api/perdas
    // -------------------------------------------------------------------
    private class PerdasHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String metodo = exchange.getRequestMethod();
                String[] segmentos = extrairSegmentos(exchange, "/api/perdas");
                Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());

                if (segmentos.length == 0) {
                    if ("GET".equalsIgnoreCase(metodo)) {
                        List<Perda> perdas = query.containsKey("loteId")
                                ? perdaController.listarPorLote(parseIdOuMenosUm(query.get("loteId")))
                                : perdaController.listarTodos();
                        enviarRespostaJson(exchange, 200, listaParaJson(perdas, this::perdaParaJson));
                        return;
                    }
                    if ("POST".equalsIgnoreCase(metodo)) {
                        Map<String, Object> body = SimpleJson.parseObject(lerCorpo(exchange));
                        Integer loteId = SimpleJson.getInt(body, "loteId");
                        Integer quantidade = SimpleJson.getInt(body, "quantidade");
                        String motivo = SimpleJson.getString(body, "motivo");

                        boolean ok = perdaController.registrarPerda(
                                loteId != null ? loteId : 0,
                                quantidade != null ? quantidade : 0,
                                motivo);
                        if (ok) {
                            enviarRespostaJson(exchange, 201, "{\"sucesso\":true}");
                        } else {
                            enviarErro(exchange, 400, "Não foi possível registrar a perda. Verifique o saldo do lote.");
                        }
                        return;
                    }
                    enviarErro(exchange, 405, "Método não suportado.");
                    return;
                }

                enviarErro(exchange, 404, "Recurso não encontrado.");
            } catch (Exception e) {
                enviarErro(exchange, 500, "Erro interno: " + e.getMessage());
            }
        }

        private String perdaParaJson(Perda p) {
            return String.format(
                    "{\"idPerda\":%d,\"quantidade\":%d,\"motivo\":\"%s\",\"dataRegistro\":\"%s\",\"Lote_idLote\":%d}",
                    p.getIdPerda(), (p.getQuantidade() != null ? p.getQuantidade() : 0),
                    SimpleJson.escape(p.getMotivo()),
                    (p.getDataRegistro() != null ? p.getDataRegistro().format(DT_FORMATTER) : ""),
                    p.getLoteId());
        }
    }

    // -------------------------------------------------------------------
    // USUARIOS: POST /api/usuarios/login | POST /api/usuarios/cadastro
    // -------------------------------------------------------------------
    private class UsuariosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String metodo = exchange.getRequestMethod();
                String[] segmentos = extrairSegmentos(exchange, "/api/usuarios");

                if (segmentos.length == 1 && "login".equalsIgnoreCase(segmentos[0]) && "POST".equalsIgnoreCase(metodo)) {
                    Map<String, Object> body = SimpleJson.parseObject(lerCorpo(exchange));
                    String email = SimpleJson.getString(body, "email");
                    String senha = SimpleJson.getString(body, "senha");

                    Usuario usuario = usuarioController.autenticar(email, senha);
                    if (usuario != null) {
                        enviarRespostaJson(exchange, 200, usuarioParaJson(usuario));
                    } else {
                        enviarErro(exchange, 401, "E-mail ou senha inválidos.");
                    }
                    return;
                }

                if (segmentos.length == 1 && "cadastro".equalsIgnoreCase(segmentos[0]) && "POST".equalsIgnoreCase(metodo)) {
                    Map<String, Object> body = SimpleJson.parseObject(lerCorpo(exchange));
                    String nome = SimpleJson.getString(body, "nome");
                    String email = SimpleJson.getString(body, "email");
                    String senha = SimpleJson.getString(body, "senha");

                    if (email != null && usuarioController.buscarPorEmail(email) != null) {
                        enviarErro(exchange, 409, "Já existe uma conta cadastrada com este e-mail.");
                        return;
                    }

                    boolean ok = usuarioController.cadastrar(nome, email, senha, "CLIENTE");
                    if (ok) {
                        Usuario criado = usuarioController.buscarPorEmail(email);
                        enviarRespostaJson(exchange, 201, criado != null ? usuarioParaJson(criado) : "{\"sucesso\":true}");
                    } else {
                        enviarErro(exchange, 400, "Não foi possível concluir o cadastro. Verifique nome, e-mail e senha (mín. 4 caracteres).");
                    }
                    return;
                }

                // POST /api/usuarios/recuperar-senha
                // Regra do escopo: o usuário NÃO edita a senha manualmente; o sistema gera
                // uma nova senha aleatória e (nesta demo) devolve para o front-end
                // simular o envio por e-mail.
                if (segmentos.length == 1 && "recuperar-senha".equalsIgnoreCase(segmentos[0]) && "POST".equalsIgnoreCase(metodo)) {
                    Map<String, Object> body = SimpleJson.parseObject(lerCorpo(exchange));
                    String email = SimpleJson.getString(body, "email");

                    String novaSenha = usuarioController.recuperarSenha(email);
                    if (novaSenha != null) {
                        enviarRespostaJson(exchange, 200, "{\"sucesso\":true,\"novaSenha\":\"" + SimpleJson.escape(novaSenha) + "\"}");
                    } else {
                        enviarErro(exchange, 404, "Não encontramos nenhuma conta cadastrada com este e-mail.");
                    }
                    return;
                }

                enviarErro(exchange, 404, "Recurso não encontrado.");
            } catch (Exception e) {
                enviarErro(exchange, 500, "Erro interno: " + e.getMessage());
            }
        }

        private String usuarioParaJson(Usuario u) {
            return String.format(
                    "{\"idUsuario\":%d,\"nome\":\"%s\",\"email\":\"%s\",\"perfil\":\"%s\"}",
                    u.getIdUsuario(), SimpleJson.escape(u.getNome()), SimpleJson.escape(u.getEmail()),
                    SimpleJson.escape(u.getPerfil()));
        }
    }

    // =========================================================================
    // HANDLER DE RELATÓRIOS E MÉTRICAS (item 8 do escopo)
    //   GET /api/relatorios?tipo=geral    -> Estatísticas Gerais
    //   GET /api/relatorios?tipo=vendas   -> Relatório de Vendas por produto
    //   GET /api/relatorios?tipo=estoque  -> Controle de Estoque por produto
    //   GET /api/relatorios?tipo=consumo  -> Análise de Consumo por categoria
    //   (o Relatório de Perdas em si continua em GET /api/perdas)
    // =========================================================================
    private class RelatoriosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String metodo = exchange.getRequestMethod();
                if (!"GET".equalsIgnoreCase(metodo)) {
                    enviarErro(exchange, 405, "Método não suportado.");
                    return;
                }

                Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
                String tipo = query.getOrDefault("tipo", "geral").toLowerCase();

                switch (tipo) {
                    case "vendas":
                        enviarRespostaJson(exchange, 200, listaDeMapasParaJson(relatorioController.relatorioVendas()));
                        return;
                    case "estoque":
                        enviarRespostaJson(exchange, 200, listaDeMapasParaJson(relatorioController.relatorioEstoque()));
                        return;
                    case "consumo":
                        enviarRespostaJson(exchange, 200, listaDeMapasParaJson(relatorioController.relatorioConsumo()));
                        return;
                    case "geral":
                        enviarRespostaJson(exchange, 200, mapaParaJson(relatorioController.estatisticasGerais()));
                        return;
                    default:
                        enviarErro(exchange, 400, "Tipo de relatório inválido. Use: geral, vendas, estoque ou consumo.");
                }
            } catch (Exception e) {
                enviarErro(exchange, 500, "Erro interno: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // HELPERS COMPARTILHADOS
    // =========================================================================

    private interface JsonMapper<T> {
        String toJson(T item);
    }

    private static <T> String listaParaJson(List<T> lista, JsonMapper<T> mapper) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(mapper.toJson(lista.get(i)));
            if (i < lista.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Serializa um Map genérico (usado pelos relatórios agregados, que não têm
     * uma classe de modelo própria) para JSON. Suporta String, Number, Boolean
     * e null como valores.
     */
    private static String mapaParaJson(Map<String, Object> mapa) {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Object> entry : mapa.entrySet()) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(SimpleJson.escape(entry.getKey())).append("\":");
            sb.append(valorParaJson(entry.getValue()));
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String listaDeMapasParaJson(List<Map<String, Object>> lista) {
        return listaParaJson(lista, WebServer::mapaParaJson);
    }

    private static String valorParaJson(Object valor) {
        if (valor == null) return "null";
        if (valor instanceof Number || valor instanceof Boolean) return String.valueOf(valor);
        return "\"" + SimpleJson.escape(String.valueOf(valor)) + "\"";
    }

    private static String[] extrairSegmentos(HttpExchange exchange, String prefixo) {
        String path = exchange.getRequestURI().getPath();
        String resto = path.length() > prefixo.length() ? path.substring(prefixo.length()) : "";
        resto = resto.replaceFirst("^/", "").replaceFirst("/$", "");
        if (resto.isEmpty()) return new String[0];
        return resto.split("/");
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        if (query == null || query.trim().isEmpty()) return params;
        for (String par : query.split("&")) {
            String[] kv = par.split("=", 2);
            try {
                String chave = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String valor = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                params.put(chave, valor);
            } catch (Exception ignored) {}
        }
        return params;
    }

    private static int parseIdOuMenosUm(String valor) {
        if (valor == null) return -1;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static LocalDate parseDataOuNull(String valor) {
        if (valor == null || valor.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(valor.trim().substring(0, Math.min(10, valor.trim().length())));
        } catch (Exception e) {
            return null;
        }
    }

    private static String lerCorpo(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int lidos;
            while ((lidos = is.read(buffer)) != -1) {
                bos.write(buffer, 0, lidos);
            }
            return bos.toString(StandardCharsets.UTF_8);
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

    private static void enviarErro(HttpExchange exchange, int status, String mensagem) throws IOException {
        String json = String.format("{\"erro\":\"%s\"}", SimpleJson.escape(mensagem));
        enviarRespostaJson(exchange, status, json);
    }
}