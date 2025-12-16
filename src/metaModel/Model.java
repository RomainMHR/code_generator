package metaModel;

import java.util.ArrayList;
import java.util.List;

public class Model implements MinispecElement {
	
	private String name; // Nom du package
	private List<Entity> entities;
	
	public Model() {
		this.entities = new ArrayList<>();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void addEntity(Entity e) {
		this.entities.add(e);
	}
	
	public List<Entity> getEntities() {
		return entities;
	}
	
	public void accept(Visitor v) {
		v.visitModel(this);
	}
}