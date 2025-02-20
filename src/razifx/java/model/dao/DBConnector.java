/*
 * The MIT License
 *
 * Copyright 2025 mahdihoseinzade.
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
package razifx.java.model.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import razifx.core.RaziLogger;

/**
 * DBConnector.java: This class provides a foundation for interacting with your
 * database in Java applications.
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class DBConnector {

    // init database constants
    private static final String DATABASE_DRIVER;
    private static final String DATABASE_URL;
    private static final String USERNAME;
    private static final String PASSWORD;
    private static final String MAX_POOL;
    // init connection object
    private Connection connection;
    // init properties object
    private Properties properties;

    public DBConnector() {
    }

    static {
        DATABASE_DRIVER = "com.mysql.cj.jdbc.Driver";
        DATABASE_URL = "jdbc:mysql://localhost:3306/razifx2";
        USERNAME = "root";
        PASSWORD = "1234";
        MAX_POOL = "1";
    }

    // create properties
    private Properties getProperties() {
        // TODO need modify
        if (properties == null) {
            properties = new Properties();
            properties.setProperty("user", USERNAME);
            properties.setProperty("password", PASSWORD);
            properties.setProperty("MaxPooledStatements", MAX_POOL);
        }
        return properties;
    }

    // connect database
    public Connection connect() {
        if (connection == null) {
            try {
                Class.forName(DATABASE_DRIVER);
                connection = DriverManager.getConnection(DATABASE_URL, getProperties());
            } catch (ClassNotFoundException | SQLException e) {
                RaziLogger.error("Cannot connect to db caused: " + getClass().getName(), e);
            }
        }
        return connection;
    }

    // disconnect database
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                RaziLogger.error("Cannot close to db caused: " + getClass().getName(), e);
            }
        }
    }
}
