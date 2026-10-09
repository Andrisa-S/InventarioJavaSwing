/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.stm.view;

/**
 *
 * @author laboratorio
 */
import com.stm.controller.ItemController;
import com.stm.model.Item;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class TelaPrincipal extends JFrame {
    private final ItemController controller = new ItemController();
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"Categoria", "Nome", "Qtd."}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTextField campoBusca = new JTextField(25);

    public TelaPrincipal() {
        super("Inventário do Almoxarifado");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 450);
        setLocationRelativeTo(null);

        JButton btnBuscar = new JButton("Buscar");
        JButton btnImportar = new JButton("Importar CSV");

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Busca:"));
        topo.add(campoBusca);
        topo.add(btnBuscar);
        topo.add(btnImportar);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> carregar());
        campoBusca.addActionListener(e -> carregar());
        btnImportar.addActionListener(e -> importar());

        carregar();
    }

    private void carregar() {
        try {
            modelo.setRowCount(0);
            for (Item i : controller.listar(campoBusca.getText().trim())) {
                modelo.addRow(new Object[]{i.getCategoria(), i.getNome(), i.getQuantidade()});
            }
        } catch (Exception ex) {
            mostrarErro(ex);
        }
    }

    private void importar() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Arquivos CSV (*.csv)", "csv"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            int n = controller.importarPlanilha(fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, n + " itens importados/atualizados.");
            carregar();
        } catch (Exception ex) {
            mostrarErro(ex);
        }
    }

    private void mostrarErro(Exception ex) {
        JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
