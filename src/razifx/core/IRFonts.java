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

import javafx.scene.text.Font;

/**
 * IRFonts.java: load fonts from resources/fonts
 * 
 * @deprecated this class never used for java encoding character exceptions.
 * 
 * @author mahdihoseinzade
 * @since 1.0
 */
@Deprecated
public class IRFonts {
    
    private static Font font;
    
    private IRFonts() {}
    
    public static Font koodak(double size) {
        font = Font.loadFont("file:resources/fonts/IRKoodak.ttf", size);
        return font;
    }
    
    public static Font lotus(double size) {
        font = Font.loadFont("file:resources/fonts/IRLotus.ttf", size);
        return font;
    }
    
    public static Font nazanin(double size) {
        font = Font.loadFont("file:/resources/fonts/IRNazanin.ttf", size);
        return font;
    }
    
}
