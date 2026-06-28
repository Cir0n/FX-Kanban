package fr.esgi.fx.kanban.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionManager {

    private static final String URL = "jdbc:h2:file:./kanban_db;AUTO_SERVER=TRUE;INIT=RUNSCRIPT FROM 'classpath:import.sql'";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private ConnectionManager() {
        // Classe utilitaire, ne doit pas être instanciée
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}