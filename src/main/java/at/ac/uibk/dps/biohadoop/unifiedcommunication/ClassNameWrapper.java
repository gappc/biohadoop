package at.ac.uibk.dps.biohadoop.unifiedcommunication;

import java.io.Serializable;

public class ClassNameWrapper<T> implements Serializable {

	private static final long serialVersionUID = -6546660791699378326L;

	private String className;
	private T wrapped;

	public ClassNameWrapper() {
	}

	public ClassNameWrapper(String className, T wrapped) {
		this.className = className;
		this.wrapped = wrapped;
	}

	public String getClassName() {
		return className;
	}

	public T getWrapped() {
		return wrapped;
	}

}
