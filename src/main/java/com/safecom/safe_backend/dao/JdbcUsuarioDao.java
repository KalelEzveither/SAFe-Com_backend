package com.safecom.safe_backend.dao;

import com.safecom.safe_backend.model.Usuario;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.sql.*;
import java.util.Optional;

@Repository
public class JdbcUsuarioDao implements UsuarioDao {

    private final ConnectionFactory connectionFactory;

    public JdbcUsuarioDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @PostConstruct
    public void init() {
        try (Connection conn = connectionFactory.getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS usuario (id SERIAL PRIMARY KEY, nome VARCHAR(100) NOT NULL, email VARCHAR(100) UNIQUE NOT NULL, senha VARCHAR(255) NOT NULL, cpf_cnpj VARCHAR(20) UNIQUE NOT NULL, telefone VARCHAR(20), tipo VARCHAR(20) NOT NULL DEFAULT 'CLIENTE')");
        } catch (SQLException e) {
            System.err.println("Warning: could not ensure usuario table exists: " + e.getMessage());
        }
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        String sql = "SELECT id, nome, email, senha, cpf_cnpj, telefone, tipo FROM usuario WHERE email = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> findById(long id) {
        String sql = "SELECT id, nome, email, senha, cpf_cnpj, telefone, tipo FROM usuario WHERE id = ?";
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
    public Usuario create(Usuario usuario) {
        String sql = "INSERT INTO usuario (nome, email, senha, cpf_cnpj, telefone, tipo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getSenha());
            ps.setString(4, usuario.getCpfCnpj());
            ps.setString(5, usuario.getTelefone());
            ps.setString(6, usuario.getTipo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) usuario.setId(keys.getLong(1));
            }
            return usuario;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNome(rs.getString("nome"));
        u.setEmail(rs.getString("email"));
        u.setSenha(rs.getString("senha"));
        u.setCpfCnpj(rs.getString("cpf_cnpj"));
        u.setTelefone(rs.getString("telefone"));
        u.setTipo(rs.getString("tipo"));
        return u;
    }
}
