-- ======================================================================
-- fix-encoding.sql
-- Corrige texto corrompido (ex.: "SÃ©pia" em vez de "Sépia") em um banco
-- `duplacor` que já existe e já tem dados, sem precisar recriar o banco.
--
-- Isso acontece quando o schema.sql (que está em UTF-8 correto) foi
-- importado usando um cliente MySQL configurado com outro charset
-- (geralmente latin1), fazendo cada caractere acentuado (2 bytes em UTF-8)
-- virar 2 caracteres errados quando reinterpretado.
--
-- A conversão abaixo desfaz exatamente essa reinterpretação:
--   1. Pega o texto (que o MySQL acha que é utf8mb4, mas na real contém
--      bytes de um utf8mb4 mal interpretado como latin1)
--   2. Converte para os bytes originais como se fossem latin1
--   3. Reinterpreta esses bytes como utf8mb4 (a codificação real)
--
-- USE COM CUIDADO: faça backup antes (mysqldump) e rode primeiro os
-- SELECTs comentados para conferir se o texto está mesmo corrompido
-- antes de aplicar o UPDATE.
-- ======================================================================

USE `duplacor`;

-- Passo 1 (opcional): veja quais linhas parecem corrompidas antes de mexer
-- SELECT idProduto, nome, marca FROM Produto WHERE nome LIKE '%Ã%' OR marca LIKE '%Ã%';
-- SELECT idCategoria, nome, descricao FROM Categoria WHERE nome LIKE '%Ã%' OR descricao LIKE '%Ã%';
-- SELECT idUsuario, nome FROM Usuario WHERE nome LIKE '%Ã%';

-- Passo 2: aplicar a correção tabela por tabela / coluna por coluna
UPDATE `Produto`
SET `nome`  = CONVERT(BINARY CONVERT(`nome`  USING latin1) USING utf8mb4)
WHERE `nome` LIKE '%Ã%';

UPDATE `Produto`
SET `marca` = CONVERT(BINARY CONVERT(`marca` USING latin1) USING utf8mb4)
WHERE `marca` LIKE '%Ã%';

UPDATE `Categoria`
SET `nome` = CONVERT(BINARY CONVERT(`nome` USING latin1) USING utf8mb4)
WHERE `nome` LIKE '%Ã%';

UPDATE `Categoria`
SET `descricao` = CONVERT(BINARY CONVERT(`descricao` USING latin1) USING utf8mb4)
WHERE `descricao` LIKE '%Ã%';

UPDATE `Usuario`
SET `nome` = CONVERT(BINARY CONVERT(`nome` USING latin1) USING utf8mb4)
WHERE `nome` LIKE '%Ã%';

UPDATE `Perda`
SET `motivo` = CONVERT(BINARY CONVERT(`motivo` USING latin1) USING utf8mb4)
WHERE `motivo` LIKE '%Ã%';

-- Passo 3: confira o resultado
-- SELECT idProduto, nome, marca FROM Produto;
-- SELECT idCategoria, nome, descricao FROM Categoria;
