package at.ac.uibk.dps.biohadoop.torename;

public class ClassnameProvider {

	private ClassnameProvider() {
	}
	
	public static String getClassname(Class<?> clazz) {
		return clazz.getSimpleName();
	}
}
