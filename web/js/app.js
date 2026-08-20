/**
 * DUPLA COR - APLICAÇÃO WEB PRINCIPAL (SPA & CONTROLADORES DE TELA)
 */

document.addEventListener('DOMContentLoaded', () => {
  App.init();
});

const App = {
  usuarioAtual: null,
  carrinho: [],
  categoriaFiltro: null,
  termoBusca: '',

  async init() {
    this.usuarioAtual = ApiClient.getUsuarioAtual();
    this.setupRouter();
    this.setupEventListeners();
    this.renderNavUser();
    await this.atualizarContadorCarrinho();
    this.navegarPara(window.location.hash || '#home');
  },

  // =========================================================================
  // ROTEADOR SPA (Single Page Application)
  // =========================================================================
  setupRouter() {
    window.addEventListener('hashchange', () => {
      this.navegarPara(window.location.hash || '#home');
    });
  },

  navegarPara(hash) {
    const rota = hash.split('?')[0] || '#home';
    
    // Esconde todas as páginas
    document.querySelectorAll('.page-view').forEach(page => {
      page.classList.remove('active');
    });

    // Atualiza links da navbar
    document.querySelectorAll('.nav-link').forEach(link => {
      link.classList.toggle('active', link.getAttribute('href') === rota);
    });

    // Roteamento
    switch (rota) {
      case '#home':
        this.renderHome();
        this.mostrarPagina('page-home');
        break;
      case '#produtos':
        this.renderCatalogo();
        this.mostrarPagina('page-produtos');
        break;
      case '#carrinho':
        if (this.usuarioAtual && this.usuarioAtual.perfil === 'ADMIN') {
          this.mostrarToast('O carrinho de compras é exclusivo para clientes.', 'warning');
          window.location.hash = '#home';
          break;
        }
        this.renderCarrinho();
        this.mostrarPagina('page-carrinho');
        break;
      case '#checkout':
        if (this.usuarioAtual && this.usuarioAtual.perfil === 'ADMIN') {
          this.mostrarToast('O checkout é exclusivo para clientes.', 'warning');
          window.location.hash = '#home';
          break;
        }
        if (!this.usuarioAtual) {
          this.mostrarToast('Por favor, faça login ou crie um perfil para finalizar a compra.', 'warning');
          window.location.hash = '#login';
          break;
        }
        this.renderCheckout();
        this.mostrarPagina('page-checkout');
        break;
      case '#pedidos':
        this.renderPedidos();
        this.mostrarPagina('page-pedidos');
        break;
      case '#login':
        this.alternarModoAuth('login');
        this.mostrarPagina('page-login');
        break;
      case '#admin':
      case '#admin/dashboard':
        if (!this.exigirAdmin()) break;
        this.renderAdminDashboard();
        this.mostrarPagina('page-admin');
        this.setAdminTab('dashboard');
        break;
      case '#admin/lotes':
        if (!this.exigirAdmin()) break;
        this.renderAdminLotes();
        this.mostrarPagina('page-admin');
        this.setAdminTab('lotes');
        break;
      case '#admin/produtos':
        if (!this.exigirAdmin()) break;
        this.renderAdminProdutos();
        this.mostrarPagina('page-admin');
        this.setAdminTab('produtos');
        break;
      case '#admin/categorias':
        if (!this.exigirAdmin()) break;
        this.renderAdminCategorias();
        this.mostrarPagina('page-admin');
        this.setAdminTab('categorias');
        break;
      case '#admin/perdas':
        if (!this.exigirAdmin()) break;
        this.renderAdminPerdas();
        this.mostrarPagina('page-admin');
        this.setAdminTab('perdas');
        break;
      case '#admin/pedidos':
        if (!this.exigirAdmin()) break;
        this.renderAdminPedidos();
        this.mostrarPagina('page-admin');
        this.setAdminTab('pedidos');
        break;
      default:
        this.renderHome();
        this.mostrarPagina('page-home');
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });
  },

  mostrarPagina(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('active');
  },

  setupEventListeners() {
    // Busca global no input
    const searchInput = document.getElementById('global-search');
    if (searchInput) {
      searchInput.addEventListener('input', (e) => {
        this.termoBusca = e.target.value.toLowerCase().trim();
        if (window.location.hash !== '#produtos') {
          window.location.hash = '#produtos';
        } else {
          this.renderCatalogo();
        }
      });
    }

    // Abas do Painel Admin
    document.querySelectorAll('.admin-tab').forEach(tab => {
      tab.addEventListener('click', () => {
        const target = tab.dataset.tab;
        window.location.hash = `#admin/${target}`;
      });
    });
  },

  // =========================================================================
  // AUTENTICAÇÃO & PERFIL DO USUÁRIO
  // =========================================================================
  renderNavUser() {
    const container = document.getElementById('nav-user-area');
    const adminBtn = document.getElementById('nav-admin-btn');
    const heroAdminBtn = document.getElementById('hero-admin-btn');
    if (!container) return;

    const cartBtn = document.getElementById('nav-cart-btn');

    if (this.usuarioAtual) {
      const primeiroNome = (this.usuarioAtual.nome || 'Cliente').split(' ')[0];
      container.innerHTML = `
        <div style="display: flex; align-items: center; gap: 10px;">
          <span style="font-size: 0.85rem; font-weight: 700; color: var(--primary-dark);">Olá, ${primeiroNome}</span>
          <button class="btn-icon" title="Sair da conta" onclick="App.logout()">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
          </button>
        </div>
      `;
      if (adminBtn) adminBtn.style.display = (this.usuarioAtual.perfil === 'ADMIN') ? 'inline-flex' : 'none';
      if (heroAdminBtn) heroAdminBtn.style.display = (this.usuarioAtual.perfil === 'ADMIN') ? 'inline-flex' : 'none';
      if (cartBtn) cartBtn.style.display = (this.usuarioAtual.perfil === 'ADMIN') ? 'none' : 'inline-flex';
    } else {
      container.innerHTML = `
        <a href="#login" class="btn-secondary" style="padding: 8px 18px; font-size: 0.85rem;">Entrar</a>
      `;
      if (adminBtn) adminBtn.style.display = 'none';
      if (heroAdminBtn) heroAdminBtn.style.display = 'none';
      if (cartBtn) cartBtn.style.display = 'inline-flex';
    }
  },

  exigirAdmin() {
    if (this.usuarioAtual && this.usuarioAtual.perfil === 'ADMIN') {
      return true;
    }
    this.mostrarToast('Acesso restrito. Faça login com uma conta de administrador.', 'error');
    window.location.hash = '#login';
    return false;
  },

  alternarModoAuth(modo) {
    const formLogin = document.getElementById('form-login');
    const formCadastro = document.getElementById('form-cadastro');
    const formRecuperar = document.getElementById('form-recuperar-senha');
    const tabLogin = document.getElementById('auth-tab-login');
    const tabCadastro = document.getElementById('auth-tab-cadastro');
    if (!formLogin || !formCadastro) return;

    formLogin.style.display = modo === 'login' ? 'block' : 'none';
    formCadastro.style.display = modo === 'cadastro' ? 'block' : 'none';
    if (formRecuperar) formRecuperar.style.display = modo === 'recuperar' ? 'block' : 'none';

    const ehLogin = modo === 'login';
    const ehCadastro = modo === 'cadastro';
    if (tabLogin && tabCadastro) {
      tabLogin.style.background = ehLogin ? '#ffffff' : 'transparent';
      tabLogin.style.boxShadow = ehLogin ? 'var(--shadow-sm)' : 'none';
      tabLogin.style.color = ehLogin ? 'var(--text-main)' : 'var(--text-muted)';
      tabCadastro.style.background = ehCadastro ? '#ffffff' : 'transparent';
      tabCadastro.style.boxShadow = ehCadastro ? 'var(--shadow-sm)' : 'none';
      tabCadastro.style.color = ehCadastro ? 'var(--text-main)' : 'var(--text-muted)';
    }

    const erroLogin = document.getElementById('login-erro');
    const erroCadastro = document.getElementById('cadastro-erro');
    if (erroLogin) erroLogin.style.display = 'none';
    if (erroCadastro) erroCadastro.style.display = 'none';
  },

  abrirRecuperarSenha() {
    const erroBox = document.getElementById('recuperar-erro');
    const sucessoBox = document.getElementById('recuperar-sucesso');
    if (erroBox) erroBox.style.display = 'none';
    if (sucessoBox) sucessoBox.style.display = 'none';
    this.alternarModoAuth('recuperar');
  },

  async submitRecuperarSenha(e) {
    e.preventDefault();
    const email = document.getElementById('recuperar-email').value.trim();
    const erroBox = document.getElementById('recuperar-erro');
    const sucessoBox = document.getElementById('recuperar-sucesso');
    erroBox.style.display = 'none';
    sucessoBox.style.display = 'none';

    const resultado = await ApiClient.recuperarSenha(email);
    if (resultado.sucesso) {
      sucessoBox.textContent = `Nova senha gerada: ${resultado.novaSenha} — use-a para entrar (em um ambiente real, isso seria enviado por e-mail).`;
      sucessoBox.style.display = 'block';
    } else {
      erroBox.textContent = resultado.mensagem || 'Não foi possível recuperar a senha.';
      erroBox.style.display = 'block';
    }
  },

  async submitLogin(e) {
    e.preventDefault();
    const email = document.getElementById('login-email').value.trim();
    const senha = document.getElementById('login-senha').value;
    const erroBox = document.getElementById('login-erro');

    const usuario = await ApiClient.login(email, senha);
    if (usuario) {
      this.usuarioAtual = usuario;
      this.renderNavUser();
      await this.atualizarContadorCarrinho();
      this.mostrarToast(`Bem-vinda de volta, ${usuario.nome.split(' ')[0]}!`, 'success');
      window.location.hash = usuario.perfil === 'ADMIN' ? '#admin' : '#home';
    } else if (erroBox) {
      erroBox.textContent = 'E-mail ou senha inválidos. Tente novamente.';
      erroBox.style.display = 'block';
    }
  },

  async submitCadastro(e) {
    e.preventDefault();
    const nome = document.getElementById('cadastro-nome').value.trim();
    const email = document.getElementById('cadastro-email').value.trim();
    const senha = document.getElementById('cadastro-senha').value;
    const erroBox = document.getElementById('cadastro-erro');

    const resultado = await ApiClient.cadastro(nome, email, senha);
    if (resultado.sucesso) {
      this.usuarioAtual = resultado.usuario;
      this.renderNavUser();
      await this.atualizarContadorCarrinho();
      this.mostrarToast(`Conta criada com sucesso! Bem-vinda, ${nome.split(' ')[0]}!`, 'success');
      window.location.hash = '#home';
    } else if (erroBox) {
      erroBox.textContent = resultado.mensagem || 'Não foi possível concluir o cadastro.';
      erroBox.style.display = 'block';
    }
  },

  logout() {
    ApiClient.logout();
    this.usuarioAtual = null;
    this.renderNavUser();
    this.atualizarContadorCarrinho();
    this.mostrarToast('Você saiu da sua conta.', 'info');
    window.location.hash = '#home';
  },

  // Usado pelos links do rodapé para filtrar o catálogo por categoria pelo nome
  async irParaCategoria(nomeCategoria) {
    if (!nomeCategoria) {
      this.categoriaFiltro = null;
    } else {
      const categorias = await ApiClient.getCategorias();
      const cat = categorias.find(c => c.nome.toLowerCase() === nomeCategoria.toLowerCase());
      this.categoriaFiltro = cat ? cat.idCategoria : null;
    }
    if (window.location.hash === '#produtos') {
      this.renderCatalogo();
    } else {
      window.location.hash = '#produtos';
    }
  },

  // Modal informativo simples para os links institucionais do rodapé
  mostrarInfoInstitucional(chave) {
    const conteudos = {
      retirada: {
        titulo: '📍 Retirada no Local',
        texto: 'Seu pedido fica pronto para retirada na nossa loja física em até 24h após a confirmação do pagamento. Você recebe uma notificação assim que estiver disponível — sem taxas de entrega e sem espera.'
      },
      fefo: {
        titulo: '🛡️ Garantia FEFO',
        texto: 'Utilizamos o método FEFO (First Expire, First Out): a cada compra, o sistema aloca automaticamente os lotes com validade mais próxima primeiro. Isso garante que nenhum produto vencido chegue até você e que o estoque seja sempre girado da forma mais eficiente.'
      },
      auditoria: {
        titulo: '🔍 Auditoria de Validade',
        texto: 'Todo lote cadastrado é monitorado continuamente pelo nosso sistema. Produtos que atingem a data de validade são automaticamente bloqueados para venda e registrados como perda, com rastreabilidade completa de quantidade, motivo e data — disponível no Painel Administrativo.'
      }
    };
    const dados = conteudos[chave];
    if (!dados) return;
    document.getElementById('info-institucional-titulo').textContent = dados.titulo;
    document.getElementById('info-institucional-texto').textContent = dados.texto;
    document.getElementById('modal-info-institucional').classList.add('active');
  },

  // =========================================================================
  // 1. TELA: HOME (Vitrine Glamour + Destaques FEFO)
  // =========================================================================
  async renderHome() {
    await this.renderCategoriasHome();
    const vitrine = await ApiClient.getVitrine();
    const container = document.getElementById('home-vitrine-grid');
    if (container) {
      if (vitrine.length === 0) {
        container.innerHTML = `<div class="empty-msg">Nenhum esmalte com lote disponível no momento.</div>`;
      } else {
        container.innerHTML = vitrine.slice(0, 4).map(prod => this.criarCardProdutoHTML(prod)).join('');
      }
    }
    await this.renderRecomendacoes();
  },

  // Motor de Recomendação: analisa as categorias já compradas pela usuária e sugere
  // produtos ativos das mesmas categorias que ela ainda não tem. Sem histórico
  // suficiente (ou visitante não logado), cai no fallback de produtos de Tratamento & Base.
  async renderRecomendacoes() {
    const container = document.getElementById('home-recomendacoes-grid');
    const tituloEl = document.getElementById('home-recomendacoes-titulo');
    const subEl = document.getElementById('home-recomendacoes-sub');
    if (!container) return;

    const produtos = await ApiClient.getProdutos();
    let recomendados = [];
    let personalizado = false;

    if (this.usuarioAtual) {
      const pedidos = await ApiClient.getPedidos(this.usuarioAtual.idUsuario);
      if (pedidos.length > 0) {
        const nomesComprados = new Set();
        pedidos.forEach(p => (p.itens || []).forEach(it => nomesComprados.add(it.produtoNome)));

        const categoriasCompradas = new Set();
        produtos.forEach(p => {
          if (nomesComprados.has(p.nome)) {
            (p.categorias || []).forEach(catId => categoriasCompradas.add(catId));
          }
        });

        recomendados = produtos.filter(p =>
          p.status === 'ATIVO' &&
          !nomesComprados.has(p.nome) &&
          p.categorias && p.categorias.some(c => categoriasCompradas.has(c))
        );
        personalizado = recomendados.length > 0;
      }
    }

    if (recomendados.length === 0) {
      const categorias = await ApiClient.getCategorias();
      const catTratamento = categorias.find(c => c.nome.toLowerCase().includes('tratamento'));
      recomendados = produtos.filter(p =>
        p.status === 'ATIVO' && catTratamento && p.categorias && p.categorias.includes(catTratamento.idCategoria)
      );
    }

    if (tituloEl && subEl) {
      if (personalizado) {
        tituloEl.textContent = `Selecionado para você, ${this.usuarioAtual.nome.split(' ')[0]}`;
        subEl.textContent = 'Combinações baseadas no que você já comprou';
      } else if (this.usuarioAtual) {
        tituloEl.textContent = 'O Segredo da Esmaltação Duradoura';
        subEl.textContent = 'Continue comprando para receber recomendações cada vez mais personalizadas';
      } else {
        tituloEl.textContent = 'O Segredo da Esmaltação Duradoura';
        subEl.textContent = 'Faça login para ver sugestões personalizadas com base no seu histórico de compras';
      }
    }

    container.innerHTML = recomendados.length > 0
      ? recomendados.slice(0, 4).map(prod => this.criarCardProdutoHTML(prod)).join('')
      : `<p style="color: var(--text-muted); grid-column: 1/-1;">Nenhuma sugestão disponível no momento.</p>`;
  },

  async renderCategoriasHome() {
    const container = document.getElementById('home-category-pills');
    if (!container) return;

    const cats = await ApiClient.getCategorias();
    let html = `
      <button class="cat-pill ${!this.categoriaFiltro ? 'active' : ''}" onclick="App.filtrarPorCategoria(null)">
        ✨ Todos os Esmaltes
      </button>
    `;

    cats.forEach(c => {
      const active = this.categoriaFiltro === c.idCategoria ? 'active' : '';
      html += `
        <button class="cat-pill ${active}" onclick="App.filtrarPorCategoria(${c.idCategoria})">
          💅 ${c.nome}
        </button>
      `;
    });

    container.innerHTML = html;
  },

  filtrarPorCategoria(catId) {
    this.categoriaFiltro = catId;
    window.location.hash = '#produtos';
  },

  // =========================================================================
  // 2. TELA: CATÁLOGO DE PRODUTOS
  // =========================================================================
  async renderCatalogo() {
    const container = document.getElementById('catalog-products-grid');
    if (!container) return;

    await this.renderCategoriasCatalog();
    let produtos = await ApiClient.getProdutos();

    // Filtros
    if (this.categoriaFiltro) {
      produtos = produtos.filter(p => p.categorias && p.categorias.includes(this.categoriaFiltro));
    }
    if (this.termoBusca) {
      produtos = produtos.filter(p => 
        p.nome.toLowerCase().includes(this.termoBusca) || 
        (p.marca && p.marca.toLowerCase().includes(this.termoBusca))
      );
    }

    if (produtos.length === 0) {
      container.innerHTML = `
        <div style="grid-column: 1/-1; text-align: center; padding: 48px; background: #fff; border-radius: var(--radius-md);">
          <h3 class="font-serif" style="font-size: 1.8rem; color: var(--primary-dark);">Nenhum esmalte encontrado</h3>
          <p style="color: var(--text-muted);">Tente alterar o filtro ou o termo de busca.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = produtos.map(prod => this.criarCardProdutoHTML(prod)).join('');
  },

  async renderCategoriasCatalog() {
    const container = document.getElementById('catalog-category-pills');
    if (!container) return;

    const cats = await ApiClient.getCategorias();
    let html = `
      <button class="cat-pill ${!this.categoriaFiltro ? 'active' : ''}" onclick="App.setCatalogCat(null)">
        Todos
      </button>
    `;
    cats.forEach(c => {
      const active = this.categoriaFiltro === c.idCategoria ? 'active' : '';
      html += `
        <button class="cat-pill ${active}" onclick="App.setCatalogCat(${c.idCategoria})">
          ${c.nome}
        </button>
      `;
    });
    container.innerHTML = html;
  },

  setCatalogCat(catId) {
    this.categoriaFiltro = catId;
    this.renderCatalogo();
  },

  criarCardProdutoHTML(prod) {
    const svg = FefoEngine.gerarFrascoSVG(prod.nome);
    return `
      <div class="product-card">
        <div class="product-thumb" onclick="App.abrirModalProduto(${prod.idProduto})" style="cursor: pointer;">
          ${svg}
          <div class="fefo-badge-chip">
            <span>🛡️</span> Lote FEFO Ativo
          </div>
        </div>
        <div class="product-brand">${prod.marca || 'Dupla Cor'}</div>
        <h4 class="product-name" onclick="App.abrirModalProduto(${prod.idProduto})" style="cursor: pointer;">${prod.nome}</h4>
        <div class="product-price-row">
          <div class="product-price">R$ ${prod.precoBase.toFixed(2).replace('.', ',')}</div>
          ${(this.usuarioAtual && this.usuarioAtual.perfil === 'ADMIN') ? '' : `
          <button class="btn-add-cart" title="Adicionar ao Carrinho" onclick="App.adicionarAoCarrinho(${prod.idProduto})">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path><line x1="3" y1="6" x2="21" y2="6"></line><path d="M16 10a4 4 0 0 1-8 0"></path></svg>
          </button>
          `}
        </div>
      </div>
    `;
  },

  // =========================================================================
  // 3. MODAL DE DETALHES DO PRODUTO & RASTREABILIDADE FEFO
  // =========================================================================
  async abrirModalProduto(produtoId) {
    const prod = await ApiClient.getProdutoById(produtoId);
    if (!prod) return;

    const lotes = await ApiClient.getLotesPorProduto(produtoId);
    const lotesFEFO = FefoEngine.ordenarLotesFEFO(lotes);

    const modal = document.getElementById('modal-produto');
    const content = document.getElementById('modal-produto-content');
    if (!modal || !content) return;

    let lotesHtml = '';
    if (lotesFEFO.length === 0) {
      lotesHtml = `<p style="color: var(--status-danger); font-weight: 600;">Sem lotes disponíveis para envio imediato.</p>`;
    } else {
      lotesHtml = `
        <div style="background: var(--bg-alt); padding: 14px; border-radius: var(--radius-sm); margin: 16px 0;">
          <div style="font-weight: 700; font-size: 0.85rem; color: var(--primary-dark); margin-bottom: 8px;">
            ⚡ FILA DE EXPEDIÇÃO (Algoritmo FEFO):
          </div>
          <table style="width: 100%; font-size: 0.8rem; border-collapse: collapse;">
            <thead>
              <tr style="text-align: left; color: var(--text-muted);">
                <th>Ordem</th>
                <th>Lote</th>
                <th>Validade</th>
                <th>Dias Restantes</th>
                <th>Saldo</th>
              </tr>
            </thead>
            <tbody>
              ${lotesFEFO.map((l, idx) => {
                const dias = FefoEngine.diasAteVencimento(l.dataValidade);
                return `
                  <tr style="border-top: 1px solid rgba(0,0,0,0.05);">
                    <td style="padding: 6px 0;"><strong>${idx + 1}º a sair</strong></td>
                    <td>#${l.idLote}</td>
                    <td>${FefoEngine.formatarData(l.dataValidade)}</td>
                    <td><span class="badge-status badge-disponivel">${dias} dias</span></td>
                    <td><strong>${l.quantAtual} un</strong></td>
                  </tr>
                `;
              }).join('')}
            </tbody>
          </table>
        </div>
      `;
    }

    content.innerHTML = `
      <div style="display: grid; grid-template-columns: 180px 1fr; gap: 24px; align-items: center;">
        <div style="background: var(--bg-alt); border-radius: var(--radius-md); padding: 20px; display: flex; justify-content: center;">
          ${FefoEngine.gerarFrascoSVG(prod.nome)}
        </div>
        <div>
          <div class="product-brand">${prod.marca || 'Dupla Cor'}</div>
          <h2 class="font-serif" style="font-size: 2rem; color: var(--primary-dark);">${prod.nome}</h2>
          <div class="product-price" style="margin: 8px 0;">R$ ${prod.precoBase.toFixed(2).replace('.', ',')}</div>
          <p style="font-size: 0.9rem; color: var(--text-muted);">
            Fórmula de alta cobertura, brilho espelhado e secagem rápida. Estoque controlado via lotes com garantia de frescor.
          </p>
        </div>
      </div>
      ${lotesHtml}
      <div style="display: flex; gap: 12px; margin-top: 20px;">
        ${(this.usuarioAtual && this.usuarioAtual.perfil === 'ADMIN') ? '' : `
        <button class="btn-primary" style="flex: 1;" onclick="App.adicionarAoCarrinho(${prod.idProduto}); App.fecharModais();">
          Adicionar ao Carrinho
        </button>
        `}
        <button class="btn-secondary" style="flex: ${(this.usuarioAtual && this.usuarioAtual.perfil === 'ADMIN') ? '1' : 'none'};" onclick="App.fecharModais()">Fechar</button>
      </div>
    `;

    modal.classList.add('active');
  },

  fecharModais() {
    document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
  },

  // =========================================================================
  // 4. CARRINHO PERSISTENTE
  // =========================================================================
  async adicionarAoCarrinho(produtoId) {
    const user = this.usuarioAtual;
    if (!user) {
      this.fecharModais();
      this.mostrarToast('Por favor, faça login ou crie um perfil para adicionar produtos ao carrinho.', 'warning');
      window.location.hash = '#login';
      this.navegarPara('#login');
      return;
    }
    if (user.perfil === 'ADMIN') {
      this.mostrarToast('Administradores não podem adicionar produtos ao carrinho.', 'error');
      return;
    }

    await ApiClient.adicionarAoCarrinho(user.idUsuario, produtoId, 1);
    await this.atualizarContadorCarrinho();
    this.mostrarToast('Esmalte adicionado ao carrinho com sucesso!', 'success');
  },

  async atualizarContadorCarrinho() {
    const user = this.usuarioAtual || { idUsuario: 2 };
    const itens = await ApiClient.getCarrinho(user.idUsuario);
    const totalQtd = itens.reduce((acc, it) => acc + it.quantidade, 0);
    document.querySelectorAll('.cart-badge-count').forEach(el => {
      el.textContent = totalQtd;
    });
  },

  async renderCarrinho() {
    const user = this.usuarioAtual;
    if (!user) return;

    const itens = await ApiClient.getCarrinho(user.idUsuario);
    const container = document.getElementById('cart-items-container');
    const summaryContainer = document.getElementById('cart-summary-box');
    if (!container || !summaryContainer) return;

    if (itens.length === 0) {
      container.innerHTML = `
        <div style="text-align: center; padding: 48px 0;">
          <div style="font-size: 3rem; margin-bottom: 12px;">🛍️</div>
          <h3 class="font-serif" style="font-size: 1.8rem; color: var(--primary-dark);">Seu carrinho está vazio</h3>
          <p style="color: var(--text-muted); margin-bottom: 20px;">Explore nosso catálogo e descubra cores exclusivas.</p>
          <a href="#produtos" class="btn-primary">Ver Catálogo de Esmaltes</a>
        </div>
      `;
      summaryContainer.style.display = 'none';
      return;
    }

    summaryContainer.style.display = 'block';
    let total = 0;

    container.innerHTML = itens.map(item => {
      const prod = item.produto || { nome: 'Esmalte', precoBase: 0 };
      const subtotal = prod.precoBase * item.quantidade;
      total += subtotal;

      return `
        <div class="cart-item-row">
          <div class="cart-item-info">
            <div class="cart-item-thumb">
              ${FefoEngine.gerarFrascoSVG(prod.nome)}
            </div>
            <div>
              <h4 style="font-weight: 700;">${prod.nome}</h4>
              <div style="font-size: 0.82rem; color: var(--text-muted);">${prod.marca || 'Dupla Cor'}</div>
              <div style="color: var(--primary-dark); font-weight: 800; margin-top: 4px;">
                R$ ${prod.precoBase.toFixed(2).replace('.', ',')}
              </div>
            </div>
          </div>
          <div class="cart-item-qty">
            <button class="qty-btn" onclick="App.alterarQtdCarrinho(${item.idUsuarioCarrinho}, ${item.quantidade - 1})">-</button>
            <span style="font-weight: 700; font-size: 0.9rem; min-width: 20px; text-align: center;">${item.quantidade}</span>
            <button class="qty-btn" onclick="App.alterarQtdCarrinho(${item.idUsuarioCarrinho}, ${item.quantidade + 1})">+</button>
          </div>
          <div style="font-weight: 800; font-size: 1.1rem; color: var(--primary-dark); min-width: 80px; text-align: right;">
            R$ ${subtotal.toFixed(2).replace('.', ',')}
          </div>
          <button class="btn-icon" style="width: 32px; height: 32px; border-color: transparent;" onclick="App.removerDoCarrinho(${item.idUsuarioCarrinho})">
            🗑️
          </button>
        </div>
      `;
    }).join('');

    summaryContainer.innerHTML = `
      <h3 class="font-serif" style="font-size: 1.6rem; color: var(--primary-dark); margin-bottom: 16px;">Resumo do Pedido</h3>
      <div style="display: flex; justify-content: space-between; margin-bottom: 8px; color: var(--text-muted);">
        <span>Subtotal (${itens.length} itens):</span>
        <span>R$ ${total.toFixed(2).replace('.', ',')}</span>
      </div>
      <div style="display: flex; justify-content: space-between; margin-bottom: 8px; color: var(--text-muted);">
        <span>Retirada no Local (Local Pickup):</span>
        <span style="color: var(--status-success); font-weight: 700;">Grátis (R$ 0,00)</span>
      </div>
      <div style="border-top: 1px solid var(--border-subtle); padding-top: 14px; margin-top: 14px; display: flex; justify-content: space-between; font-size: 1.3rem; font-weight: 800; color: var(--primary-dark);">
        <span>Total:</span>
        <span>R$ ${total.toFixed(2).replace('.', ',')}</span>
      </div>
      <div style="background: var(--bg-alt); padding: 12px; border-radius: var(--radius-sm); margin: 16px 0; font-size: 0.8rem; color: var(--text-muted);">
        📍 <strong>Retirada no Local:</strong> Loja Física Dupla Cor - Shopping Glamour. Apresente o número do pedido na retirada.
      </div>
      <button class="btn-primary" style="width: 100%;" onclick="window.location.hash = '#checkout'">
        Prosseguir para Finalização
      </button>
    `;
  },

  async alterarQtdCarrinho(idItem, novaQtd) {
    await ApiClient.atualizarQtdCarrinho(idItem, novaQtd);
    await this.atualizarContadorCarrinho();
    this.renderCarrinho();
  },

  async removerDoCarrinho(idItem) {
    await ApiClient.removerDoCarrinho(idItem);
    await this.atualizarContadorCarrinho();
    this.renderCarrinho();
    this.mostrarToast('Item removido do carrinho.', 'info');
  },

  // =========================================================================
  // 5. CHECKOUT & SIMULAÇÃO FEFO
  // =========================================================================
  async renderCheckout() {
    const user = this.usuarioAtual;
    if (!user) return;

    const itens = await ApiClient.getCarrinho(user.idUsuario);
    const container = document.getElementById('checkout-review-items');
    if (!container) return;

    if (itens.length === 0) {
      window.location.hash = '#carrinho';
      return;
    }

    const inputNome = document.getElementById('chk-nome');
    const inputEmail = document.getElementById('chk-email');
    if (inputNome) inputNome.value = '';
    if (inputEmail) inputEmail.value = '';


    let total = 0;
    let fefoPreviewHtml = '';

    for (const it of itens) {
      const prod = it.produto || { nome: 'Esmalte', precoBase: 0 };
      const subtotal = prod.precoBase * it.quantidade;
      total += subtotal;

      const lotesProd = await ApiClient.getLotesPorProduto(prod.idProduto);
      const sim = FefoEngine.simularAlocacao(lotesProd, it.quantidade);

      fefoPreviewHtml += `
        <div style="padding: 12px; background: var(--bg-alt); border-radius: var(--radius-sm); margin-bottom: 10px;">
          <div style="font-weight: 700; font-size: 0.9rem; color: var(--primary-dark);">${prod.nome} (${it.quantidade} un)</div>
          <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 4px;">
            ${sim.alocacoes.map(a => `• Lote #${a.loteId} (Validade: ${FefoEngine.formatarData(a.dataValidade)}) -> Alocado: <strong>${a.quantidade} un</strong>`).join('<br>')}
          </div>
        </div>
      `;
    }

    container.innerHTML = `
      <div style="margin-bottom: 20px;">
        <h4 style="font-weight: 700; margin-bottom: 8px;">Itens Selecionados:</h4>
        ${itens.map(i => `
          <div style="display: flex; justify-content: space-between; font-size: 0.9rem; padding: 6px 0; border-bottom: 1px dashed var(--border-subtle);">
            <span>${i.quantidade}x ${i.produto ? i.produto.nome : 'Produto'}</span>
            <strong>R$ ${(i.produto.precoBase * i.quantidade).toFixed(2).replace('.', ',')}</strong>
          </div>
        `).join('')}
      </div>

      <div style="margin-bottom: 20px;">
        <h4 style="font-weight: 700; color: var(--badge-fefo); margin-bottom: 8px;">
          🛡️ Alocação Inteligente de Lote (FEFO):
        </h4>
        ${fefoPreviewHtml}
      </div>

      <div style="border-top: 2px solid var(--border-subtle); padding-top: 12px; display: flex; justify-content: space-between; font-size: 1.3rem; font-weight: 800; color: var(--primary-dark);">
        <span>Total a Pagar:</span>
        <span>R$ ${total.toFixed(2).replace('.', ',')}</span>
      </div>
    `;
  },

  async confirmarPedido() {
    const user = this.usuarioAtual || { idUsuario: 2 };
    const formaPag = document.querySelector('input[name="formaPagamento"]:checked')?.value || 'PIX';

    const pedido = await ApiClient.finalizarPedido(user.idUsuario, 'PAGO (' + formaPag + ')');
    if (pedido) {
      await this.atualizarContadorCarrinho();
      this.mostrarToast('Pedido #' + pedido.idPedido + ' gerado com sucesso!', 'success');
      
      // Abre modal de sucesso
      const modal = document.getElementById('modal-sucesso-pedido');
      if (modal) {
        document.getElementById('sucesso-pedido-id').textContent = '#' + pedido.idPedido;
        document.getElementById('sucesso-pedido-total').textContent = 'R$ ' + pedido.total.toFixed(2).replace('.', ',');
        modal.classList.add('active');
      } else {
        window.location.hash = '#pedidos';
      }
    } else {
      this.mostrarToast('Erro ao processar o pedido. Verifique o estoque dos lotes.', 'error');
    }
  },

  // =========================================================================
  // 6. MEUS PEDIDOS
  // =========================================================================
  async renderPedidos() {
    const user = this.usuarioAtual || { idUsuario: 2 };
    const pedidos = await ApiClient.getPedidos(user.idUsuario);
    const container = document.getElementById('pedidos-list-container');
    if (!container) return;

    if (pedidos.length === 0) {
      container.innerHTML = `
        <div style="text-align: center; padding: 48px; background: #ffffff; border-radius: var(--radius-md);">
          <h3 class="font-serif" style="font-size: 1.8rem; color: var(--primary-dark);">Nenhum pedido realizado ainda</h3>
          <p style="color: var(--text-muted); margin-bottom: 16px;">Seus pedidos confirmados aparecerão aqui com rastreabilidade completa.</p>
          <a href="#produtos" class="btn-primary">Fazer Primeira Compra</a>
        </div>
      `;
      return;
    }

    container.innerHTML = pedidos.map(p => `
      <div style="background: #ffffff; border: 1px solid var(--border-subtle); border-radius: var(--radius-md); padding: 24px; margin-bottom: 20px; box-shadow: var(--shadow-sm);">
        <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-subtle); padding-bottom: 12px; margin-bottom: 16px;">
          <div>
            <span style="font-weight: 800; font-size: 1.2rem; color: var(--primary-dark);">Pedido #${p.idPedido}</span>
            <span style="color: var(--text-muted); font-size: 0.85rem; margin-left: 12px;">📅 ${p.dataVenda}</span>
          </div>
          <span class="badge-status badge-pago">${p.statusPagamento}</span>
        </div>
        <div style="margin-bottom: 14px;">
          <strong>Itens Comprados (Rastreabilidade de Lote):</strong>
          <ul style="list-style: none; margin-top: 8px;">
            ${(p.itens || []).map(it => `
              <li style="font-size: 0.88rem; color: var(--text-muted); padding: 4px 0;">
                💅 <strong>${it.produtoNome || 'Esmalte'}</strong> — ${it.quantidade} un (Lote #${it.Lote_idLote}) a R$ ${it.precoAplicado.toFixed(2).replace('.', ',')}
              </li>
            `).join('')}
          </ul>
        </div>
        <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-subtle); padding-top: 12px;">
          <span style="font-size: 0.85rem; color: var(--text-muted);">Modelo de Retirada: Local Pickup</span>
          <span style="font-size: 1.2rem; font-weight: 800; color: var(--primary-dark);">Total: R$ ${p.total.toFixed(2).replace('.', ',')}</span>
        </div>
      </div>
    `).join('');
  },

  // =========================================================================
  // 7. PAINEL ADMINISTRATIVO
  // =========================================================================
  setAdminTab(tabName) {
    document.querySelectorAll('.admin-tab').forEach(tab => {
      tab.classList.toggle('active', tab.dataset.tab === tabName);
    });
    document.querySelectorAll('.admin-view-content').forEach(view => {
      view.style.display = view.id === `admin-tab-${tabName}` ? 'block' : 'none';
    });
  },

  // =========================================================================
  // RELATÓRIOS: EXPORTAÇÃO CSV & IMPRESSÃO
  // =========================================================================
  exportarCSV(linhas, colunas, nomeArquivo) {
    const cabecalho = colunas.map(c => c.header).join(';');
    const corpo = linhas.map(item => {
      return colunas.map(c => {
        let valor = typeof c.value === 'function' ? c.value(item) : item[c.key];
        valor = (valor === null || valor === undefined) ? '' : String(valor);
        valor = valor.replace(/"/g, '""');
        return /[;"\n]/.test(valor) ? `"${valor}"` : valor;
      }).join(';');
    });
    // BOM (\uFEFF) garante que o Excel reconheça acentuação corretamente
    const csv = '\uFEFF' + [cabecalho, ...corpo].join('\r\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nomeArquivo;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    this.mostrarToast('Arquivo CSV exportado com sucesso!', 'success');
  },

  async exportarLotesCSV() {
    const lotes = await ApiClient.getLotes();
    const produtos = await ApiClient.getProdutos();
    this.exportarCSV(lotes, [
      { header: 'ID Lote', key: 'idLote' },
      { header: 'Produto', value: l => (produtos.find(p => p.idProduto === l.Produto_idProduto) || {}).nome || '' },
      { header: 'Qtd Inicial', key: 'quantInicial' },
      { header: 'Saldo Atual', key: 'quantAtual' },
      { header: 'Data Entrada', key: 'dataEntrada' },
      { header: 'Data Validade', key: 'dataValidade' },
      { header: 'Status', key: 'status' }
    ], 'duplacor_lotes.csv');
  },

  async exportarProdutosCSV() {
    const produtos = await ApiClient.getProdutos();
    this.exportarCSV(produtos, [
      { header: 'ID', key: 'idProduto' },
      { header: 'Nome', key: 'nome' },
      { header: 'Marca', key: 'marca' },
      { header: 'Preço Base', value: p => p.precoBase.toFixed(2).replace('.', ',') },
      { header: 'Status', key: 'status' }
    ], 'duplacor_produtos.csv');
  },

  async exportarPerdasCSV() {
    const perdas = await ApiClient.getPerdas();
    this.exportarCSV(perdas, [
      { header: 'ID Perda', key: 'idPerda' },
      { header: 'Lote', value: p => `#${p.Lote_idLote}` },
      { header: 'Quantidade', key: 'quantidade' },
      { header: 'Motivo', key: 'motivo' },
      { header: 'Data Registro', key: 'dataRegistro' }
    ], 'duplacor_perdas.csv');
  },

  async exportarPedidosCSV() {
    const pedidos = await ApiClient.getPedidos();
    this.exportarCSV(pedidos, [
      { header: 'ID Pedido', key: 'idPedido' },
      { header: 'Data Venda', key: 'dataVenda' },
      { header: 'Cliente', value: p => `#${p.Usuario_idUsuario}` },
      { header: 'Total', value: p => p.total.toFixed(2).replace('.', ',') },
      { header: 'Status Pagamento', key: 'statusPagamento' }
    ], 'duplacor_pedidos.csv');
  },

  async exportarDashboardCSV() {
    const produtos = await ApiClient.getProdutos();
    const lotes = await ApiClient.getLotes();
    const perdas = await ApiClient.getPerdas();
    const pedidos = await ApiClient.getPedidos();
    const lotesVencidos = lotes.filter(l => FefoEngine.isLoteVencido(l.dataValidade));
    const lotesAtivos = lotes.filter(l => !FefoEngine.isLoteVencido(l.dataValidade) && l.quantAtual > 0);
    const totalVendas = pedidos.reduce((acc, p) => acc + (p.total || 0), 0);
    const totalPerdas = perdas.reduce((acc, p) => acc + (p.quantidade || 0), 0);

    const resumo = [
      { indicador: 'Total de Produtos Cadastrados', valor: produtos.length },
      { indicador: 'Lotes Ativos (disponíveis)', valor: lotesAtivos.length },
      { indicador: 'Lotes Vencidos/Bloqueados', valor: lotesVencidos.length },
      { indicador: 'Total de Pedidos', valor: pedidos.length },
      { indicador: 'Total Faturado (R$)', valor: totalVendas.toFixed(2).replace('.', ',') },
      { indicador: 'Total de Unidades Perdidas', valor: totalPerdas }
    ];
    this.exportarCSV(resumo, [
      { header: 'Indicador', key: 'indicador' },
      { header: 'Valor', key: 'valor' }
    ], 'duplacor_dashboard_resumo.csv');
  },

  async renderAdminDashboard() {
    const produtos = await ApiClient.getProdutos();
    const lotes = await ApiClient.getLotes();
    const perdas = await ApiClient.getPerdas();
    const pedidos = await ApiClient.getPedidos();

    const lotesVencidos = lotes.filter(l => FefoEngine.isLoteVencido(l.dataValidade));
    const lotesAtivos = lotes.filter(l => !FefoEngine.isLoteVencido(l.dataValidade) && l.quantAtual > 0);
    const totalVendas = pedidos.reduce((acc, p) => acc + (p.total || 0), 0);

    const kpiEl = document.getElementById('admin-kpis-container');
    if (kpiEl) {
      kpiEl.innerHTML = `
        <div class="kpi-card">
          <div class="kpi-title">Total de Produtos</div>
          <div class="kpi-value">${produtos.length}</div>
          <div style="font-size: 0.75rem; color: var(--text-muted);">Cadastrados no catálogo</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-title">Lotes Ativos (FEFO)</div>
          <div class="kpi-value" style="color: var(--status-success);">${lotesAtivos.length}</div>
          <div style="font-size: 0.75rem; color: var(--text-muted);">Prontos para venda</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-title">Lotes Vencidos / Bloqueados</div>
          <div class="kpi-value" style="color: var(--status-danger);">${lotesVencidos.length}</div>
          <div style="font-size: 0.75rem; color: var(--text-muted);">Bloqueados da vitrine</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-title">Total Faturado</div>
          <div class="kpi-value">R$ ${totalVendas.toFixed(2).replace('.', ',')}</div>
          <div style="font-size: 0.75rem; color: var(--text-muted);">${pedidos.length} pedidos realizados</div>
        </div>
      `;
    }

    // Relatório de Vendas por Produto (item 8 do escopo)
    const vendasTbody = document.getElementById('admin-relatorio-vendas-tbody');
    if (vendasTbody) {
      const vendas = (await ApiClient.getRelatorioVendas()).filter(v => v.quantidadeVendida > 0);
      vendasTbody.innerHTML = vendas.length > 0
        ? vendas.map(v => `
            <tr>
              <td>${v.nome}${v.marca ? ` <span style="color: var(--text-muted); font-size: 0.8em;">(${v.marca})</span>` : ''}</td>
              <td>${v.quantidadeVendida}</td>
              <td>R$ ${Number(v.faturamento).toFixed(2).replace('.', ',')}</td>
            </tr>`).join('')
        : '<tr><td colspan="3" style="text-align:center; color: var(--text-muted);">Nenhuma venda registrada ainda</td></tr>';
    }

    // Análise de Consumo por Categoria (item 8 do escopo)
    const consumoTbody = document.getElementById('admin-relatorio-consumo-tbody');
    if (consumoTbody) {
      const consumo = (await ApiClient.getRelatorioConsumo()).filter(c => c.quantidadeVendida > 0);
      consumoTbody.innerHTML = consumo.length > 0
        ? consumo.map(c => `
            <tr>
              <td>${c.nome}</td>
              <td>${c.quantidadeVendida}</td>
              <td>R$ ${Number(c.faturamento).toFixed(2).replace('.', ',')}</td>
            </tr>`).join('')
        : '<tr><td colspan="3" style="text-align:center; color: var(--text-muted);">Nenhum consumo registrado ainda</td></tr>';
    }
  },

  async renderAdminLotes() {
    const lotes = await ApiClient.getLotes();
    const produtos = await ApiClient.getProdutos();
    const tbody = document.getElementById('admin-lotes-tbody');
    if (!tbody) return;

    tbody.innerHTML = lotes.map(l => {
      const prod = produtos.find(p => p.idProduto === l.Produto_idProduto) || { nome: 'Prod #' + l.Produto_idProduto };
      const vencido = FefoEngine.isLoteVencido(l.dataValidade);
      const dias = FefoEngine.diasAteVencimento(l.dataValidade);
      const statusBadge = vencido ? 'badge-vencido' : (l.quantAtual === 0 ? 'badge-esgotado' : 'badge-disponivel');

      return `
        <tr>
          <td><strong>#${l.idLote}</strong></td>
          <td><strong>${prod.nome}</strong></td>
          <td>${l.quantInicial} un</td>
          <td><strong style="color: var(--primary-dark);">${l.quantAtual} un</strong></td>
          <td>${FefoEngine.formatarData(l.dataEntrada)}</td>
          <td>
            ${FefoEngine.formatarData(l.dataValidade)} 
            <small style="color: ${dias < 30 ? 'var(--status-danger)' : 'var(--text-muted)'}; font-weight: 600;">(${dias}d)</small>
          </td>
          <td><span class="badge-status ${statusBadge}">${vencido ? 'VENCIDO' : l.status}</span></td>
          <td>
            ${vencido && l.quantAtual > 0 ? `
              <button class="btn-secondary" style="padding: 4px 10px; font-size: 0.75rem;" onclick="App.abrirModalDescarte(${l.idLote}, ${l.quantAtual})">
                Descartar
              </button>
            ` : '-'}
          </td>
        </tr>
      `;
    }).join('');
  },

  async renderAdminProdutos() {
    const produtos = await ApiClient.getProdutos();
    const tbody = document.getElementById('admin-produtos-tbody');
    if (!tbody) return;

    tbody.innerHTML = produtos.map(p => `
      <tr>
        <td>#${p.idProduto}</td>
        <td><strong>${p.nome}</strong></td>
        <td>${p.marca || '-'}</td>
        <td><strong>R$ ${p.precoBase.toFixed(2).replace('.', ',')}</strong></td>
        <td><span class="badge-status ${p.status === 'ATIVO' ? 'badge-disponivel' : 'badge-esgotado'}">${p.status}</span></td>
        <td>
          <button class="btn-secondary" style="padding: 4px 10px; font-size: 0.75rem;" onclick="App.excluirProduto(${p.idProduto})">Excluir</button>
        </td>
      </tr>
    `).join('');
  },

  async renderAdminCategorias() {
    const categorias = await ApiClient.getCategorias();
    const tbody = document.getElementById('admin-categorias-tbody');
    if (!tbody) return;

    tbody.innerHTML = categorias.map(c => `
      <tr>
        <td>#${c.idCategoria}</td>
        <td><strong>${c.nome}</strong></td>
        <td>${c.descricao || '-'}</td>
      </tr>
    `).join('');
  },

  async renderAdminPerdas() {
    const perdas = await ApiClient.getPerdas();
    const tbody = document.getElementById('admin-perdas-tbody');
    if (!tbody) return;

    tbody.innerHTML = perdas.map(p => `
      <tr>
        <td>#${p.idPerda}</td>
        <td>Lote #${p.Lote_idLote}</td>
        <td><strong style="color: var(--status-danger);">${p.quantidade} un</strong></td>
        <td>${p.motivo}</td>
        <td>${p.dataRegistro}</td>
      </tr>
    `).join('');
  },

  async renderAdminPedidos() {
    const pedidos = await ApiClient.getPedidos();
    const tbody = document.getElementById('admin-pedidos-tbody');
    if (!tbody) return;

    tbody.innerHTML = pedidos.map(p => `
      <tr>
        <td>#${p.idPedido}</td>
        <td>${p.dataVenda}</td>
        <td>Cliente #${p.Usuario_idUsuario}</td>
        <td><strong>R$ ${p.total.toFixed(2).replace('.', ',')}</strong></td>
        <td><span class="badge-status badge-pago">${p.statusPagamento}</span></td>
      </tr>
    `).join('');
  },

  async monitorarValidadeAdmin() {
    const count = await ApiClient.monitorarVencidos();
    this.mostrarToast(`Varredura concluída. ${count} lote(s) bloqueados por vencimento.`, 'info');
    this.renderAdminLotes();
    this.renderAdminDashboard();
  },

  abrirModalNovoLote() {
    const modal = document.getElementById('modal-novo-lote');
    if (modal) modal.classList.add('active');
  },

  abrirModalNovoProduto() {
    const modal = document.getElementById('modal-novo-produto');
    if (modal) modal.classList.add('active');
  },

  abrirModalNovaCategoria() {
    const modal = document.getElementById('modal-nova-categoria');
    if (modal) modal.classList.add('active');
  },

  abrirModalDescarte(loteId, qtd) {
    const modal = document.getElementById('modal-descarte-perda');
    if (modal) {
      document.getElementById('descarte-lote-id').value = loteId;
      document.getElementById('descarte-quantidade').value = qtd;
      modal.classList.add('active');
    }
  },

  async salvarNovoLote(e) {
    e.preventDefault();
    const prodId = document.getElementById('lote-prod-id').value;
    const qtd = parseInt(document.getElementById('lote-qtd').value, 10);
    const dtVal = document.getElementById('lote-dt-val').value;
    const dtEnt = document.getElementById('lote-dt-ent').value || new Date().toISOString().split('T')[0];

    await ApiClient.salvarLote({
      quantInicial: qtd,
      dataValidade: dtVal,
      dataEntrada: dtEnt,
      Produto_idProduto: parseInt(prodId, 10)
    });

    this.fecharModais();
    this.mostrarToast('Novo lote integrado ao estoque com sucesso!', 'success');
    this.renderAdminLotes();
  },

  async salvarNovoProduto(e) {
    e.preventDefault();
    const nome = document.getElementById('prod-nome').value;
    const marca = document.getElementById('prod-marca').value;
    const preco = parseFloat(document.getElementById('prod-preco').value);

    await ApiClient.salvarProduto({
      nome,
      marca,
      precoBase: preco,
      status: 'ATIVO',
      categorias: [1]
    });

    this.fecharModais();
    this.mostrarToast('Produto cadastrado com sucesso!', 'success');
    this.renderAdminProdutos();
  },

  async salvarNovaCategoria(e) {
    e.preventDefault();
    const nome = document.getElementById('cat-nome').value;
    const desc = document.getElementById('cat-desc').value;

    await ApiClient.salvarCategoria({ nome, descricao: desc });
    this.fecharModais();
    this.mostrarToast('Categoria cadastrada com sucesso!', 'success');
    this.renderAdminCategorias();
  },

  async salvarDescarte(e) {
    e.preventDefault();
    const loteId = document.getElementById('descarte-lote-id').value;
    const qtd = parseInt(document.getElementById('descarte-quantidade').value, 10);
    const motivo = document.getElementById('descarte-motivo').value;

    await ApiClient.registrarPerda(loteId, qtd, motivo);
    this.fecharModais();
    this.mostrarToast('Descarte e perda registrados no sistema!', 'info');
    this.renderAdminLotes();
    this.renderAdminPerdas();
  },

  async excluirProduto(id) {
    if (confirm('Deseja realmente excluir este produto?')) {
      await ApiClient.excluirProduto(id);
      this.mostrarToast('Produto excluído.', 'info');
      this.renderAdminProdutos();
    }
  },

  // =========================================================================
  // TOAST NOTIFICATIONS
  // =========================================================================
  mostrarToast(mensagem, tipo = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${tipo}`;
    toast.innerHTML = `
      <span>${tipo === 'success' ? '✨' : (tipo === 'error' ? '⚠️' : 'ℹ️')}</span>
      <span>${mensagem}</span>
    `;

    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      setTimeout(() => toast.remove(), 300);
    }, 3500);
  }
};

window.App = App;