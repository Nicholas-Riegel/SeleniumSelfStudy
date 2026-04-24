package com.seleniumstudy.tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class Module3ProblemSet {

    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        // Launches a fresh Chrome browser before each test
        driver = new ChromeDriver();
        // Navigate to saucedemo.com — our test site for this module
        driver.get("https://the-internet.herokuapp.com/login");
    }

    @AfterMethod
    public void tearDown() {
        // Quits the browser after each test, even if the test failed
        if (driver != null) {
            driver.quit();
        }
    }
}
