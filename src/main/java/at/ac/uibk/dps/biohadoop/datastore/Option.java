package at.ac.uibk.dps.biohadoop.datastore;

import java.io.Serializable;

public class Option<T> implements Serializable {

	private static final long serialVersionUID = -3765321042287092766L;

	private final String name;
	private final Class<T> clazz;
	
	public Option(String name, Class<T> clazz) {
		this.name = name;
		this.clazz = clazz;
	}

	public String getName() {
		return name;
	}

	public Class<T> getClazz() {
		return clazz;
	}
	
}
