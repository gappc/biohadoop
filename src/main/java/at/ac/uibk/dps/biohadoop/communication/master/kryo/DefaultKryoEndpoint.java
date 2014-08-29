package at.ac.uibk.dps.biohadoop.communication.master.kryo;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedKryo;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.MasterException;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.queue.SimpleTaskSubmitter;
import at.ac.uibk.dps.biohadoop.utils.HostInfo;
import at.ac.uibk.dps.biohadoop.utils.KryoRegistrator;
import at.ac.uibk.dps.biohadoop.utils.PortFinder;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;

public class DefaultKryoEndpoint implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultKryoEndpoint.class);

	private final Server server = new Server(64 * 1024, 64 * 1024);

	private String path;
	private DefaultKryoConnection<?, ?, ?> kryoServerEndpoint;

	@Override
	public void configure(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutableClass) {
		path = SimpleTaskSubmitter.QUEUE_NAME;
		if (remoteExecutableClass != null) {
			DedicatedKryo dedicated = remoteExecutableClass
					.getAnnotation(DedicatedKryo.class);
			if (dedicated != null) {
				path = dedicated.queueName();
				LOG.info("Adding dedicated Rest resource at path {}", path);
			} else {
				LOG.error("No suitable annotation for Rest resource found");
			}
		}
		kryoServerEndpoint = new DefaultKryoConnection(remoteExecutableClass,
				path);
	}

	@Override
	public void start() throws MasterException {
		LOG.info("Starting Kryo server");
		startServer();
		server.addListener(kryoServerEndpoint);
	}

	@Override
	public void stop() {
		LOG.info("KryoServer waiting to shut down");
		kryoServerEndpoint.stop();
		server.stop();
	}

	private void startServer() throws MasterException {
		new Thread(server).start();

		String prefix = path;
		String host = HostInfo.getHostname();

		PortFinder.aquireBindingLock();
		int port = HostInfo.getPort(30000);
		try {
			server.bind(port);
		} catch (IOException e) {
			throw new MasterException("Could not bin zu server " + host + ":"
					+ port);
		}
		PortFinder.releaseBindingLock();

		Environment.setPrefixed(prefix, Environment.KRYO_SOCKET_HOST, host);
		Environment.setPrefixed(prefix, Environment.KRYO_SOCKET_PORT,
				Integer.toString(port));

		Kryo kryo = server.getKryo();
		registerObjects(kryo);

		LOG.info("host: {} port: {} queue: {}", HostInfo.getHostname(), port,
				path);
	}

	private void registerObjects(Kryo kryo) throws MasterException {
		KryoObjectRegistration.registerDefaultObjects(kryo);
		Map<String, String> properties = Environment
				.getBiohadoopConfiguration().getGlobalProperties();
		if (properties != null) {
			String kryoRegistratorClassName = properties
					.get(KryoRegistrator.KRYO_REGISTRATOR);
			if (kryoRegistratorClassName != null) {
				LOG.info("Registering additional objects for Kryo serialization");
				try {
					KryoRegistrator kryoRegistrator = (KryoRegistrator) Class
							.forName(kryoRegistratorClassName).newInstance();

					KryoObjectRegistration.registerTypes(kryo,
							kryoRegistrator.getRegistrationObjects());
					KryoObjectRegistration.registerTypes(kryo, kryoRegistrator
							.getRegistrationObjectsWithSerializer());
				} catch (InstantiationException | IllegalAccessException
						| ClassNotFoundException | NoClassDefFoundError e) {
					throw new MasterException(
							"Could not register objects for Kryo serialization, KryoRegistrator="
									+ kryoRegistratorClassName, e);
				}
			}
		}
	}

}
