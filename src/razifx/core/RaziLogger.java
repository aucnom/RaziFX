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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import razifx.core.data.AppInfo;

/**
 * Logger.java: provides a basic foundation for logging in the application.
 * A custom logger class to be used throughout your application.
 * This class provides a convenient way to log messages with different levels 
 * (INFO, DEBUG, WARN, ERROR) and save them to a log file.
 * 
 * @author mahdihoseinzade
 * @since 1.0
 */
public class RaziLogger {

    private static final Logger LOGGER = Logger.getLogger(RaziLogger.class.getName());

    /**
     * Initializes the logger with a FileHandler to write logs to a file.
     */
    static {
        try {
            // Create a file handler for logging
            Handler fileHandler = new FileHandler(AppInfo.get_log_path());

            // Create a custom formatter for log messages
            Formatter formatter = new Formatter() {
                @Override
                public String format(LogRecord record) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dateStr = dateFormat.format(new Date(record.getMillis()));
                    return dateStr + " [" + record.getLevel() + "] " + record.getMessage() + "\n";
                }
            };

            fileHandler.setFormatter(formatter);
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Error creating log file: " + e.getMessage());
        }
    }

    /**
     * Logs an INFO message.
     *
     * @param message The message to be logged.
     */
    public static void info(String message) {
        LOGGER.log(Level.INFO, message);
    }

    /**
     * Logs a WARN message.
     *
     * @param message The message to be logged.
     */
    public static void warn(String message) {
        LOGGER.log(Level.WARNING, message);
    }

    /**
     * Logs an ERROR message.
     *
     * @param message The message to be logged.
     */
    public static void error(String message) {
        LOGGER.log(Level.SEVERE, message);
    }

    /**
     * Logs an ERROR message with an exception.
     *
     * @param message The message to be logged.
     * @param exception The exception to be logged.
     */
    public static void error(String message, Exception exception) {
        LOGGER.log(Level.SEVERE, message, exception);
    }
}
