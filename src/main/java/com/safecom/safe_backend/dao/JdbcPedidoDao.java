package com.safecom.safe_backend.dao;

import com.safecom.safe_backend.controller.dto.PedidoRequest;
import com.safecom.safe_backend.controller.dto.PedidoResponse;
import com.safecom.safe_backend.model.ItemCarrinho;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcPedidoDao {

    private final ConnectionFactory connectionFactory;
    private final JdbcCarrinhoDao carrinhoDao; // Reutilizamos para buscar o carrinho

    public JdbcPedidoDao(ConnectionFactory connectionFactory, JdbcCarrinhoDao carrinhoDao) {
        this.connectionFactory = connectionFactory;
        this.carrinhoDao = carrinhoDao;
    }

    public String criarPedido(PedidoRequest request) {
        Connection conn = null;
        try {
            conn = connectionFactory.getConnection();
            conn.setAutoCommit(false); // <--- INÍCIO DA TRANSAÇÃO

            // 1. Buscar itens do carrinho
            List<ItemCarrinho> itens = carrinhoDao.listarPorUsuario(request.getUsuarioId());
            if (itens.isEmpty()) {
                return "CARRINHO_VAZIO";
            }

            // Calcular total e pegar ID da barraca (regra de barraca única)
            double valorTotal = itens.stream().mapToDouble(i -> i.getPrecoUnitario() * i.getQuantidade()).sum();
            Long barracaId = itens.get(0).getBarracaId();

            // 2. Criar o PEDIDO (Cabeçalho) - Status ajustado para RESERVADO
            String sqlPedido = "INSERT INTO pedido (comprador_id, barraca_id, status, tipo_entrega, valor_total, data_pedido) VALUES (?, ?, 'AGUARDANDO_RETIRADA', ?, ?, NOW())";
            long pedidoId;
            
            try (PreparedStatement ps = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, request.getUsuarioId());
                ps.setLong(2, barracaId);
                ps.setString(3, request.getTipoEntrega());
                ps.setDouble(4, valorTotal);
                ps.executeUpdate();
                
                var rs = ps.getGeneratedKeys();
                if (rs.next()) pedidoId = rs.getLong(1);
                else throw new SQLException("Falha ao criar pedido.");
            }

            // 3. Salvar ITENS e Baixar ESTOQUE
            String sqlItem = "INSERT INTO item_pedido (pedido_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";
            String sqlEstoque = "UPDATE produto SET quantidade_estoque = quantidade_estoque - ? WHERE id = ? AND quantidade_estoque >= ?";

            try (PreparedStatement psItem = conn.prepareStatement(sqlItem);
                 PreparedStatement psEstoque = conn.prepareStatement(sqlEstoque)) {
                
                for (ItemCarrinho item : itens) {
                    // Insere Item
                    psItem.setLong(1, pedidoId);
                    psItem.setLong(2, item.getProdutoId());
                    psItem.setInt(3, item.getQuantidade());
                    psItem.setDouble(4, item.getPrecoUnitario());
                    psItem.executeUpdate();

                    // Baixa Estoque
                    psEstoque.setInt(1, item.getQuantidade());
                    psEstoque.setLong(2, item.getProdutoId());
                    psEstoque.setInt(3, item.getQuantidade()); // Validação extra no WHERE
                    int updated = psEstoque.executeUpdate();
                    
                    if (updated == 0) {
                        conn.rollback(); // Cancela tudo se faltou estoque
                        return "ESTOQUE_INSUFICIENTE: " + item.getNomeProduto();
                    }
                }
            }

            // 4. Registrar a "Promessa de Pagamento"
            String sqlPag = "INSERT INTO pagamento (pedido_id, metodo, status, troco_para) VALUES (?, ?, 'PENDENTE', ?)";
            try (PreparedStatement psPag = conn.prepareStatement(sqlPag)) {
                psPag.setLong(1, pedidoId);
                psPag.setString(2, request.getMetodoPagamento());
                if (request.getTrocoPara() != null) psPag.setDouble(3, request.getTrocoPara());
                else psPag.setNull(3, Types.DOUBLE);
                psPag.executeUpdate();
            }

            // 5. Limpar o Carrinho
            String sqlLimpar = "DELETE FROM carrinho_item WHERE usuario_id = ?";
            try (PreparedStatement psLimpar = conn.prepareStatement(sqlLimpar)) {
                psLimpar.setLong(1, request.getUsuarioId());
                psLimpar.executeUpdate();
            }

            conn.commit(); // <--- CONFIRMA A TRANSAÇÃO (Sucesso!)
            return "SUCESSO";

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return "ERRO: " + e.getMessage();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
    // Listar Pedidos do CLIENTE
    public List<PedidoResponse> listarPorComprador(Long compradorId) {
        String sql = "SELECT p.id, p.data_pedido, p.status, p.tipo_entrega, p.valor_total, " +
                     "b.nome as nome_barraca, " +
                     "STRING_AGG(CONCAT(ip.quantidade, 'x ', prod.nome), ', ') as resumo_itens " +
                     "FROM pedido p " +
                     "JOIN barraca b ON p.barraca_id = b.id " +
                     "JOIN item_pedido ip ON p.id = ip.pedido_id " +
                     "JOIN produto prod ON ip.produto_id = prod.id " +
                     "WHERE p.comprador_id = ? " +
                     "GROUP BY p.id, b.nome " +
                     "ORDER BY p.data_pedido DESC";
        
        return executarQueryPedidos(sql, compradorId);
    }

    // Listar Pedidos da BARRACA (Vendedor)
    public List<PedidoResponse> listarPorBarraca(Long barracaId) {
        String sql = "SELECT p.id, p.data_pedido, p.status, p.tipo_entrega, p.valor_total, " +
                     "u.nome as nome_cliente, " + // Aqui pegamos o nome do cliente
                     "STRING_AGG(CONCAT(ip.quantidade, 'x ', prod.nome), ', ') as resumo_itens " +
                     "FROM pedido p " +
                     "JOIN usuario u ON p.comprador_id = u.id " +
                     "JOIN item_pedido ip ON p.id = ip.pedido_id " +
                     "JOIN produto prod ON ip.produto_id = prod.id " +
                     "WHERE p.barraca_id = ? " +
                     "GROUP BY p.id, u.nome " +
                     "ORDER BY p.data_pedido DESC";
        
        return executarQueryPedidos(sql, barracaId);
    }

    // Atualizar Status (Aceitar, Finalizar, Cancelar)
    public boolean atualizarStatus(Long pedidoId, String novoStatus) {
        String sql = "UPDATE pedido SET status = ? WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, novoStatus);
            ps.setLong(2, pedidoId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Método auxiliar para evitar repetir código
    private List<PedidoResponse> executarQueryPedidos(String sql, Long idParam) {
        List<PedidoResponse> lista = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idParam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PedidoResponse p = new PedidoResponse();
                    p.setId(rs.getLong("id"));
                    p.setDataPedido(rs.getTimestamp("data_pedido").toLocalDateTime());
                    p.setStatus(rs.getString("status"));
                    p.setTipoEntrega(rs.getString("tipo_entrega"));
                    p.setValorTotal(rs.getBigDecimal("valor_total"));
                    // A coluna 6 é dinâmica (nome_barraca ou nome_cliente)
                    p.setNomeOutraParte(rs.getString(6)); 
                    p.setResumoItens(rs.getString("resumo_itens"));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }
}