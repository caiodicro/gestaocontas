package br.com.gestaocontas.telas;

import br.com.gestaocontas.dal.Conexao;
import br.com.gestaocontas.models.Usuarios;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;

/**
 *
 * @author caio
 */
public class TelaCadastroUsuario extends javax.swing.JInternalFrame {

    /**
     * Creates new form TelaCadastroUsuario
     */
    public TelaCadastroUsuario() {
        initComponents();
        txtNome.requestFocus();
    }

    // MÉTODO PARA LIMPAR CAMPOS DO FORMULÁRIO
    private void limpar_campos() {
        txtID.setText(null);
        txtNome.setText(null);
        txtLogin.setText(null);
        txtSenha.setText(null);
        txtConfirmaSenha.setText(null);
        txtContato.setText(null);
        txtObs.setText(null);
        cboStatus.setSelectedItem("Selecione");
        cboGrupo.setSelectedItem("Selecione");
        txtPesquisar.setText(null);
        btnSalvar.setEnabled(true);
        btnEditar.setEnabled(false);
        ((DefaultTableModel) tblUsuarios.getModel()).setRowCount(0);
    }

// MÉTODO PARA VALIDAR CAMPOS OBRIGATÓRIOS DO USUÁRIO
    private boolean validar_campos_usuario() {

        if (txtNome.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Preencha o nome do Usuário!");
            txtNome.requestFocus();
            return false;
        }

        if (txtLogin.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Preencha o login do Usuário!");
            txtLogin.requestFocus();
            return false;
        }

        if (txtSenha.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Senha não pode ser vazia!");
            txtSenha.requestFocus();
            return false;
        }

        if (!txtSenha.getText().equals(txtConfirmaSenha.getText())) {
            JOptionPane.showMessageDialog(null, "As senhas não coincidem!");
            txtConfirmaSenha.requestFocus();
            return false;
        }

        if (txtContato.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Preencha o contato do Usuário!");
            txtContato.requestFocus();
            return false;
        }

        if (cboStatus.getSelectedItem().equals("<Selecione>")) {
            JOptionPane.showMessageDialog(null, "Selecione o Status do Usuário!");
            return false;
        }

        if (cboGrupo.getSelectedItem().equals("<Selecione>")) {
            JOptionPane.showMessageDialog(null, "Selecione o Grupo do Usuário!");
            return false;
        }
        return true;
    }

