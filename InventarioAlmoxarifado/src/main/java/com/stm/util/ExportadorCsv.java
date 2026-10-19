/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.stm.util;

import com.stm.model.Item;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Exporta o inventário em CSV (separador ";", UTF-8 com BOM), formato que o
 * Excel em português abre direto. O arquivo gerado também pode ser importado de
 * volta: o importador ignora as linhas acima do cabeçalho e a coluna extra.
 *
 * @author laboratorio
 */
public class ExportadorCsv {
    private static final char SEP = ';';

    public void exportar(List<Item> itens, File destino) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');   // BOM: o Excel reconhece os acentos
        linha(sb, "INVENTÁRIO ALMOXARIFADO", "", "", "");
        linha(sb, "Exportado em", DataHora.formatar(LocalDateTime.now()), "", "");
        linha(sb, "Categoria", "Nome", "Qtd.", "Atualizado em");
        for (Item i : itens) {
            linha(sb, i.getCategoria(), i.getNome(), String.valueOf(i.getQuantidade()),
                    DataHora.formatar(i.getAtualizadoEm()));
        }
        Files.writeString(destino.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }

    private void linha(StringBuilder sb, String... campos) {
        for (int k = 0; k < campos.length; k++) {
            if (k > 0) sb.append(SEP);
            sb.append(escapar(campos[k]));
        }
        sb.append("\r\n");
    }

    private String escapar(String valor) {
        if (valor == null) return "";
        if (valor.indexOf(SEP) >= 0 || valor.indexOf('"') >= 0 || valor.indexOf('\n') >= 0 || valor.indexOf('\r') >= 0) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
