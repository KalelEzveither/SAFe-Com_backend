package com.safecom.safe_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor  
public class Usuario {
    private Long id;
    private String nome;
    private String email;
    private String senha; 
    private String cpfCnpj;
    private String telefone;
    private String tipo;
}
