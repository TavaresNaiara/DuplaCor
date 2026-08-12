package view;

import controller.*;
import dao.Conexao;
import model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interface Console para demonstração e interação com o sistema Dupla Cor.
 */
public class MenuPrincipal {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final ProdutoController produtoController = new ProdutoController();
    private static final CategoriaController categoriaController = new CategoriaController();
    private static final LoteController loteController = new LoteController();
    private static final PedidoController pedidoController = new PedidoController();
    private static final PerdaController perdaController = new PerdaController();
    private static final UsuarioController usuarioController = new UsuarioController();
    private static final UsuarioCarrinhoController carrinhoController = new UsuarioCarrinhoController();

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("               DUPLA COR - ESMALTARIA E VENDAS                ");
        System.out.println("           Sistema de Controle de Estoque por Lote (FEFO)      ");
        System.out.println("===============================================================");

        // Testar conexão inicial
        if (Conexao.testarConexao()) {
            System.out.println(" Conexão com o banco de dados MySQL estabelecida com sucesso!");
        } else {
            System.err.println(" ATENÇÃO: Falha ao conectar no MySQL. Verifique suas variáveis de ambiente (.env).");
        }

        boolean rodando = true;
        while (rodando) {
            exibirMenuPrincipal();
            int opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1 -> menuProdutos();
                case 2 -> menuCategorias();
                case 3 -> menuLotes();
                case 4 -> menuVendas();
                case 5 -> menuCarrinho();
                case 6 -> menuPerdas();
                case 7 -> menuUsuarios();
                case 8 -> executarDemonstracaoCompleta();
                case 9 -> {
                    System.out.println("\nEncerrando a aplicação Dupla Cor. Até logo!");
                    rodando = false;
                }
                default -> System.out.println("Opção inválida! Tente novamente.");
            }
        }
    }

    private static void exibirMenuPrincipal() {
        System.out.println("\n--------------------- MENU PRINCIPAL ---------------------");
        System.out.println("1. Gestão de Produtos (Catálogo)");
        System.out.println("2. Gestão de Categorias");
        System.out.println("3. Gestão de Estoque por Lotes (FEFO)");
        System.out.println("4. Gestão de Vendas e Pedidos");
        System.out.println("5. Carrinho de Compras Persistente");
        System.out.println("6. Relatório e Registro de Perdas");
        System.out.println("7. Gestão de Usuários");
        System.out.println("8. Executar Demonstração Automática (Fluxo FEFO)");
        System.out.println("9. Sair");
        System.out.println("----------------------------------------------------------");
    }

    // =========================================================================
    // 1. PRODUTOS
    // =========================================================================
    private static void menuProdutos() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n--- GESTÃO DE PRODUTOS ---");
            System.out.println("1. Cadastrar Produto");
            System.out.println("2. Listar Todos os Produtos (Catálogo)");
            System.out.println("3. Listar Produtos Disponíveis (Vitrine com Estoque Válido)");
            System.out.println("4. Buscar Produto por ID");
            System.out.println("5. Atualizar Produto");
            System.out.println("6. Inativar Produto (Exclusão Lógica)");
            System.out.println("7. Excluir Produto (Físico)");
            System.out.println("0. Voltar ao Menu Principal");

            int op = lerInteiro("Opção: ");
            switch (op) {
                case 1 -> {
                    String nome = lerString("Nome do esmalte/produto: ");
                    String marca = lerString("Marca (ex: Risqué, Impala, Colorama): ");
                    BigDecimal preco = lerBigDecimal("Preço base (ex: 8.90): ");

                    System.out.println("Categorias disponíveis:");
                    List<Categoria> cats = categoriaController.listarTodos();
                    cats.forEach(c -> System.out.println("  [" + c.getIdCategoria() + "] " + c.getNome()));

                    String catInput = lerString("IDs das categorias separados por vírgula (ex: 1,3) ou ENTER para nenhuma: ");
                    List<Integer> catIds = new ArrayList<>();
                    if (!catInput.trim().isEmpty()) {
                        for (String part : catInput.split(",")) {
                            try {
                                catIds.add(Integer.parseInt(part.trim()));
                            } catch (NumberFormatException ignored) {}
                        }
                    }

                    if (produtoController.cadastrar(nome, marca, preco, catIds)) {
                        System.out.println(" Produto cadastrado com sucesso!");
                    } else {
                        System.err.println(" Erro ao cadastrar produto.");
                    }
                }
                case 2 -> {
                    List<Produto> prods = produtoController.listarTodos();
                    System.out.println("\n--- CATÁLOGO COMPLETO (" + prods.size() + " produtos) ---");
                    for (Produto p : prods) {
                        System.out.printf("ID: %-3d | Nome: %-25s | Marca: %-12s | Preço: R$ %-6.2f | Status: %-8s%n",
                                p.getIdProduto(), p.getNome(), (p.getMarca() != null ? p.getMarca() : "-"),
                                p.getPrecoBase(), p.getStatus());
                    }
                }
                case 3 -> {
                    List<Produto> vitrine = produtoController.listarVitrine();
                    System.out.println("\n--- VITRINE (Apenas com Lote Válido e Disponível) ---");
                    if (vitrine.isEmpty()) {
                        System.out.println("Nenhum produto com lote disponível no momento.");
                    } else {
                        for (Produto p : vitrine) {
                            System.out.printf("ID: %-3d | Nome: %-25s | Marca: %-12s | Preço: R$ %-6.2f%n",
                                    p.getIdProduto(), p.getNome(), (p.getMarca() != null ? p.getMarca() : "-"), p.getPrecoBase());
                        }
                    }
                }
                case 4 -> {
                    int id = lerInteiro("ID do Produto: ");
                    Produto p = produtoController.buscarPorId(id);
                    if (p != null) {
                        System.out.println("\nDetalhes do Produto:");
                        System.out.println("ID: " + p.getIdProduto());
                        System.out.println("Nome: " + p.getNome());
                        System.out.println("Marca: " + p.getMarca());
                        System.out.println("Preço Base: R$ " + p.getPrecoBase());
                        System.out.println("Status: " + p.getStatus());
                        System.out.print("Categorias: ");
                        if (p.getCategorias() != null && !p.getCategorias().isEmpty()) {
                            p.getCategorias().forEach(c -> System.out.print("[" + c.getNome() + "] "));
                            System.out.println();
                        } else {
                            System.out.println("Nenhuma");
                        }
                    } else {
                        System.err.println("Produto não encontrado.");
                    }
                }
                case 5 -> {
                    int id = lerInteiro("ID do Produto para atualizar: ");
                    Produto p = produtoController.buscarPorId(id);
                    if (p != null) {
                        String novoNome = lerString("Novo Nome (ENTER para manter '" + p.getNome() + "'): ");
                        String novaMarca = lerString("Nova Marca (ENTER para manter '" + p.getMarca() + "'): ");
                        String precoStr = lerString("Novo Preço (ENTER para manter R$ " + p.getPrecoBase() + "): ");
                        String novoStatus = lerString("Novo Status [ATIVO/INATIVO] (ENTER para manter '" + p.getStatus() + "'): ");

                        BigDecimal novoPreco = precoStr.trim().isEmpty() ? p.getPrecoBase() : new BigDecimal(precoStr.trim().replace(",", "."));
                        String nomeFinal = novoNome.trim().isEmpty() ? p.getNome() : novoNome;
                        String marcaFinal = novaMarca.trim().isEmpty() ? p.getMarca() : novaMarca;
                        String statusFinal = novoStatus.trim().isEmpty() ? p.getStatus() : novoStatus;

                        if (produtoController.atualizar(id, nomeFinal, marcaFinal, novoPreco, statusFinal, null)) {
                            System.out.println(" Produto atualizado com sucesso!");
                        } else {
                            System.err.println(" Erro ao atualizar produto.");
                        }
                    } else {
                        System.err.println("Produto não encontrado.");
                    }
                }
                case 6 -> {
                    int id = lerInteiro("ID do Produto para inativar: ");
                    if (produtoController.inativar(id)) {
                        System.out.println(" Produto inativado com sucesso (exclusão lógica).");
                    } else {
                        System.err.println(" Erro ao inativar produto.");
                    }
                }
                case 7 -> {
                    int id = lerInteiro("ID do Produto para excluir definitivamente: ");
                    if (produtoController.excluir(id)) {
                        System.out.println(" Produto excluído com sucesso.");
                    } else {
                        System.err.println(" Erro ao excluir produto (verifique se existem lotes ou pedidos vinculados).");
                    }
                }
                case 0 -> voltar = true;
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    // =========================================================================
    // 2. CATEGORIAS
    // =========================================================================
    private static void menuCategorias() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n--- GESTÃO DE CATEGORIAS ---");
            System.out.println("1. Cadastrar Categoria");
            System.out.println("2. Listar Categorias");
            System.out.println("3. Buscar Categoria por ID");
            System.out.println("4. Atualizar Categoria");
            System.out.println("5. Excluir Categoria");
            System.out.println("0. Voltar");

            int op = lerInteiro("Opção: ");
            switch (op) {
                case 1 -> {
                    String nome = lerString("Nome da categoria: ");
                    String desc = lerString("Descrição: ");
                    if (categoriaController.cadastrar(nome, desc)) {
                        System.out.println(" Categoria cadastrada com sucesso!");
                    } else {
                        System.err.println(" Erro ao cadastrar categoria.");
                    }
                }
                case 2 -> {
                    List<Categoria> lista = categoriaController.listarTodos();
                    System.out.println("\n--- CATEGORIAS CADASTRADAS ---");
                    for (Categoria c : lista) {
                        System.out.printf("ID: %-3d | Nome: %-20s | Descrição: %s%n",
                                c.getIdCategoria(), c.getNome(), (c.getDescricao() != null ? c.getDescricao() : "-"));
                    }
                }
                case 3 -> {
                    int id = lerInteiro("ID da Categoria: ");
                    Categoria c = categoriaController.buscarPorId(id);
                    if (c != null) {
                        System.out.println("ID: " + c.getIdCategoria() + " | Nome: " + c.getNome() + " | Descrição: " + c.getDescricao());
                    } else {
                        System.err.println("Categoria não encontrada.");
                    }
                }
                case 4 -> {
                    int id = lerInteiro("ID da Categoria para atualizar: ");
                    Categoria c = categoriaController.buscarPorId(id);
                    if (c != null) {
                        String nome = lerString("Novo Nome (ENTER para '" + c.getNome() + "'): ");
                        String desc = lerString("Nova Descrição (ENTER para manter): ");
                        String nomeF = nome.trim().isEmpty() ? c.getNome() : nome;
                        String descF = desc.trim().isEmpty() ? c.getDescricao() : desc;
                        if (categoriaController.atualizar(id, nomeF, descF)) {
                            System.out.println(" Categoria atualizada!");
                        }
                    } else {
                        System.err.println("Categoria não encontrada.");
                    }
                }
                case 5 -> {
                    int id = lerInteiro("ID da Categoria para excluir: ");
                    if (categoriaController.excluir(id)) {
                        System.out.println(" Categoria excluída com sucesso.");
                    } else {
                        System.err.println(" Erro ao excluir categoria.");
                    }
                }
                case 0 -> voltar = true;
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    // =========================================================================
    // 3. LOTES (FEFO)
    // =========================================================================
    private static void menuLotes() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n--- GESTÃO DE ESTOQUE POR LOTES (FEFO) ---");
            System.out.println("1. Cadastrar Lote (Entrada de Remessa)");
            System.out.println("2. Listar Todos os Lotes");
            System.out.println("3. Listar Lotes por Produto (Ordem FEFO - Vencimento Próximo)");
            System.out.println("4. Buscar Lote por ID");
            System.out.println("5. Executar Monitoramento de Validade (Bloquear Vencidos)");
            System.out.println("6. Atualizar Quantidade do Lote");
            System.out.println("0. Voltar");

            int op = lerInteiro("Opção: ");
            switch (op) {
                case 1 -> {
                    int prodId = lerInteiro("ID do Produto associado: ");
                    int qtd = lerInteiro("Quantidade inicial da remessa: ");
                    LocalDate dtVal = lerData("Data de validade (dd/MM/yyyy): ");
                    LocalDate dtEnt = lerData("Data de entrada (dd/MM/yyyy) [ENTER para hoje]: ");
                    if (dtEnt == null) dtEnt = LocalDate.now();

                    if (loteController.cadastrar(qtd, dtVal, dtEnt, prodId)) {
                        System.out.println(" Lote cadastrado e integrado ao estoque com sucesso!");
                    } else {
                        System.err.println(" Erro ao cadastrar lote.");
                    }
                }
                case 2 -> {
                    List<Lote> lotes = loteController.listarTodos();
                    System.out.println("\n--- LOTES EM ESTOQUE ---");
                    for (Lote l : lotes) {
                        String prodNome = (l.getProduto() != null) ? l.getProduto().getNome() : ("Prod #" + l.getProdutoId());
                        System.out.printf("Lote #%-3d | Produto: %-22s | Inicial: %-4d | Saldo: %-4d | Validade: %-10s | Status: %-10s%n",
                                l.getIdLote(), prodNome, l.getQuantInicial(), l.getQuantAtual(),
                                l.getDataValidade().format(DATE_FORMATTER), l.getStatus());
                    }
                }
                case 3 -> {
                    int prodId = lerInteiro("ID do Produto: ");
                    List<Lote> fefo = loteController.listarDisponiveisFEFO(prodId);
                    System.out.println("\n--- FILA FEFO (First Expired, First Out) ---");
                    if (fefo.isEmpty()) {
                        System.out.println("Nenhum lote disponível/válido para este produto.");
                    } else {
                        int pos = 1;
                        for (Lote l : fefo) {
                            System.out.printf("%dº a sair -> Lote #%-3d | Saldo: %-4d un | Validade: %s | Entrada: %s%n",
                                    pos++, l.getIdLote(), l.getQuantAtual(),
                                    l.getDataValidade().format(DATE_FORMATTER),
                                    l.getDataEntrada().format(DATE_FORMATTER));
                        }
                    }
                }
                case 4 -> {
                    int id = lerInteiro("ID do Lote: ");
                    Lote l = loteController.buscarPorId(id);
                    if (l != null) {
                        System.out.println(l);
                    } else {
                        System.err.println("Lote não encontrado.");
                    }
                }
                case 5 -> {
                    int bloqueados = loteController.monitorarEBloquearVencidos();
                    System.out.println(" Varredura de validade concluída. Lotes bloqueados/atualizados: " + bloqueados);
                }
                case 6 -> {
                    int id = lerInteiro("ID do Lote: ");
                    Lote l = loteController.buscarPorId(id);
                    if (l != null) {
                        int novaQtd = lerInteiro("Nova quantidade atual (Saldo): ");
                        if (loteController.atualizar(id, novaQtd, l.getDataValidade(), l.getDataEntrada(), l.getStatus())) {
                            System.out.println(" Quantidade do lote atualizada.");
                        }
                    }
                }
                case 0 -> voltar = true;
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    // =========================================================================
    // 4. VENDAS E PEDIDOS
    // =========================================================================
    private static void menuVendas() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n--- GESTÃO DE VENDAS E PEDIDOS ---");
            System.out.println("1. Realizar Venda Direta (com baixa automática por FEFO)");
            System.out.println("2. Finalizar Pedido a partir do Carrinho Persistente");
            System.out.println("3. Listar Todos os Pedidos");
            System.out.println("4. Detalhar Pedido por ID (Ver Itens e Lotes consumidos)");
            System.out.println("5. Listar Pedidos de um Usuário");
            System.out.println("0. Voltar");

            int op = lerInteiro("Opção: ");
            switch (op) {
                case 1 -> {
                    int userId = lerInteiro("ID do Usuário Comprador: ");
                    int prodId = lerInteiro("ID do Produto (Esmalte): ");
                    int qtd = lerInteiro("Quantidade: ");
                    String statusPag = lerString("Status do Pagamento [PAGO/APROVADO/PENDENTE]: ");

                    Pedido ped = pedidoController.realizarVendaDireta(userId, prodId, qtd, statusPag);
                    if (ped != null) {
                        System.out.println("\n Venda realizada com sucesso!");
                        System.out.println("Pedido ID: #" + ped.getIdPedido() + " | Total: R$ " + ped.getTotal());
                        System.out.println("Itens alocados dos lotes:");
                        ped.getItensPedido().forEach(it ->
                                System.out.println("  -> " + it.getQuantidade() + " un alocadas do Lote #" + it.getLoteId() +
                                        " a R$ " + it.getPrecoAplicado()));
                    } else {
                        System.err.println(" Erro ao processar venda.");
                    }
                }
                case 2 -> {
                    int userId = lerInteiro("ID do Usuário para checkout do carrinho: ");
                    Pedido ped = pedidoController.finalizarCompraDoCarrinho(userId, "PAGO");
                    if (ped != null) {
                        System.out.println("\n Compra finalizada com sucesso a partir do carrinho!");
                        System.out.println("Pedido ID: #" + ped.getIdPedido() + " | Total: R$ " + ped.getTotal());
                    } else {
                        System.err.println(" Erro ao finalizar compra do carrinho.");
                    }
                }
                case 3 -> {
                    List<Pedido> pedidos = pedidoController.listarTodos();
                    System.out.println("\n--- HISTÓRICO DE PEDIDOS ---");
                    for (Pedido p : pedidos) {
                        String userNome = (p.getUsuario() != null) ? p.getUsuario().getNome() : ("User #" + p.getUsuarioId());
                        System.out.printf("Pedido #%-4d | Data: %-19s | Cliente: %-20s | Total: R$ %-7.2f | Status: %s%n",
                                p.getIdPedido(), p.getDataVenda(), userNome, p.getTotal(), p.getStatusPagamento());
                    }
                }
                case 4 -> {
                    int id = lerInteiro("ID do Pedido: ");
                    Pedido p = pedidoController.buscarPorId(id);
                    if (p != null) {
                        System.out.println("\n================ PEDIDO #" + p.getIdPedido() + " ================");
                        System.out.println("Cliente: " + (p.getUsuario() != null ? p.getUsuario().getNome() : p.getUsuarioId()));
                        System.out.println("Data da Venda: " + p.getDataVenda());
                        System.out.println("Status: " + p.getStatusPagamento());
                        System.out.println("Total: R$ " + p.getTotal());
                        System.out.println("Itens do Pedido (Rastreabilidade por Lote):");
                        for (ItemPedido item : p.getItensPedido()) {
                            String prodDesc = (item.getLote() != null && item.getLote().getProduto() != null)
                                    ? item.getLote().getProduto().getNome()
                                    : "Produto";
                            System.out.printf("  * %s | Qtd: %d | Lote #%d | Preço Unit: R$ %.2f | Subtotal: R$ %.2f%n",
                                    prodDesc, item.getQuantidade(), item.getLoteId(), item.getPrecoAplicado(), item.getSubtotal());
                        }
                        System.out.println("=================================================");
                    } else {
                        System.err.println("Pedido não encontrado.");
                    }
                }
                case 5 -> {
                    int userId = lerInteiro("ID do Usuário: ");
                    List<Pedido> lista = pedidoController.listarPorUsuario(userId);
                    System.out.println("\nPedidos do usuário #" + userId + " (" + lista.size() + " encontrados):");
                    for (Pedido p : lista) {
                        System.out.printf("Pedido #%-4d | Data: %-19s | Total: R$ %-7.2f | Status: %s%n",
                                p.getIdPedido(), p.getDataVenda(), p.getTotal(), p.getStatusPagamento());
                    }
                }
                case 0 -> voltar = true;
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    // =========================================================================
    // 5. CARRINHO PERSISTENTE
    // =========================================================================
    private static void menuCarrinho() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n--- CARRINHO DE COMPRAS PERSISTENTE ---");
            System.out.println("1. Adicionar Produto ao Carrinho");
            System.out.println("2. Visualizar Carrinho do Usuário");
            System.out.println("3. Alterar Quantidade de Item");
            System.out.println("4. Remover Item do Carrinho");
            System.out.println("5. Limpar Todo o Carrinho");
            System.out.println("0. Voltar");

            int op = lerInteiro("Opção: ");
            switch (op) {
                case 1 -> {
                    int userId = lerInteiro("ID do Usuário: ");
                    int prodId = lerInteiro("ID do Produto: ");
                    int qtd = lerInteiro("Quantidade: ");
                    if (carrinhoController.adicionarItem(userId, prodId, qtd)) {
                        System.out.println(" Item adicionado/atualizado no carrinho persistente!");
                    } else {
                        System.err.println(" Erro ao adicionar item no carrinho.");
                    }
                }
                case 2 -> {
                    int userId = lerInteiro("ID do Usuário: ");
                    List<UsuarioCarrinho> itens = carrinhoController.listarPorUsuario(userId);
                    System.out.println("\n--- CARRINHO DO USUÁRIO #" + userId + " ---");
                    if (itens.isEmpty()) {
                        System.out.println("Carrinho vazio.");
                    } else {
                        for (UsuarioCarrinho it : itens) {
                            String pNome = (it.getProduto() != null) ? it.getProduto().getNome() : ("Prod #" + it.getProdutoId());
                            BigDecimal pPreco = (it.getProduto() != null) ? it.getProduto().getPrecoBase() : BigDecimal.ZERO;
                            BigDecimal subtotal = pPreco.multiply(BigDecimal.valueOf(it.getQuantidade()));
                            System.out.printf("Item #%-3d | Produto: %-22s | Qtd: %-3d | Unit: R$ %-6.2f | Subtotal: R$ %-6.2f%n",
                                    it.getIdUsuarioCarrinho(), pNome, it.getQuantidade(), pPreco, subtotal);
                        }
                        System.out.println("Total Estimado: R$ " + carrinhoController.calcularTotalCarrinho(userId));
                    }
                }
                case 3 -> {
                    int cartId = lerInteiro("ID do Registro no Carrinho: ");
                    int novaQtd = lerInteiro("Nova quantidade: ");
                    if (carrinhoController.atualizarQuantidade(cartId, novaQtd)) {
                        System.out.println(" Quantidade atualizada.");
                    }
                }
                case 4 -> {
                    int cartId = lerInteiro("ID do Registro no Carrinho para remover: ");
                    if (carrinhoController.removerItem(cartId)) {
                        System.out.println(" Item removido do carrinho.");
                    }
                }
                case 5 -> {
                    int userId = lerInteiro("ID do Usuário: ");
                    if (carrinhoController.limparCarrinho(userId)) {
                        System.out.println(" Carrinho esvaziado.");
                    }
                }
                case 0 -> voltar = true;
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    // =========================================================================
    // 6. PERDAS E AUDITORIA
    // =========================================================================
    private static void menuPerdas() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n--- RELATÓRIO E REGISTRO DE PERDAS ---");
            System.out.println("1. Registrar Perda de Lote (Vencimento/Avaria com baixa de estoque)");
            System.out.println("2. Listar Todas as Perdas Registradas");
            System.out.println("3. Listar Perdas por Lote");
            System.out.println("0. Voltar");

            int op = lerInteiro("Opção: ");
            switch (op) {
                case 1 -> {
                    int loteId = lerInteiro("ID do Lote que sofreu perda: ");
                    int qtd = lerInteiro("Quantidade perdida/descartada: ");
                    String motivo = lerString("Motivo (ex: PRODUTO VENCIDO, AVARIA, QUEBRA): ");

                    if (perdaController.registrarPerda(loteId, qtd, motivo)) {
                        System.out.println(" Perda registrada e saldo do lote devidamente ajustado!");
                    } else {
                        System.err.println(" Erro ao registrar perda.");
                    }
                }
                case 2 -> {
                    List<Perda> perdas = perdaController.listarTodos();
                    System.out.println("\n--- HISTÓRICO DE AUDITORIA DE PERDAS ---");
                    for (Perda p : perdas) {
                        String prodDesc = (p.getLote() != null && p.getLote().getProduto() != null)
                                ? p.getLote().getProduto().getNome()
                                : ("Lote #" + p.getLoteId());
                        System.out.printf("Perda #%-3d | Data: %-19s | Lote: %-4d | Item: %-20s | Qtd: %-4d | Motivo: %s%n",
                                p.getIdPerda(), p.getDataRegistro(), p.getLoteId(), prodDesc, p.getQuantidade(), p.getMotivo());
                    }
                }
                case 3 -> {
                    int loteId = lerInteiro("ID do Lote: ");
                    List<Perda> perdas = perdaController.listarPorLote(loteId);
                    System.out.println("\nPerdas registradas no Lote #" + loteId + ":");
                    for (Perda p : perdas) {
                        System.out.printf("ID #%-3d | Qtd: %-4d | Data: %-19s | Motivo: %s%n",
                                p.getIdPerda(), p.getQuantidade(), p.getDataRegistro(), p.getMotivo());
                    }
                }
                case 0 -> voltar = true;
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    // =========================================================================
    // 7. USUÁRIOS
    // =========================================================================
    private static void menuUsuarios() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n--- GESTÃO DE USUÁRIOS ---");
            System.out.println("1. Cadastrar Usuário");
            System.out.println("2. Listar Usuários");
            System.out.println("3. Buscar Usuário por E-mail");
            System.out.println("4. Testar Autenticação (Login)");
            System.out.println("5. Recuperação de Senha (Geração automática de token/nova senha)");
            System.out.println("0. Voltar");

            int op = lerInteiro("Opção: ");
            switch (op) {
                case 1 -> {
                    String nome = lerString("Nome completo: ");
                    String email = lerString("E-mail: ");
                    String senha = lerString("Senha (mínimo 4 caracteres): ");
                    String perfil = lerString("Perfil [CLIENTE / ADMIN / FUNCIONARIO]: ");

                    if (usuarioController.cadastrar(nome, email, senha, perfil)) {
                        System.out.println(" Usuário cadastrado com sucesso!");
                    } else {
                        System.err.println(" Erro ao cadastrar usuário.");
                    }
                }
                case 2 -> {
                    List<Usuario> lista = usuarioController.listarTodos();
                    System.out.println("\n--- USUÁRIOS CADASTRADOS ---");
                    for (Usuario u : lista) {
                        System.out.printf("ID: %-3d | Nome: %-25s | Email: %-25s | Perfil: %s%n",
                                u.getIdUsuario(), u.getNome(), u.getEmail(), u.getPerfil());
                    }
                }
                case 3 -> {
                    String email = lerString("Digite o e-mail: ");
                    Usuario u = usuarioController.buscarPorEmail(email);
                    if (u != null) {
                        System.out.println("ID: " + u.getIdUsuario() + " | Nome: " + u.getNome() + " | Perfil: " + u.getPerfil() + " | Cadastrado em: " + u.getDataCadastro());
                    } else {
                        System.err.println("Usuário não encontrado.");
                    }
                }
                case 4 -> {
                    String email = lerString("E-mail: ");
                    String senha = lerString("Senha: ");
                    Usuario u = usuarioController.autenticar(email, senha);
                    if (u != null) {
                        System.out.println(" Login bem-sucedido! Bem-vinda(o), " + u.getNome() + " (" + u.getPerfil() + ")");
                    } else {
                        System.err.println(" Falha na autenticação: E-mail ou senha incorretos.");
                    }
                }
                case 5 -> {
                    String email = lerString("E-mail para recuperação: ");
                    String novaSenha = usuarioController.recuperarSenha(email);
                    if (novaSenha != null) {
                        System.out.println(" Nova senha gerada com sucesso: " + novaSenha);
                        System.out.println("(Em ambiente real, enviada por e-mail)");
                    }
                }
                case 0 -> voltar = true;
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    // =========================================================================
    // 8. DEMONSTRAÇÃO COMPLETA AUTOMÁTICA
    // =========================================================================
    private static void executarDemonstracaoCompleta() {
        System.out.println("\n===============================================================");
        System.out.println("        INICIANDO DEMONSTRAÇÃO COMPLETA DO FLUXO FEFO          ");
        System.out.println("===============================================================");

        System.out.println("\n1. Consultando catálogo e vitrine de produtos...");
        List<Produto> prods = produtoController.listarVitrine();
        System.out.println("Produtos prontos para venda na vitrine: " + prods.size());

        System.out.println("\n2. Consultando lotes do produto 'Vermelho Royal' (ID 1)...");
        List<Lote> lotes = loteController.listarDisponiveisFEFO(1);
        System.out.println("Lotes encontrados ordenados por validade:");
        for (Lote l : lotes) {
            System.out.println("  -> Lote #" + l.getIdLote() + " com saldo " + l.getQuantAtual() + " un e validade " + l.getDataValidade());
        }

        System.out.println("\n3. Executando simulação de venda de 5 unidades...");
        Pedido p = pedidoController.realizarVendaDireta(2, 1, 5, "PAGO");
        if (p != null) {
            System.out.println(" Venda efetuada com sucesso! Pedido #" + p.getIdPedido());
            System.out.println("Total: R$ " + p.getTotal());
        }

        System.out.println("\n4. Verificando auditoria de perdas registradas...");
        List<Perda> perdas = perdaController.listarTodos();
        System.out.println("Total de registros de perda no histórico: " + perdas.size());

        System.out.println("\n Demonstração finalizada com sucesso!");
    }

    // =========================================================================
    // MÉTODOS AUXILIARES DE ENTRADA
    // =========================================================================
    private static int lerInteiro(String msg) {
        while (true) {
            System.out.print(msg);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.err.println("Entrada inválida. Digite um número inteiro.");
            }
        }
    }

    private static String lerString(String msg) {
        System.out.print(msg);
        return scanner.nextLine();
    }

    private static BigDecimal lerBigDecimal(String msg) {
        while (true) {
            System.out.print(msg);
            String input = scanner.nextLine().trim().replace(",", ".");
            try {
                return new BigDecimal(input);
            } catch (Exception e) {
                System.err.println("Valor decimal inválido. Exemplo: 8.90");
            }
        }
    }

    private static LocalDate lerData(String msg) {
        while (true) {
            System.out.print(msg);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.err.println("Data no formato inválido. Use dd/MM/yyyy (ex: 15/10/2026).");
            }
        }
    }
}