    // MÉTODO PARA INSERIR UM USUÁRIO NO BANCO DE DADOS
    private void criar_usuario() {
        String sqlVerifica = "SELECT COUNT(*) FROM tb_usuarios WHERE login_usuario = ?";
        String sqlInsere = "INSERT INTO tb_usuarios(login_usuario, nome_usuario, senha_usuario, contato_usuario, status_usuario, grupo_usuario, obs_usuario) VALUES (?,?,?,?,?,?,?)";
        String sqlLog = "INSERT INTO tb_log_cad_usuario(id_log_usuario, id_usuario_alterado, operacao_log, descricao_log) VALUES (?, ?, 'Criado', ?)";

        int confirma = JOptionPane.showConfirmDialog(null, "Deseja criar um novo usuário?", "Atenção!", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            Connection conexao = null;
            try {
                conexao = Conexao.conectar();
                if (!validar_campos_usuario()) {
                    return;
                }

                try (PreparedStatement pstCheck = conexao.prepareStatement(sqlVerifica)) {
                    pstCheck.setString(1, txtLogin.getText());
                    try (ResultSet rs = pstCheck.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(null, "Já existe um usuário com este login!");
                            txtLogin.requestFocus();
                            return;
                        }
                    }
                }
                conexao.setAutoCommit(false);
                Usuarios usuario = new Usuarios(
                        txtNome.getText(),
                        txtLogin.getText(),
                        txtSenha.getText(),
                        cboGrupo.getSelectedItem().toString(),
                        txtContato.getText(),
                        cboStatus.getSelectedItem().toString(),
                        txtObs.getText());

                try (PreparedStatement pstInsereUsuario = conexao.prepareStatement(sqlInsere, Statement.RETURN_GENERATED_KEYS)) {
                    pstInsereUsuario.setString(1, usuario.getUsuario());
                    pstInsereUsuario.setString(2, usuario.getNome());
                    pstInsereUsuario.setString(3, usuario.getSenha());
                    pstInsereUsuario.setString(4, usuario.getContato());
                    pstInsereUsuario.setString(5, usuario.getStatus());
                    pstInsereUsuario.setString(6, usuario.getGrupo());
                    pstInsereUsuario.setString(7, usuario.getObs());

                    int usuarioCadastrado = pstInsereUsuario.executeUpdate();
                    if (usuarioCadastrado > 0) {
                        try (ResultSet rs = pstInsereUsuario.getGeneratedKeys()) {
                            if (rs.next()) {
                                int idUsuarioGerado = rs.getInt(1);
                                String descricao
                                        = "Nome: " + usuario.getNome() + " | "
                                        + "Login: " + usuario.getUsuario() + " | "
                                        + "Contato: " + usuario.getContato() + " | "
                                        + "Status: " + usuario.getStatus() + " | "
                                        + "Grupo: " + usuario.getGrupo() + " | "
                                        + "Obs: " + usuario.getObs();
                                try (PreparedStatement pstInsereLog = conexao.prepareStatement(sqlLog)) {
                                    pstInsereLog.setString(1, TelaMenuPrincipal.lblUsuarioLogado.getText());
                                    pstInsereLog.setInt(2, idUsuarioGerado);
                                    pstInsereLog.setString(3, descricao);
                                    pstInsereLog.executeUpdate();
                                }
                            }
                        }
                    }
                }
                conexao.commit();
                JOptionPane.showMessageDialog(null, "Usuário Cadastrado com sucesso!");
                limpar_campos();

            } catch (SQLException e) {
                try {
                    if (conexao != null) {
                        conexao.rollback();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(null, "Erro ao cadastrar Usuário: " + e.getMessage());

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

    // MÉTODO PARA PESQUISAR USUÁRIO
    private void pesquisar_usuario() {
        String sql = "SELECT id_usuario AS ID, nome_usuario AS Usuário, login_usuario AS Login, grupo_usuario AS Grupo, status_usuario AS Status, contato_usuario AS Contato, obs_usuario AS Obs "
                + "FROM tb_usuarios WHERE nome_usuario LIKE ?";

        try (Connection conexao = Conexao.conectar()) {
            PreparedStatement pstPesq = conexao.prepareStatement(sql);
            pstPesq.setString(1, txtPesquisar.getText() + "%");
            ResultSet rs = pstPesq.executeQuery();
            tblUsuarios.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar usuário, verifique conexão do Banco de Dados:\n" + e.getMessage());
        }
    }

    // MÉTODO PARA SETAR CAMPOS DA TABELA NO FORMULÁRIO
    private void setar_usuario() {
        int set = tblUsuarios.getSelectedRow();

        if (set >= 0) {
            btnSalvar.setEnabled(false);
            btnEditar.setEnabled(true);

            txtID.setText(tblUsuarios.getValueAt(set, 0).toString());
            txtNome.setText(tblUsuarios.getValueAt(set, 1).toString());
            txtLogin.setText(tblUsuarios.getValueAt(set, 2).toString());
            cboGrupo.setSelectedItem(tblUsuarios.getValueAt(set, 3).toString());
            cboStatus.setSelectedItem(tblUsuarios.getValueAt(set, 4).toString());
            txtContato.setText(tblUsuarios.getValueAt(set, 5).toString());
            txtObs.setText(tblUsuarios.getValueAt(set, 6).toString());

            try (Connection conexao = Conexao.conectar()) {
                String sqlSenha = "SELECT senha_usuario FROM tb_usuarios WHERE login_usuario = ?";
                String login = tblUsuarios.getValueAt(set, 2).toString();
                try (PreparedStatement pst = conexao.prepareStatement(sqlSenha)) {
                    pst.setString(1, login);
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            txtSenha.setText(rs.getString("senha_usuario"));
                            txtConfirmaSenha.setText(rs.getString("senha_usuario"));
                        }
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro ao buscar todos os dados do usuário: " + e.getMessage());
            }
        }
    }

// MÉTODO PARA EDITAR UM USUÁRIO NO BANCO DE DADOS
    private void editar_usuario() {
        String sqlVerifica = "SELECT COUNT(*) FROM tb_usuarios WHERE login_usuario = ? AND id_usuario <> ?";
        String sqlBusca = "SELECT login_usuario, nome_usuario, contato_usuario, status_usuario, grupo_usuario, obs_usuario FROM tb_usuarios WHERE id_usuario = ?";
        String sqlUpdate = "UPDATE tb_usuarios SET login_usuario=?, nome_usuario=?, senha_usuario=?, contato_usuario=?, status_usuario=?, grupo_usuario=?, obs_usuario=? WHERE id_usuario=?";
        String sqlLog = "INSERT INTO tb_log_cad_usuario(id_log_usuario, id_usuario_alterado, operacao_log, descricao_log) VALUES (?, ?, 'Alterado', ?)";

        int confirma = JOptionPane.showConfirmDialog(null, "Deseja alterar o usuário selecionado?", "Atenção!", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            Connection conexao = null;
            if (!validar_campos_usuario()) {
                return;
            }
            try {
                conexao = Conexao.conectar();
                try (PreparedStatement pstCheck = conexao.prepareStatement(sqlVerifica)) {
                    pstCheck.setString(1, txtLogin.getText());
                    pstCheck.setInt(2, Integer.parseInt(txtID.getText()));
                    try (ResultSet rs = pstCheck.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(null, "Já existe um usuário com este login!");
                            txtLogin.requestFocus();
                            return;
                        }
                    }
                }
                String antigoLogin = "", antigoNome = "", antigoContato = "", antigoStatus = "", antigoGrupo = "", antigaObs = "";

                try (PreparedStatement pstBusca = conexao.prepareStatement(sqlBusca)) {
                    pstBusca.setInt(1, Integer.parseInt(txtID.getText()));
                    try (ResultSet rs = pstBusca.executeQuery()) {
                        if (rs.next()) {
                            antigoLogin = rs.getString("login_usuario");
                            antigoNome = rs.getString("nome_usuario");
                            antigoContato = rs.getString("contato_usuario");
                            antigoStatus = rs.getString("status_usuario");
                            antigoGrupo = rs.getString("grupo_usuario");
                            antigaObs = rs.getString("obs_usuario");
                        }
                    }
                }

                conexao.setAutoCommit(false);

                try (PreparedStatement pstUpdate = conexao.prepareStatement(sqlUpdate)) {
                    pstUpdate.setString(1, txtLogin.getText());
                    pstUpdate.setString(2, txtNome.getText());
                    pstUpdate.setString(3, txtSenha.getText());
                    pstUpdate.setString(4, txtContato.getText());
                    pstUpdate.setString(5, cboStatus.getSelectedItem().toString());
                    pstUpdate.setString(6, cboGrupo.getSelectedItem().toString());
                    pstUpdate.setString(7, txtObs.getText());
                    pstUpdate.setInt(8, Integer.parseInt(txtID.getText()));

                    int alterado = pstUpdate.executeUpdate();

                    if (alterado > 0) {
                        String descricao
                                = "Nome: " + antigoNome + " -> " + txtNome.getText() + " | "
                                + "Login: " + antigoLogin + " -> " + txtLogin.getText() + " | "
                                + "Contato: " + antigoContato + " -> " + txtContato.getText() + " | "
                                + "Status: " + antigoStatus + " -> " + cboStatus.getSelectedItem() + " | "
                                + "Grupo: " + antigoGrupo + " -> " + cboGrupo.getSelectedItem() + " | "
                                + "Obs: " + antigaObs + " -> " + txtObs.getText();

                        try (PreparedStatement pstLog = conexao.prepareStatement(sqlLog)) {
                            pstLog.setString(1, TelaMenuPrincipal.lblUsuarioLogado.getText());
                            pstLog.setInt(2, Integer.parseInt(txtID.getText()));
                            pstLog.setString(3, descricao);
                            pstLog.executeUpdate();
                        }
                    }
                }

                conexao.commit();
                JOptionPane.showMessageDialog(null, "Usuário alterado com sucesso!");
                limpar_campos();

            } catch (SQLException e) {
                try {
                    if (conexao != null) {
                        conexao.rollback();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(null, "Erro ao alterar Usuário: " + e.getMessage());

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
        lblCadastroUsuarios = new javax.swing.JLabel();
        panelFormCadastro = new javax.swing.JPanel();
        lblID = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        lblNome = new javax.swing.JLabel();
        txtNome = new javax.swing.JTextField();
        lblLogin = new javax.swing.JLabel();
        txtLogin = new javax.swing.JTextField();
        lblSenha = new javax.swing.JLabel();
        txtSenha = new javax.swing.JPasswordField();
        lblConfirmaSenha = new javax.swing.JLabel();
        txtConfirmaSenha = new javax.swing.JPasswordField();
        lblContato = new javax.swing.JLabel();
        txtContato = new javax.swing.JTextField();
        lblStatus = new javax.swing.JLabel();
        cboStatus = new javax.swing.JComboBox<>();
        lblObs = new javax.swing.JLabel();
        txtObs = new javax.swing.JTextField();
        lblGrupo = new javax.swing.JLabel();
        cboGrupo = new javax.swing.JComboBox<>();
        panelBotoes = new javax.swing.JPanel();
        btnSalvar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        panelPesquisar = new javax.swing.JPanel();
        lblPesquisar = new javax.swing.JLabel();
        txtPesquisar = new javax.swing.JTextField();
        lblLupa = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblUsuarios = new javax.swing.JTable();
        lblDuploClique = new javax.swing.JLabel();

        setClosable(true);
        setTitle("Gestão de Contas 1.0 - Cadastro Usuários");
        setToolTipText("");

        panelFundo.setBackground(new java.awt.Color(255, 255, 255));

        lblCadastroUsuarios.setBackground(new java.awt.Color(204, 204, 204));
        lblCadastroUsuarios.setFont(new java.awt.Font("Fira Sans", 1, 30)); // NOI18N
        lblCadastroUsuarios.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCadastroUsuarios.setText("Cadastro de Usuários");
        lblCadastroUsuarios.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        panelFormCadastro.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Formulário de Cadastro de Usuário", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Fira Sans", 1, 13))); // NOI18N

        lblID.setFont(new java.awt.Font("Fira Sans", 1, 15)); // NOI18N
        lblID.setText("ID");

        txtID.setEnabled(false);

        lblNome.setFont(new java.awt.Font("Fira Sans", 1, 15)); // NOI18N
        lblNome.setText("Nome*");

        lblLogin.setFont(new java.awt.Font("Fira Sans", 1, 15)); // NOI18N
        lblLogin.setText("Login*");

        lblSenha.setFont(new java.awt.Font("Fira Sans", 1, 15)); // NOI18N
        lblSenha.setText("Senha*");

        lblConfirmaSenha.setFont(new java.awt.Font("Fira Sans", 1, 15)); // NOI18N
        lblConfirmaSenha.setText("Confirma Senha*");

        lblContato.setFont(new java.awt.Font("Fira Sans", 1, 15)); // NOI18N
        lblContato.setText("Contato*");

        lblStatus.setFont(new java.awt.Font("Fira Sans", 1, 15)); // NOI18N
        lblStatus.setText("Status*");

        cboStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "<Selecione>", "Ativo", "Inativo" }));

        lblObs.setFont(new java.awt.Font("Fira Sans", 1, 15)); // NOI18N
        lblObs.setText("Observação");

        lblGrupo.setFont(new java.awt.Font("Fira Sans", 1, 15)); // NOI18N
        lblGrupo.setText("Grupo*");

        cboGrupo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "<Selecione>", "Usuário", "Administrador" }));

        btnSalvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/gestaocontas/icones/salvar_64px.png"))); // NOI18N
        btnSalvar.setToolTipText("Criar Usuário");
        btnSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalvarActionPerformed(evt);
            }
        });

        btnEditar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/gestaocontas/icones/editar_64px.png"))); // NOI18N
        btnEditar.setToolTipText("Editar Usuário");
        btnEditar.setEnabled(false);
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });

        btnLimpar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/gestaocontas/icones/limpar_48px.png"))); // NOI18N
        btnLimpar.setToolTipText("Limpar Campos");
        btnLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBotoesLayout = new javax.swing.GroupLayout(panelBotoes);
        panelBotoes.setLayout(panelBotoesLayout);
        panelBotoesLayout.setHorizontalGroup(
            panelBotoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBotoesLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelBotoesLayout.setVerticalGroup(
            panelBotoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnSalvar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnEditar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelBotoesLayout.createSequentialGroup()
                .addComponent(btnLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelFormCadastroLayout = new javax.swing.GroupLayout(panelFormCadastro);
        panelFormCadastro.setLayout(panelFormCadastroLayout);
        panelFormCadastroLayout.setHorizontalGroup(
            panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormCadastroLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblObs)
                    .addComponent(lblNome)
                    .addComponent(lblID)
                    .addComponent(lblLogin)
                    .addComponent(lblSenha)
                    .addComponent(lblContato))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelFormCadastroLayout.createSequentialGroup()
                        .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtObs, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtContato, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                                .addComponent(txtSenha)
                                .addComponent(txtLogin)))
                        .addGap(18, 18, 18)
                        .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblStatus)
                            .addComponent(lblConfirmaSenha)
                            .addComponent(lblGrupo))
                        .addGap(18, 18, 18)
                        .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtConfirmaSenha)
                            .addComponent(cboStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cboGrupo, 0, 270, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelFormCadastroLayout.createSequentialGroup()
                        .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelBotoes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtNome))
                .addContainerGap(46, Short.MAX_VALUE))
        );
        panelFormCadastroLayout.setVerticalGroup(
            panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormCadastroLayout.createSequentialGroup()
                .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelFormCadastroLayout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblID)))
                    .addGroup(panelFormCadastroLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelBotoes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNome)
                    .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelFormCadastroLayout.createSequentialGroup()
                        .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblLogin))
                        .addGap(20, 20, 20)
                        .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSenha)
                            .addComponent(txtSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblConfirmaSenha)
                            .addComponent(txtConfirmaSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtContato, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblContato)
                            .addComponent(lblStatus))
                        .addGap(18, 18, 18)
                        .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblObs)
                            .addComponent(txtObs, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelFormCadastroLayout.createSequentialGroup()
                        .addComponent(cboStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(panelFormCadastroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cboGrupo, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblGrupo))))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        panelPesquisar.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Pesquisar Usuário Cadastrado", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Fira Sans", 1, 13))); // NOI18N

        lblPesquisar.setFont(new java.awt.Font("Fira Sans", 1, 15)); // NOI18N
        lblPesquisar.setText("Pesquisar");

        txtPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesquisarKeyReleased(evt);
            }
        });

        lblLupa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/gestaocontas/icones/pesquisar_24px.png"))); // NOI18N

        tblUsuarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nome", "Usuário", "Grupo", "Status", "Contato", "Obs"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblUsuariosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblUsuarios);

        lblDuploClique.setFont(new java.awt.Font("Fira Sans", 1, 15)); // NOI18N
        lblDuploClique.setForeground(new java.awt.Color(255, 0, 0));
        lblDuploClique.setText("*Clique para selecionar");

        javax.swing.GroupLayout panelPesquisarLayout = new javax.swing.GroupLayout(panelPesquisar);
        panelPesquisar.setLayout(panelPesquisarLayout);
        panelPesquisarLayout.setHorizontalGroup(
            panelPesquisarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPesquisarLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(panelPesquisarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPesquisarLayout.createSequentialGroup()
                        .addComponent(lblPesquisar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelPesquisarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPesquisarLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(lblDuploClique))
                            .addComponent(txtPesquisar)))
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblLupa)
                .addContainerGap())
        );
        panelPesquisarLayout.setVerticalGroup(
            panelPesquisarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPesquisarLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelPesquisarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPesquisarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblPesquisar))
                    .addComponent(lblLupa))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDuploClique)
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout panelFundoLayout = new javax.swing.GroupLayout(panelFundo);
        panelFundo.setLayout(panelFundoLayout);
        panelFundoLayout.setHorizontalGroup(
            panelFundoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFundoLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelFundoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(panelPesquisar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelFormCadastro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCadastroUsuarios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        panelFundoLayout.setVerticalGroup(
            panelFundoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFundoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCadastroUsuarios)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelFormCadastro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(42, Short.MAX_VALUE))
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

        setBounds(0, 0, 967, 759);
    }// </editor-fold>//GEN-END:initComponents

    // CHAMA MÉTODO PARA LIMPAR CAMPOS DOS FORMULÁRIOS
    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        limpar_campos();
    }//GEN-LAST:event_btnLimparActionPerformed

    // CHAMA MÉTODO PARA CADASTRAR NOVO USUÁRIO
    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarActionPerformed
        criar_usuario();
    }//GEN-LAST:event_btnSalvarActionPerformed

    // CHAMA MÉTODO PARA PESQUISAR USUÁRIO
    private void txtPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesquisarKeyReleased
        pesquisar_usuario();
    }//GEN-LAST:event_txtPesquisarKeyReleased

    // CHAMA MÉTODO PARA SETAR USUÁRIO SELECIONADO NA TABELA DE PESQUISA
    private void tblUsuariosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblUsuariosMouseClicked
        setar_usuario();
    }//GEN-LAST:event_tblUsuariosMouseClicked

    // CHAMA MÉTODO PARA EDITAR USUÁRIO JÁ CADASTRADO
    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        editar_usuario();
    }//GEN-LAST:event_btnEditarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JComboBox<String> cboGrupo;
    private javax.swing.JComboBox<String> cboStatus;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCadastroUsuarios;
    private javax.swing.JLabel lblConfirmaSenha;
    private javax.swing.JLabel lblContato;
    private javax.swing.JLabel lblDuploClique;
    private javax.swing.JLabel lblGrupo;
    private javax.swing.JLabel lblID;
    private javax.swing.JLabel lblLogin;
    private javax.swing.JLabel lblLupa;
    private javax.swing.JLabel lblNome;
    private javax.swing.JLabel lblObs;
    private javax.swing.JLabel lblPesquisar;
    private javax.swing.JLabel lblSenha;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JPanel panelBotoes;
    private javax.swing.JPanel panelFormCadastro;
    private javax.swing.JPanel panelFundo;
    private javax.swing.JPanel panelPesquisar;
    private javax.swing.JTable tblUsuarios;
    private javax.swing.JPasswordField txtConfirmaSenha;
    private javax.swing.JTextField txtContato;
    private javax.swing.JTextField txtID;
    private javax.swing.JTextField txtLogin;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextField txtObs;
    private javax.swing.JTextField txtPesquisar;
    private javax.swing.JPasswordField txtSenha;
    // End of variables declaration//GEN-END:variables
}
