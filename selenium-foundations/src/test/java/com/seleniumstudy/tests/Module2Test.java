package com.seleniumstudy.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class Module2Test {

    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        // Launches a fresh Chrome browser before each test
        driver = new ChromeDriver();
        // Navigate to saucedemo.com — our test site for this module
        driver.get("https://www.saucedemo.com");
    }

    @AfterMethod
    public void tearDown() {
        // Quits the browser after each test, even if the test failed
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void locateByIdAndName() {
        // By.id() — targets the element whose id attribute is "user-name"
        WebElement usernameField = driver.findElement(By.id("user-name"));
        usernameField.sendKeys("standard_user");

        // By.name() — targets the element whose name attribute is "password"
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("secret_sauce");

        // By.className() — targets the login button by one of its CSS classes
        WebElement loginButton = driver.findElement(By.className("submit-button"));
        loginButton.click();

        // After login, the URL changes to /inventory.html — print it to confirm
        System.out.println("URL after login: " + driver.getCurrentUrl());
    }

    @Test
    public void locateByClassName() {
        // The login page has an error logo / title element with class "login_logo"
        // By.className() finds it, then getText() reads its visible text
        WebElement logo = driver.findElement(By.className("login_logo"));
        String logoText = logo.getText();
        System.out.println("Logo text: " + logoText); // prints "Swag Labs"
    }

    @Test
    public void locateByTagName() {
        // By.tagName() with findElements() returns ALL matching elements as a List
        // Here we grab every <a> tag (hyperlink) on the login page
        List<WebElement> allLinks = driver.findElements(By.tagName("a"));

        // Print how many we found — demonstrates that tagName matches collections
        System.out.println("Number of links on login page: " + allLinks.size());
    }

    @Test
    public void locateByCssSelector() {
        // #user-name — ID selector, equivalent to By.id() but written as CSS
        WebElement usernameField = driver.findElement(By.cssSelector("#user-name"));
        usernameField.sendKeys("standard_user");

        // attribute selector — matches the input whose type attribute is "password"
        // useful when there's no clean ID or class to use
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        passwordField.sendKeys("secret_sauce");

        // multi-class selector — matches an element that has BOTH classes
        // this is where CSS beats By.className(), which can only handle one class
        WebElement loginButton = driver.findElement(By.cssSelector(".submit-button.btn_action"));
        loginButton.click();

        System.out.println("URL after login: " + driver.getCurrentUrl());
    }

    @Test
    public void locateByCssSelectorDescendant() {
        // Descendant selector: "parent child" (space between them)
        // This finds any <input> that lives anywhere inside an element with class "login-box"
        // It doesn't have to be a direct child — it can be nested several levels deep
        WebElement usernameField = driver.findElement(By.cssSelector(".login-box input[type='text']"));
        usernameField.sendKeys("standard_user");

        // Direct child selector: "parent > child" (> between them)
        // This finds a <div> that is a DIRECT child of the element with id "login_button_container"
        // Grandchildren don't count — only one level down
        WebElement buttonContainer = driver.findElement(By.cssSelector("#login_button_container > div"));

        // Print the outer HTML so we can see exactly what element was matched
        // getAttribute("outerHTML") returns the element's full HTML tag as a string
        System.out.println("Direct child div: " + buttonContainer.getAttribute("outerHTML"));
    }

    @Test
    public void locateByXpath() {
        // [@attribute='value'] — find an input where the id attribute equals 'user-name'
        // The @ symbol means "attribute" in XPath
        WebElement usernameField = driver.findElement(By.xpath("//input[@id='user-name']"));
        usernameField.sendKeys("standard_user");

        // contains(@attribute, 'partial') — partial attribute match
        // useful when IDs are dynamic or you only know part of the value
        WebElement passwordField = driver.findElement(By.xpath("//input[contains(@placeholder, 'Password')]"));
        passwordField.sendKeys("secret_sauce");

        // text() — find a button by its exact visible text
        // this is the killer feature CSS doesn't have
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Login']"));
        loginButton.click();

        System.out.println("URL after login: " + driver.getCurrentUrl());
    }

}