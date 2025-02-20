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
package razifx.core;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import razifx.core.data.AppInfo;
import razifx.java.model.dao.DBConnector;

/**
 * FirstTimeRunCheck.java: simple and effective way to determine if an
 * application is being run for the first time using a file as a marker.
 *
 * @deprecated use FirstRunCheck instead of this class.
 * @author mahdihoseinzade
 * @since 1.0
 */
@Deprecated
public class FirstTimeRunCheck {

    private FirstTimeRunCheck() {
    }

    /**
     * isFirstRun: This constant defines the name of the file that will be used
     * to track whether the application has been run before.
     *
     * @return boolean
     */
    public static boolean isFirstRun() {
        DBConnector db = new DBConnector();
        Connection conn = db.connect();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String qry = "SELECT * From jobs ";

        try {
            stmt = (PreparedStatement) conn.prepareStatement(qry);
            rs = stmt.executeQuery();

            boolean empty = true;
            while (rs.next()) {
                // ResultSet processing here
                empty = false;
            }
            if (empty) {
                return true;
            } else {
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(FirstTimeRunCheck.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static void successfulSubmitSuperUserInDatabase() {
        RaziLogger.info("new user created.");
    }
}
