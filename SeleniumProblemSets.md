# Selenium with Java — Problem Sets

Practice challenges completed after each module. Attempt each problem before asking for the solution.

---

## Module 2 Problem Set — Locators

Create a new file at:
```
selenium-foundations/src/test/java/com/seleniumstudy/tests/Module2ProblemSet.java
```

Use the same `@BeforeMethod` / `@AfterMethod` pattern as `Module2Test`. Each challenge is one `@Test` method.

---

- [x] Challenge 1 — `By.id()` and `By.name()`

Go to `https://the-internet.herokuapp.com/login`. Find the username and password fields using `By.id()` and `By.name()` respectively. Type in any values, click the login button, and print the resulting `<h4>` error or success message text to the console.

*Hint: inspect the page to find the exact ID, name, and class values you need.*

---

- [x] Challenge 2 — CSS attribute selector

Go to `https://demoqa.com/text-box`. Locate the **Current Address** textarea using only a CSS attribute selector — no ID, no class. Type your address into it, then print the value back out using `getAttribute("value")`.

*Hint: look at the `placeholder` attribute on the textarea.*

---

- [x] Challenge 3 — CSS descendant + multi-class

Go to `https://www.saucedemo.com`. Log in with `standard_user` / `secret_sauce`. On the inventory page, find the **Add to cart** button for the first product using a CSS selector that combines a parent container class and a child element. Print the button's text using `getText()`.

*Hint: each product is wrapped in a container div with a class. The button lives inside that container.*

---

- [ ] Challenge 4 — XPath by text

Go to `https://the-internet.herokuapp.com/login`. Using XPath, find the **Login** button by its visible text (not by ID or class) and click it. Then use XPath `contains(text(), ...)` to find the error message element and print its text.

*Hint: inspect the button — is it a `<button>` or an `<input>`? That determines whether you use `text()` or `@value`.*

---

- [ ] Challenge 5 — XPath tree traversal: following-sibling

Go to `https://demoqa.com/text-box`. The **Full Name** label and its input are siblings inside a form row. Using XPath, locate the label whose text is `"Full Name"`, then traverse to its sibling `<input>` and type your name into it. Do not use the input's ID directly.

*Hint: `following-sibling::input` is what you need.*

---

- [ ] Challenge 6 — CSS descendant: multiple levels deep

Go to `https://the-internet.herokuapp.com/login`. Write a CSS selector that locates the username input by navigating *down* from a high-level ancestor — do not use the ID. Your selector should include at least two levels of descent (e.g. a `form` containing a `div` containing an `input`).

Print the input's `placeholder` attribute value using `getAttribute("placeholder")` to confirm you found the right one.

*Hint: inspect the page to understand the nesting. Use the descendant (space) operator, not the direct child (`>`) operator.*

---

- [ ] Challenge 7 — CSS direct child vs descendant

Go to `https://demoqa.com/text-box`. Write **two separate** locators for the same submit button:
1. One using the descendant selector (space) — starting from the form element
2. One using the direct child selector (`>`) — one level at a time

Print `"Descendant found: " + button.getText()` and `"Direct child found: " + button.getText()` for each. Both should print the same button text, confirming both selectors work.

*Hint: check how many levels deep the button is inside the form. If the button is not a direct child of the form, the `>` version may need to go via an intermediate element.*

---

- [ ] Challenge 8 — XPath parent traversal

Go to `https://www.saucedemo.com`. Log in, then on the inventory page find any product's **price element** (the element showing the `$` price). From that price element, traverse **up** to its parent container using `..`, then from the parent find the product name element as a descendant. Print the product name text.

This simulates a real pattern: you locate something easy to find (the price), then navigate to nearby elements you couldn't locate directly.

*Hint: use `/..//` to go up one level and then back down to a descendant.*

---

- [ ] Challenge 9 — XPath preceding-sibling

Go to `https://demoqa.com/text-box`. On this form, each field has a label and an input as siblings. Find the `<input>` for **Current Address** by starting at its label using `text()`, then navigate backwards using `preceding-sibling` — wait, labels come *before* inputs, so think about which direction to travel.

Actually: find the submit button at the bottom of the form. From the submit button, use `preceding-sibling` to find the **Email** input that comes before it in the DOM. Type a value into it.

*Hint: the submit button and the form fields may not be direct siblings — inspect the structure carefully. If they're not siblings, try `preceding::input[@id='userEmail']` instead, which searches all preceding elements (not just siblings).*

---

- [ ] Challenge 10 — XPath `contains()` on a dynamic-looking class

Go to `https://www.saucedemo.com`. Log in, then on the inventory page locate **all** product title elements using `findElements` and XPath. The class name on these elements contains the word `inventory` — use `contains(@class, 'inventory')` to match them without needing the exact full class name.

Print the text of every title found, one per line. This simulates working with auto-generated or compound class names where you only know part of the value.

*Hint: use `driver.findElements(By.xpath(...))` and loop over the results.*

---

## Module 3 Problem Set — Interacting with Elements

