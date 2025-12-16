package metaModel;

import type.Type;

public class Attribute implements MinispecElement {
    private String name;
    private Type type;

    public Attribute(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }

    public void accept(Visitor v) {
        v.visitAttribute(this);
    }
}