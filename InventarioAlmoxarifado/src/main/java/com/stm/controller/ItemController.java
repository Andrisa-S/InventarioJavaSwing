/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.stm.controller;

import com.stm.dao.ItemDAO;
import com.stm.model.Item;
import com.stm.util.ExportadorCsv;
import com.stm.util.ImportadorPlanilha;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author laboratorio
 */
public class ItemController {
    private final ItemDAO dao = new ItemDAO();

    public int importarPlanilha(File arquivo) throws Exception {
        List<Item> itens = new ImportadorPlanilha().ler(arquivo);
        dao.salvarLote(itens);
        return itens.size();
    }

    // Exporta o inventário inteiro (ignora o filtro de busca da tela)
    public int exportarCsv(File destino) throws Exception {
        List<Item> itens = dao.listar("");
        new ExportadorCsv().exportar(itens, destino);
        return itens.size();
    }

    public List<Item> listar(String filtro) throws Exception {
        return dao.listar(filtro == null ? "" : filtro);
    }

    public List<String> listarCategorias() throws Exception {
        return dao.listarCategorias();
    }

    public void adicionarItem(String categoria, String nome, int quantidade) throws Exception {
        categoria = limpar(categoria);
        nome = limpar(nome);
        validar(categoria, nome, quantidade);
        try {
            dao.inserir(categoria, nome, quantidade);
        } catch (SQLException e) {
            tratarDuplicado(e, categoria, nome);
        }
    }

    public void editarItem(int id, String categoria, String nome, int quantidade) throws Exception {
        categoria = limpar(categoria);
        nome = limpar(nome);
        validar(categoria, nome, quantidade);
        try {
            dao.atualizar(id, categoria, nome, quantidade);
        } catch (SQLException e) {
            tratarDuplicado(e, categoria, nome);
        }
    }

    public void incrementar(int id) throws Exception {
        dao.ajustarQuantidade(id, 1);
    }

    public void decrementar(int id) throws Exception {
        dao.ajustarQuantidade(id, -1);
    }

    public void definirQuantidade(int id, int quantidade) throws Exception {
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser negativa.");
        }
        dao.atualizarQuantidade(id, quantidade);
    }

    public void excluir(int id) throws Exception {
        dao.excluir(id);
    }

    private String limpar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private void validar(String categoria, String nome, int quantidade) {
        if (categoria.isEmpty()) {
            throw new IllegalArgumentException("Informe a categoria.");
        }
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do item.");
        }
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser negativa.");
        }
    }

    // Converte a violação da chave única (Categoria + Nome) em mensagem amigável
    private void tratarDuplicado(SQLException e, String categoria, String nome) throws SQLException {
        if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
            throw new IllegalArgumentException(
                    "Já existe o item \"" + nome + "\" na categoria \"" + categoria + "\".");
        }
        throw e;
    }
}
