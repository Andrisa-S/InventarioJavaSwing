/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.stm.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * O SQLite guarda CURRENT_TIMESTAMP em UTC. Aqui a data/hora é convertida
 * para o horário do computador (ex.: Brasília) na tela e na exportação.
 *
 * @author laboratorio
 */
public class DataHora {
    private static final DateTimeFormatter FORMATO_BANCO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMATO_EXIBICAO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private DataHora() {}

    // "2026-10-16 17:30:00" (UTC, vindo do banco) -> 16/10/2026 14:30:00 no horário local
    public static LocalDateTime bancoParaLocal(String utc) {
        if (utc == null || utc.isBlank()) return null;
        try {
            OffsetDateTime odt = LocalDateTime.parse(utc.trim(), FORMATO_BANCO).atOffset(ZoneOffset.UTC);
            return odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String formatar(LocalDateTime data) {
        return data == null ? "" : data.format(FORMATO_EXIBICAO);
    }
}
