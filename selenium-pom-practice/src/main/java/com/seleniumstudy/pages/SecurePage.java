package com.seleniumstudy.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;


public class SecurePage extends BasePage {
    
    // - A private locator for the success flash message
    private final By successFlash = By.id("flash");
    
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

}
