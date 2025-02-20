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

import razifx.java.model.dao.DBConnector;
import java.sql.*;

/**
 * FirstRunCheck.java
 *
 * @author mahdihoseinzade
 * @since 1.1
 */
public class FirstRunCheck {

    private static DBConnector db;

    private static boolean closeTutorials = false;
    private static boolean closeHelpPane = false;
    
    /**
     * check the jobs table is empty or not
     *
     * @param userId the specific id
     * @return true if table jobs is empty
     */
    public static boolean isFirstRun(Long userId) {
        boolean isFirstRun = false;
        db = new DBConnector();
        Connection connection = db.connect();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM jobs WHERE user_id = ?");
            preparedStatement.setLong(1, userId);
            resultSet = preparedStatement.executeQuery();
            int countJobs = 0;
            while (resultSet.next()) {
                countJobs++;
                if (countJobs>0) {
                    isFirstRun = false;
                    break;
                }
            }
            if (countJobs==0) {
                isFirstRun = true;
            }
        } catch (SQLException se) {
            // Handle errors for JDBC
            RaziLogger.error(se.getMessage());
            isFirstRun = true; // Assume first run if an error occurs (table might not exist)
        } catch (Exception e) {
            // Handle errors for Class.forName
            RaziLogger.error(e.getMessage());
            isFirstRun = true; // Assume first run if an error occurs
        } finally {
            // Close resources
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException se) {
                RaziLogger.error(se.getMessage());
            }
        }

        return isFirstRun;
    }

    public static boolean isCloseTutorials() {
        return closeTutorials;
    }

    public static void setCloseTutorials(boolean closeTutorials) {
        FirstRunCheck.closeTutorials = closeTutorials;
    }

    public static boolean isCloseHelpPane() {
        return closeHelpPane;
    }

    public static void setCloseHelpPane(boolean closeHelpPane) {
        FirstRunCheck.closeHelpPane = closeHelpPane;
    }
    
    
}
