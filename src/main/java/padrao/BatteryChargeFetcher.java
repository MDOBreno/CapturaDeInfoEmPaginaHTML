package padrao;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class BatteryChargeFetcher {

    public static void main(String[] args) {
        System.out.println("fsdfs");
        
        try {
            // Adiciona um delay para esperar o carregamento completo da página
            Thread.sleep(5000); // Espera 5 segundos
            
            // Carrega o documento HTML da página
            Document doc = Jsoup.connect("http://localhost:4027").get();

            // Busca o elemento que tem o atributo data-field="battery_charge"
//            Element batteryChargeElement = doc.select("span[class='battery']").first();
            Element batteryChargeElement = doc.select("span[data-field='battery_charge']").first();

            // Obtém o texto dentro do elemento (valor da bateria)
            String batteryCharge = batteryChargeElement != null ? batteryChargeElement.text() : "Não encontrado";

            // Exibe o valor da carga da bateria
            System.out.println("Carga da Bateria: " + batteryCharge + "%");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
