package type;

public class ResolvedReference extends ReferenceType {
    
    private Type actualType; 

    public ResolvedReference(String name, Type actualType) {
        super(name);
        this.actualType = actualType;
    }

    public Type getActualType() {
        return actualType;
    }

    @Override
    public boolean isCompatibleWith(Type other) {
        if (other instanceof ResolvedReference) {
            return this.actualType.isCompatibleWith(((ResolvedReference) other).actualType);
        }
        return this.actualType.isCompatibleWith(other);
    }
}