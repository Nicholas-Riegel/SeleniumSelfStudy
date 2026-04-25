package com.seleniumstudy.tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumstudy.pages.LoginPage;
import com.seleniumstudy.pages.SecurePage;

public class LoginTest {

    WebDriver driver;

    // Use `@BeforeMethod` / `@AfterMethod` for driver setup and teardown. Navigate to the login page in `@BeforeMethod`.
    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.get("https://the-internet.herokuapp.com/login");
    }

    @AfterMethod
    public void tearDown(){
        if (driver != null) driver.quit();
    }

    // Write the test class with two tests:
    // 1. `successfulLoginShowsSecurePage` — log in with `tomsmith` / `SuperSecretPassword!`, assert `SecurePage.isLoaded()` is true
    @Test
    public void successfulLoginShowsSecurePage(){
        LoginPage loginPage = new LoginPage(driver);
        SecurePage securePage = loginPage.loginAs("tomsmith", "SuperSecretPassword!");
        Assert.assertTrue(securePage.isLoaded());
    }

    // 2. `invalidLoginShowsError` — log in with bad credentials, assert the error message contains `"Your username is invalid!"`
    @Test
    public void invalidLoginShowsError(){
        LoginPage loginPage = new LoginPage(driver);
        loginPage.loginAs("asdf", "asdsfd");
        Assert.assertTrue(loginPage.getErrorMessage().contains("Your username is invalid"));

    }

}