Create a new file at:
```
selenium-foundations/src/test/java/com/seleniumstudy/tests/Module3ProblemSet.java
```

Use the same `@BeforeMethod` / `@AfterMethod` pattern as `Module3Test`. Each challenge is one `@Test` method.

---

- [ ] Challenge 1 — click, sendKeys, clear (Section 1)

Go to `https://the-internet.herokuapp.com/login`. Type an incorrect username and password and click the login button. Assert that the error message is displayed. Then clear both fields, type the correct credentials (`tomsmith` / `SuperSecretPassword!`), and click login again. Assert that the URL changes to contain `"/secure"`.

*Hint: after a failed login the error element appears in the DOM — use `isDisplayed()` to confirm it. After a successful login there's no error, and the URL changes.*

---

- [ ] Challenge 2 — sendKeys with Keys (Section 1)

Go to `https://the-internet.herokuapp.com/login`. Type the correct username into the username field, then press `Tab` to move focus to the password field (using `Keys.TAB`). Type the correct password and submit the form using `Keys.RETURN` — do not click the button. Assert the page contains the text `"You logged into a secure area!"`.

*Hint: `sendKeys(Keys.TAB)` moves browser focus just like pressing the tab key. After tabbing, send the password to the password field — you still need to find it with `findElement` before calling `sendKeys`.*

---

- [ ] Challenge 3 — getText and getAttribute (Section 2)

Go to `https://www.saucedemo.com` and log in. On the inventory page, find the **first product**. Use `getText()` to print its name and price. Then find the **Add to cart** button for that product and use `getAttribute("class")` to print its full class attribute.

Then click the button. After clicking, use `getAttribute("class")` again on the same element and print the updated class. The class value should change after the item is added to the cart.

*Hint: the same WebElement reference stays valid after a click — you don't need to re-find it.*

---

- [ ] Challenge 4 — isEnabled and isSelected (Section 3)

Go to `https://the-internet.herokuapp.com/checkboxes`. There are two checkboxes. Assert that both are enabled. Then print each checkbox's `isSelected()` state. Click the one that is currently unchecked to check it, then assert both checkboxes are now selected.

*Hint: use `findElements()` to get all checkboxes at once, then access them by index.*

---

- [ ] Challenge 5 — Select dropdowns (Section 4)

Go to `https://the-internet.herokuapp.com/dropdown`. Select **Option 1** by visible text. Print the selected option's text. Then select **Option 2** by its value attribute. Print all available options using `getOptions()`, one per line. Assert that **Option 2** is the currently selected option.

*Hint: `getFirstSelectedOption().getText()` gives you what's currently selected.*

---

- [ ] Challenge 6 — Alerts (Section 5)

Go to `https://the-internet.herokuapp.com/javascript_alerts`. 

1. Click the **JS Confirm** button. Dismiss the alert (cancel). Assert that the result text on the page says `"You clicked: Cancel"`.
2. Click the **JS Prompt** button. Type your name into the prompt and accept it. Assert that the result text contains your name.

Both assertions should be in the same test method.

*Hint: after dismissing or accepting, the result message appears in a `<p id="result">` element on the page.*

---

- [ ] Challenge 7 — iframes (Section 6)

Go to `https://the-internet.herokuapp.com/iframe`. Switch into the TinyMCE iframe. Assert that the editor body element (`#tinymce`) is displayed. Then switch back to the outer page with `defaultContent()`. Find the `<h3>` heading on the outer page and assert its text equals `"An iFrame containing the TinyMCE WYSIWYG Editor"`.

Assert both things — that you were successfully inside the iframe, and that you successfully returned to the outer page.

*Hint: if you forget to call `defaultContent()` before finding the heading, you'll get `NoSuchElementException` because you're still inside the iframe's DOM.*

---

- [ ] Challenge 8 — File upload (Section 7)

Go to `https://the-internet.herokuapp.com/upload`. Reuse the `test-upload.txt` file from `src/test/resources/`. Upload it using `sendKeys()` on the file input — do not click the input or open any dialog. Click the upload button. Assert that the confirmation heading says `"File Uploaded!"` and that the displayed filename matches `"test-upload.txt"`.

Use `System.getProperty("user.dir")` to build the file path so it works on any machine.

*Hint: the file input has `id="file-upload"` and the submit button has `id="file-submit"`.*

---

## Module 4 Problem Set — Waits

Create a new file at:
```
selenium-foundations/src/test/java/com/seleniumstudy/tests/Module4ProblemSet.java
```

Use the same `@BeforeMethod` / `@AfterMethod` pattern as `Module4Test`. Each challenge is one `@Test` method. Use `WebDriverWait` + `ExpectedConditions` for all waits — no `Thread.sleep()`.

---

- [ ] Challenge 1 — `elementToBeClickable` before interaction

Go to `https://www.saucedemo.com`. Before typing into either field or clicking the login button, use `elementToBeClickable` to wait for each element. Log in with `standard_user` / `secret_sauce`. After clicking login, use `urlContains` to confirm the page transition to the inventory page completed. Print the final URL.

