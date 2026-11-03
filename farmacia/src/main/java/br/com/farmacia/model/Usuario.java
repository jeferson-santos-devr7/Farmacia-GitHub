package br.com.farmacia.model;

/** A sessão não transporta a senha nem seu hash para a interface. */
public record Usuario(int id, String login) { }
