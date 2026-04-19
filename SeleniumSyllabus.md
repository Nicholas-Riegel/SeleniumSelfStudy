# Selenium with Java — Course Syllabus

---

## Note to Copilot (read this first before doing anything else)

This syllabus was designed in a previous conversation. Here is the full context you need:

**About the student:**
- 2 years of QA experience (manual and some automation)
- Prior automation tool: Laravel Dusk (PHP-based, uses ChromeDriver under the hood — so browser automation concepts like locators and clicking are already familiar)
- Java level: intermediate — comfortable with classes, inheritance, and interfaces. Do NOT explain basic Java concepts like variables or loops.
- Goal: get a new QA job by learning Selenium and Playwright. Needs to be interview-ready.

**Working style (important — follow this strictly):**
- Explain what you plan to do and get explicit approval before doing it
- The student does all hands-on work themselves (creating files, running commands). You provide exact commands/content but do not execute them.
- Explain every concept clearly before writing any code. No unexplained copy-pasting.
- Teach one section at a time. Complete it fully before moving on.

**Where we are:**
- Module 1 is complete. Start with Module 2: Locators.
- Project 1 (selenium-foundations) is set up at `/Users/nicholas/Dev/Selenium/SeleniumSelfStudy/selenium-foundations`.
- Java 21, Maven, Selenium 4.20.0, TestNG 7.10.2 are all configured and working.
- Tests are run via `mvn test` in the terminal for full output visibility.

**Test sites to use (the student specifically wants to test against live websites):**
- saucedemo.com — login, products, cart flows
- demoqa.com — forms, alerts, iframes, drag-and-drop
- the-internet.herokuapp.com — awkward real-world scenarios (file upload, hovers, dynamic content)

**Tone:**
- The student came from a frustrating YouTube course where nothing was explained. Be the opposite of that. Every line of code gets explained.

---



Taught by GitHub Copilot. Designed for someone with QA experience and intermediate Java knowledge.
Laravel Dusk background means browser automation concepts are already familiar — we go straight to Selenium specifics.

---

## Progress

- [x] Module 1: How Selenium Actually Works
- [ ] Module 2: Locators
- [ ] Module 3: Interacting with Elements
- [ ] Module 4: Waits
- [ ] Module 5: Page Object Model (POM)
- [ ] Module 6: TestNG Test Framework
- [ ] Module 7: JavaScript Executor
- [ ] Module 8: Advanced Browser Interactions
- [ ] Module 9: Data-Driven Testing
- [ ] Module 10: Running Tests in the Real World
- [ ] Module 11: Interview Preparation

---

## Project Structure

The course uses three separate Maven projects. Each is a clean, self-contained project with its own `pom.xml`.

| Project | Modules | Purpose |
|---|---|---|
| **Project 1 — Foundations** | 1–4 | Scratchpad for learning Selenium mechanics. No POM pattern yet. |
| **Project 2 — Page Object Model** | 5–8 | Full POM architecture built from scratch. Modules 6, 7, and 8 extend this project progressively. |
| **Project 3 — Data-Driven Testing** | 9 | Introduces Apache POI and CSV; different dependencies and test structure warrant a fresh project. |

Module 10 (CI/headless running) is applied to an existing project rather than creating a new one.

---

## Project 1 — Foundations (Modules 1–4)

---

## Module 1: How Selenium Actually Works
- [x] The WebDriver protocol: how Java code talks to a real browser
- [x] ChromeDriver, GeckoDriver — what they are and why they exist
- [x] How this differs from Dusk (under the hood they're doing the same thing)
- [x] Project setup: Maven, Selenium dependency, TestNG
- [x] `@BeforeMethod` / `@AfterMethod` — just enough TestNG to write clean tests (full TestNG coverage is in Module 6)

## Module 2: Locators
- [ ] ID, name, class, tag name — the easy ones
- [ ] CSS selectors — the most important skill, used constantly
- [ ] XPath — when to use it, how to write it without losing your mind
- [ ] How to find and verify locators in browser DevTools
- [ ] What makes a locator fragile vs. reliable

## Module 3: Interacting with Elements
- [ ] Click, sendKeys, clear
- [ ] Getting text and attribute values
- [ ] Checking element state: isDisplayed, isEnabled, isSelected
- [ ] Dropdowns with the `Select` class
- [ ] Checkboxes and radio buttons
- [ ] Alerts and browser dialogs
- [ ] iframes — the most common source of "element not found" confusion
- [ ] File uploads

## Module 4: Waits (The Most Important Module)
- [ ] Why tests fail without waits
- [ ] Implicit waits — what they do and why you should avoid them
- [ ] Explicit waits: `WebDriverWait` + `ExpectedConditions`
- [ ] Fluent waits — polling on a schedule
- [ ] The conditions you'll actually use day-to-day

---

## Project 2 — Page Object Model (Modules 5–8)

---

## Module 5: Page Object Model (POM)
- [ ] Why POM exists and what problem it solves
- [ ] Building a `BasePage` with shared utility methods
- [ ] Structuring page classes and returning page objects
- [ ] Keeping locators private and methods expressive
- [ ] Building a clean POM project from scratch — no course debris

## Module 6: TestNG Test Framework
- [ ] `@Test`, `@BeforeMethod`, `@AfterMethod`, `@BeforeClass`, `@AfterClass`
- [ ] `@DataProvider` for data-driven tests
- [ ] Assertions: TestNG's built-in vs. AssertJ (and why AssertJ is better)
- [ ] Running specific tests and suites with `testng.xml`
- [ ] Parallel test execution

## Module 7: JavaScript Executor
- [ ] When Selenium's normal commands aren't enough
- [ ] `scrollIntoView`, JS click, reading values from the DOM
- [ ] The casting pattern and why it's necessary

## Module 8: Advanced Browser Interactions
- [ ] The `Actions` class: hover, right-click, drag-and-drop, keyboard combos
- [ ] Screenshots on failure
- [ ] Handling multiple browser windows and tabs

---

## Project 3 — Data-Driven Testing (Module 9)

---

## Module 9: Data-Driven Testing
- [ ] `@DataProvider` in depth
- [ ] Reading test data from Excel using Apache POI
- [ ] Reading from CSV

## Module 10: Running Tests in the Real World
> Applied to an existing project (Project 2 or 3) — no new Maven project created.
- [ ] Running tests headlessly (no visible browser, for CI)
- [ ] Maven Surefire plugin — triggering tests with `mvn test`
- [ ] A basic GitHub Actions pipeline that runs your tests on push
- [ ] Test reporting: Allure or ExtentReports

## Module 11: Interview Preparation
- [ ] The questions interviewers actually ask about Selenium
- [ ] How to talk about POM, waits, and locator strategy
- [ ] Common gotchas: stale element, element not interactable, timing issues
- [ ] What a strong take-home project looks like

---

## How This Course Works

Each module will be taught one section at a time. For each topic:
1. The concept is explained before any code is written
2. We write code together with full explanation of every line
3. You run it and see it work
4. Key points are summarised for interview use

No unexplained copy-pasting.
