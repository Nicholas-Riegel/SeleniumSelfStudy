package com.seleniumstudy.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.seleniumstudy.pages.DemoQATextBoxPage;

public class JavaScriptExecutorTest {

    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        driver = new ChromeDriver(options);
        driver.get("https://demoqa.com/text-box");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void scrollToSubmitButtonWithJS() {
        DemoQATextBoxPage page = new DemoQATextBoxPage(driver);
        page.jsScrollToSubmitButton();
    }

    @Test
    public void jsClickSubmitsForm() {

        DemoQATextBoxPage page = new DemoQATextBoxPage(driver);
        page.fillForm("Nicholas", "nicholas@example.com");
        page.jsClickSubmitButton();

        assertThat(page.isOutputVisible()).isTrue();
    }

    @Test
    public void compareAttributeVsJSValue() {
        DemoQATextBoxPage page = new DemoQATextBoxPage(driver);
        page.fillForm("Nicholas", "nicholas@example.com");

        String viaAttribute = page.getNameViaAttribute();
        String viaJS        = page.getNameViaJS();

        System.out.println("getAttribute(value): " + viaAttribute);
        System.out.println("getValueWithJS:      " + viaJS);

        assertThat(viaAttribute).isEqualTo("Nicholas");
        assertThat(viaJS).isEqualTo("Nicholas");
    }
}