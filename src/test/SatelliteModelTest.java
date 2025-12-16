package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Optional;

import metaModel.*;
import type.*;
import XMLIO.XMLAnalyser;
import generation.GeneratorConfig;
import generation.JavaGenerator;

public class SatelliteModelTest {

    private Model model;
    private XMLAnalyser analyser;

    @Before
    public void setUp() {
        // 1. Instanciation de l'analyseur
        analyser = new XMLAnalyser();
        
        // 2. Chargement de VOTRE fichier satellite.xml
        File xmlFile = new File("satellite.xml");
        if (!xmlFile.exists()) {
            fail("Le fichier satellite.xml est introuvable à la racine du projet !");
        }
        
        model = analyser.getModelFromFile(xmlFile);
        assertNotNull("Le modèle n'a pas pu être chargé (null)", model);
    }

    @Test
    public void testEntitesCount() {
        // On s'attend à 4 entités : EnginSpatial, Satellite, Fusée, Flotte
        assertEquals("Le modèle devrait contenir exactement 4 entités", 4, model.getEntities().size());
    }

    @Test
    public void testHeritageSatellite() {
        // Récupération de l'entité Satellite
        Entity satellite = getEntityByName("Satellite");
        assertNotNull(satellite);

        // Vérification de l'héritage
        assertEquals("Satellite doit hériter de EnginSpatial", "EnginSpatial", satellite.getParentName());
        
        // Vérification des attributs propres (nom, parent)
        // Note: id et poids sont hérités, donc ils ne sont pas dans la liste des attributs propres de l'entité
        assertEquals("Satellite doit avoir 2 attributs propres (nom, parent)", 2, satellite.getAttributes().size());
    }

    @Test
    public void testCollectionFlotte() {
        Entity flotte = getEntityByName("Flotte");
        assertNotNull(flotte);

        // Récupération de l'attribut 'vaisseaux'
        Attribute vaisseaux = flotte.getAttributes().get(0);
        assertEquals("vaisseaux", vaisseaux.getName());

        // Vérification du type Collection
        assertTrue("Le type doit être une CollectionType", vaisseaux.getType() instanceof CollectionType);
        
        CollectionType colType = (CollectionType) vaisseaux.getType();
        
        // Vérification du contenu de la collection (List<EnginSpatial>)
        // On utilise une méthode helper pour "déballer" la référence résolue
        Type innerType = getEffectiveType(colType.getElementType());
        
        assertTrue("Le contenu doit être une référence vers une Entité", innerType instanceof EntityType);
        assertEquals("La liste doit contenir des 'EnginSpatial'", "EnginSpatial", innerType.getName());
    }

    @Test
    public void testResolutionReferences() {
        // Test si 'parent' dans Satellite pointe bien vers l'entité 'Flotte'
        Entity satellite = getEntityByName("Satellite");
        Attribute parentAttr = satellite.getAttributes().get(1); // 0=nom, 1=parent
        
        assertEquals("parent", parentAttr.getName());
        assertTrue("La référence doit être résolue", parentAttr.getType() instanceof ResolvedReference);
        
        ResolvedReference ref = (ResolvedReference) parentAttr.getType();
        assertEquals("Le type réel doit être Flotte", "Flotte", ref.getActualType().getName());
    }

    @Test
    public void testGenerationCode() {
        // Chargement de la configuration
        GeneratorConfig config = new GeneratorConfig();
        File configFile = new File("config.xml");
        
        if (!configFile.exists()) {
            fail("Le fichier config.xml est manquant pour le test de génération !");
        }
        config.loadConfigFile(configFile);

        // Lancement du générateur
        JavaGenerator generator = new JavaGenerator(config);
        model.accept(generator);
        String code = generator.getCode();

        // Vérifications sur le code généré
        
        // 1. Package correct
        assertTrue("Le package doit correspondre à la config", code.contains("package com.monprojet.simulation;"));
        
        // 2. Héritage Java
        assertTrue("La classe Satellite doit étendre EnginSpatial", code.contains("public class Satellite extends EnginSpatial"));
        
        // 3. Import ArrayList (car List est mappé vers ArrayList dans config.xml)
        assertTrue("L'import ArrayList doit être présent", code.contains("import java.util.ArrayList;"));
        
        // 4. Générique
        assertTrue("La liste doit être typée", code.contains("ArrayList<EnginSpatial> vaisseaux"));
    }

    // --- Méthodes utilitaires pour le test ---

    private Entity getEntityByName(String name) {
        Optional<Entity> ent = model.getEntities().stream()
                .filter(e -> e.getName().equals(name))
                .findFirst();
        
        if (ent.isPresent()) return ent.get();
        fail("L'entité " + name + " n'a pas été trouvée dans le modèle.");
        return null;
    }

    private Type getEffectiveType(Type type) {
        if (type instanceof ResolvedReference) {
            return ((ResolvedReference) type).getActualType();
        }
        return type;
    }
}