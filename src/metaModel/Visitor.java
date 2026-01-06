package metaModel;

public abstract class Visitor {
	public void visitModel(Model m) {}
	public void visitEntity(Entity e) {}
	public void visitAttribute(Attribute a) {}
}