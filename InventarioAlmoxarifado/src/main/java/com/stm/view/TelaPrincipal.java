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
import com.stm.util.DataHora;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    // Ação executada quando o usuário confirma o formulário de item (novo ou edição)
    private interface Salvar {
        void executar(String categoria, String nome, int quantidade) throws Exception;
    }

    private final ItemController controller = new ItemController();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Categoria", "Nome", "Qtd.", "Atualizado em"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tabela = new JTable(modelo);
    private final JTextField campoBusca = new JTextField(25);
    private List<Item> itensExibidos = new ArrayList<>();   // mesma ordem das linhas da tabela

    public TelaPrincipal() {
        super("Inventário do Almoxarifado");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(24);
        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();
        direita.setHorizontalAlignment(SwingConstants.RIGHT);
        tabela.getColumnModel().getColumn(2).setCellRenderer(direita);
        tabela.getColumnModel().getColumn(2).setMaxWidth(90);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(3).setMaxWidth(190);
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.getSelectedRow() >= 0) {
                    editar();   // duplo clique edita o item
                }
            }
        });

        // Topo: busca, importação e exportação
        JButton btnBuscar = new JButton("Buscar");
        JButton btnImportar = new JButton("Importar CSV");
        JButton btnExportar = new JButton("Exportar CSV");
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Busca:"));
        topo.add(campoBusca);
        topo.add(btnBuscar);
        topo.add(btnImportar);
        topo.add(btnExportar);

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
        JButton btnEditar = new JButton("Editar");
        btnEditar.setToolTipText("Também funciona com duplo clique na linha");
        JButton btnExcluir = new JButton("Excluir");

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rodape.add(btnMenos);
        rodape.add(btnMais);
        rodape.add(btnDefinir);
        rodape.add(new JLabel("     "));
        rodape.add(btnNovo);
        rodape.add(btnEditar);
        rodape.add(btnExcluir);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> carregar(null));
        campoBusca.addActionListener(e -> carregar(null));
        btnImportar.addActionListener(e -> importar());
        btnExportar.addActionListener(e -> exportar());
        btnMais.addActionListener(e -> ajustar(1));
        btnMenos.addActionListener(e -> ajustar(-1));
        btnDefinir.addActionListener(e -> definirQuantidade());
        btnNovo.addActionListener(e -> novoItem());
        btnEditar.addActionListener(e -> editar());
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
                modelo.addRow(new Object[]{i.getCategoria(), i.getNome(), i.getQuantidade(),
                    DataHora.formatar(i.getAtualizadoEm())});
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

    private boolean estaNaLista(int id) {
        for (Item i : itensExibidos) {
            if (i.getId() == id) return true;
        }
        return false;
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
        String[] criado = new String[2];   // categoria e nome, para selecionar o item depois
        boolean salvou = abrirFormulario("Novo item", null, (categoria, nome, qtd) -> {
            controller.adicionarItem(categoria, nome, qtd);
            criado[0] = categoria;
            criado[1] = nome;
        });
        if (salvou) {
            campoBusca.setText("");   // garante que o novo item apareça na lista
            carregar(null);
            selecionarPorNome(criado[0], criado[1]);
        }
    }

    private void editar() {
        Item item = itemSelecionado();
        if (item == null) return;
        boolean salvou = abrirFormulario("Editar item", item,
                (categoria, nome, qtd) -> controller.editarItem(item.getId(), categoria, nome, qtd));
        if (salvou) {
            carregar(item.getId());
            if (!estaNaLista(item.getId())) {   // o filtro de busca escondeu o item editado
                campoBusca.setText("");
                carregar(item.getId());
            }
        }
    }

    /**
     * Formulário usado para criar e para editar. Se "inicial" for nulo, os campos
     * começam vazios. Repete a janela enquanto houver erro de validação.
     * Retorna true se o item foi salvo.
     */
    private boolean abrirFormulario(String titulo, Item inicial, Salvar salvar) {
        JComboBox<String> cbCategoria = new JComboBox<>();
        cbCategoria.setEditable(true);
        try {
            for (String c : controller.listarCategorias()) {
                cbCategoria.addItem(c);
            }
        } catch (Exception ex) {
            mostrarErro(ex);
            return false;
        }
        cbCategoria.setSelectedItem(inicial == null ? "" : inicial.getCategoria());
        JTextField tfNome = new JTextField(inicial == null ? "" : inicial.getNome(), 20);
        int qtdInicial = inicial == null ? 0 : inicial.getQuantidade();
        JSpinner spQtd = new JSpinner(new SpinnerNumberModel(qtdInicial, 0, Integer.MAX_VALUE, 1));

        JPanel painel = new JPanel(new GridLayout(0, 2, 6, 6));
        painel.add(new JLabel("Categoria:"));
        painel.add(cbCategoria);
        painel.add(new JLabel("Nome:"));
        painel.add(tfNome);
        painel.add(new JLabel(inicial == null ? "Quantidade inicial:" : "Quantidade:"));
        painel.add(spQtd);

        while (true) {
            int opcao = JOptionPane.showConfirmDialog(this, painel, titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcao != JOptionPane.OK_OPTION) return false;
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
                salvar.executar(categoria, nome, qtd);
                return true;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                mostrarErro(ex);
                return false;
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

    private void exportar() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exportar inventário");
        fc.setFileFilter(new FileNameExtensionFilter("Arquivos CSV (*.csv)", "csv"));
        String sugestao = "inventario_almoxarifado_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".csv";
        fc.setSelectedFile(new File(sugestao));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File destino = fc.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".csv")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".csv");
        }
        if (destino.exists()) {
            int opcao = JOptionPane.showConfirmDialog(this,
                    "O arquivo \"" + destino.getName() + "\" já existe. Substituir?",
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opcao != JOptionPane.YES_OPTION) return;
        }
        try {
            int n = controller.exportarCsv(destino);
            JOptionPane.showMessageDialog(this, n + " itens exportados para:\n" + destino.getAbsolutePath());
        } catch (Exception ex) {
            mostrarErro(ex);
        }
    }

    private void mostrarErro(Exception ex) {
        JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
