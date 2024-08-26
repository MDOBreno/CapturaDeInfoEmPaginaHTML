





package padrao;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class BatteryChargeFetcher {

    public static void main(String[] args) {
        // Configura o driver do Chrome (caminho para o ChromeDriver)
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");

        // Inicia o WebDriver
        WebDriver driver = new ChromeDriver();

        try {
            // Acessa a página local
            driver.get("http://localhost:4027");

            // Define o tempo máximo de espera
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Espera até que o elemento com o atributo data-field="battery_charge" esteja visível
            WebElement batteryChargeElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span[data-field='battery_charge']"))
            );

            // Obtém o texto dentro do elemento (valor da bateria)
            String batteryCharge = batteryChargeElement.getText();

            // Exibe o valor da carga da bateria
            System.out.println("Carga da Bateria: " + batteryCharge + "%");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Fecha o navegador
            driver.quit();
        }
    }
}
