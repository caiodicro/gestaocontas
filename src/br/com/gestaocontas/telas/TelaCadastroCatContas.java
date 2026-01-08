package br.com.gestaocontas.telas;

import br.com.gestaocontas.dal.Conexao;
import br.com.gestaocontas.models.Categorias;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;

/**
 *
 * @author caio
 */

public class TelaCadastroCatContas extends javax.swing.JInternalFrame {

    /**
     * Creates new form TelaCadastroCatContas
     */
    public TelaCadastroCatContas() {
        initComponents();
    }

    // MÉTODO PARA LIMPAR CAMPOS DO FORMULÁRIO
    private void limpar_campos() {
        btnCadCategoria.setEnabled(true);
        btnAltCategoria.setEnabled(false);
        cboStatusCategoria.setSelectedItem("Ativo");
        txtPesquisar.setText(null);
        txtNomeCategoria.setText(null);
        ((DefaultTableModel) tblCategoria.getModel()).setRowCount(0);
    }

    // MÉTODO PARA REALIZAR BUSCA AVANÇADA NA TABELA DE CATEGORIAS CADASTRADOS
    private void pesquisar_categoria() {
        String sql = "SELECT id_categoria_conta AS Código, nome_categoria_conta AS Categoria, status_categoria AS Status FROM tb_categoria_contas WHERE nome_categoria_conta LIKE ?";

        try (Connection conexao = Conexao.conectar()) {
            PreparedStatement pstPesq = conexao.prepareStatement(sql);
            pstPesq.setString(1, txtPesquisar.getText() + "%");
            ResultSet rs = pstPesq.executeQuery();
            tblCategoria.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar Categoria, verifique conexão do Banco de Dados:\n" + e.getMessage());
        }
    }

    // MÉTODO PARA SETAR CAMPOS DA TABELA NO FORMULÁRIO
    private void setar_categoria() {
        int set = tblCategoria.getSelectedRow();
        if (set >= 0) {
            btnCadCategoria.setEnabled(false);
            btnAltCategoria.setEnabled(true);
            txtNomeCategoria.setText(tblCategoria.getValueAt(set, 1).toString());
            cboStatusCategoria.setSelectedItem(tblCategoria.getValueAt(set, 2).toString());
        }
    }

    // MÉTODO PARA VALIDAR CAMPOS DA CATEGORIA
    private boolean validar_campos_categoria() {

        if (txtNomeCategoria.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Preencha o nome da Categoria!");
            txtNomeCategoria.requestFocus();
            return false;
        }

        if (cboStatusCategoria.getSelectedItem() == null
                || cboStatusCategoria.getSelectedItem().toString().trim().isEmpty()
                || cboStatusCategoria.getSelectedItem().equals("<Selecione>")) {
            JOptionPane.showMessageDialog(null, "Selecione o status da Categoria!");
            return false;
        }

        return true;
    }

