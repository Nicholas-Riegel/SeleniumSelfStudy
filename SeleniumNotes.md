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
