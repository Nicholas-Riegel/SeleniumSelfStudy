package com.seleniumstudy.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BasePage {

    // protected — subclasses can access these directly, but tests cannot
    protected WebDriver driver;
    protected WebDriverWait wait;

    // You'll see this pattern in a lot of tutorials:
    //  @BeforeClass
    // public void setUp() {
    //     driver = new ChromeDriver();
    // }
    // @AfterClass
    // public void tearDown() {
    //     driver.quit();
    // }
    // The problem: BasePage is a page utility class, not a test class. Putting TestNG annotations in it gives it two jobs — managing driver lifecycle AND providing page utilities. Those are separate concerns.
    //     Every test class that extends BasePage silently inherits @BeforeClass/@AfterClass — if you ever want a test class to not follow that setup (headless driver, Firefox, different timeout), you have to fight the inheritance
    // If you want to run the same test against two different browsers, you can't — the driver type is baked into BasePage
    // BasePage now depends on TestNG, which means it can't be used outside of a test context

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Click an element, waiting until it's clickable first
    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    // Clear a field and type into it, waiting until it's visible first
    protected void type(By locator, String text) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        field.clear();
        field.sendKeys(text);
    }

    // Read the visible text of an element, waiting until it's visible first
    protected String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }
}