    // MÉTODO PARA INSERIR UMA CATEGORIA NO BANCO DE DADOS
    private void criar_categoria() {

        String sqlInsert = "INSERT INTO tb_categoria_contas(nome_categoria_conta, status_categoria) VALUES (?, ?)";
        String sqlLog = "INSERT INTO tb_log_cad_categorias(id_log_usuario, operacao_log, descricao_log) VALUES (?, 'Criado', ?)";

        int confirma = JOptionPane.showConfirmDialog(null, "Deseja criar Categoria '" + txtNomeCategoria.getText() + "'?", "Atenção!", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            if (!validar_campos_categoria()) {
                return;
            }
            Connection conexao = null;
            try {
                conexao = Conexao.conectar();
                conexao.setAutoCommit(false);

                Categorias categoria = new Categorias(
                        txtNomeCategoria.getText().trim(),
                        cboStatusCategoria.getSelectedItem().toString()
                );

                int inserido;
                try (PreparedStatement pstInsert = conexao.prepareStatement(sqlInsert)) {
                    pstInsert.setString(1, categoria.getNome());
                    pstInsert.setString(2, categoria.getStatus());
                    inserido = pstInsert.executeUpdate();
                }

                if (inserido > 0) {
                    try (PreparedStatement pstLog = conexao.prepareStatement(sqlLog)) {
                        pstLog.setString(1, TelaMenuPrincipal.lblUsuarioLogado.getText());
                        pstLog.setString(2, categoria.getNome() + " / " + categoria.getStatus());
                        pstLog.executeUpdate();
                    }
                }
                conexao.commit();
                JOptionPane.showMessageDialog(null, "Categoria criada com sucesso!");
                limpar_campos();

            } catch (SQLException e) {
                try {
                    if (conexao != null) {
                        conexao.rollback();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(null, "Erro ao criar Categoria: " + e.getMessage());

            } finally {
                try {
                    if (conexao != null) {
                        conexao.setAutoCommit(true);
                        conexao.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // MÉTODO PARA ALTERAR UMA CATEGORIA NO BANCO DE DADOS
    private void editar_categoria() {

        String sqlAlterar = "UPDATE tb_categoria_contas SET nome_categoria_conta=?, status_categoria=? WHERE id_categoria_conta=?";
        String sqlBusca = "SELECT nome_categoria_conta, status_categoria FROM tb_categoria_contas WHERE id_categoria_conta=?";
        String sqlLog = "INSERT INTO tb_log_cad_categorias(id_log_usuario, operacao_log, descricao_log) VALUES (?, 'Alterado', ?)";

        int linhaSelecionada = tblCategoria.getSelectedRow();

        int confirma = JOptionPane.showConfirmDialog(null, "Deseja alterar a Categoria '" + txtNomeCategoria.getText() + "'?", "Atenção!", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            if (!validar_campos_categoria()) {
                return;
            }
            Connection conexao = null;
            try {
                conexao = Conexao.conectar();
                conexao.setAutoCommit(false);

                int idCategoria = Integer.parseInt(tblCategoria.getValueAt(linhaSelecionada, 0).toString());

                Categorias categoria = new Categorias(
                        txtNomeCategoria.getText().trim(),
                        cboStatusCategoria.getSelectedItem().toString()
                );
                categoria.setId(idCategoria);

                String antigoNome = "";
                String antigoStatus = "";

                try (PreparedStatement pstBusca = conexao.prepareStatement(sqlBusca)) {
                    pstBusca.setInt(1, categoria.getId());
                    try (ResultSet rs = pstBusca.executeQuery()) {
                        if (rs.next()) {
                            antigoNome = rs.getString("nome_categoria_conta");
                            antigoStatus = rs.getString("status_categoria");
                        }
                    }
                }

                int alterado;
                try (PreparedStatement pstUpdate = conexao.prepareStatement(sqlAlterar)) {
                    pstUpdate.setString(1, categoria.getNome());
                    pstUpdate.setString(2, categoria.getStatus());
                    pstUpdate.setInt(3, categoria.getId());
                    alterado = pstUpdate.executeUpdate();
                }

                if (alterado > 0) {
                    String descricao
                            = "Nome: " + antigoNome + " -> " + categoria.getNome() + " | "
                            + "Status: " + antigoStatus + " -> " + categoria.getStatus();

                    try (PreparedStatement pstLog = conexao.prepareStatement(sqlLog)) {
                        pstLog.setString(1, TelaMenuPrincipal.lblUsuarioLogado.getText());
                        pstLog.setString(2, descricao);
                        pstLog.executeUpdate();
                    }
                }

                conexao.commit();
                JOptionPane.showMessageDialog(null, "Categoria alterada com sucesso!");
                limpar_campos();

            } catch (SQLException e) {
                try {
                    if (conexao != null) {
                        conexao.rollback();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(null, "Erro ao alterar Categoria: " + e.getMessage());

            } finally {
                try {
                    if (conexao != null) {
                        conexao.setAutoCommit(true);
                        conexao.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelFundo = new javax.swing.JPanel();
        lblPesquisar = new javax.swing.JLabel();
        txtPesquisar = new javax.swing.JTextField();
        lblLupa = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCategoria = new javax.swing.JTable();
        lblCategoria = new javax.swing.JLabel();
        txtNomeCategoria = new javax.swing.JTextField();
        btnCadCategoria = new javax.swing.JButton();
        btnAltCategoria = new javax.swing.JButton();
        cboStatusCategoria = new javax.swing.JComboBox<>();
        btnLimpar = new javax.swing.JButton();
        lblCadCatContas = new javax.swing.JLabel();

        setClosable(true);
        setTitle("Gestão Contas 1.0 - Cadastro Categoria de Contas");

        panelFundo.setBackground(new java.awt.Color(255, 255, 255));

        lblPesquisar.setFont(new java.awt.Font("Fira Sans", 1, 15)); // NOI18N
        lblPesquisar.setText("Pesquisar");

        txtPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesquisarKeyReleased(evt);
            }
        });

        lblLupa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/gestaocontas/icones/pesquisar_64px.png"))); // NOI18N

        tblCategoria.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Categoria", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblCategoria.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCategoriaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblCategoria);

        lblCategoria.setFont(new java.awt.Font("Fira Sans", 1, 15)); // NOI18N
        lblCategoria.setText("Categoria");

        btnCadCategoria.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/gestaocontas/icones/salvar_64px.png"))); // NOI18N
        btnCadCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCadCategoriaActionPerformed(evt);
            }
        });

        btnAltCategoria.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/gestaocontas/icones/editar_64px.png"))); // NOI18N
        btnAltCategoria.setEnabled(false);
        btnAltCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAltCategoriaActionPerformed(evt);
            }
        });

        cboStatusCategoria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione", "Ativo", "Inativo" }));

        btnLimpar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/gestaocontas/icones/limpar_48px.png"))); // NOI18N
        btnLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparActionPerformed(evt);
            }
        });

        lblCadCatContas.setFont(new java.awt.Font("Fira Sans", 0, 25)); // NOI18N
        lblCadCatContas.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCadCatContas.setText("CADASTRO DE CATEGORIA DE CONTAS");
        lblCadCatContas.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout panelFundoLayout = new javax.swing.GroupLayout(panelFundo);
        panelFundo.setLayout(panelFundoLayout);
        panelFundoLayout.setHorizontalGroup(
            panelFundoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFundoLayout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(panelFundoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelFundoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(panelFundoLayout.createSequentialGroup()
                            .addComponent(lblPesquisar)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(lblLupa))
                        .addGroup(panelFundoLayout.createSequentialGroup()
                            .addComponent(btnCadCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnAltCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(panelFundoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(btnLimpar)
                                .addComponent(cboStatusCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelFundoLayout.createSequentialGroup()
                        .addComponent(lblCategoria)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNomeCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblCadCatContas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(45, Short.MAX_VALUE))
        );
        panelFundoLayout.setVerticalGroup(
            panelFundoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFundoLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblCadCatContas, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelFundoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelFundoLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(panelFundoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblPesquisar)
                            .addComponent(txtPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblLupa))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(panelFundoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNomeCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCategoria))
                .addGap(19, 19, 19)
                .addGroup(panelFundoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelFundoLayout.createSequentialGroup()
                        .addComponent(cboStatusCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnLimpar))
                    .addComponent(btnAltCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCadCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFundo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelFundo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setBounds(0, 0, 560, 500);
    }// </editor-fold>//GEN-END:initComponents

    private void txtPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesquisarKeyReleased
        pesquisar_categoria();
    }//GEN-LAST:event_txtPesquisarKeyReleased

    private void tblCategoriaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCategoriaMouseClicked
        setar_categoria();
    }//GEN-LAST:event_tblCategoriaMouseClicked

    private void btnCadCategoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCadCategoriaActionPerformed
        criar_categoria();
    }//GEN-LAST:event_btnCadCategoriaActionPerformed

    private void btnAltCategoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAltCategoriaActionPerformed
        editar_categoria();
    }//GEN-LAST:event_btnAltCategoriaActionPerformed

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        limpar_campos();
    }//GEN-LAST:event_btnLimparActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAltCategoria;
    private javax.swing.JButton btnCadCategoria;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JComboBox<String> cboStatusCategoria;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCadCatContas;
    private javax.swing.JLabel lblCategoria;
    private javax.swing.JLabel lblLupa;
    private javax.swing.JLabel lblPesquisar;
    private javax.swing.JPanel panelFundo;
    private javax.swing.JTable tblCategoria;
    private javax.swing.JTextField txtNomeCategoria;
    private javax.swing.JTextField txtPesquisar;
    // End of variables declaration//GEN-END:variables
}
