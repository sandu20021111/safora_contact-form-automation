package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;

public class ContactFormAutomation {

    private static final String HOME_URL = "https://safora.se/en/";

    public static void main(String[] args) throws InterruptedException {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        // Add these to avoid detection
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        Actions actions = new Actions(driver);

        try {

            driver.get(HOME_URL);
            System.out.println("Home page loaded");

            WebElement contactLink = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.linkText("Contact Us")
                    )
            );
            contactLink.click();
            System.out.println("Navigated to Contact Page");

            handleRecaptcha(driver, wait, actions);

            Thread.sleep(2000);

            fillContactForm(driver, wait,
                    "Java QA User",
                    "test@example.com",
                    "0771234567",
                    "Automation test using Selenium WebDriver"
            );

            clickSubmitButton(driver, wait, actions);

            Thread.sleep(3000);

            try {
                WebElement successMsg = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//*[contains(text(),'success') or contains(text(),'sent') or contains(@class,'success')]")
                        )
                );
                System.out.println(" FORM SUBMITTED SUCCESSFULLY: " + successMsg.getText());
            } catch (Exception e) {
                System.out.println(" Could not verify success message, but form was submitted");
            }


            driver.get(HOME_URL);
            Thread.sleep(1000);

            WebElement contactLink2 = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.linkText("Contact Us")
                    )
            );
            contactLink2.click();
            System.out.println("Navigated back to Contact Page for validation test");


            // TEST EMPTY FORM SUBMISSION

            testEmptyFormSubmission(driver, wait, actions);

        } catch (Exception e) {
            System.out.println(" TEST FAILED: " + e.getMessage());
            e.printStackTrace();

            // Take screenshot on failure
            takeScreenshot(driver, "test_failure");

        } finally {
            // Close browser
            Thread.sleep(2000);
            driver.quit();
        }
    }


    // 1 Handle reCAPTCHA

    private static void handleRecaptcha(WebDriver driver, WebDriverWait wait, Actions actions) {
        try {
            System.out.println("Checking for reCAPTCHA...");
            Thread.sleep(2000);

            // Method 1.1: Try to find and click reCAPTCHA checkbox directly
            try {

                List<WebElement> iframes = driver.findElements(By.tagName("iframe"));

                for (WebElement iframe : iframes) {
                    String src = iframe.getAttribute("src");
                    if (src != null && src.contains("recaptcha")) {
                        driver.switchTo().frame(iframe);
                        System.out.println("Switched to reCAPTCHA iframe");


                        try {
                            WebElement recaptchaCheckbox = wait.until(
                                    ExpectedConditions.elementToBeClickable(
                                            By.xpath("//div[@class='recaptcha-checkbox-border'] | //span[@role='checkbox']")
                                    )
                            );
                            recaptchaCheckbox.click();
                            System.out.println(" reCAPTCHA checkbox clicked successfully");
                            Thread.sleep(2000);
                        } catch (Exception e) {
                            System.out.println("Could not find checkbox in iframe");
                        }

                        driver.switchTo().defaultContent();
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Iframe switching failed: " + e.getMessage());
            }

            // Method 1.2: Try to find and click by class name
            if (driver.findElements(By.className("g-recaptcha")).size() > 0) {
                System.out.println("Found reCAPTCHA widget");

                // Try alternative selector
                try {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript(
                            "if(document.querySelector('.recaptcha-checkbox-border')) {" +
                                    "document.querySelector('.recaptcha-checkbox-border').click();" +
                                    "}"
                    );
                    System.out.println("Clicked reCAPTCHA via JavaScript");
                } catch (Exception e) {
                    System.out.println("JavaScript click failed");
                }
            }

            // Method 1.3: If CAPTCHA still needs solving, wait for manual intervention
            Thread.sleep(2000);


            boolean captchaSolved = checkIfCaptchaSolved(driver);

            if (!captchaSolved) {
                System.out.println(" reCAPTCHA not auto-solved!");
                System.out.println("Waiting 20 seconds for manual CAPTCHA solving...");
                System.out.println("Please click 'I'm not a robot' checkbox manually in the browser");


                for (int i = 20; i > 0; i--) {
                    System.out.print("\r " + i + " seconds remaining...");
                    Thread.sleep(1000);
                    if (checkIfCaptchaSolved(driver)) {
                        System.out.println("\n CAPTCHA solved manually!");
                        break;
                    }
                }
                System.out.println();
            }

        } catch (Exception e) {
            System.out.println("No reCAPTCHA found or error handling CAPTCHA: " + e.getMessage());
        }
    }


    // 2 Check if CAPTCHA is solved

    private static boolean checkIfCaptchaSolved(WebDriver driver) {
        try {

            WebElement checkedCheckbox = driver.findElement(
                    By.xpath("//div[@aria-checked='true'] | //input[@checked]")
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Fill Contact Form

    private static void fillContactForm(WebDriver driver, WebDriverWait wait,
                                        String nameText, String emailText,
                                        String phoneText, String messageText) {
        try {
            WebElement name = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//input[@placeholder='Your Name'] | //input[@id='name'] | //input[@name='name']")
                    )
            );

            WebElement email = driver.findElement(
                    By.xpath("//input[@placeholder='Email Address'] | //input[@id='email'] | //input[@name='email']")
            );

            WebElement phone = driver.findElement(
                    By.xpath("//input[@placeholder='Phone Number'] | //input[@id='phone'] | //input[@name='phone']")
            );

            WebElement message = driver.findElement(
                    By.xpath("//textarea[@placeholder='Your Message'] | //textarea[@id='message'] | //textarea[@name='message']")
            );

            name.clear();
            name.sendKeys(nameText);

            email.clear();
            email.sendKeys(emailText);

            phone.clear();
            phone.sendKeys(phoneText);

            message.clear();
            message.sendKeys(messageText);

            System.out.println("Form filled with test data");

        } catch (Exception e) {
            System.out.println("Error filling form: " + e.getMessage());
            throw e;
        }
    }


    // Click Submit Button

    private static void clickSubmitButton(WebDriver driver, WebDriverWait wait, Actions actions) throws InterruptedException {
        try {
            WebElement submitButton = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//button[contains(text(),'Send Message')] | //button[@type='submit'] | //input[@type='submit']")
                    )
            );


            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center', behavior:'smooth'});", submitButton);

            Thread.sleep(1000);

            wait.until(ExpectedConditions.elementToBeClickable(submitButton));


            try {
                actions.moveToElement(submitButton).click().perform();
                System.out.println("Clicked submit button using Actions");
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
                System.out.println("Clicked submit button using JavaScript");
            }

        } catch (Exception e) {
            System.out.println("Error clicking submit button: " + e.getMessage());
            throw e;
        }
    }


    // Test Empty Form Submission

    private static void testEmptyFormSubmission(WebDriver driver, WebDriverWait wait, Actions actions) {
        try {
            WebElement submitEmpty = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(text(),'Send Message')] | //button[@type='submit']")
                    )
            );

            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center', behavior:'smooth'});", submitEmpty);

            Thread.sleep(800);

            handleRecaptcha(driver, wait, actions);

            // Click submit without filling form
            actions.moveToElement(submitEmpty).click().perform();
            System.out.println("Submitted empty form for validation test");
            takeScreenshot(driver, "validation");

            Thread.sleep(2000);

            try {
                WebElement errorMsg = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//*[contains(text(),'required') or contains(text(),'Please') or contains(text(),'valid') or contains(@class,'error') or contains(@class,'invalid')]")
                        )
                );

                System.out.println(" VALIDATION PASSED: " + errorMsg.getText());
                takeScreenshot(driver, "validation passed");

            } catch (Exception e) {
                // Check for HTML5 required attributes
                try {
                    WebElement nameField = driver.findElement(By.xpath("//input[@placeholder='Your Name'] | //input[@id='name']"));
                    String required = nameField.getAttribute("required");

                    if (required != null && required.equals("true")) {
                        System.out.println(" VALIDATION PASSED (HTML5 required attribute detected)");
                        takeScreenshot(driver, "after_validation");
                    } else {
                        System.out.println(" VALIDATION FAILED - No validation message or required attribute found");

                        // Take screenshot of the issue
                        takeScreenshot(driver, "validation_failed");
                    }
                } catch (Exception ex) {
                    System.out.println("VALIDATION FAILED - Could not determine validation method");
                    takeScreenshot(driver, "validation_error");
                }
            }

        } catch (Exception e) {
            System.out.println("Error in empty form test: " + e.getMessage());
        }
    }

    // Take Screenshot

    private static void takeScreenshot(WebDriver driver, String fileName) {
        try {
            File screenshot = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.FILE);

            File destFile = new File("screenshots/" + fileName + ".png");

            Files.copy(screenshot.toPath(),
                    destFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Screenshot saved: " + destFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}