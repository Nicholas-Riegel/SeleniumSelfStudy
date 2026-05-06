package com.seleniumstudy.tests;

import com.seleniumstudy.base.BaseTest;
import com.seleniumstudy.pages.LoginPage;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginTest extends BaseTest {

    @Test(groups = "smoke")
    public void successfulLoginLandsOnProductsPage() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.loginAs("standard_user", "secret_sauce");
        assertThat(loginPage.isOnProductsPage()).isTrue();
    }

    @Test(groups = "regression")
    public void invalidPasswordShowsError() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.loginAs("standard_user", "wrong_password");
        assertThat(loginPage.getErrorMessage())
            .isEqualTo("Epic sadface: Username and password do not match any user in this service");
    }
}