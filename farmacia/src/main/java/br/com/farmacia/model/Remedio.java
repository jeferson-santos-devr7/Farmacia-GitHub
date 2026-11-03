package br.com.farmacia.model;

import java.math.BigDecimal;

public record Remedio(int id, String nome, String fabricante, BigDecimal preco, int quantidade) { }
