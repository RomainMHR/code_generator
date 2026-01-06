package generation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GeneratorConfig {

    // Nom Modèle Minispec -> Package Java dans config
    private Map<String, String> modelPackages = new HashMap<>();

    // Nom du Type Minispec -> Définition Java (Type + Package) dans config
    private Map<String, JavaTypeDefinition> typeDefinitions = new HashMap<>();

    public static class JavaTypeDefinition {
        public String javaType;
        public String javaPackage;

        public JavaTypeDefinition(String type, String pkg) {
            this.javaType = type;
            this.javaPackage = pkg;
        }
    }

    public void loadConfigFile(File configFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(configFile);
            doc.getDocumentElement().normalize();

            NodeList modelNodes = doc.getElementsByTagName("model");
            for (int i = 0; i < modelNodes.getLength(); i++) {
                Element e = (Element) modelNodes.item(i);
                modelPackages.put(e.getAttribute("name"), e.getAttribute("package"));
            }

            NodeList primNodes = doc.getElementsByTagName("primitive");
            for (int i = 0; i < primNodes.getLength(); i++) {
                Element e = (Element) primNodes.item(i);
                String name = e.getAttribute("name");
                String type = e.getAttribute("type");
                String pkg = e.getAttribute("package");
                
                typeDefinitions.put(name, new JavaTypeDefinition(type, pkg));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getJavaPackageForModel(String modelName) {
        return modelPackages.getOrDefault(modelName, modelName);
    }

    public JavaTypeDefinition getJavaType(String minispecTypeName) {
        return typeDefinitions.get(minispecTypeName);
    }
    
    public boolean isKnownType(String typeName) {
        return typeDefinitions.containsKey(typeName);
    }
}