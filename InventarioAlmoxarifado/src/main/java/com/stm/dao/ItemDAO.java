/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.stm.dao;

import com.stm.model.Item;
import com.stm.util.ConnectionFactory;
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
    // Importa ou atualiza vários itens numa única transação
    public void salvarLote(List<Item> itens) throws SQLException {
        String sql = "INSERT INTO item (categoria, nome, quantidade) VALUES (?,?,?) "
                   + "ON DUPLICATE KEY UPDATE quantidade = VALUES(quantidade)";
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

    public List<Item> listar(String filtro) throws SQLException {
        String sql = "SELECT id, categoria, nome, quantidade FROM item "
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
                    lista.add(i);
                }
            }
        }
        return lista;
    }

    public void atualizarQuantidade(int id, int quantidade) throws SQLException {
        String sql = "UPDATE item SET quantidade = ? WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quantidade);
            ps.setInt(2, id);
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
