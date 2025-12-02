package com.safecom.safe_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Cria Getters, Setters, toString, Equals automaticamente
@AllArgsConstructor // Cria construtor com tudo
@NoArgsConstructor  // Cria construtor vazio
public class Usuario {
    private Long id;
    private String nome;
    private String email;
    private String senha; // hashed
    private String cpfCnpj;
    private String telefone;
    private String tipo; // CLIENTE or VENDEDOR
}
