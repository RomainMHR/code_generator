package type;

public class EntityType extends Type {
	
    private String name;
    public EntityType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isCompatibleWith(Type other) {
        return other instanceof EntityType && 
               this.name.equals(((EntityType) other).name);
    }
}