package tacos;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class DesignAndOrderTacosBrowserTest {
  
  private static HtmlUnitDriver browser;
  
  @LocalServerPort
  private int port;
  
  @Autowired
  TestRestTemplate rest;
  
  @BeforeAll
  public static void setup() {
    browser = new HtmlUnitDriver();
    browser.manage().timeouts()
        .implicitlyWait(10, TimeUnit.SECONDS);
  }
  
  @AfterAll
  public static void closeBrowser() {
    browser.quit();
  }
  
  @Test
  public void testDesignATacoPage_HappyPath() throws Exception {
    browser.get(homePageUrl());
    clickDesignATaco();
    assertDesignPageElements();
    buildAndSubmitATaco("Basic Taco", "FLTO", "GRBF", "CHED", "TMTO", "SLSA");
    clickBuildAnotherTaco();
    buildAndSubmitATaco("Another Taco", "COTO", "CARN", "JACK", "LETC", "SRCR");
    fillInAndSubmitOrderForm();
    assertThat(browser.getCurrentUrl()).isEqualTo(homePageUrl());
  }
  
  @Test
  public void testDesignATacoPage_EmptyOrderInfo() throws Exception {
    browser.get(homePageUrl());
    clickDesignATaco();
    assertDesignPageElements();
    buildAndSubmitATaco("Basic Taco", "FLTO", "GRBF", "CHED", "TMTO", "SLSA");
    submitEmptyOrderForm();
    fillInAndSubmitOrderForm();
    assertThat(browser.getCurrentUrl()).isEqualTo(homePageUrl());
  }

  @Test
  public void testDesignATacoPage_InvalidOrderInfo() throws Exception {
    browser.get(homePageUrl());
    clickDesignATaco();
    assertDesignPageElements();
    buildAndSubmitATaco("Basic Taco", "FLTO", "GRBF", "CHED", "TMTO", "SLSA");
    submitInvalidOrderForm();
    fillInAndSubmitOrderForm();
    assertThat(browser.getCurrentUrl()).isEqualTo(homePageUrl());
  }

  //
  // Browser test action methods
  //
  private void buildAndSubmitATaco(String name, String... ingredients) {
    assertDesignPageElements();

    for (String ingredient : ingredients) {
      browser.findElementByCssSelector("input[value='" + ingredient + "']").click();      
    }
    browser.findElementByCssSelector("input#name").sendKeys(name);
    browser.findElementByCssSelector("form").submit();
  }

  private void assertDesignPageElements() {
    assertThat(browser.getCurrentUrl()).isEqualTo(designPageUrl());
    List<WebElement> ingredientGroups = browser.findElementsByClassName("ingredient-group");
    assertThat(ingredientGroups.size()).isEqualTo(5);
    
    WebElement wrapGroup = browser.findElementByCssSelector("div.ingredient-group#wraps");
    List<WebElement> wraps = wrapGroup.findElements(By.tagName("div"));
    assertThat(wraps.size()).isEqualTo(2);
    assertIngredient(wrapGroup, 0, "FLTO", "Flour Tortilla");
    assertIngredient(wrapGroup, 1, "COTO", "Corn Tortilla");
    
    WebElement proteinGroup = browser.findElementByCssSelector("div.ingredient-group#proteins");
    List<WebElement> proteins = proteinGroup.findElements(By.tagName("div"));
    assertThat(proteins.size()).isEqualTo(2);
    assertIngredient(proteinGroup, 0, "GRBF", "Ground Beef");
    assertIngredient(proteinGroup, 1, "CARN", "Carnitas");

    WebElement cheeseGroup = browser.findElementByCssSelector("div.ingredient-group#cheeses");
    List<WebElement> cheeses = proteinGroup.findElements(By.tagName("div"));
    assertThat(cheeses.size()).isEqualTo(2);
    assertIngredient(cheeseGroup, 0, "CHED", "Cheddar");
    assertIngredient(cheeseGroup, 1, "JACK", "Monterrey Jack");

    WebElement veggieGroup = browser.findElementByCssSelector("div.ingredient-group#veggies");
    List<WebElement> veggies = proteinGroup.findElements(By.tagName("div"));
    assertThat(veggies.size()).isEqualTo(2);
    assertIngredient(veggieGroup, 0, "TMTO", "Diced Tomatoes");
    assertIngredient(veggieGroup, 1, "LETC", "Lettuce");

    WebElement sauceGroup = browser.findElementByCssSelector("div.ingredient-group#sauces");
    List<WebElement> sauces = proteinGroup.findElements(By.tagName("div"));
    assertThat(sauces.size()).isEqualTo(2);
    assertIngredient(sauceGroup, 0, "SLSA", "Salsa");
    assertIngredient(sauceGroup, 1, "SRCR", "Sour Cream");
  }
  

  private void fillInAndSubmitOrderForm() {
    assertThat(browser.getCurrentUrl()).startsWith(orderDetailsPageUrl());
    fillField("input#deliveryName", "Ima Hungry");
    fillField("input#deliveryStreet", "1234 Culinary Blvd.");
    fillField("input#deliveryCity", "Foodsville");
    fillField("input#deliveryState", "CO");
    fillField("input#deliveryZip", "81019");
    fillField("input#ccNumber", "4111111111111111");
    fillField("input#ccExpiration", "10/23");
    fillField("input#ccCVV", "123");
    browser.findElementByCssSelector("form").submit();
  }

  private void submitEmptyOrderForm() {
    assertThat(browser.getCurrentUrl()).isEqualTo(currentOrderDetailsPageUrl());
    browser.findElementByCssSelector("form").submit();
    
    assertThat(browser.getCurrentUrl()).isEqualTo(orderDetailsPageUrl());

    List<String> validationErrors = getValidationErrorTexts();
    assertThat(validationErrors.size()).isEqualTo(9);
    assertThat(validationErrors).containsExactlyInAnyOrder(
        "Please correct the problems below and resubmit.",
        "Delivery name is required",
        "Street is required",
        "City is required",
        "State is required",
        "Zip code is required",
        "Not a valid credit card number",
        "Must be formatted MM/YY",
        "Invalid CVV"
        );
  }

  private List<String> getValidationErrorTexts() {
    List<WebElement> validationErrorElements = browser.findElementsByClassName("validationError");
    List<String> validationErrors = validationErrorElements.stream()
        .map(el -> el.getText())
        .collect(Collectors.toList());
    return validationErrors;
  }

  private void submitInvalidOrderForm() {
    assertThat(browser.getCurrentUrl()).startsWith(orderDetailsPageUrl());
    fillField("input#deliveryName", "I");
    fillField("input#deliveryStreet", "1");
    fillField("input#deliveryCity", "F");
    fillField("input#deliveryState", "C");
    fillField("input#deliveryZip", "8");
    fillField("input#ccNumber", "1234432112344322");
    fillField("input#ccExpiration", "14/91");
    fillField("input#ccCVV", "1234");
    browser.findElementByCssSelector("form").submit();
    
    assertThat(browser.getCurrentUrl()).isEqualTo(orderDetailsPageUrl());

    List<String> validationErrors = getValidationErrorTexts();
    assertThat(validationErrors.size()).isEqualTo(4);
    assertThat(validationErrors).containsExactlyInAnyOrder(
        "Please correct the problems below and resubmit.",
        "Not a valid credit card number",
        "Must be formatted MM/YY",
        "Invalid CVV"
        );
  }

  private void fillField(String fieldName, String value) {
    WebElement field = browser.findElementByCssSelector(fieldName);
    field.clear();
    field.sendKeys(value);
  }
  
  private void assertIngredient(WebElement ingredientGroup, 
                                int ingredientIdx, String id, String name) {
    List<WebElement> proteins = ingredientGroup.findElements(By.tagName("div"));
    WebElement ingredient = proteins.get(ingredientIdx);
    assertThat(
        ingredient.findElement(By.tagName("input")).getAttribute("value"))
        .isEqualTo(id);
    assertThat(
        ingredient.findElement(By.tagName("span")).getText())
        .isEqualTo(name);
  }

  private void clickDesignATaco() {
    assertThat(browser.getCurrentUrl()).isEqualTo(homePageUrl());
    browser.findElementByCssSelector("a[id='design']").click();
  }

  private void clickBuildAnotherTaco() {
    assertThat(browser.getCurrentUrl()).startsWith(orderDetailsPageUrl());
    browser.findElementByCssSelector("a[id='another']").click();
  }

 
  //
  // URL helper methods
  //
  private String designPageUrl() {
    return homePageUrl() + "design";
  }

  private String homePageUrl() {
    return "http://localhost:" + port + "/";
  }

  private String orderDetailsPageUrl() {
    return homePageUrl() + "orders";
  }

  private String currentOrderDetailsPageUrl() {
    return homePageUrl() + "orders/current";
  }

}
