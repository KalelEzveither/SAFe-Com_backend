package com.safecom.safe_backend.dao;

import com.safecom.safe_backend.model.ItemCarrinho;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcCarrinhoDao {

    private final ConnectionFactory connectionFactory;

    public JdbcCarrinhoDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public List<ItemCarrinho> listarPorUsuario(long usuarioId) {
        List<ItemCarrinho> lista = new ArrayList<>();
        // Fazemos JOIN para o Front já receber o nome e preço do produto
        String sql = "SELECT c.id, c.usuario_id, c.produto_id, c.quantidade, " +
                     "p.nome, p.preco, p.imagem_url, p.barraca_id, b.nome as nome_barraca " +
                     "FROM carrinho_item c " +
                     "JOIN produto p ON c.produto_id = p.id " +
                     "JOIN barraca b ON p.barraca_id = b.id " +
                     "WHERE c.usuario_id = ?";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemCarrinho item = new ItemCarrinho();
                    item.setId(rs.getLong("id"));
                    item.setUsuarioId(rs.getLong("usuario_id"));
                    item.setProdutoId(rs.getLong("produto_id"));
                    item.setQuantidade(rs.getInt("quantidade"));
                    
                    // Dados extras
                    item.setNomeProduto(rs.getString("nome"));
                    item.setPrecoUnitario(rs.getDouble("preco"));
                    item.setImagemUrl(rs.getString("imagem_url"));
                    item.setBarracaId(rs.getLong("barraca_id"));
                    item.setNomeBarraca(rs.getString("nome_barraca"));
                    
                    lista.add(item);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    public String adicionarItem(long usuarioId, long produtoId, int quantidade) {
        try (Connection conn = connectionFactory.getConnection()) {
            
            // 1. Descobrir a barraca do NOVO produto
            long novaBarracaId = 0;
            String sqlBarraca = "SELECT barraca_id FROM produto WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlBarraca)) {
                ps.setLong(1, produtoId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) novaBarracaId = rs.getLong("barraca_id");
            }

            // 2. Verificar se o carrinho já tem produtos de OUTRA barraca
            String sqlCheck = "SELECT COUNT(*) FROM carrinho_item c " +
                              "JOIN produto p ON c.produto_id = p.id " +
                              "WHERE c.usuario_id = ? AND p.barraca_id <> ?";
            
            try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                ps.setLong(1, usuarioId);
                ps.setLong(2, novaBarracaId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return "ERRO_BARRACA_DIFERENTE"; // Retorna código de erro
                }
            }

            // 3. Se passou, Insere ou Atualiza (Upsert)
            // Tenta Insert primeiro
            String sqlInsert = "INSERT INTO carrinho_item (usuario_id, produto_id, quantidade) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setLong(1, usuarioId);
                ps.setLong(2, produtoId);
                ps.setInt(3, quantidade);
                ps.executeUpdate();
            } catch (SQLException e) {
                // Se der erro de Unique (já existe), faz Update somando
                String sqlUpdate = "UPDATE carrinho_item SET quantidade = quantidade + ? WHERE usuario_id = ? AND produto_id = ?";
                try (PreparedStatement psUp = conn.prepareStatement(sqlUpdate)) {
                    psUp.setInt(1, quantidade);
                    psUp.setLong(2, usuarioId);
                    psUp.setLong(3, produtoId);
                    psUp.executeUpdate();
                }
            }
            return "SUCESSO";

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removerItem(long id) {
        String sql = "DELETE FROM carrinho_item WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void limparCarrinho(long usuarioId) {
        String sql = "DELETE FROM carrinho_item WHERE usuario_id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}