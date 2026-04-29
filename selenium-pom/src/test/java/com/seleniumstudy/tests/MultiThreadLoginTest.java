package com.seleniumstudy.tests;

import com.seleniumstudy.pages.LoginPage;
import com.seleniumstudy.pages.ProductsPage;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiThreadLoginTest {

    private ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        driver.set(new ChromeDriver(options));
        driver.get().get("https://www.saucedemo.com");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }

    @Test(groups = "smoke")
    public void successfulLoginLandsOnProductsPage() {
        LoginPage loginPage = new LoginPage(driver.get());
        ProductsPage productsPage = loginPage.loginAs("standard_user", "secret_sauce");
        assertThat(productsPage.isLoaded()).isTrue();
    }

    @Test(groups = "regression")
    public void invalidPasswordShowsError() {
        LoginPage loginPage = new LoginPage(driver.get());
        loginPage.loginAs("standard_user", "wrong_password");
        assertThat(loginPage.getErrorMessage()).isEqualTo("Epic sadface: Username and password do not match any user in this service");
    }

    @DataProvider(name = "loginScenarios", parallel = true)
    public Object[][] loginScenarios() {
        return new Object[][] {
            { "standard_user",  "secret_sauce", true  },
            { "locked_out_user","secret_sauce", false },
            { "standard_user",  "wrong_pass",   false }
        };
    }

    @Test(dataProvider = "loginScenarios", groups = "regression")
    public void loginScenario(String username, String password, boolean shouldSucceed) {
        LoginPage loginPage = new LoginPage(driver.get());
        loginPage.loginAs(username, password);

        if (shouldSucceed) {
            ProductsPage productsPage = new ProductsPage(driver.get());
            assertThat(productsPage.isLoaded()).isTrue();
        } else {
            assertThat(loginPage.getErrorMessage()).isNotEmpty();
        }
    }
}