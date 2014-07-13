package at.ac.uibk.dps.biohadoop.torename;

public class Helper {

	private Helper() {
	}
	
	public static String getClassname(Class<?> clazz) {
		return clazz.getSimpleName();
	}
}
