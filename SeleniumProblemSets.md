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
