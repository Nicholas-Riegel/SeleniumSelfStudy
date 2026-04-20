package com.seleniumstudy.tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Module1Test {

    // Field — shared between @BeforeMethod, @Test, and @AfterMethod
    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        // Runs before every @Test — launches a fresh browser
        driver = new ChromeDriver();
    }

    @AfterMethod
    public void tearDown() {
        // Runs after every @Test — closes the browser even if the test failed
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void openBrowser() {
        driver.get("https://www.saucedemo.com");
        String title = driver.getTitle();
        System.out.println("Page title: " + title);
    }

}