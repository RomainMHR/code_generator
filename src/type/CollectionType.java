package type;

public class CollectionType extends Type {
    public enum Format { LIST, SET, BAG, ARRAY }
    
    private Format format;
    private Type elementType;
    private Integer minSize;
    private Integer maxSize;

    // Constructeur
    public CollectionType(Format format, Type elementType, Integer min, Integer max) {
        this.format = format;
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
        return format.toString() + "<" + elementType.getName() + ">";
    }

    @Override
    public boolean isCompatibleWith(Type other) {
        if (other instanceof CollectionType) {
            CollectionType otherCol = (CollectionType) other;
            return this.format == otherCol.format && 
                   this.elementType.isCompatibleWith(otherCol.elementType);
        }
        return false;
    }
    
    public boolean isFixedSize() {
        return format == Format.ARRAY;
    }
}