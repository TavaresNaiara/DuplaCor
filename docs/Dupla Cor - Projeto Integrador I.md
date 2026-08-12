Dupla Cor \- Escopo

O projeto Dupla Cor consiste no desenvolvimento de uma plataforma de e-commerce especializada no setor de esmaltaria, projetada para integrar operações de venda online com um sistema de gestão de inventário. O objetivo da aplicação é a implementação de uma arquitetura de estoque baseada em lotes, permitindo a rastreabilidade total de cada unidade desde a entrada no sistema até a retirada final pela consumidora. Diferente de modelos de estoque simplificados, este sistema isola a entidade do produto (catálogo) da entidade física do lote, onde cada remessa é obrigatoriamente vinculada a uma data de validade específica.

Para otimizar a eficiência financeira e garantir a conformidade com prazos de consumo, o software executa automaticamente o algoritmo FEFO (First Expired, First Out), que prioriza a reserva e o despacho dos itens com vencimento mais próximo. Caso um lote atinja sua data de expiração sem ser comercializado, o sistema bloqueia sua exibição na vitrine de vendas e o registra em um Relatório de Perdas por Vencimento, permitindo auditorias precisas sobre o desperdício de materiais.

No que diz respeito à experiência da usuária, a plataforma utiliza um carrinho persistente implementado em nível de banco de dados, garantindo que a seleção de produtos permaneça vinculada permanentemente ao perfil do cliente, mesmo após o encerramento da sessão. Além disso, um motor de recomendação inteligente processa o histórico de compras para sugerir produtos complementares (como bases tratativas ou finalizadores), personalizando a interface de acordo com os padrões de consumo detectados.

Para assegurar o foco na lógica de estoque e banco de dados, o projeto delimita suas fronteiras operacionais excluindo a integração com gateways de pagamento reais (utilizando simulações transacionais) e a logística de entrega em domicílio, adotando o modelo exclusivo de retirada no local (Local Pickup). O escopo também se restringe à comercialização de mercadorias, não abrangendo agendamentos de serviços de manicure, e será disponibilizado como uma aplicação Web responsiva.

Exclusão de funcionalidade: o usuário não poderá editar a senha quando redefinir a senha. Será enviada uma senha nova para o email cadastrado do sistema do usuário.

###   

Dupla Cor \- Funcionalidades

### **1\. Gestão de Produtos (Catálogo)**

**Descrição:**  
 Responsável pelo gerenciamento dos produtos disponíveis na plataforma, representando apenas a entidade de catálogo (sem controle direto de estoque físico). Permite organizar, exibir e manter os itens disponíveis para venda, garantindo que apenas produtos com estoque válido sejam apresentados às usuárias.

● Cadastro de Produtos: Inserção de nome, descrição, marca, categoria, imagens e preço base  
 ● Editar: Alteração de informações cadastrais e visuais  
 ● Inativar (Exclusão Lógica): Remoção do produto da vitrine sem exclusão permanente  
 ● Categorização: Associação do produto a uma ou mais categorias  
 ● Vinculação com Lotes: Associação indireta com unidades disponíveis em estoque  
 ● Exibição na Vitrine: Produtos só são exibidos se houver lote válido disponível

### **2\. Gestão de Estoque por Lotes**

**Descrição:**  
 Módulo central do sistema responsável pelo controle físico dos produtos. Implementa a separação entre produto (catálogo) e lote (estoque), garantindo rastreabilidade completa e controle por data de validade.

● Cadastro de Lotes: Registro de remessas com quantidade, data de entrada e data de validade  
 ● Rastreabilidade: Acompanhamento do lote desde a entrada até a saída  
 ● Separação Produto x Lote: Distinção entre entidade lógica e física  
 ● Atualização de Quantidade: Controle automático após vendas ou perdas  
 ● Bloqueio por Vencimento: Lotes expirados são automaticamente indisponibilizados  
 ● Consulta de Lotes: Visualização detalhada por validade e status

### **3\. Gestão de Vendas e Carrinho Persistente**

**Descrição:**  
 Responsável pelo fluxo de compra dentro da plataforma, garantindo que a experiência da usuária seja contínua e consistente, com persistência de dados mesmo após o encerramento da sessão.

● Carrinho Persistente: Armazenamento dos itens no banco de dados  
 ● Adicionar/Remover Itens: Gerenciamento dinâmico do carrinho  
 ● Simulação de Pagamento: Processamento fictício para validação da compra  
 ● Reserva de Estoque: Associação dos itens do carrinho aos lotes disponíveis  
 ● Finalização de Pedido: Confirmação da compra com baixa automática no estoque  
 ● Modelo de Retirada: Exclusivamente “Retirada no Local” (Local Pickup)

### **4\. Algoritmo FEFO (First Expired, First Out)**

**Descrição:**  
 Responsável por garantir que os produtos com menor prazo de validade sejam priorizados na venda, otimizando o giro de estoque e reduzindo perdas.

● Priorização Automática: Seleção de lotes com menor prazo de validade  
 ● Reserva Inteligente: Associação automática ao lote mais próximo do vencimento  
 ● Baixa de Estoque: Processamento respeitando a ordem FEFO  
 ● Otimização de Perdas: Redução de desperdícios  
 ● Execução Transparente: Processo automático sem intervenção do usuário

### **5\. Gestão de Perdas e Validade**

**Descrição:**  
 Controla produtos vencidos e garante que não sejam comercializados, além de fornecer dados para análise e auditoria de perdas.

● Monitoramento de Validade: Verificação contínua das datas de vencimento  
 ● Bloqueio de Venda: Produtos vencidos não são exibidos  
 ● Registro de Perdas: Inclusão automática de lotes expirados  
 ● Relatório de Perdas por Vencimento: Histórico detalhado  
 ● Análise de Desperdício: Apoio à tomada de decisão

### **6\. Gestão de Usuários e Autenticação**

**Descrição:**  
 Responsável pelo controle de acesso ao sistema, garantindo segurança e gerenciamento dos dados das usuárias.

● Cadastro de Usuários: Nome, e-mail e senha  
 ● Autenticação: Login e logout  
 ● Recuperação de Senha:  
 ○ Geração automática de nova senha enviada por e-mail  
 ○ Restrição: Não permite alteração manual após redefinição  
 ● Perfis de Usuário: Visualização de pedidos e histórico de compras

### **7\. Motor de Recomendação Inteligente**

**Descrição:**  
 Sistema responsável por personalizar a experiência da usuária com base em seu comportamento e histórico de compras.

● Análise de Histórico: Processamento de compras anteriores  
 ● Sugestão de Produtos: Recomendação de itens complementares  
 ● Personalização da Interface: Destaque de produtos relevantes  
 ● Aumento de Engajamento: Incentivo a novas compras

### **8\. Gestão de Relatórios e Métricas**

**Descrição:**  
 Fornece informações estratégicas sobre o desempenho do sistema, permitindo análise de vendas, estoque e comportamento das usuárias.

● Relatório de Vendas: Histórico de pedidos  
 ● Controle de Estoque: Quantidade por lote e validade  
 ● Relatório de Perdas: Produtos vencidos e descartados  
 ● Análise de Consumo: Identificação de padrões de compra  
 ● Estatísticas Gerais: Indicadores para tomada de decisão

