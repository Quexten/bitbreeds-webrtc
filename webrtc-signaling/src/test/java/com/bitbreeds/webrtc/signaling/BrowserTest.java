package com.bitbreeds.webrtc.signaling;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;

/**
 * Copyright (c) 27/06/16, Jonas Waage
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class BrowserTest {

    @Test
    public void testFull() throws Exception {

        String firefoxPath = System.getProperty("firefox.path");
        //OS X * /Firefox.app/Contents/MacOS/firefox
        System.setProperty("com.bitbreeds.keystore","./src/test/resources/ws2.jks");
        System.setProperty("com.bitbreeds.keystore.alias","websocket");
        System.setProperty("com.bitbreeds.keystore.pass","websocket");

        if(firefoxPath != null) {
            SimpleSignalingExample.main();

            File fl = new File(".././web/index.html");

            String url = "file://" + fl.getAbsolutePath();
            System.out.println(url);
            FirefoxBinary binary = new FirefoxBinary(new File(firefoxPath));
            FirefoxProfile firefoxProfile = new FirefoxProfile();
            WebDriver driver = new FirefoxDriver(binary, firefoxProfile);
            driver.get(url);

            (new WebDriverWait(driver, 20)).until(
                    (ExpectedCondition<Boolean>) d -> {
                        assert d != null;
                        return d.findElement(By.id("status")).getText().equalsIgnoreCase("ONMESSAGE");
                    }
            );

            driver.quit();
        }

    }



}
