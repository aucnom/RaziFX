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
package razifx.core.data;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * AppInfo Class
 * Store application information and necessary data
 * @author mahdihoseinzade
 * @since 1.0
 */
public final class AppInfo {
    
    private final static String APP_NAME = "Razi FX";
    private final static String APP_AUTHOR = "Mahdi HOSEIN ZADE";
    private final static String APP_VERSION = "1.0.12";
    private final static String APP_TYPE = "Accounting Software";
    private static final String LOG_FILE_PATH = System.getProperty("user.dir") + File.separatorChar + "logs" +
            File.separatorChar + "razifxlogs.log"; 
    private static final String absolutePath = System.getProperty("user.dir") + File.separatorChar + "logs" + File.separatorChar;
    
    
    /**
     * <h5>running_for_first_time variable</h5>
     * <p>The answer to the question of whether the software has been run by the-
     * user for the first time is a very important issue. This variable holds the
     * answer in various ways, including collecting initial information about 
     * whether or not the software has been used.</p>
     * <p>If the user has not run the software, its value is correct. If the user
     * runs the software for the first time and answers the introductory questions, 
     * its value is false from then on.</p>
     * @deprecated use FirstTimeRunCheck instead.
     * @see razifx.core.FirstTimeRunCheck
     */
    @Deprecated
    public static boolean running_for_first_time = true;

    /**
     * Private constructors in AppInfo are needed to control object creation, 
     * promote encapsulation, and support specific design patterns. and allow me
     * to restrict how and when a class is instantiated.
     */
    private AppInfo() {}
    
    
    /**
     * This method is used for all parts of the software to access information,
     * which is used for security and encapsulation.
     * Makes the value of a private variable accessible to the public.
     */
    
    /**
     * getAppName method
     * @return APP_NAME
     */
    public static String getAppName() { return APP_NAME; }
    /**
     * getAppAuthor method
     * @return APP_AUTHOR
     */
    public static String getAppAuthor() { return APP_AUTHOR; }
    /**
     * getAppVersion method
     * @return APP_VERSION
     */
    public static String getAppVersion() { return APP_VERSION; }
    /**
     * getAppType method
     * @return APP_TYPE
     */
    public static String getAppType() { return APP_TYPE; }
    
    /**
     * get_log_path method
     * @return LOG_FILE_PATH
     */
    public static String get_log_path() { return LOG_FILE_PATH; }

    /**
     * getAbsolutePath
     * @return absolutePath of source directory
     */
    public static String getAbsolutePath() {
        return absolutePath;
    }
    
    /**
     * private getDateNow method: for file names
     * @return date now
     */
    private static String getDateNow() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");  
        Date date = new Date();  
        return formatter.format(date);
    } 

}
