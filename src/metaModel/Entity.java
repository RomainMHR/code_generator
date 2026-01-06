package metaModel;

import java.util.ArrayList;
import java.util.List;

public class Entity implements MinispecElement {
	private String name;
	private String parentName; // Pour l'héritage (Partie 4)
	private List<Attribute> attributes;

	public Entity() {
		this.attributes = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public void addAttribute(Attribute a) {
		this.attributes.add(a);
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void accept(Visitor v) {
		v.visitEntity(this);
	}
}