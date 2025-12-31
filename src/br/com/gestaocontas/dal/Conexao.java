package br.com.gestaocontas.dal;

import java.sql.*;
import javax.swing.JOptionPane;

/**
 *
 * @author caio
 */
public class Conexao {

    public static Connection conectar() {
        java.sql.Connection conexao = null;
        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/gestaocontas?characterEncoding=utf-8";
        String user = "gestaocontas";
        String password = "123456";
        
        try {
            Class.forName(driver);
            conexao = DriverManager.getConnection(url, user, password);
            return conexao;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro de conexão no Banco de Dados:\n" + e.getMessage());
            return null;
        }
    }
}
