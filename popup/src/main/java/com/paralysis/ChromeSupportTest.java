package com.paralysis;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.html5.SessionStorage;

public class ChromeSupportTest {
    public static void main(String args[]) throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

        ChromeDriver driver = new ChromeDriver();

        System.out.println(driver.getCurrentUrl());

        driver.get("https://www.facebook.com/");
        SessionStorage session = driver.getSessionStorage();
        for(String key: session.keySet())
            System.out.printf("%s: %s%n", key, session.getItem(key));

        driver.quit();
    }
}
