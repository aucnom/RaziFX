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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * EnterValidation.java
 * 
 * @author mahdihoseinzade
 * @since 1.1
 */
public class EnterValidation {
    
    private static DBConnector db;
    private static Connection connection;
    
    static {
        db = new DBConnector();
    }
    
    
    public static boolean isTableEmpty(String tableName) throws SQLException {
        connection = db.connect();
        try (Statement statement = connection.createStatement()) {
            // Efficiently check for any row without retrieving the entire table
            String sql = "SELECT 1 FROM " + tableName + " LIMIT 1";  // LIMIT 1 for efficiency

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                return !resultSet.next(); // If no row is found, it's empty
            }
        }
    }
}
