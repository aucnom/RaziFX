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

package razifx.core.preloader;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import razifx.core.RaziLogger;

/**
 * <h1>razifx.core.preloader.Preloader Class</h1>
 * <p>A JavaFX Preloader is a special class that you can define in your JavaFX application to provide visual</p>
 * <p>feedback to the user while the main application is loading. This is crucial for a smoother user</p>
 * <p>experience, especially for larger applications that take time to initialize.</p>
 * 
 * <h2>Key Benefits:</h2>
 * <ol>
 * <li>Improved User Experience: Provides visual feedback during application startup, making the wait time
 * feel less frustrating. </li>   
 * <li>Customizable: Allows you to create a unique and branded loading experience.   </li>
 * <li>Better Performance: By handling loading tasks in a separate thread, you can potentially improve the 
 * perceived performance of your application.</li>
 * </ol>
 * 
 * @author mahdihoseinzade
 * @since 1.0
 */
public class RaziPreloader extends Preloader {

    private Stage preloaderStage;
    private Scene scene;
    
    public RaziPreloader() {
        
    }

    @Override
    public void init() throws Exception {
        RaziLogger.info("Preloader started");
        Parent root2 = FXMLLoader.load(getClass().getResource("SplashScreen.fxml"));
        scene = new Scene(root2);
    }
    /**
     * <h3>start(Stage primaryStage)</h3>
     * <p>This method is called when the Preloader starts. Here, you'll typically create</p>
     * <p>the visual components of your Preloader (e.g., a progress bar, a loading label).</p>
     * 
     * @param primaryStage
     * @throws Exception 
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.preloaderStage = primaryStage;
        /** <p>set preloader scene and show stage</p> */
        preloaderStage.setScene(scene);
        preloaderStage.setResizable(false);
        preloaderStage.initStyle(StageStyle.UNDECORATED);
        preloaderStage.show();
    }
    
    /**
     * <h3>handleApplicationNotification(Preloader.ProgressNotification info)</h3>
     * <p>This method is called by the main application to send progress updates to the Preloader.</p>
     * <p>You can use this information to update the progress indicator.</p>
     * <p>Handle all things, update the progress on screen.</p>
     * 
     * @param info 
     */
    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        if (info instanceof ProgressNotification) {
            SplashScreenController.label.setText("Loading.." + ((ProgressNotification) info).getProgress() + "%"); // * 100ß
            // TODO need log
        }
    }
    
    /**
     * <h3>handleStateChangeNotification(StateChangeNotification info)</h3>
     * <p>handle all necessary preload instances and objects and what time close the preloader</p>
     * 
     * @param info 
     */
    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        StateChangeNotification.Type type = info.getType();
        switch (type) {
            case BEFORE_START:
                /** <p>called after Main@init and before Main@start is called</p> */
                RaziLogger.info("Preloader launched.");
                // TODO do handle
                preloaderStage.hide();
                break;
        }
    }
    
}
