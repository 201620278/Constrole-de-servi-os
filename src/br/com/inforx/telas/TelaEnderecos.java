/*
 * The MIT License
 *
 * Copyright 2022 cicero Diego.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package br.com.inforx.telas;

/**
 * Tela responsável pela gestão dos endereços
 *
 * @author Cicero Diego
 */
import br.com.inforx.dao.ModuloConexao;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Tela para a gestão de endereços
 *
 * @author Cicero Diego
 */
public class TelaEnderecos extends javax.swing.JInternalFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    /**
     * Creates new form TelaCadEnderecos
     */
    public TelaEnderecos() {
        initComponents();
        conexao = ModuloConexao.conectar();
    }
    String rua;
    String bairro;
    String cidade;
    String uf;

    /**
     * Método resposável por retornar o cep através de viacep
     *
     * @param cep
     */
    public void buscarCep(String cep) {
        String json;

        try {
            URL url = new URL("https://viacep.com.br/ws/" + cep + "/json/");
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            StringBuilder jsonSb = new StringBuilder();

            br.lines().forEach(l -> jsonSb.append(l.trim()));
            json = jsonSb.toString();

            json = json.replaceAll("[{},:]", "");
            json = json.replaceAll("\"", "\n");
            String array[] = new String[30];
            array = json.split("\n");

            rua = array[7];
            bairro = array[15];
            cidade = array[19];
            uf = array[23];

            txtEndRua.setText(rua);
            txtEndBairro.setText(bairro);
            txtEndCidade.setText(cidade);
            txtEndUf.setText(uf);
            //JOptionPane.showMessageDialog(null, logradouro + " " + bairro + " " + cidade + " " + uf);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método Responsavel por consultar o endereço
     */
    private void consultarEnd() {
        String sql = "select * from tbenderecos where cep=?";
        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtEndCep.getText());
            rs = pst.executeQuery();
            if (rs.next()) {
                txtEndRua.setText(rs.getString(2));
                txtEndBairro.setText(rs.getString(3));
                txtEndCidade.setText(rs.getString(4));
                txtEndUf.setText(rs.getString(5));

                btnEndCreate.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(null, "Endereço não cadastrado");
                limpar();
            }
        } catch (HeadlessException | SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    /**
     * Método Responsavel por adicionar o endereço
     */
    private void adicionarEnd() {
        String sql = "insert into tbenderecos(cep, rua, bairro, cidade, uf) values(?, ?, ?, ?, ?)";
        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtEndCep.getText());
            pst.setString(2, txtEndRua.getText());
            pst.setString(3, txtEndBairro.getText());
            pst.setString(4, txtEndCidade.getText());
            pst.setString(5, txtEndUf.getText());

            if ((txtEndRua.getText().isEmpty()) || (txtEndBairro.getText().isEmpty())
                    || (txtEndCidade.getText().isEmpty()) || (txtEndUf.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Prencha todos os campos obrigatorios marcados com um *");
            } else {
                int adicionado = pst.executeUpdate();
                if (adicionado > 0) {
                    JOptionPane.showMessageDialog(null, "Cliente adicionado com sucesso");
                    limpar();
                }
            }
        } catch (HeadlessException | SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    /**
     * Método Responsavel por alterar o endereço
     */
    private void alterarEnd() {
        int confirma = JOptionPane.showConfirmDialog(null, "Confima as alterações nos dados do endereço?", "Atenção!", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            String sql = "UPDATE tbenderecos  SET rua=?, bairro=?, cidade=?, uf=? WHERE cep=?";
            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtEndRua.getText());
                pst.setString(2, txtEndBairro.getText());
                pst.setString(3, txtEndCidade.getText());
                pst.setString(4, txtEndUf.getText());
                pst.setString(5, txtEndCep.getText());

                if ((txtEndCep.getText().isEmpty()) || (txtEndRua.getText().isEmpty())
                        || (txtEndBairro.getText().isEmpty()) || (txtEndCidade.getText().isEmpty())
                        || (txtEndUf.getText().isEmpty())) {

                    JOptionPane.showMessageDialog(null,
                            "Prencha todos os campos obrigatorios marcados com um *");
                } else {
                    int adicionado = pst.executeUpdate();
                    if (adicionado > 0) {
                        limpar();
                        btnEndCreate.setEnabled(true);

                        JOptionPane.showMessageDialog(null, "Cliente alterado com sucesso");
                    }
                }
            } catch (HeadlessException | SQLException e) {
                JOptionPane.showMessageDialog(null, "Erro ao salvar" + e);
            }
        }
    }

    /**
     * Método Responsavel por remover o endereço
     */
    private void removerEnd() {
        int confirma = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja excluir esse endereço ?",
                "Atenção", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM tbenderecos WHERE cep=?";
            try {
                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtEndCep.getText());
                int apagado = pst.executeUpdate();
                if (apagado > 0) {
                    JOptionPane.showMessageDialog(null, "Endereço removido com sucesso");
                    limpar();
                    btnEndCreate.setEnabled(true);
                }
            } catch (HeadlessException | SQLException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    /**
     * Método Responsavel por limpar os campos da tela de cadastro de endereços
     */
    private void limpar() {
        txtEndCep.setText(null);
        txtEndRua.setText(null);
        txtEndBairro.setText(null);
        txtEndCidade.setText(null);
        txtEndUf.setText(null);
        btnEndCreate.setEnabled(true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        txtEndRua = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtEndCep = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtEndBairro = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtEndCidade = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        btnEndCreate = new javax.swing.JButton();
        btnEndRead = new javax.swing.JButton();
        btnEndUpdate = new javax.swing.JButton();
        btnEndDelete = new javax.swing.JButton();
        txtEndUf = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setBorder(null);
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(836, 522));

        jLabel3.setText("*  Rua");

        jLabel5.setText("* Cep");

        jLabel6.setText("* Bairro");

        jLabel7.setText("* Cidade");

        txtEndCidade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEndCidadeActionPerformed(evt);
            }
        });

        jLabel8.setText("* UF");

        btnEndCreate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bd/com/inforx/icones/create.png"))); // NOI18N
        btnEndCreate.setPreferredSize(new java.awt.Dimension(80, 80));
        btnEndCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEndCreateActionPerformed(evt);
            }
        });

        btnEndRead.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bd/com/inforx/icones/read.png"))); // NOI18N
        btnEndRead.setPreferredSize(new java.awt.Dimension(80, 80));
        btnEndRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEndReadActionPerformed(evt);
            }
        });

        btnEndUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bd/com/inforx/icones/update.png"))); // NOI18N
        btnEndUpdate.setPreferredSize(new java.awt.Dimension(80, 80));
        btnEndUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEndUpdateActionPerformed(evt);
            }
        });

        btnEndDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bd/com/inforx/icones/delete.png"))); // NOI18N
        btnEndDelete.setPreferredSize(new java.awt.Dimension(80, 80));
        btnEndDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEndDeleteActionPerformed(evt);
            }
        });

        btnBuscar.setBackground(new java.awt.Color(0, 0, 255));
        btnBuscar.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnBuscar.setForeground(new java.awt.Color(255, 255, 255));
        btnBuscar.setText("Buscar na Web");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        jPanel1.setBackground(java.awt.Color.orange);
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setText("Cadastro de Endereços");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(308, 308, 308))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(34, 34, 34)
                        .addComponent(txtEndRua, javax.swing.GroupLayout.PREFERRED_SIZE, 451, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(65, 65, 65)
                        .addComponent(txtEndCep, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(31, 31, 31)
                        .addComponent(btnBuscar))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel6)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(jLabel8)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtEndBairro, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtEndCidade, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtEndUf, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 146, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnEndRead, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnEndCreate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnEndUpdate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEndDelete, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(77, 77, 77)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtEndCep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBuscar))
                        .addGap(29, 29, 29)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtEndRua, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtEndBairro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtEndCidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(txtEndUf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnEndCreate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnEndRead, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(btnEndUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(btnEndDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(40, 40, 40))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel3, jLabel5, jLabel6, jLabel7, jLabel8, txtEndBairro, txtEndCep, txtEndCidade, txtEndRua});

        setBounds(0, 0, 860, 546);
    }// </editor-fold>//GEN-END:initComponents

    private void txtEndCidadeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEndCidadeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtEndCidadeActionPerformed

    private void btnEndReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEndReadActionPerformed
        // faz a consulta no banco
        consultarEnd();
    }//GEN-LAST:event_btnEndReadActionPerformed

    private void btnEndCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEndCreateActionPerformed
        //Adiciona um novo endereço no banco
        adicionarEnd();
    }//GEN-LAST:event_btnEndCreateActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        // TODO add your handling code here:
        //buscarCep(txtEndCep.getText());
        txtEndRua.setText("Aguarde...");
        if (txtEndCep.getText().length() == 8) {
            buscarCep(txtEndCep.getText());
        }
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnEndUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEndUpdateActionPerformed
        alterarEnd();
    }//GEN-LAST:event_btnEndUpdateActionPerformed

    private void btnEndDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEndDeleteActionPerformed
        removerEnd();
    }//GEN-LAST:event_btnEndDeleteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEndCreate;
    private javax.swing.JButton btnEndDelete;
    private javax.swing.JButton btnEndRead;
    private javax.swing.JButton btnEndUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txtEndBairro;
    private javax.swing.JTextField txtEndCep;
    private javax.swing.JTextField txtEndCidade;
    private javax.swing.JTextField txtEndRua;
    private javax.swing.JTextField txtEndUf;
    // End of variables declaration//GEN-END:variables

}
