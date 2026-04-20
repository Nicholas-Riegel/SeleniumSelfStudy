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
