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
    private GeneratorConfig config;

    @Before
    public void setUp() {
        // 1. Chargement de la configuration
        config = new GeneratorConfig();
        File configFile = new File("config.xml");
        
        if (!configFile.exists()) {
            fail("Le fichier config.xml est introuvable à la racine !");
        }
        config.loadConfigFile(configFile);

        // 2. Instanciation de l'analyseur AVEC la config (Injection de dépendance)
        analyser = new XMLAnalyser(config);
        
        // 3. Chargement du modèle satellite.xml
        File xmlFile = new File("satellite.xml");
        if (!xmlFile.exists()) {
            fail("Le fichier satellite.xml est introuvable à la racine !");
        }
        
        model = analyser.getModelFromFile(xmlFile);
        assertNotNull("Le modèle n'a pas pu être chargé (null)", model);
    }

    @Test
    public void testEntitesCount() {
        assertEquals("Le modèle devrait contenir exactement 4 entités", 4, model.getEntities().size());
    }

    @Test
    public void testHeritageSatellite() {
        Entity satellite = getEntityByName("Satellite");
        assertEquals("Satellite doit hériter de EnginSpatial", "EnginSpatial", satellite.getParentName());
    }

    @Test
    public void testCollectionFlotte() {
        Entity flotte = getEntityByName("Flotte");
        Attribute vaisseaux = flotte.getAttributes().get(0);
        assertTrue("Le type doit être une CollectionType", vaisseaux.getType() instanceof CollectionType);
        
        CollectionType colType = (CollectionType) vaisseaux.getType();
        Type innerType = getEffectiveType(colType.getElementType());
        
        assertEquals("La liste doit contenir des 'EnginSpatial'", "EnginSpatial", innerType.getName());
    }

    @Test
    public void testResolutionReferences() {
        Entity satellite = getEntityByName("Satellite");
        Attribute parentAttr = satellite.getAttributes().get(1); // parent
        
        assertTrue("La référence doit être résolue", parentAttr.getType() instanceof ResolvedReference);
        assertEquals("Le type réel doit être Flotte", "Flotte", ((ResolvedReference)parentAttr.getType()).getActualType().getName());
    }

    @Test
    public void testGenerationCode() {
        // Plus besoin de recharger la config ici, elle est déjà chargée dans le setUp
        JavaGenerator generator = new JavaGenerator(config);
        model.accept(generator);
        String code = generator.getCode();

        assertTrue("Le package doit correspondre à la config", code.contains("package com.monprojet.simulation;"));
        assertTrue("La classe Satellite doit étendre EnginSpatial", code.contains("public class Satellite extends EnginSpatial"));
        assertTrue("L'import ArrayList doit être présent", code.contains("import java.util.ArrayList;"));
        assertTrue("La liste doit être typée", code.contains("ArrayList<EnginSpatial> vaisseaux"));
    }

    // --- Helpers ---

    private Entity getEntityByName(String name) {
        Optional<Entity> ent = model.getEntities().stream()
                .filter(e -> e.getName().equals(name))
                .findFirst();
        if (ent.isPresent()) return ent.get();
        fail("Entité " + name + " introuvable.");
        return null;
    }

    private Type getEffectiveType(Type type) {
        if (type instanceof ResolvedReference) {
            return ((ResolvedReference) type).getActualType();
        }
        return type;
    }
}