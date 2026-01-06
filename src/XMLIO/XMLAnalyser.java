package XMLIO;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import generation.GeneratorConfig;

import java.io.*;
import java.util.*;

import metaModel.*;
import metaModel.Entity;
import type.*; 

public class XMLAnalyser {
	
	private GeneratorConfig config;

    public XMLAnalyser(GeneratorConfig config) {
    	this.config = config;
    }

    public Model getModelFromInputStream(InputStream stream) {
        try {
            DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
            DocumentBuilder constructeur = fabrique.newDocumentBuilder();
            Document document = constructeur.parse(stream);
            document.getDocumentElement().normalize();
            
            return getModelFromDocument(document);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Model getModelFromFile(File file) {      
        try {
            return getModelFromInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Model getModelFromDocument(Document document) {
        Element root = document.getDocumentElement();
        Model model = new Model();
        
        if (root.hasAttribute("name")) {
            model.setName(root.getAttribute("name"));
        }

        NodeList entityNodes = root.getElementsByTagName("entity");
        
        for (int j = 0; j < entityNodes.getLength(); j++) {
            Element entityElement = (Element) entityNodes.item(j);
            Entity entity = parseEntity(entityElement);
            model.addEntity(entity);
        }

        resolveReferences(model);
                
        return model;
    }

    private Entity parseEntity(Element e) {
        Entity entity = new Entity();
        entity.setName(e.getAttribute("name"));
        
        if (e.hasAttribute("extends")) {
            entity.setParentName(e.getAttribute("extends"));
        }

        NodeList nList = e.getElementsByTagName("attribute");
        for (int i = 0; i < nList.getLength(); i++) {
            Element attrElement = (Element) nList.item(i);
            
            String name = attrElement.getAttribute("name");
            String typeName = attrElement.getAttribute("type");
            String colType = attrElement.getAttribute("collection");
            String minStr = attrElement.getAttribute("min");
            String maxStr = attrElement.getAttribute("max");

            Type builtType = buildType(typeName, colType, minStr, maxStr);

            Attribute attr = new Attribute(name, builtType);
            entity.addAttribute(attr);
        }
        
        return entity;
    }
    
    private Type buildType(String typeName, String colType, String minStr, String maxStr) {
        Type coreType;

        if (isPrimitive(typeName)) {
            coreType = new ResolvedReference(typeName, new PrimitiveType(typeName));
        } 
        else {
            coreType = new UnresolvedReference(typeName);
        }

        if (colType != null && !colType.isEmpty()) {
            try {
                CollectionType.Kind kind = CollectionType.Kind.valueOf(colType.toUpperCase());
                Integer min = (minStr == null || minStr.isEmpty()) ? null : Integer.parseInt(minStr);
                Integer max = (maxStr == null || maxStr.isEmpty() || maxStr.equals("*")) ? null : Integer.parseInt(maxStr);
                
                return new CollectionType(kind, coreType, min, max);
            } catch (Exception e) {
                return coreType;
            }
        }
        return coreType;
    }

    private void resolveReferences(Model model) {
        Set<String> validEntityNames = new HashSet<>();
        for (Entity e : model.getEntities()) {
            validEntityNames.add(e.getName());
        }

        for (Entity entity : model.getEntities()) {
            for (Attribute attr : entity.getAttributes()) {
                Type currentType = attr.getType();

                if (currentType instanceof UnresolvedReference) {
                    Type resolved = resolve((UnresolvedReference) currentType, validEntityNames);
                    attr.setType(resolved);
                }
                
                else if (currentType instanceof CollectionType) {
                    CollectionType col = (CollectionType) currentType;
                    Type elemType = col.getElementType();
                    
                    if (elemType instanceof UnresolvedReference) {
                        Type resolvedElem = resolve((UnresolvedReference) elemType, validEntityNames);
                        col.setElementType(resolvedElem);
                    }
                }
            }
        }
    }

    private Type resolve(UnresolvedReference ref, Set<String> validEntityNames) {
        String name = ref.getName();

        if (validEntityNames.contains(name)) {
            return new ResolvedReference(name, new EntityType(name));
        }

        if (isPrimitive(name)) {
            return new ResolvedReference(name, new PrimitiveType(name));
        }

        throw new RuntimeException("Erreur Sémantique : Le type '" + name + "' est introuvable dans le modèle.");
    }

    private boolean isPrimitive(String typeName) {
        if (this.config != null && this.config.isKnownType(typeName)) {
            return true;
        }
        return false;
    }
}