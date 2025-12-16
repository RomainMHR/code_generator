package generation;

import metaModel.*;
import type.*;
import java.util.HashSet;
import java.util.Set;

public class JavaGenerator extends Visitor {

    private StringBuilder buffer = new StringBuilder();
    private boolean generateMethods = false;
    private GeneratorConfig config;

    public JavaGenerator(GeneratorConfig config) {
        this.config = config;
    }

    public String getCode() {
        return buffer.toString();
    }

    @Override
    public void visitModel(Model model) {
        for (Entity entity : model.getEntities()) {
            visitEntityInModel(entity, model.getName());
            buffer.append("\n----------------------\n");
        }
    }

    public void visitEntityInModel(Entity entity, String modelName) {
        String javaPackage = config.getJavaPackageForModel(modelName);
        
        if (javaPackage != null && !javaPackage.isEmpty()) {
            buffer.append("package ").append(javaPackage).append(";\n\n");
        }

        generateImports(entity);

        buffer.append("public class ").append(entity.getName());
        if (entity.getParentName() != null && !entity.getParentName().isEmpty()) {
            buffer.append(" extends ").append(entity.getParentName());
        }
        buffer.append(" {\n");

        this.generateMethods = false;
        for (Attribute attr : entity.getAttributes()) {
            attr.accept(this);
        }
        buffer.append("\n");

        buffer.append("    public ").append(entity.getName()).append("() { }\n\n");

        this.generateMethods = true;
        for (Attribute attr : entity.getAttributes()) {
            attr.accept(this);
        }

        buffer.append("}");
    }

    private void generateImports(Entity entity) {
        Set<String> imports = new HashSet<>();

        for (Attribute attr : entity.getAttributes()) {
            Type t = attr.getType();
            collectImports(t, imports);
        }

        for (String imp : imports) {
            buffer.append("import ").append(imp).append(";\n");
        }
        buffer.append("\n");
    }

    private void collectImports(Type type, Set<String> imports) {
        Type effectiveType = getEffectiveType(type);

        // Si COLLECTION
        if (effectiveType instanceof CollectionType) {
            CollectionType col = (CollectionType) effectiveType;
            
            String rawName = col.getName();
            String kindName = rawName.contains("<") ? rawName.substring(0, rawName.indexOf('<')) : rawName;
            
            GeneratorConfig.JavaTypeDefinition def = config.getJavaType(kindName);
            if (def == null) {
                 String titleCase = kindName.charAt(0) + kindName.substring(1).toLowerCase();
                 def = config.getJavaType(titleCase);
            }
            if (def != null && def.javaPackage != null && !def.javaPackage.isEmpty()) {
                imports.add(def.javaPackage);
            }

            // Récursivité : on continue avec le type interne
            collectImports(col.getElementType(), imports);
        }
        // Si ENTITY
        else if (effectiveType instanceof EntityType) {
            GeneratorConfig.JavaTypeDefinition def = config.getJavaType(effectiveType.getName());
            if (def != null && def.javaPackage != null && !def.javaPackage.isEmpty()) {
                imports.add(def.javaPackage);
            }
        }
        // Si PRIMITIVE
        else if (effectiveType instanceof PrimitiveType) {
             GeneratorConfig.JavaTypeDefinition def = config.getJavaType(effectiveType.getName());
             if (def != null && def.javaPackage != null && !def.javaPackage.isEmpty()) {
                 imports.add(def.javaPackage);
             }
        }
    }
    @Override
    public void visitAttribute(Attribute attr) {
        String typeName = resolveJavaTypeName(attr.getType());
        String name = attr.getName();

        if (!generateMethods) {
            buffer.append("    private ").append(typeName).append(" ").append(name).append(";\n");
        } else {
            String capName = name.substring(0, 1).toUpperCase() + name.substring(1);
            buffer.append("    public ").append(typeName).append(" get").append(capName).append("() {\n");
            buffer.append("        return this.").append(name).append(";\n");
            buffer.append("    }\n");
            buffer.append("    public void set").append(capName).append("(").append(typeName).append(" ").append(name).append(") {\n");
            buffer.append("        this.").append(name).append(" = ").append(name).append(";\n");
            buffer.append("    }\n\n");
        }
    }

    private String resolveJavaTypeName(Type type) {
        Type effectiveType = getEffectiveType(type);

        if (effectiveType instanceof CollectionType) {
            CollectionType col = (CollectionType) effectiveType;
            String inner = resolveJavaTypeName(col.getElementType());
            
            String rawName = col.getName();
            String kindName = rawName.contains("<") ? rawName.substring(0, rawName.indexOf('<')) : rawName;
            
            GeneratorConfig.JavaTypeDefinition def = config.getJavaType(kindName);
            if (def == null) {
                String titleCase = kindName.charAt(0) + kindName.substring(1).toLowerCase();
                def = config.getJavaType(titleCase);
            }
            
            String containerType = (def != null) ? def.javaType : kindName;
            
            if (col.isFixedSize()) return inner + "[]";
            return containerType + "<" + inner + ">";
        } 
        else {
            GeneratorConfig.JavaTypeDefinition def = config.getJavaType(effectiveType.getName());
            return (def != null) ? def.javaType : effectiveType.getName();
        }
    }
    
    private Type getEffectiveType(Type type) {
        if (type instanceof ResolvedReference) {
            return ((ResolvedReference) type).getActualType();
        }
        return type;
    }
 }