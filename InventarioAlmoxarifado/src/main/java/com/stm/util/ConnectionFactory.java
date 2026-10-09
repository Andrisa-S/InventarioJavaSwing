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
import java.util.Properties;

/**
 *
 * @author laboratorio
 */
public class ConnectionFactory {
    private static final Properties PROPS = carregar();

    private static Properties carregar() {
        Properties p = new Properties();
        Path arq = Path.of(System.getProperty("user.home"), ".almoxarifado", "db.properties");
        if (Files.exists(arq)) {
            try (InputStream in = Files.newInputStream(arq)) {
                p.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Falha ao ler a configuração do banco", e);
            }
        }
        return p;
    }

    private static String get(String chave, String variavelAmbiente) {
        String v = System.getenv(variavelAmbiente);
        return v != null ? v : PROPS.getProperty(chave);
    }

    public static Connection getConnection() throws SQLException {
        String url = get("db.url", "ALMOX_DB_URL");
        if (url == null) {
            throw new SQLException("Configuração do banco não encontrada (db.properties).");
        }
        return DriverManager.getConnection(url,
                get("db.user", "ALMOX_DB_USER"),
                get("db.password", "ALMOX_DB_PASSWORD"));
    }
}
