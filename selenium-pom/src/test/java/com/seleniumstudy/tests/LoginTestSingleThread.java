package com.seleniumstudy.tests;

import com.seleniumstudy.pages.LoginPage;
import com.seleniumstudy.pages.ProductsPage;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginTestSingleThread {

    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        driver = new ChromeDriver(options);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver.get("https://www.saucedemo.com");
        driver.manage().deleteAllCookies();
        // saucedemo stores login state in localStorage, not just cookies
        // JavascriptExecutor is covered in Module 7 — used here as a practical necessity
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
        driver.navigate().refresh();
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        if (driver != null) driver.quit();
    }

    @Test(groups = "smoke")
    public void successfulLoginLandsOnProductsPage() {
        LoginPage loginPage = new LoginPage(driver);
        ProductsPage productsPage = loginPage.loginAs("standard_user", "secret_sauce");
        assertThat(productsPage.isLoaded()).isTrue();
    }

    @Test(groups = "regression")
    public void invalidPasswordShowsError() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.loginAs("standard_user", "wrong_password");
        assertThat(loginPage.getErrorMessage()).isEqualTo("Epic sadface: Username and password do not match any user in this service");
    }

    @DataProvider(name = "loginScenarios")
    public Object[][] loginScenarios() {
        return new Object[][] {
            { "standard_user",  "secret_sauce", true  },
            { "locked_out_user","secret_sauce", false },
            { "standard_user",  "wrong_pass",   false }
        };
    }

    @Test(dataProvider = "loginScenarios", groups = "regression")
    public void loginScenario(String username, String password, boolean shouldSucceed) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.loginAs(username, password);

        if (shouldSucceed) {
            ProductsPage productsPage = new ProductsPage(driver);
            assertThat(productsPage.isLoaded()).isTrue();
        } else {
            assertThat(loginPage.getErrorMessage()).isNotEmpty();
        }
    }
}