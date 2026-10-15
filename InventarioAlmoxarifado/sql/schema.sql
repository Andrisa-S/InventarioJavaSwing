-- Estrutura do banco (SQLite).
-- A aplicação cria esta tabela automaticamente na primeira execução;
-- este arquivo serve apenas como documentação.
CREATE TABLE IF NOT EXISTS item (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    categoria     TEXT NOT NULL COLLATE NOCASE,
    nome          TEXT NOT NULL COLLATE NOCASE,
    quantidade    INTEGER NOT NULL DEFAULT 0 CHECK (quantidade >= 0),
    atualizado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (categoria, nome)
);
