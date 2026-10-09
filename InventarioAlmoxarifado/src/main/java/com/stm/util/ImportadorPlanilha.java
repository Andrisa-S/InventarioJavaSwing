/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.stm.util;

/**
 *
 * @author laboratorio
 */
import com.stm.model.Item;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImportadorPlanilha {

    public List<Item> ler(File arquivo) throws IOException {
        List<String> linhas = lerLinhas(arquivo);
        char sep = detectarSeparador(linhas);
        List<Item> itens = new ArrayList<>();
        boolean achouCabecalho = false;

        for (String linha : linhas) {
            if (linha.isBlank()) continue;
            List<String> c = dividir(linha, sep);

            if (!achouCabecalho) {
                if (c.get(0).equalsIgnoreCase("categoria")) achouCabecalho = true;
                continue;   // ignora o título e o próprio cabeçalho
            }

            String categoria = c.get(0);
            String nome = c.size() > 1 ? c.get(1) : "";
            if (categoria.isEmpty() || nome.isEmpty()) continue;

            String q = c.size() > 2 ? c.get(2) : "";
            int qtd;
            try {
                qtd = q.isEmpty() ? 0 : (int) Double.parseDouble(q.replace(",", "."));
            } catch (NumberFormatException e) {
                throw new IOException("Quantidade inválida para \"" + nome + "\": " + q);
            }

            Item i = new Item();
            i.setCategoria(categoria);
            i.setNome(nome);
            i.setQuantidade(qtd);
            itens.add(i);
        }

        if (!achouCabecalho) {
            throw new IOException("Cabeçalho 'Categoria / Nome / Qtd.' não encontrado no CSV.");
        }
        return itens;
    }

    private List<String> lerLinhas(File arquivo) throws IOException {
        byte[] bytes = Files.readAllBytes(arquivo.toPath());
        String texto;
        try {
            texto = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            texto = new String(bytes, Charset.forName("windows-1252"));
        }
        if (texto.startsWith("\uFEFF")) texto = texto.substring(1);   // remove BOM
        return Arrays.asList(texto.split("\\R"));
    }

    private char detectarSeparador(List<String> linhas) {
        for (String l : linhas) {
            if (l.toLowerCase().contains("categoria")) {
                return l.contains(";") ? ';' : ',';
            }
        }
        return ';';
    }

    private List<String> dividir(String linha, char sep) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean aspas = false;
        for (int i = 0; i < linha.length(); i++) {
            char ch = linha.charAt(i);
            if (ch == '"') {
                if (aspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    atual.append('"');
                    i++;
                } else {
                    aspas = !aspas;
                }
            } else if (ch == sep && !aspas) {
                campos.add(atual.toString().trim());
                atual.setLength(0);
            } else {
                atual.append(ch);
            }
        }
        campos.add(atual.toString().trim());
        return campos;
    }
}
