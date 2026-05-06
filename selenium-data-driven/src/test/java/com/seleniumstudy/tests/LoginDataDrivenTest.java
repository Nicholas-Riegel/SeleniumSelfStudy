package com.seleniumstudy.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginDataDrivenTest {

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
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    // Each inner row = one test run.
    // Column 1: username, Column 2: password, Column 3: expected result (true = login should succeed)
    @DataProvider(name = "loginData")
    public Object[][] loginData() {
        return new Object[][] {
            { "standard_user",  "secret_sauce", true  },
            { "locked_out_user","secret_sauce", false },
            { "",               "",             false },
        };
    }

    // TestNG calls this method once per row in the DataProvider.
    // The parameters username, password, shouldSucceed are automatically populated.
    @Test(dataProvider = "loginData")
    public void loginTest(String username, String password, boolean shouldSucceed) {

        driver.get("https://www.saucedemo.com");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("login-button")).click();

        if (shouldSucceed) {
            // After a successful login the inventory page appears
            wait.until(ExpectedConditions.urlContains("inventory"));
            assertThat(driver.getCurrentUrl()).contains("inventory");
        } else {
            // On failure an error message is shown
            String error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']"))
            ).getText();
            assertThat(error).isNotEmpty();
        }
    }

    @DataProvider(name = "loginDataFromExcel")
    public Object[][] loginDataFromExcel() throws IOException {

        // getClassLoader().getResource() looks in src/test/resources/ —
        // Maven copies everything from that folder into target/test-classes/,
        // and the ClassLoader finds it there at runtime.
        String filePath = getClass().getClassLoader().getResource("login-data.xlsx").getPath();

        // try-with-resources: automatically closes the file when done, even if an exception is thrown
        try (Workbook workbook = WorkbookFactory.create(new File(filePath))) {

            Sheet sheet = workbook.getSheet("LoginData");

            // getLastRowNum() returns the index of the last used/filled in row (0-based).
            // Row 0 = headers, so data rows are 1 through lastRowNum.
            int lastRow = sheet.getLastRowNum();
            Object[][] data = new Object[lastRow][3];

            // DataFormatter converts any cell to a String regardless of its type.
            // Without it, getStringCellValue() throws an exception on Boolean or numeric cells.
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= lastRow; i++) {
                
                Row row = sheet.getRow(i);

                data[i - 1][0] = row.getCell(0) != null 
                    ? formatter.formatCellValue(row.getCell(0)) 
                    : "";

                data[i - 1][1] = row.getCell(1) != null 
                    ? formatter.formatCellValue(row.getCell(1)) 
                    : "";

                // Boolean.parseBoolean() converts "true"/"TRUE" → true, anything else → false
                data[i - 1][2] = Boolean.parseBoolean(formatter.formatCellValue(row.getCell(2)));
            }

            return data;
        }
    }

    @Test(dataProvider = "loginDataFromExcel")
    public void loginTestFromExcel(String username, String password, boolean shouldSucceed) {

        driver.get("https://www.saucedemo.com");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("login-button")).click();

        if (shouldSucceed) {
            wait.until(ExpectedConditions.urlContains("inventory"));
            assertThat(driver.getCurrentUrl()).contains("inventory");
        } else {
            String error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']"))
            ).getText();
            assertThat(error).isNotEmpty();
        }
    }

    @DataProvider(name = "loginDataFromCSV")
    public Object[][] loginDataFromCSV() throws IOException {

        // Same classpath lookup as the Excel version — Maven copies the file
        // from src/test/resources/ to target/test-classes/ at build time.
        String filePath = getClass().getClassLoader().getResource("login-data.csv").getPath();

        // Files.lines() opens the file and streams each line as a String.
        // We wrap it in try-with-resources so the stream (and the file handle) close automatically.
        try (var lines = Files.lines(Paths.get(filePath))) {

            return lines
                .skip(1)                          // skip the header row
                .map(line -> line.split(",", -1)) // split each line into String[] of values
                .map(values -> new Object[] {
                    values[0],                              // username — already a String
                    values[1],                              // password — already a String
                    Boolean.parseBoolean(values[2])         // "true"/"false" → boolean
                })
                .toArray(Object[][]::new); // collect stream of Object[] into Object[][]
        }
    }

    @Test(dataProvider = "loginDataFromCSV")
    public void loginTestFromCSV(String username, String password, boolean shouldSucceed) {

        driver.get("https://www.saucedemo.com");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("login-button")).click();

        if (shouldSucceed) {
            wait.until(ExpectedConditions.urlContains("inventory"));
            assertThat(driver.getCurrentUrl()).contains("inventory");
        } else {
            String error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']"))
            ).getText();
            assertThat(error).isNotEmpty();
        }
    }
}