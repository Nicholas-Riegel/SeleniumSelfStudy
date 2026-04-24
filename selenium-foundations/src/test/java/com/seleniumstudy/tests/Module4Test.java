package com.seleniumstudy.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class Module4Test {

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

    @Test
    public void loginWithExplicitWait() {

        driver.get("https://www.saucedemo.com");

        // Wait until the username field is ready to receive input
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement username = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("user-name"))
        );
        username.sendKeys("standard_user");

        driver.findElement(By.id("password")).sendKeys("secret_sauce");
        driver.findElement(By.id("login-button")).click();

        // After clicking login, wait for the product list to appear
        // — this is the correct replacement for Thread.sleep()
        wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.className("inventory_container"))
        );

        System.out.println("Products page loaded: " + driver.getCurrentUrl());
    }

    @Test
    public void explicitWaitConditions() {
        
        driver.get("https://www.saucedemo.com");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // elementToBeClickable — safest choice before typing or clicking
        // ensures the field is visible AND not disabled before we touch it
        wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")))
            .sendKeys("standard_user");

        driver.findElement(By.id("password")).sendKeys("secret_sauce");

        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")))
            .click();

        // urlContains — wait for the page transition to complete
        // more reliable than immediately trying to find elements on the next page
        wait.until(ExpectedConditions.urlContains("inventory"));
        System.out.println("URL confirmed: " + driver.getCurrentUrl());

        // visibilityOfElementLocated — the inventory list is now visible
        // use this before reading text or asserting on content
        WebElement inventory = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list"))
        );
        System.out.println("Inventory visible: " + inventory.isDisplayed());

        // titleContains — page title check, useful as a lightweight "did we land here?" assertion
        wait.until(ExpectedConditions.titleContains("Swag Labs"));
        System.out.println("Title confirmed: " + driver.getTitle());
    }
}
