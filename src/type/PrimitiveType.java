package type;

public class PrimitiveType extends Type {
    private String typeName;

    public PrimitiveType(String name) {
        this.typeName = name;
    }

    @Override
    public String getName() {
        return typeName;
    }

    @Override
    public boolean isCompatibleWith(Type other) {
        return other instanceof PrimitiveType && 
               this.typeName.equals(((PrimitiveType) other).typeName);
    }
}