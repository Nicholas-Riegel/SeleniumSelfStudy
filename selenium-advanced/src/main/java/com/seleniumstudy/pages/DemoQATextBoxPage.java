package com.seleniumstudy.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class DemoQATextBoxPage extends BasePage {

    private final By submitButton = By.id("submit");
    private final By fullNameField = By.id("userName");
    private final By emailField    = By.id("userEmail");
    private final By outputSection = By.id("output");
    
    public DemoQATextBoxPage(WebDriver driver) {
        super(driver);
    }

    public void jsScrollToSubmitButton() {
        jsScrollToElement(submitButton);
    }

    public void fillForm(String name, String email) {
        type(fullNameField, name);
        type(emailField, email);
    }

    public void jsClickSubmitButton() {
        jsClick(submitButton);
    }

    public boolean isOutputVisible() {
        return wait.until(
            ExpectedConditions.visibilityOfElementLocated(outputSection)
        ) != null;
    }

    public String getNameViaAttribute() {
        return driver.findElement(By.id("userName")).getAttribute("value");
    }

    public String getNameViaJS() {
        return getValueWithJS(fullNameField);
    }
}