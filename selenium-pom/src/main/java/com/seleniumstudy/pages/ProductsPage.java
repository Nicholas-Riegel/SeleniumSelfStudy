package com.seleniumstudy.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProductsPage extends BasePage {

    private final By pageTitle = By.cssSelector(".title");

    public ProductsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return getText(pageTitle).equals("Products");
    }
}