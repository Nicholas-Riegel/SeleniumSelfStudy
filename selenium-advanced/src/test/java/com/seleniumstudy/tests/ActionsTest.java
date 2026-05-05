package com.seleniumstudy.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

public class ActionsTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown(ITestResult result) throws IOException {

        // ITestResult is injected by TestNG — it tells us whether the test passed or failed
        if (result.getStatus() == ITestResult.FAILURE) {
            // Cast driver to TakesScreenshot — every ChromeDriver implements this interface
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            // Build a unique filename: testName + timestamp so files never overwrite each other
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String testName = result.getMethod().getMethodName();
            String fileName = testName + "_" + timestamp + ".png";

            // Save into selenium-advanced/screenshots/
            Path screenshotsDir = Paths.get("screenshots");
            Files.createDirectories(screenshotsDir);
            Files.copy(srcFile.toPath(), screenshotsDir.resolve(fileName));

            System.out.println("Screenshot saved: " + screenshotsDir.resolve(fileName).toAbsolutePath());
        }

        if (driver != null) driver.quit();
    }

    @Test
    public void hoverOverMenuShowsSubMenu() {

        driver.get("https://demoqa.com/menu");

        // Wait for the top-level "Main Item 2" link to be visible
        WebElement mainItem2 = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.linkText("Main Item 2"))
        );

        // Move the mouse over it — this triggers the CSS hover state
        // which makes the sub-menu appear
        new Actions(driver)
            .moveToElement(mainItem2)
            .perform();

        // "Sub Item" only becomes visible after hovering over Main Item 2
        WebElement subItem = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.linkText("Sub Item"))
        );

        assertThat(subItem.isDisplayed()).isTrue();
    }

    @Test
    public void dragAndDrop() {
        driver.get("https://the-internet.herokuapp.com/drag_and_drop");

        WebElement source = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("column-a"))
        );
        WebElement target = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("column-b"))
        );

        new Actions(driver)
            .dragAndDrop(source, target)
            .perform();

        String headerA = driver.findElement(By.cssSelector("#column-a header")).getText();

        assertThat(headerA).isEqualTo("B");
    }

    @Test
    public void keyboardComboSelectAllAndReplace() {
        
        driver.get("https://demoqa.com/text-box");

        WebElement nameField = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("userName"))
        );

        nameField.sendKeys("Old Text");

        // Cmd+A selects all text in the field (macOS uses Cmd, not Ctrl)
        new Actions(driver)
            .click(nameField)
            .keyDown(Keys.COMMAND)
            .sendKeys("a")
            .keyUp(Keys.COMMAND)
            .sendKeys("New Text")
            .perform();

        assertThat(nameField.getAttribute("value")).isEqualTo("New Text");
    }

    // This test is intentionally broken to prove that the screenshot mechanism works.
    // It asserts something wrong so TestNG marks it as FAILURE, triggering the screenshot.
    @Test
    public void deliberatelyFailingTest() {
        
        driver.get("https://demoqa.com/text-box");
        // The page title is "DEMOQA" — we assert something wrong on purpose
        assertThat(driver.getTitle()).isEqualTo("THIS WILL FAIL");
    }

    @Test
    public void switchToNewWindowAndBack() {

        driver.get("https://the-internet.herokuapp.com/windows");

        // Das aktuelle Fenster-Handle speichern, BEVOR wir auf den Link klicken.
        // Danach wissen wir immer, welches Fenster das "Original" ist.
        String originalWindow = driver.getWindowHandle();

        // Klick auf den Link, der ein neues Browserfenster öffnet
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Click Here"))).click();

        // Warten, bis Selenium zwei offene Fenster sieht.
        // Ohne dieses Wait kann getWindowHandles() noch das alte Set zurückgeben —
        // das neue Fenster ist noch nicht registriert.
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        // Alle offenen Fenster durchgehen und das neue (nicht-originale) finden
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        // Wir sind jetzt im neuen Fenster — alle weiteren Befehle gelten für dieses Fenster
        WebElement heading = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.tagName("h3"))
        );
        assertThat(heading.getText()).isEqualTo("New Window");

        // Nur das aktuelle (neue) Fenster schließen — nicht den ganzen Browser
        driver.close();

        // Zurück zum Original-Fenster wechseln — Pflicht nach driver.close(),
        // sonst ist driver in einem ungültigen Zustand
        driver.switchTo().window(originalWindow);

        // Bestätigen, dass wir wirklich wieder auf der Original-Seite sind
        assertThat(driver.getTitle()).isEqualTo("The Internet");
    }
}