package type;

public class CollectionType extends Type {
    public enum Kind { LIST, SET, BAG, ARRAY }
    
    private Kind kind;
    private Type elementType;
    private Integer minSize;
    private Integer maxSize;

    // Constructeur
    public CollectionType(Kind kind, Type elementType, Integer min, Integer max) {
        this.kind = kind;
        this.elementType = elementType;
        this.minSize = min;
        this.maxSize = max;
    }

    public Type getElementType() {
        return elementType;
    }
    
    public void setElementType(Type elementType) {
        this.elementType = elementType;
    }

    @Override
    public String getName() {
        return kind.toString() + "<" + elementType.getName() + ">";
    }

    @Override
    public boolean isCompatibleWith(Type other) {
        if (other instanceof CollectionType) {
            CollectionType otherCol = (CollectionType) other;
            return this.kind == otherCol.kind && 
                   this.elementType.isCompatibleWith(otherCol.elementType);
        }
        return false;
    }
    
    public boolean isFixedSize() {
        return kind == Kind.ARRAY;
    }
}