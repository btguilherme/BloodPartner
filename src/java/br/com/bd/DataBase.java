/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author Guilherme
 */
@WebService(serviceName = "DataBase")
public class DataBase {
    
    private Connection c;
    private final String user = "***USERNAME***";
    private final String pass = "***PASSWORD***";
    
    private void iniciaConexao() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            this.c = DriverManager.getConnection("jdbc:mysql://ip:port/dbname", user, pass);
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void fechaConexao() {
        try {
            this.c.close();
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private int getRows(ResultSet res) {
        int totalRows = 0;
        try {
            res.last();
            totalRows = res.getRow();
            res.beforeFirst();
        } catch (Exception ex) {
            return 0;
        }
        return totalRows;
    }

    /**
     * Operação de Web service
     */
    @WebMethod(operationName = "cadastrar")
    public String cadastrar(@WebParam(name = "username") String username, @WebParam(name = "full_name") String full_name, @WebParam(name = "sex") String sex, @WebParam(name = "password") String password, @WebParam(name = "email") String email, @WebParam(name = "blood_type") String blood_type, @WebParam(name = "city") String city, @WebParam(name = "state") String state) {
        iniciaConexao();
        String sql = "INSERT INTO users(username, full_name, sex, password, email, blood_type, city, state) VALUES "
                + "('" + username + "','" + full_name + "','" + sex.charAt(0) + "','" + password + "','" + email + "','" + blood_type + "','" + city + "','" + state + "')";
        
        try {
            PreparedStatement p;
            p = c.prepareStatement(sql);
            p.executeUpdate();
            fechaConexao();
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
            fechaConexao();
            return "Cadastro não realizado.\nNome de usuário já existe.";
        }
        
        return "Cadastrado com sucesso!";
    }

    /**
     * Operação de Web service
     */
    @WebMethod(operationName = "login")
    public String login(@WebParam(name = "username") String username, @WebParam(name = "password") String password) {
        iniciaConexao();
        String sql = "SELECT * FROM users u WHERE (u.username='" + username + "' AND u.password='" + password + "')";
        int row = 0;
        
        try {
            PreparedStatement p;
            p = c.prepareStatement(sql);
            ResultSet r = p.executeQuery();
            
            row = getRows(r);
            
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
            return "Falha no login";
        }
        
        fechaConexao();
        
        if (row == 1) {
            return "Logado";
        }
        
        return "Falha no login";
        
    }

    /**
     * Operação de Web service
     */
    @WebMethod(operationName = "dados")
    public String dados(@WebParam(name = "username") String username) {
        iniciaConexao();
        String sql = "SELECT * FROM users u where u.username='" + username + "'";
        String retorno = new String();
        
        try {
            PreparedStatement p;
            p = c.prepareStatement(sql);
            ResultSet r = p.executeQuery();
            
            while (r.next()) {
                retorno = r.getString("full_name") + "#" + r.getString("username") + "#" + r.getString("email") + "#" + r.getString("blood_type") + "#" + r.getString("city") + "#" + r.getString("sex") + "#" + r.getString("state");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        fechaConexao();
        
        return retorno;
    }

    /**
     * Operação de Web service
     */
    @WebMethod(operationName = "alterar")
    public String alterar(@WebParam(name = "username") String username, @WebParam(name = "full_name") String full_name, @WebParam(name = "sex") String sex, @WebParam(name = "password") String password, @WebParam(name = "email") String email, @WebParam(name = "blood_type") String blood_type, @WebParam(name = "state") String state, @WebParam(name = "city") String city) {
        
        iniciaConexao();
        String sql = "UPDATE users SET full_name='" + full_name + "', sex='" + sex.charAt(0) + "', password='" + password + "',email='" + email + "',blood_type='" + blood_type + "',city='" + city + "',state='" + state + "' WHERE username='" + username + "'";
        
        try {
            PreparedStatement p;
            p = c.prepareStatement(sql);
            p.executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
            return "Não foi possível alterar os dados.\n" + ex;
        }
        
        fechaConexao();
        
        return "Alterado com sucesso!";
    }
    
    @WebMethod(operationName = "buscaProximos")
    public String buscaProximos(@WebParam(name = "username") String username, @WebParam(name = "blood_type") String blood_type, @WebParam(name = "city") String city) {
        
        iniciaConexao();
        String sql = query(blood_type, username, city);
        String retorno = "";
        
        try {
            PreparedStatement p;
            p = c.prepareStatement(sql);
            ResultSet r = p.executeQuery();
            
            if(getRows(r) == 0){
                return "Não existe doadores próximos";
            }
            
            while (r.next()) {
                retorno = retorno.concat(r.getString("full_name") + "#" + r.getString("username") + "#" + r.getString("blood_type") + "#" + r.getString("email") + "$");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
            return "Não existe doadores próximos";
        }
        
        fechaConexao();
        
        return retorno;
    }
    
    private String query(String blood_type, String username, String city) {
        String sql = new String();
        switch (blood_type) {
            case "A+":
                //A+, A-, O+, O-
                sql = "SELECT * FROM users u WHERE ((u.username !='" + username + "' AND u.city = '" + city + "') AND (u.blood_type ='A+' OR u.blood_type ='A-' OR u.blood_type ='O+' OR u.blood_type ='O-'))";
                break;
            case "A-":
                //A-, O-
                sql = "SELECT * FROM users u WHERE ((u.username !='" + username + "' AND u.city = '" + city + "') AND (u.blood_type ='A-' OR u.blood_type ='O-'))";
                break;
            case "B+":
                //B+, B-, O+, O-
                sql = "SELECT * FROM users u WHERE ((u.username !='" + username + "' AND u.city = '" + city + "') AND (u.blood_type ='B+' OR u.blood_type ='B-' OR u.blood_type ='O+' OR u.blood_type ='O-'))";
                break;
            case "B-":
                //B-, O-
                sql = "SELECT * FROM users u WHERE ((u.username !='" + username + "' AND u.city = '" + city + "') AND (u.blood_type ='B-' OR u.blood_type ='O-'))";
                break;
            case "AB+":
                //A+, A-,B+, B-, AB+, AB-, O+, O-
                sql = "SELECT * FROM users u WHERE u.username !='" + username + "' AND u.city = '" + city + "'";
                break;
            case "AB-":
                //A-, B-, AB-, O-
                sql = "SELECT * FROM users u WHERE ((u.username !='" + username + "' AND u.city = '" + city + "') AND (u.blood_type ='A-' OR u.blood_type ='B-' OR u.blood_type ='AB-' OR u.blood_type ='O-'))";
                break;
            case "O+":
                //O+, O-
                sql = "SELECT * FROM users u WHERE ((u.username !='" + username + "' AND u.city = '" + city + "') AND (u.blood_type ='O+' OR u.blood_type ='O-'))";
                break;
            case "O-":
                //O-
                sql = "SELECT * FROM users u WHERE ((u.username !='" + username + "' AND u.city = '" + city + "') AND (u.blood_type ='O-'))";
                break;
            
        }
        
        return sql;
    }

    /**
     * Operação de Web service
     */
    @WebMethod(operationName = "addDoador")
    public String addDoador(@WebParam(name = "username") String username, @WebParam(name = "friend_full_name") String friend_full_name, @WebParam(name = "friend_username") String friend_username) {
        iniciaConexao();
        
        String sql = "SELECT * FROM friends f where f.username='" + username + "' and f.friend_username='" + friend_username + "'";
        int row = 0;
        
        try {
            PreparedStatement p;
            p = c.prepareStatement(sql);
            ResultSet r = p.executeQuery();
            
            row = getRows(r);
            
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (row == 1) {
            fechaConexao();
            return "Doador já está adicionado à sua lista!";
        }
        
        sql = "INSERT INTO friends VALUES ('" + username + "', '" + friend_full_name + "', '" + friend_username + "')";
        
        try {
            PreparedStatement p;
            p = c.prepareStatement(sql);
            p.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        fechaConexao();
        
        return "Adicionado com sucesso!";
    }

    /**
     * Operação de Web service
     */
    @WebMethod(operationName = "listarDoaramPraMim")
    public String listarDoaramPraMim(@WebParam(name = "username") String username) {
        
        iniciaConexao();
        
        String amigos = "";
        String sql = "SELECT * FROM friends f WHERE f.username = '" + username + "'";
        
        int row;
        
        try {
            PreparedStatement p;
            p = c.prepareStatement(sql);
            ResultSet r = p.executeQuery();
            
            row = getRows(r);
            
            if (getRows(r) == 0) {
                amigos = "Ninguém doou sangue à você#$";
            } else {
                while (r.next()) {
                    amigos = amigos.concat(r.getString("friend_full_name") + "#" + r.getString("friend_username") + "$");
                }
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        fechaConexao();
        
        return amigos;
    }

    /**
     * Operação de Web service
     */
    @WebMethod(operationName = "listarDoeiPara")
    public String listarDoeiPara(@WebParam(name = "friend_username") String friend_username) {
        
        iniciaConexao();
        
        String amigos = "";
        
        String sql = "SELECT username FROM friends f WHERE f.friend_username = '" + friend_username + "'";
        
        try {
            PreparedStatement p;
            p = c.prepareStatement(sql);
            ResultSet r = p.executeQuery();
            
            if (getRows(r) == 0) {
                amigos = "Você ainda não doou sangue#";
            } else {
                while (r.next()) {
                    amigos = amigos.concat(r.getString("username") + "#");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        fechaConexao();
        
        return amigos;
    }

    /*ate aqui ta ok...
     alteracoes daqui pra baixo
     */
    /**
     * Operação de Web service
     */
    @WebMethod(operationName = "removeFriend")
    public String removeFriend(@WebParam(name = "username") String username, @WebParam(name = "friend_username") String friend_username) {
        iniciaConexao();
        String sql = "DELETE FROM friends WHERE (username = '" + username + "' AND friend_username = '" + friend_username + "')";
        
        try {
            PreparedStatement p;
            p = c.prepareStatement(sql);
            p.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        fechaConexao();
        
        return "Amigo removido com sucesso!";
    }
    
}
