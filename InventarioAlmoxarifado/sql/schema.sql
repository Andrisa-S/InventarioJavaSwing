CREATE DATABASE almoxarifado CHARACTER SET utf8mb4;
USE almoxarifado;
CREATE TABLE item (
	id            INT AUTO_INCREMENT PRIMARY KEY,
	categoria     VARCHAR(60)  NOT NULL,  
	nome          VARCHAR(120) NOT NULL,
	quantidade    INT NOT NULL DEFAULT 0,
	atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	UNIQUE KEY uk_categoria_nome (categoria, nome)
);