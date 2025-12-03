package com.safecom.safe_backend.service;

import com.safecom.safe_backend.dao.ConnectionFactory;
import com.safecom.safe_backend.dao.UsuarioDao;
import com.safecom.safe_backend.model.Barraca;
import com.safecom.safe_backend.model.Usuario;
import com.safecom.safe_backend.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioDao usuarioDao;
    private final ConnectionFactory connectionFactory;

    public AuthService(UsuarioDao usuarioDao, ConnectionFactory connectionFactory) {
        this.usuarioDao = usuarioDao;
        this.connectionFactory = connectionFactory;
    }
    
    public Usuario register(Usuario usuario) {
        // hash password before saving
        usuario.setSenha(PasswordUtil.hash(usuario.getSenha()));
        // default tipo
        if (usuario.getTipo() == null) usuario.setTipo("CLIENTE");
        return usuarioDao.create(usuario);
    }

    public Optional<Usuario> login(String email, String password) {
        Optional<Usuario> found = usuarioDao.findByEmail(email);
        if (found.isEmpty()) return Optional.empty();
        Usuario u = found.get();
        String hashed = PasswordUtil.hash(password);
        if (u.getSenha() != null && u.getSenha().equals(hashed)) {
            return Optional.of(u);
        }
        return Optional.empty();
    }

    public void registrarFeiranteCompleto(Usuario usuario, Barraca barraca) {
        Connection conn = null;
        PreparedStatement psUser = null;
        PreparedStatement psBarraca = null;
        PreparedStatement psCat = null;

        try {
            conn = connectionFactory.getConnection();
            conn.setAutoCommit(false); // INÍCIO DA TRANSAÇÃO

            // Inserir Usuário
            String sqlUser = "INSERT INTO usuario (nome, email, senha, cpf_cnpj, telefone, tipo) VALUES (?, ?, ?, ?, ?, 'VENDEDOR') RETURNING id";
            psUser = conn.prepareStatement(sqlUser);
            psUser.setString(1, usuario.getNome());
            psUser.setString(2, usuario.getEmail());
            psUser.setString(3, PasswordUtil.hash(usuario.getSenha())); // Hash
            psUser.setString(4, usuario.getCpfCnpj());
            psUser.setString(5, usuario.getTelefone());
            
            ResultSet rsUser = psUser.executeQuery();
            Long userId = null;
            if (rsUser.next()) userId = rsUser.getLong(1);
            else throw new SQLException("Falha ao criar usuário");

            // Inserir Barraca (vinculada ao ID do usuário)
            String sqlBarraca = "INSERT INTO barraca (nome, descricao, usuario_id, is_aberta, imagem_url, horario_funcionamento) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
            psBarraca = conn.prepareStatement(sqlBarraca);
            psBarraca.setString(1, barraca.getNome());
            psBarraca.setString(2, barraca.getDescricao());
            psBarraca.setLong(3, userId);
            psBarraca.setBoolean(4, true);
            psBarraca.setString(5, barraca.getImagemUrl()); // Base64 
            psBarraca.setString(6, barraca.getHorarioFuncionamento());
            
            ResultSet rsBarraca = psBarraca.executeQuery();
            Long barracaId = null;
            if(rsBarraca.next()) barracaId = rsBarraca.getLong(1);

            // Inserir Categorias (Se houver)
            if (barraca.getCategoriaIds() != null && !barraca.getCategoriaIds().isEmpty()) {
                String sqlCat = "INSERT INTO barraca_categoria (barraca_id, categoria_id) VALUES (?, ?)";
                psCat = conn.prepareStatement(sqlCat);
                for (Integer catId : barraca.getCategoriaIds()) {
                    psCat.setLong(1, barracaId);
                    psCat.setInt(2, catId);
                    psCat.addBatch();
                }
                psCat.executeBatch();
            }

            conn.commit();

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw new RuntimeException("Erro na transação de feirante: " + e.getMessage());
        } finally {
            // Feche psUser, psBarraca, psCat e conn manualmente aqui para evitar memory leak
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
    }
}
