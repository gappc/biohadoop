package at.ac.uibk.dps.biohadoop.communication.master.rest;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.master.DedicatedRest;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.utils.ResourcePath;
import at.ac.uibk.dps.biohadoop.webserver.deployment.DeployingClasses;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//@Path("/")
public class RestMasterEndpoint implements MasterLifecycle {

	private static final Logger LOG = LoggerFactory
			.getLogger(RestMasterEndpoint.class);
	private static final Map<String, DefaultMasterImpl> MASTERS = new ConcurrentHashMap<>();
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	public void configure(Class<? extends RemoteExecutable<?, ?, ?>> master) {
//		Annotation annotation = master.getAnnotation(DedicatedRest.class);
//		ResourcePath.addRestEntry(((DedicatedRest) annotation).path(), master);
//		DeployingClasses.addRestfulClass(RestMasterEndpoint.class);
	}
	
	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@GET
	@Path("{path}/registration")
	@Produces(MediaType.APPLICATION_JSON)
	public Message<?> registration(@PathParam("path") String path) {
		try {
			DefaultMasterImpl masterEndpoint = buildMasterEndpoint(path);
			Object registrationObject = getRegistrationObject(path);
			return masterEndpoint.handleRegistration(registrationObject);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@GET
	@Path("{path}/workinit")
	@Produces(MediaType.APPLICATION_JSON)
	public Message<?> workinit(@PathParam("path") String path) {
		try {
			DefaultMasterImpl masterEndpoint = buildMasterEndpoint(path);
			return masterEndpoint.handleWorkInit();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@POST
	@Path("{path}/work")
	@Produces(MediaType.APPLICATION_JSON)
	public Message<?> work(@PathParam("path") String path, String messageString) {
		try {
			DefaultMasterImpl masterEndpoint = buildMasterEndpoint(path);
			Message<?> inputMessage = getInputMessage(path, messageString);
			return masterEndpoint.handleWork(inputMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private DefaultMasterImpl buildMasterEndpoint(String path)
			throws InstantiationException, IllegalAccessException {
		DefaultMasterImpl masterEndpoint = MASTERS.get(path);
		if (masterEndpoint == null) {
			Class<? extends RemoteExecutable<?, ?, ?>> masterClass = ResourcePath
					.getRestEntry(path);
			String queueName = masterClass.getAnnotation(DedicatedRest.class)
					.queueName();
			masterEndpoint = DefaultMasterImpl.newInstance(queueName);
			MASTERS.put(path, masterEndpoint);
		}
		return masterEndpoint;
	}

	private Object getRegistrationObject(String path)
			throws InstantiationException, IllegalAccessException {
		Class<? extends RemoteExecutable<?, ?, ?>> masterClass = ResourcePath.getRestEntry(path);
		RemoteExecutable<?, ?, ?> master = masterClass.newInstance();
		return master.getInitalData();
	}

	private Message<?> getInputMessage(String path, String messageString)
			throws JsonParseException, JsonMappingException, IOException {
//		Class<? extends RemoteExecutable<?, ?, ?>> masterClass = ResourcePath.getRestEntry(path);
//		Class<?> receiveClass = masterClass.getAnnotation(DedicatedRest.class)
//				.masterInputClass();
//		JavaType javaType = OBJECT_MAPPER.getTypeFactory()
//				.constructParametricType(Message.class, receiveClass);
//		return OBJECT_MAPPER.readValue(messageString, javaType);
		return null;
	}

}
