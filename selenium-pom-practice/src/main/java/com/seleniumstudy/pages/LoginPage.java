package com.seleniumstudy.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    // private final By loginButton   = By.cssSelector("button.radius[type='submit']");
    private final By loginButton   = By.tagName("button");
    private final By errorMessage  = By.id("flash");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // - A `loginAs(String username, String password)` method that returns a `SecurePage`
    public SecurePage loginAs(String username, String password){
        type(usernameField, username);
        type(passwordField, password);
        click(loginButton);
        return new SecurePage(driver);
    }

    // - A `getErrorMessage()` method
    public String getErrorMessage(){
        return getText(errorMessage);
    }

}
