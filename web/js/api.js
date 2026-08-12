/**
 * DUPLA COR - CLIENTE DE COMUNICAÇÃO COM API REST & PERSISTÊNCIA LOCAL
 * Comunica-se com o backend Java (porta 8080) e mantém sincronização local de fallback.
 */

const ApiClient = {
  baseUrl: '/api',

  // Dados iniciais de fallback caso a API não esteja em execução (compatível com schema.sql)
  mockData: {
    produtos: [
      { idProduto: 1, nome: 'Vermelho Royal', marca: 'Risqué', precoBase: 8.90, status: 'ATIVO', categorias: [1] },
      { idProduto: 2, nome: 'Renda Clássica', marca: 'Risqué', precoBase: 8.50, status: 'ATIVO', categorias: [1] },
      { idProduto: 3, nome: 'Base Bomba Fortalecedora', marca: 'Impala', precoBase: 12.00, status: 'ATIVO', categorias: [3] },
      { idProduto: 4, nome: 'Preto Sépia', marca: 'Colorama', precoBase: 7.90, status: 'ATIVO', categorias: [1] },
      { idProduto: 5, nome: 'Top Coat Brilho Diamante', marca: 'Dailus', precoBase: 14.50, status: 'ATIVO', categorias: [3] },
      { idProduto: 6, nome: 'Lavanda Glam Perolado', marca: 'Dupla Cor', precoBase: 11.90, status: 'ATIVO', categorias: [2] },
      { idProduto: 7, nome: 'Ouro Champagne Glitter', marca: 'Dupla Cor', precoBase: 13.90, status: 'ATIVO', categorias: [4] }
    ],
    categorias: [
      { idCategoria: 1, nome: 'Cremoso', descricao: 'Esmaltes com acabamento cremoso tradicional e alta cobertura' },
      { idCategoria: 2, nome: 'Cintilante', descricao: 'Esmaltes com micropartículas de brilho perolado' },
      { idCategoria: 3, nome: 'Tratamento & Base', descricao: 'Bases fortalecedoras, óleos secantes e finalizadores' },
      { idCategoria: 4, nome: 'Glitter', descricao: 'Esmaltes com partículas reflexivas de glitter intenso' }
    ],
    lotes: [
      { idLote: 1, quantInicial: 50, quantAtual: 48, dataValidade: '2026-10-15', dataEntrada: '2026-01-10', status: 'DISPONIVEL', Produto_idProduto: 1 },
      { idLote: 2, quantInicial: 100, quantAtual: 100, dataValidade: '2027-05-20', dataEntrada: '2026-03-01', status: 'DISPONIVEL', Produto_idProduto: 1 },
      { idLote: 3, quantInicial: 30, quantAtual: 30, dataValidade: '2027-01-10', dataEntrada: '2026-02-15', status: 'DISPONIVEL', Produto_idProduto: 2 },
      { idLote: 4, quantInicial: 20, quantAtual: 0, dataValidade: '2025-12-31', dataEntrada: '2025-01-01', status: 'VENCIDO', Produto_idProduto: 3 },
      { idLote: 5, quantInicial: 40, quantAtual: 40, dataValidade: '2027-08-15', dataEntrada: '2026-04-10', status: 'DISPONIVEL', Produto_idProduto: 4 },
      { idLote: 6, quantInicial: 25, quantAtual: 25, dataValidade: '2027-11-30', dataEntrada: '2026-05-01', status: 'DISPONIVEL', Produto_idProduto: 5 },
      { idLote: 7, quantInicial: 35, quantAtual: 35, dataValidade: '2027-04-20', dataEntrada: '2026-06-01', status: 'DISPONIVEL', Produto_idProduto: 6 },
      { idLote: 8, quantInicial: 20, quantAtual: 20, dataValidade: '2026-12-25', dataEntrada: '2026-06-15', status: 'DISPONIVEL', Produto_idProduto: 7 }
    ],
    perdas: [
      { idPerda: 1, quantidade: 20, dataRegistro: '2026-01-02 08:30:00', motivo: 'PRODUTO VENCIDO', Lote_idLote: 4 }
    ],
    pedidos: [
      {
        idPedido: 1,
        dataVenda: '2026-05-15 14:20:00',
        total: 17.80,
        statusPagamento: 'PAGO',
        Usuario_idUsuario: 2,
        itens: [
          { idItemPedido: 1, quantidade: 2, precoAplicado: 8.90, Lote_idLote: 1, Pedido_idPedido: 1, produtoNome: 'Vermelho Royal' }
        ]
      }
    ],
    carrinho: [
      { idUsuarioCarrinho: 1, dataAdicao: '2026-08-10 10:00:00', quantidade: 1, Usuario_idUsuario: 2, Produto_idProduto: 2 },
      { idUsuarioCarrinho: 2, dataAdicao: '2026-08-10 10:05:00', quantidade: 2, Usuario_idUsuario: 2, Produto_idProduto: 5 }
    ],
    usuarios: [
      { idUsuario: 1, nome: 'Administrador Dupla Cor', email: 'admin@duplacor.com.br', senha: 'admin', perfil: 'ADMIN' },
      { idUsuario: 2, nome: 'Maria Silva', email: 'maria@email.com', senha: '123', perfil: 'CLIENTE' }
    ]
  },

  init() {
    // Carregar dados salvos no localStorage se existirem
    const saved = localStorage.getItem('duplacor_db');
    if (saved) {
      try {
        this.mockData = JSON.parse(saved);
      } catch (e) {
        console.warn('Erro ao carregar dados locais:', e);
      }
    } else {
      this.saveLocal();
    }
  },

  saveLocal() {
    localStorage.setItem('duplacor_db', JSON.stringify(this.mockData));
  },

  async request(endpoint, options = {}) {
    try {
      const response = await fetch(`${this.baseUrl}${endpoint}`, {
        headers: { 'Content-Type': 'application/json', ...options.headers },
        ...options
      });
      if (response.ok) {
        return await response.json();
      }
    } catch (e) {
      // Backend offline: o fallback local assumirá a resposta
    }
    return null;
  },

  // ==========================================
  // PRODUTOS
  // ==========================================
  async getProdutos() {
    const apiRes = await this.request('/produtos');
    if (apiRes) return apiRes;
    return this.mockData.produtos;
  },

  async getVitrine() {
    const apiRes = await this.request('/produtos?vitrine=true');
    if (apiRes) return apiRes;
    // Filtra produtos com pelo menos 1 lote válido com saldo
    return this.mockData.produtos.filter(p => {
      if (p.status !== 'ATIVO') return false;
      const lotesProd = this.mockData.lotes.filter(l => l.Produto_idProduto === p.idProduto);
      const fefoValidos = FefoEngine.ordenarLotesFEFO(lotesProd);
      return fefoValidos.length > 0;
    });
  },

  async getProdutoById(id) {
    const apiRes = await this.request(`/produtos/${id}`);
    if (apiRes) return apiRes;
    return this.mockData.produtos.find(p => p.idProduto === Number(id));
  },

  async salvarProduto(produto) {
    if (produto.idProduto) {
      const index = this.mockData.produtos.findIndex(p => p.idProduto === produto.idProduto);
      if (index !== -1) {
        this.mockData.produtos[index] = { ...this.mockData.produtos[index], ...produto };
        this.saveLocal();
        return this.mockData.produtos[index];
      }
    } else {
      const novoId = (Math.max(...this.mockData.produtos.map(p => p.idProduto), 0) || 0) + 1;
      const novo = { ...produto, idProduto: novoId, status: produto.status || 'ATIVO' };
      this.mockData.produtos.push(novo);
      this.saveLocal();
      return novo;
    }
  },

  async excluirProduto(id) {
    this.mockData.produtos = this.mockData.produtos.filter(p => p.idProduto !== Number(id));
    this.saveLocal();
    return true;
  },

  // ==========================================
  // CATEGORIAS
  // ==========================================
  async getCategorias() {
    const apiRes = await this.request('/categorias');
    if (apiRes) return apiRes;
    return this.mockData.categorias;
  },

  async salvarCategoria(cat) {
    if (cat.idCategoria) {
      const index = this.mockData.categorias.findIndex(c => c.idCategoria === cat.idCategoria);
      if (index !== -1) {
        this.mockData.categorias[index] = { ...this.mockData.categorias[index], ...cat };
        this.saveLocal();
        return this.mockData.categorias[index];
      }
    } else {
      const novoId = (Math.max(...this.mockData.categorias.map(c => c.idCategoria), 0) || 0) + 1;
      const novo = { ...cat, idCategoria: novoId };
      this.mockData.categorias.push(novo);
      this.saveLocal();
      return novo;
    }
  },

  // ==========================================
  // LOTES & ALGORITMO FEFO
  // ==========================================
  async getLotes() {
    const apiRes = await this.request('/lotes');
    if (apiRes) return apiRes;
    return this.mockData.lotes;
  },

  async getLotesPorProduto(produtoId) {
    const lotes = await this.getLotes();
    return lotes.filter(l => l.Produto_idProduto === Number(produtoId));
  },

  async salvarLote(lote) {
    if (lote.idLote) {
      const index = this.mockData.lotes.findIndex(l => l.idLote === lote.idLote);
      if (index !== -1) {
        this.mockData.lotes[index] = { ...this.mockData.lotes[index], ...lote };
        this.saveLocal();
        return this.mockData.lotes[index];
      }
    } else {
      const novoId = (Math.max(...this.mockData.lotes.map(l => l.idLote), 0) || 0) + 1;
      const novo = {
        ...lote,
        idLote: novoId,
        quantAtual: lote.quantInicial,
        status: FefoEngine.isLoteVencido(lote.dataValidade) ? 'VENCIDO' : 'DISPONIVEL'
      };
      this.mockData.lotes.push(novo);
      this.saveLocal();
      return novo;
    }
  },

  async monitorarVencidos() {
    let count = 0;
    this.mockData.lotes.forEach(l => {
      if (FefoEngine.isLoteVencido(l.dataValidade) && l.status !== 'VENCIDO') {
        l.status = 'VENCIDO';
        count++;
      }
    });
    this.saveLocal();
    return count;
  },

  // ==========================================
  // CARRINHO PERSISTENTE
  // ==========================================
  async getCarrinho(usuarioId) {
    const apiRes = await this.request(`/carrinho?usuarioId=${usuarioId}`);
    if (apiRes) return apiRes;

    const itens = this.mockData.carrinho.filter(c => c.Usuario_idUsuario === Number(usuarioId));
    return itens.map(item => {
      const produto = this.mockData.produtos.find(p => p.idProduto === item.Produto_idProduto);
      return { ...item, produto };
    });
  },

  async adicionarAoCarrinho(usuarioId, produtoId, quantidade = 1) {
    const existente = this.mockData.carrinho.find(
      c => c.Usuario_idUsuario === Number(usuarioId) && c.Produto_idProduto === Number(produtoId)
    );
    if (existente) {
      existente.quantidade += quantidade;
    } else {
      const novoId = (Math.max(...this.mockData.carrinho.map(c => c.idUsuarioCarrinho), 0) || 0) + 1;
      this.mockData.carrinho.push({
        idUsuarioCarrinho: novoId,
        dataAdicao: new Date().toISOString(),
        quantidade,
        Usuario_idUsuario: Number(usuarioId),
        Produto_idProduto: Number(produtoId)
      });
    }
    this.saveLocal();
    return true;
  },

  async atualizarQtdCarrinho(idItem, novaQtd) {
    if (novaQtd <= 0) {
      return this.removerDoCarrinho(idItem);
    }
    const item = this.mockData.carrinho.find(c => c.idUsuarioCarrinho === Number(idItem));
    if (item) {
      item.quantidade = novaQtd;
      this.saveLocal();
      return true;
    }
    return false;
  },

  async removerDoCarrinho(idItem) {
    this.mockData.carrinho = this.mockData.carrinho.filter(c => c.idUsuarioCarrinho !== Number(idItem));
    this.saveLocal();
    return true;
  },

  async limparCarrinho(usuarioId) {
    this.mockData.carrinho = this.mockData.carrinho.filter(c => c.Usuario_idUsuario !== Number(usuarioId));
    this.saveLocal();
    return true;
  },

  // ==========================================
  // PEDIDOS & CHECKOUT COM FEFO
  // ==========================================
  async getPedidos(usuarioId = null) {
    const apiRes = await this.request(usuarioId ? `/pedidos?usuarioId=${usuarioId}` : '/pedidos');
    if (apiRes) return apiRes;
    if (usuarioId) {
      return this.mockData.pedidos.filter(p => p.Usuario_idUsuario === Number(usuarioId));
    }
    return this.mockData.pedidos;
  },

  async finalizarPedido(usuarioId, statusPagamento = 'PAGO') {
    const itensCarrinho = await this.getCarrinho(usuarioId);
    if (itensCarrinho.length === 0) return null;

    let total = 0;
    const itensPedido = [];

    for (const item of itensCarrinho) {
      const prod = item.produto;
      if (!prod) continue;

      const lotesProd = this.mockData.lotes.filter(l => l.Produto_idProduto === prod.idProduto);
      const sim = FefoEngine.simularAlocacao(lotesProd, item.quantidade);

      for (const aloc of sim.alocacoes) {
        // Atualiza o saldo do lote real
        const loteReal = this.mockData.lotes.find(l => l.idLote === aloc.loteId);
        if (loteReal) {
          loteReal.quantAtual = aloc.saldoRestante;
          if (loteReal.quantAtual === 0) {
            loteReal.status = 'ESGOTADO';
          }
        }

        itensPedido.push({
          idItemPedido: itensPedido.length + 1,
          quantidade: aloc.quantidade,
          precoAplicado: prod.precoBase,
          Lote_idLote: aloc.loteId,
          produtoNome: prod.nome
        });

        total += prod.precoBase * aloc.quantidade;
      }
    }

    const novoIdPedido = (Math.max(...this.mockData.pedidos.map(p => p.idPedido), 0) || 0) + 1;
    const novoPedido = {
      idPedido: novoIdPedido,
      dataVenda: new Date().toISOString().replace('T', ' ').substring(0, 19),
      total: parseFloat(total.toFixed(2)),
      statusPagamento,
      Usuario_idUsuario: Number(usuarioId),
      itens: itensPedido
    };

    this.mockData.pedidos.unshift(novoPedido);
    await this.limparCarrinho(usuarioId);
    this.saveLocal();
    return novoPedido;
  },

  // ==========================================
  // PERDAS & AUDITORIA
  // ==========================================
  async getPerdas() {
    const apiRes = await this.request('/perdas');
    if (apiRes) return apiRes;
    return this.mockData.perdas;
  },

  async registrarPerda(loteId, quantidade, motivo) {
    const lote = this.mockData.lotes.find(l => l.idLote === Number(loteId));
    if (!lote || lote.quantAtual < quantidade) return false;

    lote.quantAtual -= quantidade;
    if (lote.quantAtual === 0) {
      lote.status = 'VENCIDO';
    }

    const novoId = (Math.max(...this.mockData.perdas.map(p => p.idPerda), 0) || 0) + 1;
    const novaPerda = {
      idPerda: novoId,
      quantidade,
      dataRegistro: new Date().toISOString().replace('T', ' ').substring(0, 19),
      motivo: motivo.toUpperCase(),
      Lote_idLote: Number(loteId)
    };

    this.mockData.perdas.unshift(novaPerda);
    this.saveLocal();
    return novaPerda;
  },

  // ==========================================
  // AUTENTICAÇÃO
  // ==========================================
  async login(email, senha) {
    const user = this.mockData.usuarios.find(
      u => u.email.toLowerCase() === email.trim().toLowerCase() && u.senha === senha.trim()
    );
    if (user) {
      localStorage.setItem('duplacor_user', JSON.stringify(user));
      return user;
    }
    return null;
  },

  getUsuarioAtual() {
    const userStr = localStorage.getItem('duplacor_user');
    if (userStr) {
      try { return JSON.parse(userStr); } catch (e) {}
    }
    // Default cliente de demonstração
    return this.mockData.usuarios[1];
  },

  logout() {
    localStorage.removeItem('duplacor_user');
  }
};

ApiClient.init();
window.ApiClient = ApiClient;
