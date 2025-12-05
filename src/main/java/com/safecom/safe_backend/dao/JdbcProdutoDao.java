package com.safecom.safe_backend.dao;

import com.safecom.safe_backend.model.Produto;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcProdutoDao implements ProdutoDao {

    private final ConnectionFactory connectionFactory;

    public JdbcProdutoDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @PostConstruct
    public void init() {
        try (Connection conn = connectionFactory.getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS produto (id SERIAL PRIMARY KEY, nome VARCHAR(100) NOT NULL, descricao TEXT, preco DECIMAL(10,2) NOT NULL, imagem_url VARCHAR(255), categoria VARCHAR(50) NOT NULL, barraca_id INT NOT NULL, CONSTRAINT fk_produto_barraca FOREIGN KEY (barraca_id) REFERENCES barraca(id))");
        } catch (SQLException e) {
            System.err.println("Warning: could not initialize produto table (DB may be unavailable): " + e.getMessage());
        }
    }

    @Override
    public List<Produto> findAll() {
        List<Produto> list = new ArrayList<>();
        String sql = "SELECT id, nome, descricao, preco, imagem_url, categoria, barraca_id FROM produto ORDER BY id";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Optional<Produto> findById(long id) {
        String sql = "SELECT id, nome, descricao, preco, imagem_url, categoria, barraca_id FROM produto WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Produto create(Produto produto) {
        String sql = "INSERT INTO produto (nome, descricao, preco, imagem_url, categoria, barraca_id, quantidade_estoque) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, produto.getNome());
            ps.setString(2, produto.getDescricao());
            ps.setBigDecimal(3, produto.getPreco());
            ps.setString(4, produto.getImagemUrl());
            ps.setString(5, produto.getCategoria());
            
            if (produto.getBarracaId() != null) {
                ps.setLong(6, produto.getBarracaId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.setInt(7, produto.getQuantidadeEstoque() != null ? produto.getQuantidadeEstoque() : 0);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) produto.setId(keys.getLong(1));
            }
            return produto;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(Produto produto) {
        String sql = "UPDATE produto SET nome = ?, descricao = ?, preco = ?, imagem_url = ?, categoria = ?, barraca_id = ?, quantidade_estoque = ? WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, produto.getNome());
            ps.setString(2, produto.getDescricao());
            ps.setBigDecimal(3, produto.getPreco());
            ps.setString(4, produto.getImagemUrl());
            ps.setString(5, produto.getCategoria());
            
            if (produto.getBarracaId() != null) {
                ps.setLong(6, produto.getBarracaId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.setInt(7, produto.getQuantidadeEstoque() != null ? produto.getQuantidadeEstoque() : 0);
            
            ps.setLong(8, produto.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(long id) {
        String sql = "DELETE FROM produto WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean baixarEstoque(long produtoId, int quantidadeComprada) {
        // O SQL tem uma cláusula 'AND' extra para garantir que não negativamos o estoque
        String sql = "UPDATE produto SET quantidade_estoque = quantidade_estoque - ? WHERE id = ? AND quantidade_estoque >= ?";

        try (Connection conn = connectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantidadeComprada); // Quanto vai subtrair
            ps.setLong(2, produtoId);         // Qual produto
            ps.setInt(3, quantidadeComprada); // Verifica se o saldo atual >= quantidadeComprada

            int linhasAfetadas = ps.executeUpdate();

            // Se linhasAfetadas > 0, significa que o update funcionou (tinha estoque).
            // Se for 0, significa que o produto não existe OU o estoque era insuficiente.
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao baixar estoque do produto " + produtoId, e);
        }
    }

    private Produto mapRow(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getLong("id"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setPreco(rs.getBigDecimal("preco"));
        p.setImagemUrl(rs.getString("imagem_url"));
        p.setCategoria(rs.getString("categoria"));
        p.setQuantidadeEstoque(rs.getInt("quantidade_estoque"));
        long bId = rs.getLong("barraca_id");
        
        if (!rs.wasNull()) p.setBarracaId(bId);
        return p;
    }

    @Override
    public List<Produto> findByBarracaId(long barracaId) {
        List<Produto> list = new ArrayList<>();
        // SQL filtrando pela FK barraca_id
        String sql = "SELECT id, nome, descricao, preco, imagem_url, categoria, quantidade_estoque, barraca_id FROM produto WHERE barraca_id = ?";

        try (Connection conn = connectionFactory.getConnection(); 
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Substitui o ? pelo ID da barraca que veio por parâmetro
            ps.setLong(1, barracaId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Reutiliza seu método mapRow para converter o ResultSet em Objeto
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produtos da barraca " + barracaId, e);
        }
        return list;
    }
}
