-- ===================================================================
-- PROJETO DE BANCO DE DADOS I - UESB
-- SISTEMA: SAFe-Com (Feirinha Digital)
-- VERSÃO FINAL COMPLETA (Com Carrinho e Pagamento)
-- ===================================================================

-- 1. LIMPEZA DO BANCO (RESET TOTAL)
DROP TABLE IF EXISTS pagamento CASCADE;
DROP TABLE IF EXISTS item_pedido CASCADE;
DROP TABLE IF EXISTS pedido CASCADE;
DROP TABLE IF EXISTS carrinho_item CASCADE; -- Tabela nova do carrinho
DROP TABLE IF EXISTS produto CASCADE;
DROP TABLE IF EXISTS barraca_categoria CASCADE;
DROP TABLE IF EXISTS categoria CASCADE;
DROP TABLE IF EXISTS barraca CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

-- ===================================================================
-- 2. CRIAÇÃO DAS TABELAS (DDL)
-- ===================================================================

-- Tabela 1: USUARIO
CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    cpf_cnpj VARCHAR(20) UNIQUE NOT NULL,
    telefone VARCHAR(20),
    tipo VARCHAR(20) NOT NULL DEFAULT 'CLIENTE' -- 'CLIENTE' ou 'VENDEDOR'
);

-- Tabela 2: BARRACA
CREATE TABLE barraca (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    imagem_url TEXT,
    horario_funcionamento VARCHAR(50),
    is_aberta BOOLEAN DEFAULT TRUE,
    usuario_id INT NOT NULL UNIQUE, -- 1 Barraca por Vendedor
    
    CONSTRAINT fk_barraca_dono FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabela 3: CATEGORIA
CREATE TABLE categoria (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

-- Tabela 4: BARRACA_CATEGORIA
CREATE TABLE barraca_categoria (
    barraca_id INT NOT NULL,
    categoria_id INT NOT NULL,
    
    PRIMARY KEY (barraca_id, categoria_id),
    CONSTRAINT fk_bc_barraca FOREIGN KEY (barraca_id) REFERENCES barraca(id) ON DELETE CASCADE,
    CONSTRAINT fk_bc_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id) ON DELETE CASCADE
);

-- Tabela 5: PRODUTO
CREATE TABLE produto (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10, 2) NOT NULL,
    quantidade_estoque INT NOT NULL DEFAULT 0,
    imagem_url TEXT,
    categoria VARCHAR(50),
    barraca_id INT NOT NULL,
    
    CONSTRAINT fk_produto_barraca FOREIGN KEY (barraca_id) REFERENCES barraca(id) ON DELETE CASCADE
);

-- Tabela 6: CARRINHO_ITEM (Novo: Persistência do Carrinho)
CREATE TABLE carrinho_item (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL,
    produto_id INT NOT NULL,
    quantidade INT NOT NULL DEFAULT 1,
    
    CONSTRAINT fk_carrinho_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT fk_carrinho_produto FOREIGN KEY (produto_id) REFERENCES produto(id) ON DELETE CASCADE,
    CONSTRAINT uq_carrinho_item UNIQUE (usuario_id, produto_id) -- Evita duplicatas
);

-- Tabela 7: PEDIDO (Atualizado com barraca_id)
CREATE TABLE pedido (
    id SERIAL PRIMARY KEY,
    data_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'AGUARDANDO_PAGAMENTO',
    tipo_entrega VARCHAR(20) DEFAULT 'RETIRADA',
    valor_total DECIMAL(10, 2) DEFAULT 0.00,
    
    comprador_id INT NOT NULL,
    barraca_id INT NOT NULL, -- Vínculo direto para facilitar queries do vendedor
    
    CONSTRAINT fk_pedido_comprador FOREIGN KEY (comprador_id) REFERENCES usuario(id),
    CONSTRAINT fk_pedido_barraca FOREIGN KEY (barraca_id) REFERENCES barraca(id)
);

-- Tabela 8: ITEM_PEDIDO
CREATE TABLE item_pedido (
    id SERIAL PRIMARY KEY,
    pedido_id INT NOT NULL,
    produto_id INT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10, 2) NOT NULL,
    
    CONSTRAINT fk_item_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_produto FOREIGN KEY (produto_id) REFERENCES produto(id)
);

-- Tabela 9: PAGAMENTO (Novo: Financeiro)
CREATE TABLE pagamento (
    id SERIAL PRIMARY KEY,
    pedido_id INT NOT NULL UNIQUE,
    metodo VARCHAR(50) NOT NULL,   -- 'PIX', 'DINHEIRO'
    status VARCHAR(50) DEFAULT 'PENDENTE',
    data_pagamento TIMESTAMP,
    troco_para DECIMAL(10,2),
    comprovante_url TEXT,
    
    CONSTRAINT fk_pagamento_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE
);

-- Inserindo as categorias padrão obrigatórias

INSERT INTO categoria (nome) VALUES 

('Hortifruti'),
('Doces'),
('Temperos'),
('Artesanato'),
('Salgados');