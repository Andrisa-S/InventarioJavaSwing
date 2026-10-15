/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.stm.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Conexão com o banco SQLite local.
 *
 * O banco é um único arquivo, criado automaticamente na primeira execução em
 * C:\Users\<usuário>\.almoxarifado\almoxarifado.db (fora do projeto e do Git).
 * Para usar outro local, defina db.path em ~\.almoxarifado\db.properties
 * ou a variável de ambiente ALMOX_DB_PATH.
 *
 * @author laboratorio
 */
public class ConnectionFactory {
    private static final Path PASTA = Path.of(System.getProperty("user.home"), ".almoxarifado");
    private static final Properties PROPS = carregar();
    private static boolean esquemaCriado = false;

    private static Properties carregar() {
        Properties p = new Properties();
        Path arq = PASTA.resolve("db.properties");
        if (Files.exists(arq)) {
            try (InputStream in = Files.newInputStream(arq)) {
                p.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Falha ao ler a configuração: " + arq, e);
            }
        }
        return p;
    }

    private static String get(String chave, String variavelAmbiente) {
        String v = System.getenv(variavelAmbiente);
        return v != null ? v : PROPS.getProperty(chave);
    }

    public static Path caminhoBanco() {
        String custom = get("db.path", "ALMOX_DB_PATH");
        Path arquivo = (custom != null && !custom.isBlank()) ? Path.of(custom.trim()) : PASTA.resolve("almoxarifado.db");
        return arquivo.toAbsolutePath();
    }

    public static synchronized Connection getConnection() throws SQLException {
        Path arquivo = caminhoBanco();
        try {
            Files.createDirectories(arquivo.getParent());
        } catch (IOException e) {
            throw new SQLException("Não foi possível criar a pasta do banco: " + arquivo.getParent(), e);
        }
        Connection con = DriverManager.getConnection("jdbc:sqlite:" + arquivo);
        if (!esquemaCriado) {
            criarEsquema(con);
            esquemaCriado = true;
        }
        return con;
    }

    // Cria a tabela na primeira execução (não apaga nada se ela já existir)
    private static void criarEsquema(Connection con) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS item ("
                   + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                   + " categoria TEXT NOT NULL COLLATE NOCASE,"
                   + " nome TEXT NOT NULL COLLATE NOCASE,"
                   + " quantidade INTEGER NOT NULL DEFAULT 0 CHECK (quantidade >= 0),"
                   + " atualizado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                   + " UNIQUE (categoria, nome))";
        try (Statement st = con.createStatement()) {
            st.execute(sql);
        }
    }
}
