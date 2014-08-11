package at.ac.uibk.dps.biohadoop.unifiedcommunication;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointConfigureException;

public class StartMaster {

	private static final Logger LOG = LoggerFactory
			.getLogger(StartMaster.class);

	private static final Map<String, String> STARTED_COMMUNICATIONS = new ConcurrentHashMap<>();

	public static List<MasterLifecycle> start(
			Class<? extends RemoteExecutable<?, ?, ?>> communicationClass,
			String queueName) {

		List<MasterLifecycle> masters = new ArrayList<>();
		
		if (STARTED_COMMUNICATIONS.get(communicationClass.getCanonicalName()) != null) {
			return masters;
		}

		Annotation[] annotations = communicationClass.getAnnotations();
		for (Annotation annotation : annotations) {
			LOG.debug(
					"Found annotation {} on possible communication endpoint {}",
					annotation, communicationClass);

			Class<? extends MasterLifecycle> masterClass = null;

			Method[] methods = annotation.annotationType().getMethods();
			try {
				for (Method method : methods) {
					LOG.debug("Found method {} for annotation {}", method,
							annotation);
					if ("master".equals(method.getName())) {
						LOG.info(
								"Found suitable annotation {} with \"master\" method",
								annotation);

						masterClass = (Class<? extends MasterLifecycle>) method
								.invoke(annotation);

						MasterLifecycle master = masterClass.newInstance();
						RemoteExecutable<?, ?, ?> communication = communicationClass
								.newInstance();
						Object registrationObject = communication
								.getInitalData();

						LOG.info("Configuring communication {} with queue={}",
								masterClass.getCanonicalName(), queueName);
						master.configure(communicationClass);
						LOG.info("Starting communication {} with queue={}",
								masterClass.getCanonicalName(), queueName);
						masters.add(master);
						STARTED_COMMUNICATIONS.put(
								communicationClass.getCanonicalName(),
								communicationClass.getCanonicalName());
					}
				}
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EndpointConfigureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return masters;
	}

}
