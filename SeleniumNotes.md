# Selenium with Java — Notes

Personal reference notes, built module by module alongside the syllabus.

---

## Module 1: How Selenium Actually Works

### Project Setup — Maven and `pom.xml`

**What does POM stand for?**
POM = *Project Object Model*. It's the configuration file Maven uses to understand your project.

**What is Maven?**
Maven is a build tool for Java. Its two main jobs are:
1. **Dependency management** — you declare what libraries you need, Maven downloads them automatically
2. **Build management** — it knows how to compile your code, run your tests, and package everything up

**What the wizard generates (and what's wrong with it)**

When you create a Maven project with VS Code's wizard, it generates a `pom.xml` with several problems:
- Java version set to `1.7` — ancient
- JUnit included as a dependency — we're using TestNG, not JUnit
- Selenium is missing entirely
- A bloated `<build>` block full of boilerplate plugins we don't need
- A `<url>` tag with a `FIXME` comment — just noise

**The correct `pom.xml` for this course, with every part explained:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
             http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <!-- Project identity — like a namespace + name + version -->
    <!-- groupId: your package namespace (convention: reverse domain) -->
    <!-- artifactId: the project name -->
    <!-- version: SNAPSHOT means "work in progress, not a final release" -->
    <groupId>com.seleniumstudy</groupId>
    <artifactId>selenium-foundations</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <!-- Everything is encoded as UTF-8 (handles special characters correctly) -->
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <!-- Compile with Java 21 — the current LTS version -->
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>

    <dependencies>

        <!-- selenium-java is a bundle — one dependency gives you WebDriver,
             ChromeDriver management, and everything else Selenium needs -->
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>4.20.0</version>
        </dependency>

        <!-- TestNG test framework -->
        <!-- scope=test means this is only available in test code,
             it won't be packaged into any output artifact -->
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>7.10.2</version>
            <scope>test</scope>
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <!-- maven-surefire-plugin is what actually runs your tests when
                 you do `mvn test`. We pin a modern version (3.2.5) because
                 the default version Maven uses is old and has issues with TestNG -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>
        </plugins>
    </build>

</project>
```

**Key things to remember:**
- The `com.something` package convention comes from reversing your domain name — it's a Java namespace tradition to guarantee uniqueness. For a self-study project the exact name doesn't matter.
- `scope>test` on TestNG is important — it means TestNG is only on the classpath during testing, not in any packaged output.
- The Java compiler source/target must match the JRE you have installed. If you have Java 21, set both to `21`.
- `maven-surefire-plugin` is the bridge between Maven and your tests. Without a modern version pinned, TestNG tests may not run correctly.

### How Selenium Actually Works

Your Java code does not talk directly to the browser. The chain is:

```
Your Java code
      ↓
  WebDriver API  (the Selenium library)
      ↓
  ChromeDriver  (a small local server)
      ↓
  Chrome browser
```

- The **WebDriver API** sends HTTP requests to **ChromeDriver**
- ChromeDriver translates those into real browser commands
- This follows the **W3C WebDriver Protocol** — a standard that all browser drivers implement
- That's why the same Selenium code works across Chrome, Firefox, Edge etc. (each has its own driver: GeckoDriver, EdgeDriver, etc.)

**Laravel Dusk comparison:** Dusk does exactly the same thing under the hood — it runs ChromeDriver and communicates with it via the same protocol. Selenium just gives you direct access to that layer instead of wrapping it in a PHP facade.

### `@BeforeMethod` and `@AfterMethod`

These TestNG annotations run automatically around every `@Test` method.

| Annotation | When it runs | Used for |
|---|---|---|
| `@BeforeMethod` | Before each test | Launching the browser |
| `@AfterMethod` | After each test (even if the test fails) | Quitting the browser |

**The problem without them**

Without `@BeforeMethod` / `@AfterMethod`, you have to create and quit the driver inside every single test method:

```java
@Test
public void openBrowser() {
    // Have to set up the driver in every test
    WebDriver driver = new ChromeDriver();
    driver.get("https://www.saucedemo.com");
    System.out.println("Page title: " + driver.getTitle());
    // Have to quit in every test — and if the test crashes before this line,
    // the browser stays open forever
    driver.quit();
}
```

This has two problems:
1. **Repetition** — every test repeats the same setup/teardown boilerplate
2. **Unreliable cleanup** — if the test throws an exception before `driver.quit()`, the browser process is left running

**The solution — `@BeforeMethod` / `@AfterMethod`**

Move setup and teardown out of the tests entirely. The driver becomes a class field so all three methods can share it:

```java
public class Module1Test {

    // Class field — shared between setUp, the test, and tearDown
    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        // Runs automatically before every @Test — launches a fresh browser
        driver = new ChromeDriver();
    }

    @AfterMethod
    public void tearDown() {
        // Runs automatically after every @Test — even if the test failed
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void openBrowser() {
        // No setup or teardown here — just the actual test logic
        driver.get("https://www.saucedemo.com");
        System.out.println("Page title: " + driver.getTitle());
    }

}
```

The flow for each test:
```
@BeforeMethod (setUp)   → launches Chrome
@Test                   → your actual test logic
@AfterMethod (tearDown) → quits Chrome, no matter what
```

**Why the null check in tearDown?**

If `setUp` itself fails (e.g. Chrome can't launch), `driver` will still be null. Calling `driver.quit()` on null would throw a `NullPointerException` — a second error on top of the first. The null check prevents that.

---

## Module 2: Locators

### Section 1: The `By` Class

#### How Selenium locators differ from Dusk

In Laravel Dusk, you pass a raw string directly into a method:

```php
$browser->click('#username');
```

Selenium is more explicit. It has a class called `By` that represents a *strategy + value* pair. You don't pass a raw string — you pass a `By` object:

```java
driver.findElement(By.id("username"));           // locate by ID attribute
driver.findElement(By.cssSelector("#username")); // locate using a CSS selector
driver.findElement(By.xpath("//input[@id='username']")); // locate using XPath
```

Every `By.something(...)` call returns a `By` object. `findElement()` accepts a `By`. That's the contract.

#### `findElement` vs `findElements`

| Method | Returns | Throws if nothing found? |
|---|---|---|
| `findElement(By)` | A single `WebElement` | Yes — `NoSuchElementException` |
| `findElements(By)` | A `List<WebElement>` | No — returns an empty list |

- Use `findElement` most of the time — you're locating one specific element
- Use `findElements` when you want to check *if* something exists (empty list = not there), or when grabbing a collection like table rows

#### What is a `WebElement`?

`findElement` returns a `WebElement` — Selenium's Java representation of a DOM element. Once you have it, you interact with it:

```java
// findElement returns a WebElement, stored in a variable
WebElement usernameField = driver.findElement(By.id("user-name"));

usernameField.sendKeys("standard_user"); // type text into the element
usernameField.click();                   // click the element
usernameField.getText();                 // read the element's visible text content
```

The pattern is always the same:

```
driver.findElement(By.someStrategy("someValue"))  →  WebElement  →  do something with it
```

All the different locator strategies (ID, CSS, XPath) are just different ways to construct that `By` object. The rest of Module 2 is about learning those strategies.

---

### Section 2: ID, Name, Class, and Tag Name

#### How to inspect elements in DevTools

Before writing any locator, you need to find the right value. Open Chrome, go to `https://www.saucedemo.com`, then:

- Right-click any element → **Inspect**
- Or press `F12` / `Cmd+Option+I` to open DevTools, then use the element picker (cursor icon, top-left of DevTools) to click any element on the page

You'll see the HTML for that element in the **Elements** tab. That's where you find IDs, class names, names, and tags.

#### `By.id()`

The best locator when it's available. IDs are meant to be unique on a page — no ambiguity, always the right element.

On saucedemo.com the username field HTML:

```html
<input type="text" id="user-name" ...>
```

```java
driver.findElement(By.id("user-name")); // "user-name" is the value of the id attribute
```

**Rule:** Use `By.id()` whenever an element has a stable, meaningful ID. It's fast and unambiguous. Always your first choice.

#### `By.name()`

Targets the `name` attribute. Common on form inputs — it's the attribute that gets submitted with HTTP POST requests.

On saucedemo.com the password field:

```html
<input type="password" id="password" name="password" ...>
```

```java
driver.findElement(By.name("password")); // "password" is the value of the name attribute
```

**Rule:** Use `By.name()` on form fields that have a `name` attribute but no useful `id`.

#### `By.className()`

Targets elements by their CSS class. Many elements often share the same class, so this can match more than one element.

On saucedemo.com the login button:

```html
<input type="submit" class="submit-button btn_action" value="Login" ...>
```

```java
driver.findElement(By.className("submit-button")); // pass ONE class name only
```

**Important:** `By.className()` does not accept a space-separated list. Passing `"submit-button btn_action"` will throw an exception. If you need to match on multiple classes, use a CSS selector (Section 3).

**Rule:** Use `By.className()` only when a class is unique enough to pinpoint one element. For multi-class matching, switch to CSS.

#### `By.tagName()`

Targets elements purely by their HTML tag (`input`, `button`, `h1`, `a`, etc.). Almost always matches many elements, so it's rarely used with `findElement`. It shines with `findElements` when you want a whole collection.

```java
// Get every link on the page — tagName is perfect for collections like this
List<WebElement> allLinks = driver.findElements(By.tagName("a"));
System.out.println("Number of links: " + allLinks.size());
```

**Rule:** Don't use `By.tagName()` with `findElement` — you'll just get whichever matching element happens to be first in the DOM. Use it with `findElements` to grab collections.

#### Summary

| Strategy | What it targets | When to use it |
|---|---|---|
| `By.id()` | The `id` attribute | First choice — always prefer this if available |
| `By.name()` | The `name` attribute | Form inputs without a useful `id` |
| `By.className()` | A single CSS class | When a class uniquely identifies one element |
| `By.tagName()` | The HTML tag type | With `findElements` to grab collections |

---

### Section 3: CSS Selectors

CSS selectors are the most important locator strategy to master. They're more powerful than `By.id()` / `By.className()`, more readable than XPath, and faster to write. Most experienced automation engineers reach for CSS first. The syntax is identical to CSS you'd write for styling.

#### Core syntax

```css
input                        /* any <input> element — by tag */
#user-name                   /* element with id="user-name" — the # means ID */
.submit-button               /* element with class="submit-button" — the . means class */
.submit-button.btn_action    /* element that has BOTH classes — chain with no space */
input[type="text"]           /* <input> whose type attribute equals "text" */
input[placeholder="Username"] /* <input> whose placeholder attribute equals "Username" */
div.login_wrapper input      /* any <input> anywhere inside a div.login_wrapper — space means descendant */
form > input                 /* <input> that is a direct child of a <form> — > means direct child only */
```

#### How to test a selector in DevTools before writing code

1. Open DevTools (`F12`)
2. Go to the **Console** tab
3. Type: `document.querySelector("your-selector-here")`
4. If it highlights an element in the DOM panel — your selector works
5. If it returns `null` — it didn't match anything

This lets you validate a selector in seconds before putting it in your test code.

#### In Selenium

All CSS selectors go into `By.cssSelector()`:

```java
// ID selector — equivalent to By.id("user-name") but written as CSS
driver.findElement(By.cssSelector("#user-name"));

// Attribute selector — useful when there's no clean ID or class to target
driver.findElement(By.cssSelector("input[placeholder='Username']"));

// Multi-class selector — matches elements that have BOTH classes
// this is where CSS beats By.className(), which can only handle one class
driver.findElement(By.cssSelector(".submit-button.btn_action"));

// Descendant selector — input anywhere inside a div with class login_wrapper
driver.findElement(By.cssSelector("div.login_wrapper input"));
```

#### The selectors you'll use 90% of the time

| Selector | Example | What it matches |
|---|---|---|
| `#id` | `#user-name` | Element with that ID |
| `.class` | `.submit-button` | Element with that class |
| `.class1.class2` | `.submit-button.btn_action` | Element with **both** classes |
| `tag[attr="val"]` | `input[type="submit"]` | Tag with a matching attribute value |
| `parent child` | `form input` | Input anywhere inside a form |

#### The test

```java
@Test
public void locateByCssSelector() {
    // #user-name — ID selector, equivalent to By.id() but written as CSS
    WebElement usernameField = driver.findElement(By.cssSelector("#user-name"));
    usernameField.sendKeys("standard_user");

    // attribute selector — matches the input whose type attribute is "password"
    // useful when there's no clean ID or class to use
    WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
    passwordField.sendKeys("secret_sauce");

    // multi-class selector — matches an element that has BOTH classes
    // this is where CSS beats By.className(), which can only handle one class
    WebElement loginButton = driver.findElement(By.cssSelector(".submit-button.btn_action"));
    loginButton.click();

    System.out.println("URL after login: " + driver.getCurrentUrl());
}
```

---

### Section 4: XPath

#### What is XPath?

XPath (XML Path Language) is a query language for navigating XML — and since HTML is a tree of nodes, it works on HTML too. Where CSS selectors describe elements by their appearance (class, ID, attributes), XPath describes elements by their **position in the tree** and their **text content**.

Text content is XPath's killer feature. CSS cannot select by text. XPath can. That's the main reason you reach for XPath when CSS isn't enough.

#### Absolute vs relative — never use absolute

```xpath
/html/body/div/input   ← absolute — starts from the root, every step must match exactly
//input                ← relative — find an <input> anywhere in the document
```

Never write absolute XPath. One DOM change anywhere above your target breaks it completely. Always start with `//`.

#### Core syntax

```xpath
//input                              ← any <input> anywhere in the document
//input[@id='user-name']             ← <input> where id attribute equals 'user-name' — @ means "attribute"
//input[@type='text']                ← <input> where type attribute equals 'text'
//input[@placeholder='Username']     ← <input> where placeholder attribute equals 'Username'
//button[text()='Login']             ← <button> whose exact visible text is 'Login' — CSS cannot do this
//button[contains(text(), 'Log')]    ← <button> whose text contains 'Log' — partial match
//input[contains(@id, 'user')]       ← <input> whose id attribute contains 'user' — partial attribute match
```

#### Tree traversal — moving in all directions

This is where XPath genuinely beats CSS. CSS can only move *downward* through the tree. XPath can move *up*, *sideways*, and *downward*.

Consider this HTML:

```html
<div class="form-group">
  <label for="user-name">Username</label>
  <input id="user-name" type="text" />
  <span class="error-msg">Required field</span>
</div>
```

**Moving down (child / descendant)**
```xpath
//div[@class='form-group']/input         ← direct child <input> of that div (/ = one level down)
//div[@class='form-group']//input        ← any descendant <input> inside that div (// = any depth)
```

**Moving sideways (siblings)**
```xpath
//label[@for='user-name']/following-sibling::input
```
Start at the `<label>` with `for='user-name'`, then move to the next `<input>` sibling that comes *after* it in the DOM.

```xpath
//span[@class='error-msg']/preceding-sibling::input
```
Start at the `<span>`, then move to the `<input>` sibling that comes *before* it.

| Axis | Direction | Example |
|---|---|---|
| `following-sibling::tag` | Forward through siblings | `//label/following-sibling::input` |
| `preceding-sibling::tag` | Backward through siblings | `//span/preceding-sibling::input` |
| `following-sibling::*` | Any forward sibling | `//label/following-sibling::*` |

**Moving up (parent)**
```xpath
//input[@id='user-name']/..
```
The `..` means "go up one level to the parent". Starting at the `<input>`, this returns the `<div class="form-group">` that contains it.

```xpath
//input[@id='user-name']/parent::div
```
Same thing, but more explicit — goes to the parent and confirms it's a `<div>`. Fails if the parent is a different tag.

**Combining traversal — a real-world example**

Imagine you want the error message span that lives in the same group as a specific input, but there's no useful class or ID on the span:

```xpath
//input[@id='user-name']/following-sibling::span
```
Start at the known input, move sideways to the span. This is far more stable than trying to guess the span's position from the top of the document.

#### When to use XPath vs CSS

| Situation | Use |
|---|---|
| There's a usable ID, class, or attribute | CSS |
| You need to match by visible text | XPath |
| You need to navigate to a parent | XPath (`/..` or `/parent::tag`) |
| You need to navigate to a sibling | XPath (`/following-sibling::` or `/preceding-sibling::`) |
| IDs are auto-generated with predictable parts | XPath `contains(@id, 'part')` |
| Everything else | CSS |

#### Testing XPath in DevTools

In the Chrome DevTools console, use `$x()` — Chrome's built-in XPath evaluator:

```javascript
$x("//input[@id='user-name']")   // returns an array — if not empty, your XPath matched
```

#### In Selenium

```java
driver.findElement(By.xpath("//input[@id='user-name']"));
driver.findElement(By.xpath("//input[contains(@placeholder, 'Password')]"));
driver.findElement(By.xpath("//label[@for='user-name']/following-sibling::input"));
driver.findElement(By.xpath("//input[@id='user-name']/.."));  // parent element
```

#### The test

```java
@Test
public void locateByXpath() {
    // [@attribute='value'] — find an input where the id attribute equals 'user-name'
    // The @ symbol means "attribute" in XPath
    WebElement usernameField = driver.findElement(By.xpath("//input[@id='user-name']"));
    usernameField.sendKeys("standard_user");

    // contains(@attribute, 'partial') — partial attribute match
    // useful when IDs are dynamic or you only know part of the value
    WebElement passwordField = driver.findElement(By.xpath("//input[contains(@placeholder, 'Password')]"));
    passwordField.sendKeys("secret_sauce");

    // The login button on saucedemo is <input type="submit"> — its text is in the
    // value attribute, not the tag body. So we use @value rather than text()
    // This is why you always inspect the HTML before writing XPath
    WebElement loginButton = driver.findElement(By.xpath("//input[@value='Login']"));
    loginButton.click();

    System.out.println("URL after login: " + driver.getCurrentUrl());
}
```

---

### Section 5: Fragile vs Reliable Locators

This is the section that separates someone who can *write* locators from someone who can *think* about them.

#### What makes a locator fragile?

A locator is fragile if it breaks when the page changes in a way that has nothing to do with the feature you're testing. The test fails — but not because anything is broken. It's a false failure, and it erodes trust in your test suite.

**Auto-generated IDs — the worst offender**

Some front-end frameworks (Angular, React, older JSF apps) generate IDs at runtime:

```html
<input id="j_id0:j_id23:username" ...>
<input id="mat-input-47" ...>
```

The number in `mat-input-47` increments every time the component tree changes. Today it's `47`. After a developer adds a new form field somewhere else — it's `51`. Your locator is broken, even though the username field itself is completely unchanged.

**How to spot them:** if the ID contains a number with no obvious meaning, treat it as auto-generated and find something else.

**Absolute XPath**

```xpath
/html/body/div[2]/div/form/div[1]/input
```

Breaks if *anything* in the DOM above your element changes — a wrapper div is added, the layout shifts, a class changes. The element is still there. Your locator is dead.

**Positional CSS**

```css
div:nth-child(3)
```

"The third child of its parent." If someone inserts a new element before it, your selector silently points at the wrong element — no error thrown, just wrong behaviour. Worse than a crash.

#### What makes a locator reliable?

Reliable locators are anchored to something that reflects *intent* — something a developer would only change if the feature itself changed.

**In order of preference:**

1. **`data-testid` attribute** — e.g. `data-testid="login-button"`. Added specifically for automation, no styling or functional purpose. Won't change when design or text changes.
2. **Meaningful, stable ID** — e.g. `id="user-name"`. Not auto-generated, clearly named for this specific element.
3. **Meaningful class with context** — e.g. `.submit-button` inside `form.login-form`. The combination is specific enough.
4. **Descriptive attribute** — e.g. `input[placeholder='Username']`, `button[aria-label='Close']`. Changes only if the element's purpose changes.
5. **XPath by text** — e.g. `//button[text()='Add to Cart']`. If the button text changes, the feature arguably changed too.
6. **XPath tree traversal** — anchored to a stable parent, navigated to child or sibling.

At the bottom: auto-generated IDs, absolute XPath, positional selectors.

#### The `data-testid` pattern

The most robust approach in modern teams is adding custom attributes purely for testing:

```html
<button data-testid="login-button">Login</button>
```

```java
// Attribute selector with no tag — matches any element with this data-testid value
driver.findElement(By.cssSelector("[data-testid='login-button']"));
```

This attribute has no styling or functional purpose — it exists only for automation. It won't change when design, class, or text changes. Advocating for `data-testid` attributes is a mark of a senior automation engineer.

#### The one-sentence rule

> A good locator breaks when the *element's purpose changes*, not when the *page's appearance changes*.

---

## Module 3: Interacting with Elements

### Section 1: `click()`, `sendKeys()`, `clear()`

#### `sendKeys(text)`

Simulates real keystrokes into a field. **It appends — it does not replace.** If a field already contains text and you call `sendKeys("hello")`, you get whatever was there before plus `"hello"` concatenated together.

#### `clear()`

Wipes the field's current value and fires the appropriate DOM events (important — some apps listen for those events to trigger validation). **Always call `clear()` before `sendKeys()` if there's any chance the field already has content.** Only works on editable fields (`input`, `textarea`) — not on read-only elements.

The correct pattern for typing into a field safely:
```java
field.clear();
field.sendKeys("your value");
```

#### `click()`

Clicks the centre of the element. Two specific failure modes worth knowing by name:

- **`ElementNotInteractableException`** — the element exists in the DOM but is hidden (`display: none`, zero height/width). Selenium refuses to click invisible elements because a real user couldn't either.
- **`ElementClickInterceptedException`** — the element is visible, but something is overlapping it (cookie banner, sticky header, modal). Selenium finds your element, but the click lands on whatever is on top.

#### The `Keys` enum

`sendKeys()` also accepts special keystroke constants from the `Keys` class — not just strings:

```java
Keys.TAB          // move focus to the next field
Keys.ENTER        // press Enter (same as RETURN — use either)
Keys.RETURN       // press Enter
Keys.BACK_SPACE
Keys.ESCAPE
Keys.chord(Keys.CONTROL, "a")  // Ctrl+A — select all (Windows/Linux)
Keys.chord(Keys.COMMAND, "a")  // Cmd+A — select all (Mac)
```

The most common use: pressing Enter to submit a form without locating the submit button. This also tests that keyboard submission works — important for accessibility.

**Key interview points:**
- `sendKeys` appends — always `clear()` first if the field might have existing content
- `ElementNotInteractableException` = element is hidden; `ElementClickInterceptedException` = something is overlapping it
- `Keys.RETURN` / `Keys.ENTER` let you submit forms via keyboard — tests accessibility as well as functionality

---

### Section 2: `getText()` and `getAttribute()`

These two methods look similar but read completely different things.

#### `getText()`

Returns the **visible text content** of an element — what a user would see on the page. Equivalent to the text between an element's opening and closing tags.

Two important behaviours:
- **Strips leading/trailing whitespace automatically** — no need to call `.trim()`
- **Returns an empty string `""` if the element is hidden** — not an exception, just empty. Unexpected empty strings usually mean the element is there but not visible.

#### `getAttribute(name)`

Returns the value of an **HTML attribute** on the element — the things written inside the opening tag: `id`, `class`, `href`, `placeholder`, `value`, `type`, `disabled`, `checked`, etc.

```java
element.getAttribute("placeholder"); // the hint text shown in an empty field
element.getAttribute("value");       // what's currently typed in an input field
element.getAttribute("href");        // the URL a link points to
element.getAttribute("type");        // e.g. "text", "password", "submit"
element.getAttribute("checked");     // "true" if checked, null if not
```

#### The critical difference

| Method | Reads | Example use |
|---|---|---|
| `getText()` | Visible text between tags | Error messages, headings, button labels |
| `getAttribute("value")` | The `value` attribute | What's typed in an input field |
| `getAttribute("href")` | The `href` attribute | Verifying a link's URL |
| `getAttribute("placeholder")` | The `placeholder` attribute | Confirming hint text on an empty field |

**The trap:** `<input>` is a self-closing tag — there's nothing between the tags, so `getText()` on an input always returns `""`. To read what's typed in a field, you must use `getAttribute("value")`.

#### `getAttribute()` on boolean attributes

Some attributes are boolean — present or absent, with no value:

```html
<input type="checkbox" checked>   <!-- checked is present -->
<input type="checkbox">           <!-- checked is absent -->
```

- Attribute **present** → `getAttribute("checked")` returns `"true"`
- Attribute **absent** → returns `null` (not `"false"` — null)

Always check for `null`, not for the string `"false"`.

**Key interview points:**
- `getText()` = visible text; `getAttribute("value")` = what's in an input field — these are the two most commonly confused
- `getText()` returns `""` on hidden elements and on inputs — not an exception
- Boolean attributes return `"true"` when present, `null` when absent

---

### Section 3: `isDisplayed()`, `isEnabled()`, `isSelected()`

These three methods check the **state** of an element. They all return a `boolean`.

#### `isDisplayed()`

Returns `true` if the element is visible to the user — rendered on screen, taking up space. Returns `false` if hidden via CSS (`display: none`, `visibility: hidden`, zero dimensions, etc.).

**Critical distinction:** `isDisplayed()` requires the element to already exist in the DOM. It answers "is this existing element visible?" — not "does this element exist?". If the element isn't in the DOM at all, `findElement` will throw `NoSuchElementException` before you even get to call `isDisplayed()`. When an element might not be in the DOM at all, use `findElements` (plural) and check if the list is empty.

Common use: asserting that an error message or success banner appeared after an action.

#### `isEnabled()`

Returns `true` if the element is interactive — i.e. not disabled. Returns `false` if the element has the `disabled` HTML attribute.

Common use: asserting that a Submit button is greyed out until required fields are filled.

#### `isSelected()`

Returns `true` if the element is currently selected. Only meaningful for three element types:
- Checkboxes — is it ticked?
- Radio buttons — is it the active option?
- `<option>` elements inside a `<select>` — is it the chosen option?

Calling `isSelected()` on a regular button or text input is meaningless — it just returns `false`.

**Prefer `isSelected()` over `getAttribute("checked")`** for checkboxes and radio buttons — it returns an actual `boolean` instead of a string or null.

#### Getting elements by index from `findElements`

When a page has multiple identical elements (like a list of checkboxes), use `findElements` to get the whole list, then `.get(index)` to pull out a specific one:

```java
// Returns a List<WebElement> of all checkboxes on the page
// .get(0) = first, .get(1) = second — zero-indexed like all Java lists
WebElement checkbox1 = driver.findElements(By.cssSelector("input[type='checkbox']")).get(0);
WebElement checkbox2 = driver.findElements(By.cssSelector("input[type='checkbox']")).get(1);
```

**Key interview points:**
- `isDisplayed()` = is it visible; `isEnabled()` = is it interactive; `isSelected()` = is it ticked/chosen
- `isDisplayed()` requires the element to exist in the DOM first — it can't detect a missing element
- `isSelected()` is only meaningful on checkboxes, radio buttons, and `<option>` elements
- Prefer `isSelected()` over `getAttribute("checked")` — returns a real boolean, no null handling needed

---

### Section 4: Dropdowns — the `Select` class

#### Why dropdowns need special treatment

A native HTML dropdown uses a `<select>` tag containing `<option>` tags. You can't interact with it cleanly using just `click()` and `sendKeys()` — clicking opens it, but then you'd need to individually locate and click each `<option>`, which is fragile. Selenium provides a dedicated `Select` wrapper class for this.

**Important:** `Select` only works with native `<select>` elements. Many modern sites use custom dropdowns built from `<div>`s and `<ul>`s styled to look like dropdowns — those are just regular elements and need `click()` like anything else. Always inspect before assuming.

#### Using the `Select` class

Wrap the `<select>` `WebElement` in a `Select` object, then call methods on that:

```java
// 1. Find the <select> element as usual
WebElement dropdownElement = driver.findElement(By.id("dropdown"));

// 2. Wrap it in a Select — this unlocks the dropdown-specific methods
Select dropdown = new Select(dropdownElement);
```

Three ways to choose an option:

```java
// By visible text — what the user sees in the dropdown (must match exactly)
dropdown.selectByVisibleText("Option 1");

// By value — the value attribute on the <option> tag, not the displayed text
// <option value="2">Option 2</option> — the value is "2"
dropdown.selectByValue("2");

// By index — zero-based position in the list (0 = first option)
dropdown.selectByIndex(0);
```

Two useful reading methods:

```java
// Get the currently selected option as a WebElement, then read its text
dropdown.getFirstSelectedOption().getText();

// Get all options as a List<WebElement> — useful for looping
dropdown.getOptions();
```

**Which selection method to use?**
- `selectByVisibleText` — most readable; use when text is stable
- `selectByValue` — more robust when the displayed text might change but the underlying value won't
- `selectByIndex` — fragile (order can change); only use when text and value are both unreliable

The import needed: `import org.openqa.selenium.support.ui.Select;`

**Key interview points:**
- `Select` only works on native `<select>` elements — custom dropdowns need `click()` instead
- `selectByVisibleText` must match exactly what's shown — no partial matches
- `getFirstSelectedOption().getText()` is how you read what's currently selected
- `selectByValue` targets the HTML `value` attribute, not the visible text — inspect the HTML to find it

---

### Section 5: Alerts and Browser Dialogs

#### What are browser alerts?

Browser alerts are native OS-level dialog boxes — they're not part of the HTML page. Selenium can't reach them with `findElement` because they exist outside the DOM entirely.

| Type | Description | Buttons |
|---|---|---|
| **Alert** | Simple message — "Something happened." | OK |
| **Confirm** | Yes/no question — "Are you sure?" | OK / Cancel |
| **Prompt** | Asks for input — "Enter your name:" | OK / Cancel + text field |

#### `switchTo().alert()`

When an alert is open, switch Selenium's focus to it:

```java
Alert alert = driver.switchTo().alert();
```

Methods on the `Alert` object:

```java
alert.getText();          // read the message text shown in the dialog
alert.accept();           // click OK
alert.dismiss();          // click Cancel (or close)
alert.sendKeys("text");   // type into a prompt input only
```

**Critical rule:** If an alert is open and you try to interact with the page without handling it first, Selenium throws `UnhandledAlertException`. Always handle the alert before doing anything else.

The import needed: `import org.openqa.selenium.Alert;`

**Key interview points:**
- Alerts are outside the DOM — `findElement` can't reach them, `switchTo().alert()` is the only way in
- `accept()` = OK, `dismiss()` = Cancel
- `sendKeys()` on an alert only works for prompt dialogs — it types into the prompt's input field
- Leaving an alert unhandled causes `UnhandledAlertException` on the next page interaction

---

### Section 6: iframes

#### What is an iframe?

An `<iframe>` is an HTML element that embeds a completely separate HTML document inside the current page. It has its own DOM — Selenium can't find elements inside it with a normal `findElement` call from the outer page.

#### The problem iframes create

When you call `driver.findElement(...)`, Selenium searches the **current browsing context** (the active DOM). If your target element is inside an iframe, it's in a *different* DOM — invisible to Selenium until you switch into it.

#### Switching into an iframe

There are three overloads of `switchTo().frame()`:

```java
driver.switchTo().frame(WebElement iframeElement);  // by WebElement — most reliable
driver.switchTo().frame("frameName");                // by name or id attribute
driver.switchTo().frame(0);                          // by index (0-based) — fragile
```

Prefer the WebElement approach: find the `<iframe>` tag on the outer page first, then pass it in. This survives page changes better than name/index.

#### Switching back out

After you're done inside the iframe, you must switch back before touching anything on the outer page:

```java
driver.switchTo().defaultContent();  // back to the top-level page (works from any depth)
driver.switchTo().parentFrame();     // one level up (useful for nested iframes)
```

**`defaultContent()` vs `parentFrame()`:** `defaultContent()` always goes back to the root — use it when you're done with iframes entirely. `parentFrame()` goes up just one level — useful when iframes are nested inside each other.

#### The critical rule

After switching into an iframe, *all* `findElement` calls search that iframe's DOM. After switching back out, they search the outer page again. Forgetting to call `defaultContent()` before interacting with outer-page elements causes `NoSuchElementException`.

#### contenteditable elements (TinyMCE, etc.)

Rich text editors like TinyMCE use a `contenteditable` `<body>` inside an iframe — not a standard `<input>` or `<textarea>`. This means:
- `clear()` throws `InvalidElementStateException` — it only works on standard form inputs
- `sendKeys()` may or may not work reliably depending on the editor's JavaScript interception
- Properly controlling these editors requires JavaScript Executor — covered in Module 7

**Key interview points:**
- iframes have a separate DOM — `findElement` can't cross into them without `switchTo().frame()`
- Always call `switchTo().defaultContent()` before interacting with the outer page after an iframe
- Three switch methods: by WebElement (preferred), by name/id, by index
- `defaultContent()` = back to root; `parentFrame()` = one level up
- `contenteditable` elements (rich text editors) can't be controlled with `clear()` — need JavaScript Executor

---

### Section 7: File Uploads

#### The core insight

File upload inputs (`<input type="file">`) normally open an OS-level file picker dialog — which Selenium can't interact with. The trick: **don't open the dialog at all**. Just call `sendKeys()` on the file input with the absolute path to your file as a string. The browser accepts it silently, as if the user had selected the file manually.

```java
WebElement fileInput = driver.findElement(By.id("file-upload"));
fileInput.sendKeys("/absolute/path/to/your/file.txt");
```

No special libraries, no OS automation, no `Robot` class — just `sendKeys()`.

#### Where to keep test files

Maven projects have a standard location for files used during tests: `src/test/resources/`. Anything placed there is available at runtime and clearly signals "this is test data, not production code."

#### Building the path dynamically with `System.getProperty("user.dir")`

You never want to hardcode an absolute path like `/Users/nicholas/Dev/...` — that breaks the moment anyone else runs the project.

`System.getProperty("user.dir")` is a built-in Java system property that returns the **current working directory at runtime**. When Maven runs tests, that directory is always the Maven project root (the folder containing `pom.xml`). So:

```java
String filePath = System.getProperty("user.dir") + "/src/test/resources/test-upload.txt";
```

This resolves to the correct absolute path on any machine, regardless of where the project lives. It's the standard portable approach for referencing test resource files in Selenium projects.

#### After upload

After submitting the file, assert on the confirmation content the page returns — typically the filename. This proves the upload actually went through, not just that the button was clicked.

**Key interview points:**
- `sendKeys()` on `<input type="file">` bypasses the OS file picker — no OS automation needed
- The file path must be **absolute** — relative paths don't work with file inputs
- `System.getProperty("user.dir")` gives the Maven project root at runtime — use it to build portable paths
- Store test files in `src/test/resources/` — that's the Maven convention for test data
- Always assert on the post-upload confirmation, not just the click

---

## Module 4: Waits

### Section 1: Why Tests Fail Without Waits

#### The core timing problem

When your code calls `driver.findElement(By.id("some-button"))`, Selenium asks the browser: "Is this element in the DOM *right now*?" If the answer is no — even for a fraction of a second — you get a `NoSuchElementException` and the test crashes. The browser doesn't care that the element will be there in 200ms. Selenium asked at the wrong moment, got "no", and gave up.

Modern web apps are **asynchronous**. After a click or page navigation:
- JavaScript executes and renders new elements
- API calls go out and responses come back
- Animations run before elements become interactive
- SPAs swap content without a full page reload

Your Java code runs at full CPU speed — it will reach the next `findElement` before the browser has finished its work.

#### Flaky tests

If you never add waits, tests pass sometimes and fail sometimes without any code change. These are called **flaky tests** — they're arguably worse than consistently failing tests because you can't trust your suite.

#### Why `Thread.sleep()` is not the answer

```java
driver.findElement(By.id("login-button")).click();
Thread.sleep(3000); // wait 3 seconds
WebElement product = driver.findElement(By.className("inventory_item"));
```

Two problems:
1. **Fragile** — on a slow network or CI machine, 3 seconds might not be enough. On a fast machine, you're wasting 2.9 seconds on something that was ready in 100ms.
2. **Compounds** — 100 tests × 3 second sleep = 5 minutes of pure idle time added to your suite.

`Thread.sleep()` is a red flag in any code review.

#### The right mental model

Instead of "wait X seconds", say: **"keep trying until this condition is true, or give up after Y seconds."**

| Wait type | How it works | When to use |
|---|---|---|
| **Implicit wait** | Retries every `findElement` for up to N seconds globally | Rarely — it has serious problems |
| **Explicit wait** | Waits for a specific condition on a specific element | Almost always — this is the right tool |
| **Fluent wait** | Like explicit, but with configurable polling interval | Specific advanced scenarios |

**Key interview points:**
- `NoSuchElementException` on a valid element almost always means a timing issue, not a missing element
- Flaky tests (non-deterministic pass/fail) are usually caused by missing waits
- `Thread.sleep()` is hardcoded, brittle, and wastes time — never use it in production test code
- The correct approach is condition-based waiting: keep trying until ready, bail after a timeout

### Section 2: Implicit Waits

#### What they are

A global setting applied to the driver once — after which every `findElement` automatically retries for up to N seconds before throwing `NoSuchElementException`:

```java
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
```

Set it once, it applies everywhere. That's the appeal.

#### Why you should mostly avoid them

**Problem 1 — They interact badly with explicit waits.** Mixing implicit and explicit waits produces unpredictable timeouts (they can compound rather than override). The behaviour is driver-dependent.

**Problem 2 — They slow down negative assertions.** If you want to assert an error message does *not* appear, Selenium will wait the full 10 seconds on every check before concluding the element isn't there.

**Problem 3 — They mask real problems.** A global retry hides timing issues you should actually know about. Explicit waits force you to think about *what condition* you're waiting for.

#### The rule

Use explicit waits instead. You'll see implicit waits in older codebases — know what they are and why they're problematic.

**Key interview points:**
- Implicit wait is global; explicit wait is targeted at a specific condition on a specific element
- Mixing implicit and explicit waits causes unpredictable timeout behaviour — don't do it
- Negative assertions (`findElements().isEmpty()`) become very slow with implicit waits
- Implicit waits are considered a code smell in modern Selenium projects

### Section 3: Explicit Waits

#### The two classes

```java
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration; // standard Java library — not Selenium
```

- `WebDriverWait` — sets up the wait with a driver and a timeout
- `ExpectedConditions` — a library of pre-built conditions to wait for

#### Basic structure

```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

WebElement element = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.id("inventory-container"))
);
```

- `wait.until(condition)` — keeps polling until the condition is true, then returns the result
- If the timeout expires before the condition is met, throws `TimeoutException`
- The same `wait` object can be reused — create it once, call `wait.until(...)` as many times as needed

#### The conditions you'll use most

| Condition | What it waits for | Returns |
|---|---|---|
| `visibilityOfElementLocated(By)` | Element is in DOM **and** visible | `WebElement` |
| `elementToBeClickable(By)` | Element is visible **and** enabled | `WebElement` |
| `presenceOfElementLocated(By)` | Element is in DOM (may be invisible) | `WebElement` |
| `invisibilityOfElementLocated(By)` | Element is gone or hidden | `Boolean` |
| `textToBePresentInElement(element, text)` | Element's text contains a value | `Boolean` |
| `urlContains(text)` | Current URL contains a string | `Boolean` |
| `titleContains(text)` | Page title contains a string | `Boolean` |
| `alertIsPresent()` | A JS alert has appeared | `Alert` |

#### `visibilityOfElementLocated` vs `elementToBeClickable`

- **`visibilityOfElementLocated`** — element is there and visible. Use when reading text or asserting existence.
- **`elementToBeClickable`** — element is visible *and* not disabled. Use before `click()` or `sendKeys()`. Clicking a disabled button produces silent failure — this condition catches that.

**Rule:** before any `click()` or `sendKeys()`, use `elementToBeClickable`. Before reading text, use `visibilityOfElementLocated`.

**Key interview points:**
- Explicit waits are scoped to a specific element and condition — that's what makes them better than implicit waits
- `WebDriverWait` + `ExpectedConditions` is the standard pattern — know it cold
- `elementToBeClickable` is the safest pre-click check; `visibilityOfElementLocated` is the safest pre-read check
- `TimeoutException` means the condition never became true within the timeout — usually a locator problem or a page that never finished loading
- `Duration` comes from `java.time.Duration` — standard Java, not Selenium

### Section 4: Fluent Waits

Fluent wait is `WebDriverWait`'s more configurable sibling. Two extra controls over `WebDriverWait`:
1. **Polling interval** — how often to check the condition
2. **Exceptions to ignore** — swallow specific exceptions during polling instead of crashing

#### Syntax

```java
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.NoSuchElementException;

FluentWait<WebDriver> wait = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(15))
    .pollingEvery(Duration.ofMillis(500))
    .ignoring(NoSuchElementException.class);

WebElement element = wait.until(
    d -> d.findElement(By.id("some-element"))
);
```

- `.withTimeout()` — total time before giving up
- `.pollingEvery()` — how frequently to check
- `.ignoring()` — exceptions to swallow and retry through (can chain multiple)
- The lambda `d -> d.findElement(...)` — fluent wait takes a `Function<WebDriver, T>` instead of an `ExpectedConditions` constant

#### When to use it

- **Different polling interval** — slow-loading content where you don't want to hammer the DOM every 500ms
- **DOM flicker** — elements that briefly disappear during re-renders cause `StaleElementReferenceException`; fluent wait lets you ignore it and retry through the flicker:

```java
FluentWait<WebDriver> wait = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(10))
    .pollingEvery(Duration.ofSeconds(1))
    .ignoring(NoSuchElementException.class)
    .ignoring(StaleElementReferenceException.class);
```

#### Relationship to `WebDriverWait`

`WebDriverWait` is a subclass of `FluentWait` — pre-configured with 500ms polling and `NoSuchElementException` already ignored. It's a convenience wrapper. That's why the APIs look nearly identical.

**The rule:** default to `WebDriverWait` + `ExpectedConditions`. Reach for `FluentWait` only when you need a custom polling interval or need to ignore `StaleElementReferenceException`.

**Key interview points:**
- `WebDriverWait` is a subclass of `FluentWait` — same underlying mechanism, less configuration
- Use fluent wait for DOM flicker (ignore `StaleElementReferenceException`) or heavy pages where reduced polling saves overhead
- The lambda syntax `d -> d.findElement(...)` is a `Function<WebDriver, T>` — the driver is passed in automatically

### Section 5: The Conditions You'll Actually Use Day-to-Day

#### The decision tree

| Situation | Condition to use |
|---|---|
| Before `click()` or `sendKeys()` | `elementToBeClickable(By)` |
| After navigation or a click that loads new content | `visibilityOfElementLocated(By)` |
| Asserting something is gone (spinner, modal) | `invisibilityOfElementLocated(By)` |
| Waiting for a page transition | `urlContains(text)` or `titleContains(text)` |
| Before handling a JS alert | `alertIsPresent()` |

#### Chaining — skipping the variable

`elementToBeClickable` and `visibilityOfElementLocated` both return a `WebElement`. You can chain directly instead of storing it:

```java
wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")))
    .sendKeys("standard_user");
```

Clean for one-liners where you don't need to reuse the element.

#### `urlContains` after a click

After clicking a button that triggers navigation, use `urlContains` before trying to find elements on the new page. It's a reliable page-transition check — confirms you actually landed before you start looking for content.

```java
driver.findElement(By.id("login-button")).click();
wait.until(ExpectedConditions.urlContains("inventory")); // page transition confirmed
wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list")));
```

**Key interview points:**
- Always use `elementToBeClickable` before interacting — clicking a disabled element produces silent failure
- `urlContains` is the safest post-navigation check before finding elements on the new page
- `wait.until()` returns the result of the condition — chain `.click()` or `.sendKeys()` directly to avoid unnecessary variables
- One `WebDriverWait` instance can be reused for multiple `wait.until()` calls throughout a test

---

## Module 5: Page Object Model (POM)

### Section 1: Why POM Exists

#### The problem

Without POM, locators and interactions are scattered directly in test methods:

```java
driver.findElement(By.id("user-name")).sendKeys("standard_user");
driver.findElement(By.id("password")).sendKeys("secret_sauce");
driver.findElement(By.id("login-button")).click();
```

With 2 tests this is manageable. With 20, you have a maintenance problem:

- The login button's ID changes → update it in 20 places
- You want to add a wait before clicking → update it in 20 places
- A new person reads the test → they see raw WebDriver calls, not intent

#### The two concerns POM separates

| Concern | Belongs in |
|---|---|
| What the page looks like (locators) | Page class |
| What the test does (flow + assertions) | Test class |

A test written against a POM-structured project reads like a description of what a user does, not a WebDriver instruction manual:

```java
LoginPage loginPage = new LoginPage(driver);
ProductsPage productsPage = loginPage.loginAs("standard_user", "secret_sauce");
assertTrue(productsPage.isLoaded());
```

One locator change → fix it in one place. Every test that uses that page gets the fix for free.

#### The one-sentence rule

> Page classes know *what's on the page*. Tests know *what to verify*.

### Section 2: Project Structure

Project 2 (selenium-pom) uses a clean Maven layout. The key addition over Project 1 is a `pages/` package under `src/main/java/`:

```
src/
  main/java/
    com/seleniumstudy/
      pages/
        BasePage.java        ← shared driver, shared utility methods
        LoginPage.java       ← locators + actions for the login page
        ProductsPage.java    ← locators + actions for the products page
  test/java/
    com/seleniumstudy/
      tests/
        LoginTest.java       ← only flow and assertions, no raw WebDriver
```

Page classes live in `src/main/java/` — they're application support code, not tests. Tests live in `src/test/java/` as before.

### Section 3: `BasePage`

`BasePage` is the parent class that all page classes extend. Its two responsibilities:

1. **Hold the driver** — passed in from the test and stored as a field, so every page class has access to it via inheritance without needing to pass it around
2. **Provide shared utility methods** — wrappers around common `WebDriverWait` + WebDriver interactions, written once and reused by every page

```java
public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        field.clear();
        field.sendKeys(text);
    }

    protected String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }
}
```

Key points:
- `protected` fields — accessible to child page classes via inheritance, not exposed publicly
- The wait is built once in the constructor with a shared timeout — every subclass uses it automatically
- `click()`, `type()`, `getText()` already embed the correct wait condition — page classes don't repeat this logic

### Section 4: Page Classes

Each page class represents one page (or significant section) of the application. The rules:

1. **Locators are private** — they're implementation details. Tests never reference a `By` object directly.
2. **Methods are expressive** — named after what the user does, not what Selenium does. `loginAs()` not `findUsernameAndSendKeys()`.
3. **Methods return page objects** — an action that causes navigation returns the page you land on. This lets tests chain pages together fluently.

```java
public class LoginPage extends BasePage {

    // Locators are private — the test never sees a By object
    private final By usernameField = By.id("user-name");
    private final By passwordField = By.id("password");
    private final By loginButton   = By.id("login-button");
    private final By errorMessage  = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver); // passes the driver up to BasePage
    }

    // Returns ProductsPage because that's where a successful login lands you
    public ProductsPage loginAs(String username, String password) {
        type(usernameField, username);
        type(passwordField, password);
        click(loginButton);
        return new ProductsPage(driver);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }
}
```

The `return new ProductsPage(driver)` pattern is called **method chaining by page object**. The test doesn't need to know what URL login navigates to — `LoginPage` handles that and hands back the correct page object.

### Section 5: The Test — What POM Looks Like from the Test Side

```java
public class LoginTest {

    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.get("https://www.saucedemo.com");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void successfulLoginLandsOnProductsPage() {
        LoginPage loginPage = new LoginPage(driver);
        ProductsPage productsPage = loginPage.loginAs("standard_user", "secret_sauce");
        assertTrue(productsPage.isLoaded());
    }

    @Test
    public void invalidPasswordShowsError() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.loginAs("standard_user", "wrong_password");
        assertEquals(loginPage.getErrorMessage(), "Epic sadface: Username and password do not match");
    }
}
```

The test reads like a specification, not a WebDriver script. No `By` objects, no `sendKeys`, no `wait.until` — all of that is in the page classes where it belongs.

**Key interview points:**
- POM separates *what a page looks like* (page class) from *what the test verifies* (test class)
- Locators are private fields — the test never references a `By` directly
- `BasePage` holds the driver and shared utility methods (click, type, getText with built-in waits) — subclasses inherit them
- Page methods return the next page object — tests chain pages together naturally
- One locator change in one place fixes every test that uses that page

### Section 6: How the Driver Actually Gets to the Page

#### Dependency injection — the test owns the driver

The page never creates the driver. It receives one. This is called **dependency injection** — the dependency (driver) is injected from outside rather than created internally.

The full flow:

```
@BeforeMethod
  driver = new ChromeDriver();        ← driver is BORN here

@Test
  new LoginPage(driver)               ← driver is PASSED IN
       ↓
  LoginPage constructor: super(driver)
       ↓
  BasePage constructor: this.driver = driver
                        this.wait   = new WebDriverWait(driver, ...)

  loginAs() calls type() and click()
  — those BasePage methods use this.driver
  — the exact same Chrome instance the test created

@AfterMethod
  driver.quit()                       ← driver DIES here
```

The test controls the entire driver lifecycle. The page borrows it.

#### Why not put `@BeforeClass` / `@AfterClass` in `BasePage`?

Some tutorials do this:

```java
public class BasePage {
    protected WebDriver driver;

    @BeforeClass
    public void setUp() {
        driver = new ChromeDriver();
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
    }
}
```

This is a bad pattern. The problems:

1. **`BasePage` is a utility class, not a test class.** Putting TestNG annotations in it gives it two jobs — managing driver lifecycle AND providing page utilities. Those are separate concerns.
2. **Every test class that extends `BasePage` silently inherits that setup.** If you ever want a test to use a headless driver, Firefox, or a different timeout — you have to fight the inheritance.
3. **`BasePage` now depends on TestNG**, which means it can't be used outside a test context.

**The rule:** `BasePage` is a plain Java class. It knows nothing about TestNG. Driver lifecycle belongs entirely in the test class — `@BeforeMethod` creates it, `@AfterMethod` quits it.

#### Why not add more methods to `BasePage` upfront?

A raw `find()` wrapper adds nothing:

```java
// This is just noise — it's identical to calling driver.findElement() directly
protected WebElement find(By locator) {
    return driver.findElement(locator);
}
```

The existing `click()`, `type()`, and `getText()` are worth having because they each do two things: interact *and* wait correctly. `click()` doesn't just call `.click()` — it waits for `elementToBeClickable` first. That's the value.

Methods earn their place when a concrete need arises. If a page class hits something that would be useful everywhere (e.g. `isDisplayed(By)`, `waitForUrl(String)`), add it to `BasePage` then.

### Section 7: `LoginPage` and `ProductsPage` in Detail

#### `private final` locators

```java
private final By usernameField = By.id("user-name");
```

- `private` — the test can never see or reference this `By` object directly
- `final` — the locator is set once when the class loads and never changes. Signals intent: this is a constant, not something that gets reassigned.

#### `super(driver)` — the one required line

Every page constructor must call `super(driver)`. That's what runs `BasePage`'s constructor, which stores the driver and creates the wait. Without it the driver and wait are never initialised — every inherited method would throw a `NullPointerException`.

#### Returning the next page object

```java
public ProductsPage loginAs(String username, String password) {
    type(usernameField, username);
    type(passwordField, password);
    click(loginButton);
    return new ProductsPage(driver);  // ← same driver, passed forward
}
```

`loginAs()` passes the same driver instance into `ProductsPage`. The test gets back a fully initialised `ProductsPage` without knowing anything about the URL or navigation that just happened. The test just works with the object it receives.

#### `isLoaded()` — the standard page verification pattern

```java
public boolean isLoaded() {
    return getText(pageTitle).equals("Products");
}
```

Every page class should have an `isLoaded()` method. It reads a piece of content that only exists on that page and confirms it's correct. Tests call this immediately after navigation:

```java
ProductsPage productsPage = loginPage.loginAs("standard_user", "secret_sauce");
assertTrue(productsPage.isLoaded()); // proves we actually landed here
```

This is better than asserting on the URL — it confirms the page rendered correctly, not just that the browser navigated to the right address.

**Key interview points:**
- Pages don't create drivers — they receive one. The test owns the lifecycle.
- `@BeforeClass` in `BasePage` couples test infrastructure to a utility class — bad design
- `super(driver)` is mandatory in every page constructor — it initialises the driver and wait in `BasePage`
- Locators are `private final` — private so tests can't reach them, final because they're constants
- Returning the next page object from an action method hides navigation details from the test
- `isLoaded()` verifies page content rendered correctly — better than asserting on the URL alone

---
