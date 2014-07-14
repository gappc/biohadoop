package at.ac.uibk.dps.biohadoop.utils;

public class ClassnameProvider {

	private ClassnameProvider() {
	}
	
	public static String getClassname(Class<?> clazz) {
		return clazz.getSimpleName();
	}
}
