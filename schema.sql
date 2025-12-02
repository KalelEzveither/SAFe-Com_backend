-- Tabela 1: USUARIO (Unifica Cliente e Vendedor)
CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    cpf_cnpj VARCHAR(20) UNIQUE NOT NULL,
    telefone VARCHAR(20),
    tipo VARCHAR(20) NOT NULL DEFAULT 'CLIENTE' -- 'CLIENTE' ou 'VENDEDOR'
);

-- Tabela 2: BARRACA (A loja do vendedor)
CREATE TABLE barraca (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    usuario_id INT NOT NULL UNIQUE, -- 1 Vendedor = 1 Barraca
    
    CONSTRAINT fk_barraca_dono FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- Tabela 3: PRODUTO (O item de venda)
CREATE TABLE produto (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10, 2) NOT NULL, -- DECIMAL para dinheiro correto
    imagem_url VARCHAR(255),       -- Link da foto (http://...)
    categoria VARCHAR(50) NOT NULL, -- Ex: 'COMIDA', 'ARTESANATO'
    barraca_id INT NOT NULL,
    
    CONSTRAINT fk_produto_barraca FOREIGN KEY (barraca_id) REFERENCES barraca(id)
);

-- Tabela 4: PEDIDO (Cabeçalho da Compra/Reserva)
CREATE TABLE pedido (
    id SERIAL PRIMARY KEY,
    data_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'ABERTO', -- ABERTO, PAGO, FINALIZADO
    valor_total DECIMAL(10, 2) DEFAULT 0.00,
    comprador_id INT NOT NULL,
    
    CONSTRAINT fk_pedido_comprador FOREIGN KEY (comprador_id) REFERENCES usuario(id)
);

-- Tabela 5: ITEM_PEDIDO (Detalhes do que foi comprado)
CREATE TABLE item_pedido (
    id SERIAL PRIMARY KEY,
    pedido_id INT NOT NULL,
    produto_id INT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10, 2) NOT NULL, -- Preço congelado na hora da compra
    
    CONSTRAINT fk_item_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    CONSTRAINT fk_item_produto FOREIGN KEY (produto_id) REFERENCES produto(id)
);

-- --- 3. DADOS DE TESTE (SEED) ---

-- Criando Usuários
INSERT INTO usuario (nome, email, senha, cpf_cnpj, telefone, tipo) VALUES 
('João Vendedor', 'joao@teste.com', '123', '111.111.111-11', '7799991111', 'VENDEDOR'),
('Maria Cliente', 'maria@teste.com', '123', '222.222.222-22', '7798882222', 'CLIENTE');

-- Criando a Barraca do João
INSERT INTO barraca (