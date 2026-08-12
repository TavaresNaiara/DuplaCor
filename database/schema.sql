-- ======================================================================
-- Banco de Dados: duplacor
-- Sistema de Gestão de Estoque e Venda de Esmaltes (Dupla Cor)
-- Baseado no DER: Dupla Cor Diagrama.mwb
-- ======================================================================

CREATE DATABASE IF NOT EXISTS `duplacor` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `duplacor`;

-- Desativar verificação de chaves estrangeiras durante a criação
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- Tabela: Usuario
-- -----------------------------------------------------
DROP TABLE IF EXISTS `UsuarioCarrinho`;
DROP TABLE IF EXISTS `ItemPedido`;
DROP TABLE IF EXISTS `Pedido`;
DROP TABLE IF EXISTS `Perda`;
DROP TABLE IF EXISTS `Lote`;
DROP TABLE IF EXISTS `Produto_has_Categoria`;
DROP TABLE IF EXISTS `Produto`;
DROP TABLE IF EXISTS `Categoria`;
DROP TABLE IF EXISTS `Usuario`;

CREATE TABLE IF NOT EXISTS `Usuario` (
    `idUsuario` INT NOT NULL AUTO_INCREMENT,
    `nome` VARCHAR(45) NOT NULL,
    `email` VARCHAR(45) NOT NULL,
    `senha` VARCHAR(45) NOT NULL,
    `perfil` VARCHAR(45) NULL DEFAULT 'CLIENTE',
    `dataCadastro` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    `tokenRecuperacao` VARCHAR(45) NULL DEFAULT NULL,
    PRIMARY KEY (`idUsuario`),
    UNIQUE INDEX `email_UNIQUE` (`email` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;

-- -----------------------------------------------------
-- Tabela: Categoria
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `Categoria` (
    `idCategoria` INT NOT NULL AUTO_INCREMENT,
    `nome` VARCHAR(45) NOT NULL,
    `descricao` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`idCategoria`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;

-- -----------------------------------------------------
-- Tabela: Produto
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `Produto` (
    `idProduto` INT NOT NULL AUTO_INCREMENT,
    `nome` VARCHAR(45) NOT NULL,
    `marca` VARCHAR(45) NULL DEFAULT NULL,
    `precoBase` DECIMAL(10,2) NOT NULL,
    `status` VARCHAR(45) NOT NULL DEFAULT 'ATIVO',
    PRIMARY KEY (`idProduto`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;

-- -----------------------------------------------------
-- Tabela: Produto_has_Categoria (N:N)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `Produto_has_Categoria` (
    `Produto_idProduto` INT NOT NULL,
    `Categoria_idCategoria` INT NOT NULL,
    PRIMARY KEY (`Produto_idProduto`, `Categoria_idCategoria`),
    INDEX `fk_Produto_has_Categoria_Categoria1_idx` (`Categoria_idCategoria` ASC),
    INDEX `fk_Produto_has_Categoria_Produto_idx` (`Produto_idProduto` ASC),
    CONSTRAINT `fk_Produto_has_Categoria_Produto`
        FOREIGN KEY (`Produto_idProduto`)
        REFERENCES `Produto` (`idProduto`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT `fk_Produto_has_Categoria_Categoria1`
        FOREIGN KEY (`Categoria_idCategoria`)
        REFERENCES `Categoria` (`idCategoria`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;

-- -----------------------------------------------------
-- Tabela: Lote
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `Lote` (
    `idLote` INT NOT NULL AUTO_INCREMENT,
    `quantInicial` INT NOT NULL,
    `quantAtual` INT NULL DEFAULT 0,
    `dataValidade` DATE NOT NULL,
    `dataEntrada` DATE NOT NULL,
    `status` VARCHAR(45) NOT NULL DEFAULT 'DISPONIVEL',
    `Produto_idProduto` INT NOT NULL,
    PRIMARY KEY (`idLote`),
    INDEX `fk_Lote_Produto1_idx` (`Produto_idProduto` ASC),
    CONSTRAINT `fk_Lote_Produto1`
        FOREIGN KEY (`Produto_idProduto`)
        REFERENCES `Produto` (`idProduto`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;

-- -----------------------------------------------------
-- Tabela: Perda
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `Perda` (
    `idPerda` INT NOT NULL AUTO_INCREMENT,
    `quantidade` INT NULL DEFAULT 0,
    `dataRegistro` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    `motivo` VARCHAR(45) NULL DEFAULT NULL,
    `Lote_idLote` INT NOT NULL,
    PRIMARY KEY (`idPerda`),
    INDEX `fk_Perda_Lote1_idx` (`Lote_idLote` ASC),
    CONSTRAINT `fk_Perda_Lote1`
        FOREIGN KEY (`Lote_idLote`)
        REFERENCES `Lote` (`idLote`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;

-- -----------------------------------------------------
-- Tabela: Pedido
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `Pedido` (
    `idPedido` INT NOT NULL AUTO_INCREMENT,
    `dataVenda` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    `total` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `statusPagamento` VARCHAR(45) NOT NULL DEFAULT 'APROVADO',
    `Usuario_idUsuario` INT NOT NULL,
    PRIMARY KEY (`idPedido`),
    INDEX `fk_Pedido_Usuario1_idx` (`Usuario_idUsuario` ASC),
    CONSTRAINT `fk_Pedido_Usuario1`
        FOREIGN KEY (`Usuario_idUsuario`)
        REFERENCES `Usuario` (`idUsuario`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;

-- -----------------------------------------------------
-- Tabela: ItemPedido
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ItemPedido` (
    `idItemPedido` INT NOT NULL AUTO_INCREMENT,
    `quantidade` INT NOT NULL,
    `precoAplicado` DECIMAL(10,2) NOT NULL,
    `Lote_idLote` INT NOT NULL,
    `Pedido_idPedido` INT NOT NULL,
    PRIMARY KEY (`idItemPedido`, `Pedido_idPedido`),
    INDEX `fk_ItemPedido_Lote1_idx` (`Lote_idLote` ASC),
    INDEX `fk_ItemPedido_Pedido1_idx` (`Pedido_idPedido` ASC),
    CONSTRAINT `fk_ItemPedido_Lote1`
        FOREIGN KEY (`Lote_idLote`)
        REFERENCES `Lote` (`idLote`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT `fk_ItemPedido_Pedido1`
        FOREIGN KEY (`Pedido_idPedido`)
        REFERENCES `Pedido` (`idPedido`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;

-- -----------------------------------------------------
-- Tabela: UsuarioCarrinho (Carrinho Persistente)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `UsuarioCarrinho` (
    `idUsuarioCarrinho` INT NOT NULL AUTO_INCREMENT,
    `dataAdicao` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    `quantidade` INT NOT NULL DEFAULT 1,
    `Usuario_idUsuario` INT NOT NULL,
    `Produto_idProduto` INT NOT NULL,
    PRIMARY KEY (`idUsuarioCarrinho`),
    INDEX `fk_UsuarioCarrinho_Usuario1_idx` (`Usuario_idUsuario` ASC),
    INDEX `fk_UsuarioCarrinho_Produto1_idx` (`Produto_idProduto` ASC),
    CONSTRAINT `fk_UsuarioCarrinho_Usuario1`
        FOREIGN KEY (`Usuario_idUsuario`)
        REFERENCES `Usuario` (`idUsuario`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT `fk_UsuarioCarrinho_Produto1`
        FOREIGN KEY (`Produto_idProduto`)
        REFERENCES `Produto` (`idProduto`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;

-- Reativar verificação de chaves estrangeiras
SET FOREIGN_KEY_CHECKS = 1;

-- ======================================================================
-- DADOS INICIAIS (SEEDING PARA TESTES)
-- ======================================================================

-- Inserir Usuários
INSERT INTO `Usuario` (`idUsuario`, `nome`, `email`, `senha`, `perfil`, `dataCadastro`) VALUES
(1, 'Administrador Dupla Cor', 'admin@duplacor.com.br', 'admin123', 'ADMIN', NOW()),
(2, 'Maria Silva', 'maria@email.com', 'maria123', 'CLIENTE', NOW()),
(3, 'Juliana Santos', 'juliana@email.com', 'ju123', 'CLIENTE', NOW());

-- Inserir Categorias
INSERT INTO `Categoria` (`idCategoria`, `nome`, `descricao`) VALUES
(1, 'Cremoso', 'Esmaltes com acabamento cremoso tradicional e alta cobertura'),
(2, 'Cintilante', 'Esmaltes com micropartículas de brilho perolado'),
(3, 'Tratamento & Base', 'Bases fortalecedoras, óleos secantes e finalizadores'),
(4, 'Glitter', 'Esmaltes com partículas reflexivas de glitter intenso');

-- Inserir Produtos
INSERT INTO `Produto` (`idProduto`, `nome`, `marca`, `precoBase`, `status`) VALUES
(1, 'Vermelho Royal', 'Risqué', 8.90, 'ATIVO'),
(2, 'Renda Clássica', 'Risqué', 8.50, 'ATIVO'),
(3, 'Base Bomba Fortalecedora', 'Impala', 12.00, 'ATIVO'),
(4, 'Preto Sépia', 'Colorama', 7.90, 'ATIVO'),
(5, 'Top Coat Brilho Diamante', 'Dailus', 14.50, 'ATIVO');

-- Inserir Relacionamento Produto x Categoria (N:N)
INSERT INTO `Produto_has_Categoria` (`Produto_idProduto`, `Categoria_idCategoria`) VALUES
(1, 1), -- Vermelho Royal -> Cremoso
(2, 1), -- Renda -> Cremoso
(3, 3), -- Base Bomba -> Tratamento & Base
(4, 1), -- Preto Sépia -> Cremoso
(5, 3); -- Top Coat -> Tratamento & Base

-- Inserir Lotes (com datas variadas para testar o algoritmo FEFO)
-- Lote 1: Vermelho Royal com validade mais próxima (2026-10-15) -> Deve sair primeiro pelo FEFO
-- Lote 2: Vermelho Royal com validade mais distante (2027-05-20)
-- Lote 3: Renda com lote normal (2027-01-10)
-- Lote 4: Base Bomba com lote expirado (teste de perda)
INSERT INTO `Lote` (`idLote`, `quantInicial`, `quantAtual`, `dataValidade`, `dataEntrada`, `status`, `Produto_idProduto`) VALUES
(1, 50, 48, '2026-10-15', '2026-01-10', 'DISPONIVEL', 1),
(2, 100, 100, '2027-05-20', '2026-03-01', 'DISPONIVEL', 1),
(3, 30, 30, '2027-01-10', '2026-02-15', 'DISPONIVEL', 2),
(4, 20, 0, '2025-12-31', '2025-01-01', 'VENCIDO', 3),
(5, 40, 40, '2027-08-15', '2026-04-10', 'DISPONIVEL', 4),
(6, 25, 25, '2027-11-30', '2026-05-01', 'DISPONIVEL', 5);

-- Inserir Perdas registradas (Lote 4 que venceu)
INSERT INTO `Perda` (`idPerda`, `quantidade`, `dataRegistro`, `motivo`, `Lote_idLote`) VALUES
(1, 20, '2026-01-02 08:30:00', 'PRODUTO VENCIDO', 4);

-- Inserir Pedido de exemplo
INSERT INTO `Pedido` (`idPedido`, `dataVenda`, `total`, `statusPagamento`, `Usuario_idUsuario`) VALUES
(1, '2026-05-15 14:20:00', 17.80, 'PAGO', 2);

-- Inserir Itens do Pedido (2 unidades do Lote 1 do Vermelho Royal)
INSERT INTO `ItemPedido` (`idItemPedido`, `quantidade`, `precoAplicado`, `Lote_idLote`, `Pedido_idPedido`) VALUES
(1, 2, 8.90, 1, 1);

-- Inserir Carrinho Persistente de exemplo
INSERT INTO `UsuarioCarrinho` (`idUsuarioCarrinho`, `dataAdicao`, `quantidade`, `Usuario_idUsuario`, `Produto_idProduto`) VALUES
(1, NOW(), 1, 3, 2), -- Juliana tem 1 Renda no carrinho
(2, NOW(), 2, 3, 5); -- Juliana tem 2 Top Coat no carrinho
