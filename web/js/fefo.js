/**
 * DUPLA COR - MOTOR DO ALGORITMO FEFO (First Expired, First Out)
 * Gerencia a ordenação, alocação de estoque por lote e renderização visual dos esmaltes.
 */

const FefoEngine = {
  /**
   * Ordena uma lista de lotes aplicando a regra FEFO:
   * Prioriza lotes com menor data de validade que estejam disponíveis e não expirados.
   */
  ordenarLotesFEFO(lotes) {
    if (!Array.isArray(lotes)) return [];
    return [...lotes]
      .filter(l => l.quantAtual > 0 && l.status === 'DISPONIVEL' && !this.isLoteVencido(l.dataValidade))
      .sort((a, b) => new Date(a.dataValidade) - new Date(b.dataValidade));
  },

  /**
   * Verifica se uma data de validade está expirada em relação à data atual.
   */
  isLoteVencido(dataValidadeStr) {
    if (!dataValidadeStr) return false;
    const [ano, mes, dia] = dataValidadeStr.split('-').map(Number);
    const dtVal = new Date(ano, mes - 1, dia);
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    return dtVal < hoje;
  },

  /**
   * Calcula quantos dias faltam até o vencimento do lote.
   */
  diasAteVencimento(dataValidadeStr) {
    if (!dataValidadeStr) return 0;
    const [ano, mes, dia] = dataValidadeStr.split('-').map(Number);
    const dtVal = new Date(ano, mes - 1, dia);
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const diffTime = dtVal - hoje;
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  },

  /**
   * Formata data ISO (YYYY-MM-DD) para o formato brasileiro (DD/MM/YYYY).
   */
  formatarData(dataStr) {
    if (!dataStr) return '-';
    const partes = dataStr.split('T')[0].split('-');
    if (partes.length === 3) {
      return `${partes[2]}/${partes[1]}/${partes[0]}`;
    }
    return dataStr;
  },

  /**
   * Simula a alocação de estoque de um produto utilizando os lotes disponíveis (Algoritmo FEFO).
   * Retorna os lotes impactados e quanto cada lote fornecerá.
   */
  simularAlocacao(lotes, quantidadeDesejada) {
    const lotesOrdenados = this.ordenarLotesFEFO(lotes);
    const alocacoes = [];
    let restante = quantidadeDesejada;

    for (const lote of lotesOrdenados) {
      if (restante <= 0) break;
      const disponivel = lote.quantAtual;
      const alocar = Math.min(restante, disponivel);

      alocacoes.push({
        loteId: lote.idLote,
        quantidade: alocar,
        dataValidade: lote.dataValidade,
        saldoAnterior: disponivel,
        saldoRestante: disponivel - alocar,
        esgotado: (disponivel - alocar) === 0
      });

      restante -= alocar;
    }

    return {
      atendido: restante === 0,
      quantidadeAlocada: quantidadeDesejada - restante,
      quantidadeFaltante: restante,
      alocacoes
    };
  },

  /**
   * Gera SVG estilizado de frasco de esmalte de luxo com cor baseada no ID ou categoria.
   */
  gerarFrascoSVG(produtoNome = '', corHex = null) {
    const coresPadrao = [
      { cap: '#2B1E3A', liquid: '#B81432', shadow: '#780B20' }, // Vermelho Royal
      { cap: '#D4AF37', liquid: '#F8E9E8', shadow: '#E4CFCE' }, // Renda / Nude
      { cap: '#7B2CBF', liquid: '#9B72CF', shadow: '#5C3D99' }, // Lavanda Glam
      { cap: '#2B1E3A', liquid: '#1E1B24', shadow: '#0C0A0E' }, // Preto Sépia
      { cap: '#D4AF37', liquid: '#E2F0D9', shadow: '#C5DEC0' }, // Base Fortalecedora
      { cap: '#C8B6FF', liquid: '#F72585', shadow: '#B5179E' }, // Rosa Pink
      { cap: '#D4AF37', liquid: '#E9D8A6', shadow: '#EE9B00' }  // Glitter Ouro
    ];

    let hash = 0;
    for (let i = 0; i < produtoNome.length; i++) {
      hash = produtoNome.charCodeAt(i) + ((hash << 5) - hash);
    }
    const index = Math.abs(hash) % coresPadrao.length;
    const esquema = coresPadrao[index];

    const liquidColor = corHex || esquema.liquid;
    const capColor = esquema.cap;
    const shadowColor = esquema.shadow;

    return `
      <svg class="nail-polish-svg" viewBox="0 0 100 150" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="grad-cap-${index}" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stop-color="${capColor}" />
            <stop offset="100%" stop-color="#120A1C" />
          </linearGradient>
          <linearGradient id="grad-bottle-${index}" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stop-color="${liquidColor}" />
            <stop offset="80%" stop-color="${shadowColor}" />
            <stop offset="100%" stop-color="#1E0A2A" />
          </linearGradient>
          <linearGradient id="glass-shine" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stop-color="rgba(255,255,255,0.4)" />
            <stop offset="50%" stop-color="rgba(255,255,255,0.05)" />
            <stop offset="100%" stop-color="rgba(255,255,255,0.2)" />
          </linearGradient>
        </defs>
        <!-- Tampa / Pincel -->
        <rect x="38" y="8" width="24" height="42" rx="4" fill="url(#grad-cap-${index})" />
        <rect x="42" y="50" width="16" height="6" rx="2" fill="#EBDFF5" />
        <line x1="41" y1="16" x2="41" y2="44" stroke="rgba(255,255,255,0.25)" stroke-width="2" />

        <!-- Corpo de Vidro -->
        <rect x="22" y="56" width="56" height="82" rx="14" fill="url(#grad-bottle-${index})" />
        
        <!-- Brilho de Vidro de Luxo -->
        <rect x="26" y="60" width="48" height="74" rx="10" fill="url(#glass-shine)" />
        <path d="M 28 66 Q 36 62 48 62" stroke="rgba(255,255,255,0.6)" stroke-width="3" stroke-linecap="round" fill="none" />
        <ellipse cx="50" cy="128" rx="20" ry="4" fill="rgba(0,0,0,0.15)" />

        <!-- Rótulo Minimalista Dupla Cor -->
        <rect x="32" y="84" width="36" height="24" rx="3" fill="rgba(255,255,255,0.92)" />
        <text x="50" y="96" text-anchor="middle" font-family="'Cormorant Garamond', serif" font-size="7.5" font-weight="bold" fill="#2B1E3A">DUPLA COR</text>
        <line x1="38" y1="99" x2="62" y2="99" stroke="#9B72CF" stroke-width="0.8" />
        <text x="50" y="104" text-anchor="middle" font-family="'Plus Jakarta Sans', sans-serif" font-size="4.5" font-weight="600" fill="#6E5D82">FEFO VERIFIED</text>
      </svg>
    `;
  }
};

window.FefoEngine = FefoEngine;
