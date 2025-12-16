package type;

public abstract class ReferenceType extends Type {
    protected String name;

    public ReferenceType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}