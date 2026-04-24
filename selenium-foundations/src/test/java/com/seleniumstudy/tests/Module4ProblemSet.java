package com.seleniumstudy.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class Module4ProblemSet {

    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Challenge 1 — elementToBeClickable before interaction
    @Test
    public void challenge1() {

    }

    // Challenge 2 — visibilityOfElementLocated after navigation
    @Test
    public void challenge2() {

    }

    // Challenge 3 — invisibilityOfElementLocated
    @Test
    public void challenge3() {

    }

    // Challenge 4 — textToBePresentInElement
    @Test
    public void challenge4() {

    }

    // Challenge 5 — alertIsPresent
    @Test
    public void challenge5() {

    }

}
