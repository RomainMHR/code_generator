package type;

public class UnresolvedReference extends ReferenceType {

    public UnresolvedReference(String name) {
        super(name);
    }

    @Override
    public boolean isCompatibleWith(Type other) {
        // Une référence non résolue n'est jamais valide pour une comparaison de type
        // Elle doit d'abord être résolue.
        return false;
    }
    
    @Override
    public String toString() {
        return "Unresolved(" + name + ")";
    }
}