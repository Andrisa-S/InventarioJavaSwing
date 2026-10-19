/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.stm.dao;

import com.stm.model.Item;
import com.stm.util.ConnectionFactory;
import com.stm.util.DataHora;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author laboratorio
 */
public class ItemDAO {

    // Importa ou atualiza vários itens numa única transação.
    // A data/hora só muda nos itens cuja quantidade realmente mudou.
    public void salvarLote(List<Item> itens) throws SQLException {
        String sql = "INSERT INTO item (categoria, nome, quantidade) VALUES (?,?,?) "
                   + "ON CONFLICT(categoria, nome) DO UPDATE SET "
                   + "quantidade = excluded.quantidade, atualizado_em = CURRENT_TIMESTAMP "
                   + "WHERE quantidade <> excluded.quantidade";
        try (Connection con = ConnectionFactory.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Item i : itens) {
                    ps.setString(1, i.getCategoria());
                    ps.setString(2, i.getNome());
                    ps.setInt(3, i.getQuantidade());
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // Cadastra um item novo (falha se Categoria + Nome já existirem)
    public void inserir(String categoria, String nome, int quantidade) throws SQLException {
        String sql = "INSERT INTO item (categoria, nome, quantidade) VALUES (?,?,?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, categoria);
            ps.setString(2, nome);
            ps.setInt(3, quantidade);
            ps.executeUpdate();
        }
    }

    // Edita categoria, nome e quantidade (falha se o novo Categoria + Nome já existir em outro item)
    public void atualizar(int id, String categoria, String nome, int quantidade) throws SQLException {
        String sql = "UPDATE item SET categoria = ?, nome = ?, quantidade = ?, "
                   + "atualizado_em = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, categoria);
            ps.setString(2, nome);
            ps.setInt(3, quantidade);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    public List<Item> listar(String filtro) throws SQLException {
        String sql = "SELECT id, categoria, nome, quantidade, atualizado_em FROM item "
                   + "WHERE categoria LIKE ? OR nome LIKE ? ORDER BY categoria, nome";
        List<Item> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String f = "%" + filtro + "%";
            ps.setString(1, f);
            ps.setString(2, f);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Item i = new Item();
                    i.setId(rs.getInt("id"));
                    i.setCategoria(rs.getString("categoria"));
                    i.setNome(rs.getString("nome"));
                    i.setQuantidade(rs.getInt("quantidade"));
                    i.setAtualizadoEm(DataHora.bancoParaLocal(rs.getString("atualizado_em")));
                    lista.add(i);
                }
            }
        }
        return lista;
    }

    public List<String> listarCategorias() throws SQLException {
        String sql = "SELECT DISTINCT categoria FROM item ORDER BY categoria";
        List<String> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(rs.getString(1));
            }
        }
        return lista;
    }

    // Soma (delta positivo) ou subtrai (delta negativo) direto no banco; nunca fica abaixo de 0.
    // Se a quantidade não muda (ex.: já está em 0 e o delta é -1), a data/hora também não muda.
    public void ajustarQuantidade(int id, int delta) throws SQLException {
        String sql = "UPDATE item SET quantidade = MAX(quantidade + ?, 0), "
                   + "atualizado_em = CURRENT_TIMESTAMP "
                   + "WHERE id = ? AND MAX(quantidade + ?, 0) <> quantidade";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, id);
            ps.setInt(3, delta);
            ps.executeUpdate();
        }
    }

    public void atualizarQuantidade(int id, int quantidade) throws SQLException {
        String sql = "UPDATE item SET quantidade = ?, atualizado_em = CURRENT_TIMESTAMP "
                   + "WHERE id = ? AND quantidade <> ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quantidade);
            ps.setInt(2, id);
            ps.setInt(3, quantidade);
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM item WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
