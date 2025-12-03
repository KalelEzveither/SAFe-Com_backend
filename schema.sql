-- ===================================================================
-- PROJETO DE BANCO DE DADOS I - UESB
-- SISTEMA: SAFe-Com (Feirinha Digital)
-- AUTORES: Equipe SAFe-Com
-- ===================================================================

-- 1. LIMPEZA DO BANCO (RESET)
-- Remove as tabelas antigas para recriar a estrutura do zero
DROP TABLE IF EXISTS item_pedido CASCADE;
DROP TABLE IF EXISTS pedido CASCADE;
DROP TABLE IF EXISTS produto CASCADE;
DROP TABLE IF EXISTS barraca_categoria CASCADE;
DROP TABLE IF EXISTS categoria CASCADE;
DROP TABLE IF EXISTS barraca CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

-- ===================================================================
-- 2. CRIAÇÃO DAS TABELAS (DDL)
-- ===================================================================

-- Tabela 1: USUARIO (Unifica Cliente e Vendedor)
CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    cpf_cnpj VARCHAR(20) UNIQUE NOT NULL,
    telefone VARCHAR(20),
    tipo VARCHAR(20) NOT NULL DEFAULT 'CLIENTE' -- Valores: 'CLIENTE' ou 'VENDEDOR'
);

-- Tabela 2: BARRACA (Loja do Vendedor)
-- Relacionamento 1:1 com Usuário (Vendedor)
CREATE TABLE barraca (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    imagem_url TEXT,                -- Foto salva em base64
    horario_funcionamento VARCHAR(50),        -- Ex: "12:00 às 14:00"
    is_aberta BOOLEAN DEFAULT TRUE,           -- Status: Aberto (TRUE) ou Fechado (FALSE)
    usuario_id INT NOT NULL UNIQUE,           -- UNIQUE garante a regra de 1 barraca por vendedor
    
    CONSTRAINT fk_barraca_dono FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabela 3: CATEGORIA (Categorias fixas do sistema)
CREATE TABLE categoria (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

-- Tabela 4: BARRACA_CATEGORIA (Tabela Associativa N:N)
-- Uma barraca pode ter várias categorias (ex: Lanches e Bebidas)
CREATE TABLE barraca_categoria (
    barraca_id INT NOT NULL,
    categoria_id INT NOT NULL,
    
    PRIMARY KEY (barraca_id, categoria_id),
    CONSTRAINT fk_bc_barraca FOREIGN KEY (barraca_id) REFERENCES barraca(id) ON DELETE CASCADE,
    CONSTRAINT fk_bc_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id) ON DELETE CASCADE
);

-- Tabela 5: PRODUTO (Itens vendidos na barraca)
CREATE TABLE produto (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10, 2) NOT NULL,
    quantidade_estoque INT NOT NULL DEFAULT 0, -- Controle de estoque simples
    imagem_url TEXT, -- base64
    categoria VARCHAR(50),                     -- Categoria específica do produto (opcional)
    barraca_id INT NOT NULL,
    
    CONSTRAINT fk_produto_barraca FOREIGN KEY (barraca_id) REFERENCES barraca(id) ON DELETE CASCADE
);

-- Tabela 6: PEDIDO (Cabeçalho da Transação)
CREATE TABLE pedido (
    id SERIAL PRIMARY KEY,
    data_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'ABERTO',       -- Estados: ABERTO, PAGO, FINALIZADO, CANCELADO
    tipo_entrega VARCHAR(20) DEFAULT 'RETIRADA', -- Valores: RETIRADA, ENTREGA
    valor_total DECIMAL(10, 2) DEFAULT 0.00,
    comprador_id INT NOT NULL,
    
    CONSTRAINT fk_pedido_comprador FOREIGN KEY (comprador_id) REFERENCES usuario(id)
);

-- Tabela 7: ITEM_PEDIDO (Detalhes dos itens comprados)
CREATE TABLE item_pedido (
    id SERIAL PRIMARY KEY,
    pedido_id INT NOT NULL,
    produto_id INT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10, 2) NOT NULL,    -- Preço congelado no momento da compra
    
    CONSTRAINT fk_item_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_produto FOREIGN KEY (produto_id) REFERENCES produto(id)
);

-- ===================================================================
-- 3. CARGA DE DADOS INICIAIS (DML / SEED)
-- ===================================================================

-- Inserindo as categorias padrão obrigatórias
INSERT INTO categoria (nome) VALUES 
('Hortifruti'),
('Doces'),
('Temperos'),
('Artesanato'),
('Salgados');

-- Usuário Admin/Teste para facilitar a apresentação
INSERT INTO usuario (nome, email, senha, cpf_cnpj, telefone, tipo) VALUES 
('Admin Teste', 'admin@safe.com', '123456', '000.000.000-00', '77999999999', 'CLIENTE');