package com.seleniumstudy.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SecurePage extends BasePage {
    
    // - A private locator for the success flash message
    private final By successFlash = By.id("flash");
    private final By logoutButton = By.cssSelector("a[href='/logout']");
    
    public SecurePage(WebDriver driver){
        super(driver);
    }

    // - An `isLoaded()` method that returns `true` if the success message is visible
    public boolean isLoaded(){
        return getText(successFlash).contains("You logged into a secure area!");
    }
    
    // - A `getSuccessMessage()` method that returns its text
    public String getSuccessMessage(){
        return getText(successFlash);
    }

    public LoginPage logout(){
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(logoutButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        wait.until(ExpectedConditions.urlContains("/login"));
        return new LoginPage(driver);
    }

}
