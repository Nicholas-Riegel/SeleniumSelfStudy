package com.seleniumstudy.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.testng.Assert;

public class Module2ProblemSet { 

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

    // Challenge 1.
    @Test
    public void loginSucceeds() throws InterruptedException {

        WebElement usernameField = driver.findElement(By.id("username"));
        usernameField.sendKeys("tomsmith");
        
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("SuperSecretPassword!");
        
        WebElement button = driver.findElement(By.cssSelector("#login button"));
        button.click();

        Thread.sleep(2000);

        WebElement welcomeElement = driver.findElement(By.cssSelector("#content h4"));
        String welcomeText = welcomeElement.getText();

        Assert.assertEquals(welcomeText, "Welcome to the Secure Area. When you are done click logout below.");
    }
    
    @Test
    public void loginFails() throws InterruptedException {

        WebElement usernameField = driver.findElement(By.id("username"));
        usernameField.sendKeys("tomsmith");
        
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("SuperSecretPassword");
        
        WebElement button = driver.findElement(By.cssSelector("#login button"));
        button.click();

        Thread.sleep(1000);

        WebElement errorElement = driver.findElement(By.id("flash"));
        String errorText = errorElement.getText();

        Assert.assertTrue(errorText.contains(errorText));
    }

    // Challenge 2. 
    @Test
    public void getAddress(){

        driver.get("https://demoqa.com/text-box");

        WebElement addressElement = driver.findElement(By.cssSelector("textarea[placeholder='Current Address']"));

        addressElement.sendKeys("This is my address.\nThere are many like it but this one is mine.");

        String address = addressElement.getAttribute("value");

        Assert.assertTrue(address.contains("This is my address."));
    }

    // Challenge 3.
    // Go to `https://www.saucedemo.com`. Log in with `standard_user` / `secret_sauce`. On the inventory page, find the **Add to cart** button for the first product using a CSS selector that combines a parent container class and a child element. Print the button's text using `getText()`.
    @Test
    public void cssDescendants() throws InterruptedException {

        driver.get("https://www.saucedemo.com");

        WebElement usernameField = driver.findElement(By.id("user-name"));
        usernameField.sendKeys("standard_user");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("secret_sauce");
        
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();

        Thread.sleep(1000);
        
        WebElement firstAddToCartButton = driver.findElement(By.cssSelector(".inventory_item:first-child button"));

        System.out.println(firstAddToCartButton.getText());

    }
}
