import java.io.File;
import metaModel.Model;
import XMLIO.XMLAnalyser;
import generation.JavaGenerator;
import generation.GeneratorConfig;

public class Main {
    public static void main(String[] args) {
        // Chargement de la config
        GeneratorConfig config = new GeneratorConfig();
        config.loadConfigFile(new File("config.xml"));

        // Chargement modèle
        XMLAnalyser analyser = new XMLAnalyser();
        Model model = analyser.getModelFromFile(new File("satellite.xml"));
        
        if (model != null) {
            // Génération avec Config
            JavaGenerator generator = new JavaGenerator(config);
            model.accept(generator);
            
            System.out.println(generator.getCode());
        }
    }
}