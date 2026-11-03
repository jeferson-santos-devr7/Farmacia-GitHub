-- Sistema Farmácia: MySQL 8.0.16 ou superior (recomendado MySQL 8.4).
-- Execute em uma instalação nova. Não substitui tabelas antigas incompatíveis.
CREATE DATABASE IF NOT EXISTS farmacia_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE farmacia_db;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(50) NOT NULL,
    senha VARCHAR(255) NOT NULL COMMENT 'Hash PBKDF2 SHA256, nunca senha em texto puro',
    CONSTRAINT uk_usuarios_login UNIQUE (login),
    CONSTRAINT ck_usuarios_login CHECK (CHAR_LENGTH(TRIM(login)) BETWEEN 3 AND 50)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS remedios (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    fabricante VARCHAR(100) NOT NULL,
    preco DECIMAL(10,2) NOT NULL,
    quantidade INT NOT NULL,
    CONSTRAINT ck_remedios_nome CHECK (CHAR_LENGTH(TRIM(nome)) > 0),
    CONSTRAINT ck_remedios_fabricante CHECK (CHAR_LENGTH(TRIM(fabricante)) > 0),
    CONSTRAINT ck_remedios_preco CHECK (preco >= 0),
    CONSTRAINT ck_remedios_quantidade CHECK (quantidade >= 0)
) ENGINE=InnoDB;

-- Acesso didático previsto no enunciado: admin / 123.
-- Uma nova execução NÃO redefine a senha de um usuário já existente.
INSERT INTO usuarios (login, senha)
SELECT 'admin', 'pbkdf2_sha256$600000$RmFybWFjaWFBdWxhMjAyNg==$BEWMHUeXHMskqVPK2+YAahKz9H0LBnftTQFqBmzOJvw='
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE login = 'admin');
