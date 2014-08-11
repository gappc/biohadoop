package at.ac.uibk.dps.biohadoop.utils;

//TODO Rename to ClassNameProvider to adhere to std
public class ClassnameProvider {

	private ClassnameProvider() {
	}

	public static String getClassname(Class<?> clazz) {
		return clazz.getSimpleName();
	}
}
