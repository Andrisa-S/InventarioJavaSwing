/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.stm.controller;

import com.stm.dao.ItemDAO;
import com.stm.model.Item;
import com.stm.util.ImportadorPlanilha;
import java.io.File;
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

    public List<Item> listar(String filtro) throws Exception {
        return dao.listar(filtro == null ? "" : filtro);
    }

    public void atualizarQuantidade(int id, int quantidade) throws Exception {
        dao.atualizarQuantidade(id, quantidade);
    }

    public void excluir(int id) throws Exception {
        dao.excluir(id);
    }
}
