package com.seleniumstudy.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Click an element, waiting until it's clickable first
    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    // Clear a field and type into it, waiting until it's visible first
    protected void type(By locator, String text) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        field.clear();
        field.sendKeys(text);
    }

    // Read the visible text of an element, waiting until it's visible first
    protected String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    // Scroll the page until this element is in view.
    // Uses presenceOfElementLocated rather than visibilityOfElementLocated because JavaScript
    // can work with elements that Selenium considers non-visible (e.g. off-screen elements).
    // presenceOfElementLocated just checks the element exists in the DOM — enough for JS to work with.
    protected void jsScrollToElement(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        // arguments[0] is how you pass a Java object into a JavaScript snippet.
        // Selenium wraps your JS string in an anonymous function and injects the WebElement
        // as the first argument. Inside the JS, arguments[0] IS that element as a live DOM node.
        // scrollIntoView(true) = align the element to the top of the viewport.
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    // Click an element using JavaScript — bypasses Selenium's interactability check.
    // Use sparingly: a JS click can "succeed" on an element a real user couldn't reach
    // (e.g. hidden under an overlay). Legitimate for specific edge cases, not a general
    // fix for clicks that fail with normal click().
    protected void jsClick(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        // arguments[0] is the WebElement, available as a DOM node inside the JS.
        // .click() here is a native DOM method, not Selenium's click() — no interactability check.
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    // Read the current value of an input field via JavaScript.
    // Why not getAttribute("value")? 
    // getAttribute reads the *initial* HTML attribute value (what was in the markup when the page loaded). 
    // The JS .value property reads the *live* current value — what's actually in the field right now, including anything typed by the user or set by JavaScript at runtime. 
    // More reliable for post-interaction field reads.
    protected String getValueWithJS(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        // "return" is required — executeScript only sends a value back to Java if you
        // explicitly return it from the JS snippet. Without it, you get null.
        // executeScript always returns Object, so we cast to String.
        return (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].value;", element);
    }
}