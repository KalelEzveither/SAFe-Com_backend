package com.safecom.safe_backend.dao;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component 
public class ConnectionFactory {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String user;
    @Value("${spring.datasource.password}")
    private String password;

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}