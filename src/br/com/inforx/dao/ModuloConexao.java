package br.com.inforx.dao;

import java.sql.*;

/**
 * loadbalance
 *
 * @author cicer
 */
public class ModuloConexao {
    // Responsavel por estabelecer a conexão com o banco

    public static Connection conector() {
        java.sql.Connection conexao = null;
        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/dbinforx";
        String user = "root";
        String pass = "";
        
        try {
            Class.forName(driver);
            conexao = DriverManager.getConnection(url,user,pass);
            return conexao;
        } catch (Exception e) {
            //a linha serve de apoio para mostra o erro.
            System.out.println(e);
            return null;
        }
    }
}
