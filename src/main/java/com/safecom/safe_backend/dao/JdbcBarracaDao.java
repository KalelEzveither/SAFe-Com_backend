package com.safecom.safe_backend.dao;

import com.safecom.safe_backend.model.Barraca;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcBarracaDao implements BarracaDao {

    private final ConnectionFactory connectionFactory;

    public JdbcBarracaDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @PostConstruct
    public void init() {
        // Garante que a tabela existe ao iniciar
        try (Connection conn = connectionFactory.getConnection(); Statement st = conn.createStatement()) {
            // Cria tabela Barraca (Atualizada)
            st.execute("CREATE TABLE IF NOT EXISTS barraca (" +
                    "id SERIAL PRIMARY KEY, " +
                    "nome VARCHAR(100) NOT NULL, " +
                    "descricao TEXT, " +
                    "usuario_id INT NOT NULL UNIQUE, " +
                    "imagem_url VARCHAR(255), " +
                    "horario_funcionamento VARCHAR(50), " +
                    "is_aberta BOOLEAN DEFAULT TRUE, " +
                    "CONSTRAINT fk_barraca_dono FOREIGN KEY (usuario_id) REFERENCES usuario(id))");
            
            // Cria tabela de Categorias Fixas (se não existir)
            st.execute("CREATE TABLE IF NOT EXISTS categoria (id SERIAL PRIMARY KEY, nome VARCHAR(50) NOT NULL UNIQUE)");
            
            // Cria tabela de Ligação (Barraca <-> Categoria)
            st.execute("CREATE TABLE IF NOT EXISTS barraca_categoria (" +
                    "barraca_id INT NOT NULL, " +
                    "categoria_id INT NOT NULL, " +
                    "PRIMARY KEY (barraca_id, categoria_id), " +
                    "CONSTRAINT fk_bc_barraca FOREIGN KEY (barraca_id) REFERENCES barraca(id), " +
                    "CONSTRAINT fk_bc_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id))");
                    
        } catch (SQLException e) {
            System.err.println("Warning: could not ensure tables exist: " + e.getMessage());
        }
    }

    @Override
    public List<Barraca> findAll() {
        List<Barraca> list = new ArrayList<>();
        // Trazendo todos os campos novos
        String sql = "SELECT id, nome, descricao, usuario_id, imagem_url, horario_funcionamento, is_aberta FROM barraca";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Optional<Barraca> findById(long id) {
        String sql = "SELECT id, nome, descricao, usuario_id, imagem_url, horario_funcionamento, is_aberta FROM barraca WHERE id = ?";
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
    public Optional<Barraca> findByUsuarioId(long usuarioId) {
        String sql = "SELECT id, nome, descricao, usuario_id, imagem_url, horario_funcionamento, is_aberta FROM barraca WHERE usuario_id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Barraca create(Barraca barraca) {
        // SQL corrigido com todos os 6 campos
        String sqlBarraca = "INSERT INTO barraca (nome, descricao, usuario_id, is_aberta, imagem_url, horario_funcionamento) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlCategoria = "INSERT INTO barraca_categoria (barraca_id, categoria_id) VALUES (?, ?)";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = connectionFactory.getConnection();
            conn.setAutoCommit(false); // <--- O PULO DO GATO: Inicia Transação

            // 1. Inserir a Barraca
            ps = conn.prepareStatement(sqlBarraca, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, barraca.getNome());
            ps.setString(2, barraca.getDescricao());
            ps.setLong(3, barraca.getUsuarioId());
            ps.setBoolean(4, barraca.getIsAberta() != null ? barraca.getIsAberta() : true);
            ps.setString(5, barraca.getImagemUrl());
            ps.setString(6, barraca.getHorarioFuncionamento());

            ps.executeUpdate();

            // Pegar o ID gerado
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    barraca.setId(keys.getLong(1));
                }
            }

            // 2. Inserir Categorias (Se houver)
            // Aqui ele pega a lista [1, 5] que veio do Flutter e salva na tabela auxiliar
            if (barraca.getCategoriaIds() != null && !barraca.getCategoriaIds().isEmpty()) {
                try (PreparedStatement psCat = conn.prepareStatement(sqlCategoria)) {
                    for (Integer catId : barraca.getCategoriaIds()) {
                        psCat.setLong(1, barraca.getId());
                        psCat.setInt(2, catId);
                        psCat.addBatch(); // Prepara o lote
                    }
                    psCat.executeBatch(); // Executa o lote
                }
            }

            conn.commit(); // <--- CONFIRMA TUDO
            return barraca;

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw new RuntimeException("Erro ao criar barraca: " + e.getMessage(), e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) {
                    conn.setAutoCommit(true); // Devolve a conexão ao estado normal
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean update(Barraca barraca) {
        // Update simplificado (sem mexer nas categorias por enquanto para ganhar tempo)
        String sql = "UPDATE barraca SET nome = ?, descricao = ?, imagem_url = ?, horario_funcionamento = ?, is_aberta = ? WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barraca.getNome());
            ps.setString(2, barraca.getDescricao());
            ps.setString(3, barraca.getImagemUrl());
            ps.setString(4, barraca.getHorarioFuncionamento());
            ps.setBoolean(5, barraca.getIsAberta());
            ps.setLong(6, barraca.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(long id) {
        // Primeiro apaga as categorias, depois a barraca (por causa da chave estrangeira)
        // Mas como definimos ON DELETE CASCADE no banco (se você rodou o script), basta apagar a barraca.
        String sql = "DELETE FROM barraca WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Barraca mapRow(ResultSet rs) throws SQLException {
        Barraca b = new Barraca();
        b.setId(rs.getLong("id"));
        b.setNome(rs.getString("nome"));
        b.setDescricao(rs.getString("descricao"));
        b.setUsuarioId(rs.getLong("usuario_id"));
        
        // Mapeando os novos campos
        b.setIsAberta(rs.getBoolean("is_aberta"));
        b.setImagemUrl(rs.getString("imagem_url"));
        b.setHorarioFuncionamento(rs.getString("horario_funcionamento"));
        
        // Obs: Não estamos carregando a lista de categorias aqui no findAll() para não pesar.
        // Se precisar exibir as categorias na tela, podemos fazer um método separado depois.
        return b;
    }
}