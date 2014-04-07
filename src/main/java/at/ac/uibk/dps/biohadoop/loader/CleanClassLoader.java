package at.ac.uibk.dps.biohadoop.loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class CleanClassLoader {

	public void startCleanClassLoader(String[] args, List<URL> urls) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ClassLoader classloader =
                new URLClassLoader(
                        urls.toArray(new URL[0]),
                        ClassLoader.getSystemClassLoader().getParent());
		
        Class<?> mainClass = classloader.loadClass("at.ac.uibk.dps.biohadoop.loader.WeldLoader");
        Method startWeldContainer = mainClass.getMethod("startWeldContainer", new Class[] {String[].class});
        Thread.currentThread().setContextClassLoader(classloader);
        
        startWeldContainer.invoke(null, new Object[] {args});
	}
}