*Hint: reuse the same `WebDriverWait` instance for all three waits.*

---

- [ ] Challenge 2 — `visibilityOfElementLocated` after navigation

Go to `https://www.saucedemo.com` and log in. After confirming the URL change, wait for the inventory list to be visible using `visibilityOfElementLocated`. Once visible, find all product name elements and print each one's text. There should be 6 products.

*Hint: `findElements(By.className("inventory_item_name"))` after the wait.*

---

- [ ] Challenge 3 — `invisibilityOfElementLocated`

Go to `https://the-internet.herokuapp.com/dynamic_loading/1`. This page has a hidden element and a **Start** button. Click Start, then use `invisibilityOfElementLocated` to wait for the loading bar (`#loading`) to disappear. Once it's gone, find the result element (`#finish`) and assert its text equals `"Hello World!"`.

*Hint: the loading bar disappears when loading is complete — that's your signal that the result is ready.*

---

- [ ] Challenge 4 — `textToBePresentInElement`

Go to `https://the-internet.herokuapp.com/dynamic_loading/2`. Click Start. This time the result element doesn't exist at all until loading completes — it's added to the DOM dynamically. Wait using `visibilityOfElementLocated` for the `#finish` element to appear, then separately assert using `textToBePresentInElement` that the element contains the text `"Hello World!"`. Print the element's text.

*Hint: you'll need two separate `wait.until()` calls — one for visibility, one for the text check.*

---

- [ ] Challenge 5 — `alertIsPresent`

Go to `https://the-internet.herokuapp.com/javascript_alerts`. Click the **JS Alert** button. Use `alertIsPresent()` to wait for the alert before handling it. Accept the alert. Then wait for the result `<p id="result">` to be visible and assert its text equals `"You successfully clicked an alert"`.

*Hint: `alertIsPresent()` returns the `Alert` object — you can accept it directly from `wait.until()`.*

---

## Module 5 Problem Set — Page Object Model

Build a brand new Maven project for this — do not add to `selenium-pom`. Call it `selenium-pom-practice`. Set it up identically: Java 21, Selenium 4.20.0, TestNG 7.10.2.

The site is `https://the-internet.herokuapp.com`. You'll build the full POM structure from scratch.

### What to build

```
src/main/java/com/seleniumstudy/pages/
    BasePage.java
    LoginPage.java
    SecurePage.java

src/test/java/com/seleniumstudy/tests/
    LoginTest.java
```

No code is given upfront. Build each class yourself based on what you learned in Module 5. Ask for a hint or solution for any specific challenge once you've attempted it.

---

- [x] Challenge 1 — `BasePage`

Build `BasePage` from scratch. It should:
- Hold a `WebDriver` and `WebDriverWait` as `protected` fields
- Accept a `WebDriver` in its constructor and build the wait with a 10-second timeout
- Provide `click(By)`, `type(By, String)`, and `getText(By)` utility methods — each with the correct `ExpectedConditions` wait built in

Do not look at `selenium-pom`'s `BasePage`. Write it from memory.

---

- [x] Challenge 2 — `LoginPage`

Go to `https://the-internet.herokuapp.com/login` and inspect the page. Build `LoginPage` with:
- Private locators for the username field, password field, login button, and error message
- A `login(String username, String password)` method that returns a `SecurePage`
- A `getErrorMessage()` method

*Hint: the error message element has a specific class — inspect it in DevTools.*

---

- [x] Challenge 3 — `SecurePage`

Build `SecurePage` with:
- A private locator for the success flash message
- An `isLoaded()` method that returns `true` if the success message is visible
- A `getSuccessMessage()` method that returns its text

*Hint: the success message has `id="flash"`. Its text contains a newline character — `getText()` will include it. You may want to use `.contains()` for the assertion rather than exact equality.*

---

- [x] Challenge 4 — `LoginTest`

Write the test class with two tests:
1. `successfulLoginShowsSecurePage` — log in with `tomsmith` / `SuperSecretPassword!`, assert `SecurePage.isLoaded()` is true
2. `invalidLoginShowsError` — log in with bad credentials, assert the error message contains `"Your username is invalid!"`

Use `@BeforeMethod` / `@AfterMethod` for driver setup and teardown. Navigate to the login page in `@BeforeMethod`.

Run `mvn test` — both tests should pass.

---

- [x] Challenge 5 — Add a logout flow

`SecurePage` has a **Logout** button. Extend your page classes so a test can do this full flow:

```java
SecurePage securePage = loginPage.login("tomsmith", "SuperSecretPassword!");
LoginPage backToLogin = securePage.logout();
assertTrue(backToLogin.isLoaded());
```

You'll need to:
- Add a `logout()` method to `SecurePage` that clicks the logout button and returns a `LoginPage`
- Add an `isLoaded()` method to `LoginPage` that confirms the login form is visible

*Hint: `LoginPage.isLoaded()` can check for `visibilityOfElementLocated` on the login button.*
