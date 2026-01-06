package type;

public class UnresolvedReference extends ReferenceType {

    public UnresolvedReference(String name) {
        super(name);
    }

    @Override
    public boolean isCompatibleWith(Type other) {
        return false;
    }
    
    @Override
    public String toString() {
        return "Unresolved(" + name + ")";
    }
}