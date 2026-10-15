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
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TelaPrincipal extends JFrame {
    private final ItemController controller = new ItemController();
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"Categoria", "Nome", "Qtd."}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tabela = new JTable(modelo);
    private final JTextField campoBusca = new JTextField(25);
    private List<Item> itensExibidos = new ArrayList<>();   // mesma ordem das linhas da tabela

    public TelaPrincipal() {
        super("Inventário do Almoxarifado");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(24);
        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();
        direita.setHorizontalAlignment(SwingConstants.RIGHT);
        tabela.getColumnModel().getColumn(2).setCellRenderer(direita);
        tabela.getColumnModel().getColumn(2).setMaxWidth(90);

        // Topo: busca e importação
        JButton btnBuscar = new JButton("Buscar");
        JButton btnImportar = new JButton("Importar CSV");
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Busca:"));
        topo.add(campoBusca);
        topo.add(btnBuscar);
        topo.add(btnImportar);

        // Rodapé: ações sobre o item selecionado
        JButton btnMenos = new JButton("-");
        JButton btnMais = new JButton("+");
        Font fonteGrande = btnMenos.getFont().deriveFont(Font.BOLD, 18f);
        btnMenos.setFont(fonteGrande);
        btnMais.setFont(fonteGrande);
        btnMenos.setToolTipText("Diminuir 1 unidade do item selecionado");
        btnMais.setToolTipText("Aumentar 1 unidade do item selecionado");
        JButton btnDefinir = new JButton("Definir qtd...");
        JButton btnNovo = new JButton("Novo item");
        JButton btnExcluir = new JButton("Excluir");

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rodape.add(btnMenos);
        rodape.add(btnMais);
        rodape.add(btnDefinir);
        rodape.add(new JLabel("     "));
        rodape.add(btnNovo);
        rodape.add(btnExcluir);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> carregar(null));
        campoBusca.addActionListener(e -> carregar(null));
        btnImportar.addActionListener(e -> importar());
        btnMais.addActionListener(e -> ajustar(1));
        btnMenos.addActionListener(e -> ajustar(-1));
        btnDefinir.addActionListener(e -> definirQuantidade());
        btnNovo.addActionListener(e -> novoItem());
        btnExcluir.addActionListener(e -> excluir());

        carregar(null);
    }

    // Recarrega a tabela; se idSelecionar não for nulo, mantém esse item selecionado
    private void carregar(Integer idSelecionar) {
        try {
            itensExibidos = controller.listar(campoBusca.getText().trim());
            modelo.setRowCount(0);
            int linhaSelecionar = -1;
            for (int r = 0; r < itensExibidos.size(); r++) {
                Item i = itensExibidos.get(r);
                modelo.addRow(new Object[]{i.getCategoria(), i.getNome(), i.getQuantidade()});
                if (idSelecionar != null && i.getId() == idSelecionar) {
                    linhaSelecionar = r;
                }
            }
            if (linhaSelecionar >= 0) {
                tabela.setRowSelectionInterval(linhaSelecionar, linhaSelecionar);
                tabela.scrollRectToVisible(tabela.getCellRect(linhaSelecionar, 0, true));
            }
        } catch (Exception ex) {
            mostrarErro(ex);
        }
    }

    private Item itemSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0 || linha >= itensExibidos.size()) {
            JOptionPane.showMessageDialog(this, "Selecione um item na tabela.", "Atenção", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return itensExibidos.get(linha);
    }

    private void ajustar(int delta) {
        Item item = itemSelecionado();
        if (item == null) return;
        try {
            if (delta > 0) {
                controller.incrementar(item.getId());
            } else {
                controller.decrementar(item.getId());
            }
            carregar(item.getId());
        } catch (Exception ex) {
            mostrarErro(ex);
        }
    }

    private void definirQuantidade() {
        Item item = itemSelecionado();
        if (item == null) return;
        String texto = JOptionPane.showInputDialog(this,
                "Nova quantidade para \"" + item.getNome() + "\":", String.valueOf(item.getQuantidade()));
        if (texto == null) return;
        try {
            int qtd = Integer.parseInt(texto.trim());
            controller.definirQuantidade(item.getId(), qtd);
            carregar(item.getId());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Digite um número inteiro (0 ou mais).", "Atenção", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            mostrarErro(ex);
        }
    }

    private void novoItem() {
        JComboBox<String> cbCategoria = new JComboBox<>();
        cbCategoria.setEditable(true);
        try {
            for (String c : controller.listarCategorias()) {
                cbCategoria.addItem(c);
            }
        } catch (Exception ex) {
            mostrarErro(ex);
            return;
        }
        cbCategoria.setSelectedItem("");
        JTextField tfNome = new JTextField(20);
        JSpinner spQtd = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));

        JPanel painel = new JPanel(new GridLayout(0, 2, 6, 6));
        painel.add(new JLabel("Categoria:"));
        painel.add(cbCategoria);
        painel.add(new JLabel("Nome:"));
        painel.add(tfNome);
        painel.add(new JLabel("Quantidade inicial:"));
        painel.add(spQtd);

        while (true) {
            int opcao = JOptionPane.showConfirmDialog(this, painel, "Novo item",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcao != JOptionPane.OK_OPTION) return;
            try {
                spQtd.commitEdit();
            } catch (ParseException ignorado) {
                // mantém o último valor válido do spinner
            }
            Object digitado = cbCategoria.getEditor().getItem();
            String categoria = digitado == null ? "" : digitado.toString().trim();
            String nome = tfNome.getText().trim();
            int qtd = ((Number) spQtd.getValue()).intValue();
            try {
                controller.adicionarItem(categoria, nome, qtd);
                campoBusca.setText("");   // garante que o novo item apareça na lista
                carregar(null);
                selecionarPorNome(categoria, nome);
                return;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                mostrarErro(ex);
                return;
            }
        }
    }

    private void selecionarPorNome(String categoria, String nome) {
        for (int r = 0; r < itensExibidos.size(); r++) {
            Item i = itensExibidos.get(r);
            if (i.getCategoria().equalsIgnoreCase(categoria) && i.getNome().equalsIgnoreCase(nome)) {
                tabela.setRowSelectionInterval(r, r);
                tabela.scrollRectToVisible(tabela.getCellRect(r, 0, true));
                return;
            }
        }
    }

    private void excluir() {
        Item item = itemSelecionado();
        if (item == null) return;
        int opcao = JOptionPane.showConfirmDialog(this,
                "Excluir \"" + item.getNome() + "\" (" + item.getCategoria() + ")?\nEsta ação não pode ser desfeita.",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcao != JOptionPane.YES_OPTION) return;
        try {
            controller.excluir(item.getId());
            carregar(null);
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
            carregar(null);
        } catch (Exception ex) {
            mostrarErro(ex);
        }
    }

    private void mostrarErro(Exception ex) {
        JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
