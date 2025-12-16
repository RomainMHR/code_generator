package type;

public abstract class Type {
    public abstract String getName();
    
    public abstract boolean isCompatibleWith(Type other);
}
