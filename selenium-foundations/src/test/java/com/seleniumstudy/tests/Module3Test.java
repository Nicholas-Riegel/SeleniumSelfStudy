package com.seleniumstudy.tests;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Module3Test {

    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        // Each test gets a fresh browser — no URL here because different tests
        // will navigate to different pages
        driver = new ChromeDriver();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void sendKeysAppendsWithoutClear() {
        driver.get("https://www.saucedemo.com");

        WebElement usernameField = driver.findElement(By.id("user-name"));

        // First sendKeys — types "wrong_user" into the empty field
        usernameField.sendKeys("wrong_user");
        System.out.println("After first sendKeys:              " + usernameField.getAttribute("value"));
        // prints: wrong_user

        // Second sendKeys WITHOUT clear() — it APPENDS to what's already there
        usernameField.sendKeys("standard_user");
        System.out.println("After second sendKeys (no clear):  " + usernameField.getAttribute("value"));
        // prints: wrong_userstandard_user — definitely not what you wanted

        // The correct pattern: clear() first, then sendKeys()
        usernameField.clear();
        usernameField.sendKeys("standard_user");
        System.out.println("After clear() + sendKeys:          " + usernameField.getAttribute("value"));
        // prints: standard_user — correct
    }

    @Test
    public void submitWithEnterKey() {
        driver.get("https://www.saucedemo.com");

        driver.findElement(By.id("user-name")).sendKeys("standard_user");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("secret_sauce");

        // Instead of calling click() on the login button, we press Enter from
        // inside the password field. Many forms treat Enter as a submit trigger —
        // this tests that keyboard submission works, not just mouse clicks.
        // Keys.RETURN and Keys.ENTER are interchangeable — both send the Enter keystroke.
        passwordField.sendKeys(Keys.RETURN);

        // If the login worked, the URL will contain "inventory"
        System.out.println("URL after Enter key submit: " + driver.getCurrentUrl());
        Assert.assertTrue(driver.getCurrentUrl().contains("inventory"),
            "Expected URL to contain 'inventory' after login");
    }

    @Test
    public void getTextVsGetAttribute() {
        driver.get("https://www.saucedemo.com");

        // Attempt login with wrong credentials to trigger an error message
        driver.findElement(By.id("user-name")).sendKeys("wrong_user");
        driver.findElement(By.id("password")).sendKeys("wrong_pass");
        driver.findElement(By.id("login-button")).click();

        // getText() reads the visible text between the tags — the error message text
        WebElement errorMessage = driver.findElement(By.cssSelector("[data-test='error']"));
        String messageText = errorMessage.getText();
        System.out.println("getText():                   " + messageText);
        // prints: Epic sadface: Username and password do not match any user's credentials

        // Assert immediately after capturing — the error message should not be empty
        Assert.assertFalse(messageText.isEmpty(), "Error message should not be empty");

        // Now go to saucedemo and log in to get to the inventory page
        driver.findElement(By.id("user-name")).clear();
        driver.findElement(By.id("user-name")).sendKeys("standard_user");
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("secret_sauce");
        driver.findElement(By.id("login-button")).click();

        // getText() on a product title — reads the visible heading text
        WebElement firstProductTitle = driver.findElement(By.cssSelector(".inventory_item_name"));
        System.out.println("Product title getText():     " + firstProductTitle.getText());
        // prints: Sauce Labs Backpack (or whatever the first product is)
    }

    @Test
    public void getAttributeExamples() {
        driver.get("https://www.saucedemo.com");

        WebElement usernameField = driver.findElement(By.id("user-name"));

        // getAttribute("placeholder") — reads the hint text shown in an empty field
        // This is an HTML attribute on the tag, not visible content — getText() would return ""
        System.out.println("placeholder:  " + usernameField.getAttribute("placeholder"));
        // prints: Username

        // Type something in — then read it back with getAttribute("value")
        // getText() on an input always returns "" — inputs are self-closing tags with no text content
        usernameField.sendKeys("standard_user");
        System.out.println("value:        " + usernameField.getAttribute("value"));
        // prints: standard_user

        // Confirm getText() returns empty string for an input — not an error, just how inputs work
        System.out.println("getText():    '" + usernameField.getText() + "'");
        // prints: '' — empty string, because there's no text between <input> tags

        // getAttribute("type") — the type attribute on the input tag
        System.out.println("type:         " + usernameField.getAttribute("type"));
        // prints: text

        Assert.assertEquals(usernameField.getAttribute("placeholder"), "Username");
        Assert.assertEquals(usernameField.getAttribute("value"), "standard_user");
    }

    @Test
    public void isEnabledAndIsSelectedExample() {
        // the-internet's checkboxes page has two plain native checkboxes —
        // no custom styling, so isSelected() works directly on the input elements
        driver.get("https://the-internet.herokuapp.com/checkboxes");

        // Grab both checkboxes — the first is unchecked, the second is checked by default
        WebElement checkbox1 = driver.findElements(By.cssSelector("input[type='checkbox']")).get(0);
        WebElement checkbox2 = driver.findElements(By.cssSelector("input[type='checkbox']")).get(1);

        // isEnabled() — neither checkbox is disabled, so both should return true
        System.out.println("Checkbox 1 enabled:   " + checkbox1.isEnabled());
        // prints: true

        // isSelected() — checkbox 1 is unchecked by default, checkbox 2 is checked
        System.out.println("Checkbox 1 selected:  " + checkbox1.isSelected());
        // prints: false
        System.out.println("Checkbox 2 selected:  " + checkbox2.isSelected());
        // prints: true

        // Click checkbox 1 to select it, then verify the state changed
        checkbox1.click();
        System.out.println("Checkbox 1 after click: " + checkbox1.isSelected());
        // prints: true

        Assert.assertTrue(checkbox1.isEnabled());
        Assert.assertTrue(checkbox1.isSelected(), "Checkbox 1 should be selected after clicking");
        Assert.assertTrue(checkbox2.isSelected(), "Checkbox 2 should still be selected");
    }

    @Test
    public void selectDropdown() {
        // the-internet has a simple native <select> dropdown — perfect for this
        driver.get("https://the-internet.herokuapp.com/dropdown");

        // Find the <select> element, then wrap it in a Select object
        WebElement dropdownElement = driver.findElement(By.id("dropdown"));
        Select dropdown = new Select(dropdownElement);

        // Before selecting anything — read what's currently selected
        System.out.println("Default selected: " + dropdown.getFirstSelectedOption().getText());
        // prints: Please select an option

        // Select by visible text — the string must match exactly what's shown in the dropdown
        dropdown.selectByVisibleText("Option 1");
        System.out.println("After selectByVisibleText: " + dropdown.getFirstSelectedOption().getText());
        // prints: Option 1

        // Select by value — the value attribute on the <option> tag (inspect the HTML to find it)
        // <option value="2">Option 2</option> — the value is "2", the visible text is "Option 2"
        dropdown.selectByValue("2");
        System.out.println("After selectByValue: " + dropdown.getFirstSelectedOption().getText());
        // prints: Option 2

        // Print all available options to the console
        System.out.println("All options:");
        for (WebElement option : dropdown.getOptions()) {
            // getText() on each option gives the visible label
            System.out.println("  " + option.getText());
        }

        Assert.assertEquals(dropdown.getFirstSelectedOption().getText(), "Option 2");
    }

    @Test
    public void handleAlerts() {
        // the-internet has a dedicated page with all three alert types
        driver.get("https://the-internet.herokuapp.com/javascript_alerts");

        // --- Simple Alert ---
        // Click the button that triggers a basic alert (just an OK button)
        driver.findElement(By.cssSelector("button[onclick='jsAlert()']")).click();

        // Switch focus from the page to the alert dialog
        Alert alert = driver.switchTo().alert();

        // Read the message text — useful for asserting the right alert appeared
        System.out.println("Alert text: " + alert.getText());
        // prints: I am a JS Alert

        // Accept = click OK — dismisses the alert and returns focus to the page
        alert.accept();

        // --- Confirm Dialog ---
        // Click the button that triggers a confirm dialog (OK and Cancel)
        driver.findElement(By.cssSelector("button[onclick='jsConfirm()']")).click();

        Alert confirm = driver.switchTo().alert();
        System.out.println("Confirm text: " + confirm.getText());
        // prints: I am a JS Confirm

        // dismiss() = click Cancel
        confirm.dismiss();

        // The page shows the result of the action — check it reflects "cancelled"
        WebElement result = driver.findElement(By.id("result"));
        System.out.println("After cancel: " + result.getText());
        // prints: You clicked: Cancel

        // --- Prompt Dialog ---
        // Click the button that triggers a prompt (has a text input)
        driver.findElement(By.cssSelector("button[onclick='jsPrompt()']")).click();

        Alert prompt = driver.switchTo().alert();
        System.out.println("Prompt text: " + prompt.getText());
        // prints: I am a JS prompt

        // Type into the prompt input, then accept (OK)
        prompt.sendKeys("Hello from Selenium");
        prompt.accept();

        // The page displays whatever was typed into the prompt
        System.out.println("After prompt: " + driver.findElement(By.id("result")).getText());
        // prints: You entered: Hello from Selenium

        Assert.assertEquals(driver.findElement(By.id("result")).getText(), "You entered: Hello from Selenium");
    }

    @Test
    public void handleIframe() {
        // the-internet's iframe page embeds a TinyMCE rich text editor inside an iframe
        driver.get("https://the-internet.herokuapp.com/iframe");

        // At this point Selenium is on the outer page — the editor is not findable yet
        // because it lives inside the iframe's DOM

        // Step 1: find the iframe element on the outer page
        WebElement iframeElement = driver.findElement(By.id("mce_0_ifr"));

        // Step 2: switch into the iframe — now findElement searches the iframe's DOM
        driver.switchTo().frame(iframeElement);

        // Step 3: interact with elements inside the iframe
        // The editor's content area is a <body> element inside the iframe
        WebElement editorBody = driver.findElement(By.id("tinymce"));

        // Proof that we're inside the iframe: we can find and see this element.
        // (If we hadn't switched, findElement would throw NoSuchElementException.)
        // Typing into TinyMCE reliably requires JavaScript — covered in Module 7.
        System.out.println("Editor text: " + editorBody.getText());
        Assert.assertTrue(editorBody.isDisplayed(), "Editor body should be visible inside the iframe");

        // Step 4: switch back to the outer page — essential before touching anything outside
        driver.switchTo().defaultContent();

        // Now we can interact with elements on the outer page again
        // (e.g. a heading that lives outside the iframe)
        WebElement heading = driver.findElement(By.cssSelector("h3"));
        System.out.println("Outer page heading: " + heading.getText());
        // prints: An iFrame containing the TinyMCE WYSIWYG Editor
    }

    // -------------------------------------------------------------------------
    // Section 7: File Uploads
    // -------------------------------------------------------------------------
    // File inputs (<input type="file">) look scary but are straightforward:
    // skip the OS file picker entirely and just sendKeys() the absolute file path.
    // The browser silently accepts it as if the user had selected the file manually.
    @Test
    public void fileUpload() {
        driver.get("https://the-internet.herokuapp.com/upload");

        // Build an absolute path to our test resource file.
        // System.getProperty("user.dir") returns the Maven project root at runtime,
        // i.e. the selenium-foundations/ directory.
        String filePath = System.getProperty("user.dir")
                + "/src/test/resources/test-upload.txt";

        // Find the <input type="file"> and send the file path as a string.
        // This bypasses the OS file picker dialog — no robot/AutoIt needed.
        WebElement fileInput = driver.findElement(By.id("file-upload"));
        fileInput.sendKeys(filePath);

        // Click the upload button to submit
        driver.findElement(By.id("file-submit")).click();

        // After upload, the page shows a confirmation with the filename
        WebElement confirmationHeader = driver.findElement(By.cssSelector("h3"));
        WebElement uploadedFilename = driver.findElement(By.id("uploaded-files"));

        System.out.println("Confirmation: " + confirmationHeader.getText()); // "File Uploaded!"
        System.out.println("Uploaded file: " + uploadedFilename.getText());  // "test-upload.txt"

        Assert.assertEquals(confirmationHeader.getText(), "File Uploaded!");
        Assert.assertEquals(uploadedFilename.getText(), "test-upload.txt");
    }
}