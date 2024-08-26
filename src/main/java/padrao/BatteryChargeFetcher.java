





package padrao;


//PegarDoHTML
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;


//PegarDoAparelho
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;




public class BatteryChargeFetcher {

    public static void main(String[] args) {
    	
    	
    	
    	TipoFonte fonte = TipoFonte.PEGAR_DO_HTML;

        switch (fonte) {
            case PEGAR_DO_HTML:
                System.out.println("Selecionado: Pegar do HTML");
                
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
                
                break;
            case PEGAR_DO_APARELHO:
                System.out.println("Selecionado: Pegar do Aparelho");
                
                try {
                    String batteryCapacity = getBatteryCapacity();
                    System.out.println("Battery Capacity: " + batteryCapacity + "%");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                break;
        }
    }
    
    
    private static String getBatteryCapacity() throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        
        switch (osName) {
            case "linux":
                return readBatteryCapacityLinux();
            case "mac os x":
                return getBatteryCapacityMacOS();
            case "windows":
                return getBatteryCapacityWindows();
            default:
                // Assume 100% battery for non-supported systems or desktops without battery
                return "100";
        }
    }

    private static String readBatteryCapacityLinux() throws IOException {
        String filePath = "/sys/class/power_supply/BAT0/capacity";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.readLine();
        } catch (IOException e) {
            // Handle case where battery information is not available
            return "100";
        }
    }

    private static String getBatteryCapacityMacOS() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("pmset", "-g", "batt");
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            System.out.println("Frase que devia ter o %: " + line);
            System.out.println("    Moral da historia, pegar assim n eh confiavel, olha o que ele vai dizer agora:");
            if (line != null && line.contains("%")) {
                return line.split(";")[1].trim().split(" ")[0];
            }
            return "100";
        }
    }

    private static String getBatteryCapacityWindows() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("wmic", "batterystatus", "get", "EstimatedChargeRemaining");
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            reader.readLine(); // Skip the header line
            String line = reader.readLine();
            if (line != null) {
                return line.trim();
            }
            return "100";
        }
    }
}
