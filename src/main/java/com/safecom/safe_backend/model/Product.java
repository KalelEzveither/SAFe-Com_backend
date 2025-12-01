package com.safecom.safe_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Cria Getters, Setters, toString, Equals automaticamente
@AllArgsConstructor // Cria construtor com tudo
@NoArgsConstructor  // Cria construtor vazio
public class Product {
    private Long id;
    private String name;
    private String description;
    private double price;
    private int quantity;
}