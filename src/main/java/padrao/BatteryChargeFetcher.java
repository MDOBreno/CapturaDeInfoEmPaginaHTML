

package padrao;


//PegarDoHTML
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;


//PegarDoAparelho
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;


//Escrever no arquivo o valor capturado do html
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;




public class BatteryChargeFetcher {

    public static void main(String[] args) {
    	
    	
    	
    	TipoFonte fonte = TipoFonte.PEGAR_DO_HTML;
    	
    	try {
	    	while(true) {
	    		switch (fonte) {
		            case PEGAR_DO_HTML:
		            	
		                System.out.println("Selecionado: Pegar do HTML");
		                
		                // Configura o driver do Chrome (caminho para o ChromeDriver)
		                System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
		            	
		
		                // Inicia o WebDriver
		                WebDriver driver;
		                try {
		                	// Inicializa o ChormeDrive
		                	ChromeOptions options = new ChromeOptions();
		                	options.addArguments("--headless"); // modo invisível
		                	driver = new ChromeDriver(options);
		                    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
		        		} catch (Exception e) {
		        			// Executa o script de atualização do ChromeDriver
		                    try {
		        				executeChromeDriverUpdateScript();
		        			} catch (IOException e1) {
		        				// Caso o script não tenha sido executado com sucesso
		        				System.out.println("Erro ao tentar atualizar o ChromeDriver automaticamente:");
		        				e1.printStackTrace();
		        			}
		                    // Inicializa o ChormeDrive apos ter atualizado
		        			driver = new ChromeDriver();
		        		}
		
		                try {
		                    // Acessa a página local
		                    driver.get("http://localhost:4027");
		
		                    // Define o tempo máximo de espera
		                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
		
		                    // Espera até que o elemento com o atributo data-field="novo IP Global" esteja visível e com conteúdo
		                    WebElement ipExternalElement = wait.until(webDriver -> {
		                        WebElement element = webDriver.findElement(By.cssSelector("p[data-field='network_ipExternal']"));
		                        return element.isDisplayed() && !element.getText().isEmpty() ? element : null;
		                    });
		
		                    // Obtém o texto dentro do elemento (valor do novo IP Global)
		                    String ip = ipExternalElement.getText();
		
		                    // Exibe o valor do novo IP Global
		                    System.out.println("IP Externo: " + ip);
		                    
		                    //Escreve o conteudo capturado do html em um arquivo txt
		                    if (!ip.equals("")) {
		                    	
		                    	if (ip.equals("offline")) {
		                    		try {
		                    		    reiniciarOiStatistica();
		                    		    System.out.println("iStatistica reiniciado com sucesso.");
		                    		} catch (IOException e) {
		                    		    System.out.println("Erro ao tentar reiniciar o iStatistica.");
		                    		    e.printStackTrace();
		                    		}
		                    		continue;
								} else {
									try {
										Files.write(
												Paths.get("/Users/brenomedeiros/Library/Mobile Documents/com~apple~CloudDocs/Servidor/ipExterno.txt"),
												ip.getBytes(StandardCharsets.UTF_8)
												);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
		
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
	
	            // Aguarda 1 minuto (calcular conversao para milisegundos)
	    		long millis = (1*60*1000);
	            Thread.sleep(millis);
	    	}
    	
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    
    //Método que vai fazer o script rodar
    private static void executeChromeDriverUpdateScript() throws IOException {
    	// Executa o comando
        try {
            // Comando para obter a versão do ChromeDriver
        	String[] commandVersao = {
        		    "/bin/bash", "-c", "/Applications/Google\\ Chrome.app/Contents/MacOS/Google\\ Chrome --version"
        		};

            Process process = new ProcessBuilder(commandVersao).start();
            String saidaDaExecucao = readStream(process.getInputStream()).trim();

            System.out.println("A saída da execução é: " + saidaDaExecucao);

            // Fazendo o split para pegar o número da versão
            String[] partes = saidaDaExecucao.split(" ");
            if (partes.length > 1) {
                String minhaVersaoDoChrome = partes[2]; // Número da versão

                // Comando para baixar e instalar o ChromeDriver correspondente
                String[] command = {
                    "/bin/bash", "-c", 
                    "VERSION=\"" + minhaVersaoDoChrome + "\" && curl -o chromedriver-mac.zip \"https://storage.googleapis.com/chrome-for-testing-public/$VERSION/mac-arm64/chromedriver-mac-arm64.zip\" && unzip chromedriver-mac.zip && mv chromedriver-mac-arm64/chromedriver /usr/local/bin/chromedriver && chmod +x /usr/local/bin/chromedriver && rm -rf chromedriver-mac-arm64 && rm -f chromedriver-mac.zip"
                };

                Process installProcess = new ProcessBuilder(command).start();
                installProcess.waitFor(); // Aguarda a instalação terminar

                System.out.println("ChromeDriver atualizado para a versão: " + minhaVersaoDoChrome);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Método que vai fazer o script rodar
    private static void executeAbrirOiStatistica() throws IOException {
        // O comando para abrir o aplicativo iStatistica
        String[] command = {
                "/bin/bash", "-c", "open -a \"iStatistica\""
        };

        // Executa o comando
        Process process = new ProcessBuilder(command).start();
        String saidaDaExecucao = readStream(process.getInputStream());
//        System.out.println("A saidaDaExecucao é: " + saidaDaExecucao);

        // Espera o processo terminar
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Erro ao executar o comando para abrir o iStatistica.");
            }
        } catch (InterruptedException e) {
            throw new IOException("Erro ao aguardar o término do comando para abrir o iStatistica.", e);
        }
    }
    
    private static void fecharOiStatistica() throws IOException {
        String[] command = {
                "/bin/bash", "-c", "pkill -f iStatistica"
        };

        Process process = new ProcessBuilder(command).start();
        String saidaDaExecucao = readStream(process.getInputStream());

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Erro ao executar o comando para fechar o iStatistica.");
            }
        } catch (InterruptedException e) {
            throw new IOException("Erro ao aguardar o término do comando para fechar o iStatistica.", e);
        }
    }
    
    private static void reiniciarOiStatistica() throws IOException {
        fecharOiStatistica();
        
        // Espera 5 segundos
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new IOException("Erro ao esperar 5 segundos antes de reabrir o iStatistica", e);
        }
        
        // Reabre o app
        executeAbrirOiStatistica();
    }


	// Método auxiliar para ler e capturar a saída do processo
	private static String readStream(InputStream inputStream) throws IOException {
		StringBuilder output = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
          	}
      	}
      	return output.toString();
 	}
    
}
