package at.ac.uibk.dps.biohadoop.tasksystem.adapter.kryo;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.AdapterException;
import at.ac.uibk.dps.biohadoop.utils.HostInfo;
import at.ac.uibk.dps.biohadoop.utils.KryoRegistrator;
import at.ac.uibk.dps.biohadoop.utils.PortFinder;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;

public class KryoAdapter implements Adapter {

	private static final Logger LOG = LoggerFactory
			.getLogger(KryoAdapter.class);

	private final Server server = new Server(64 * 1024, 64 * 1024);

	private String pipelineName;
	private KryoConnection<?, ?, ?> kryoConnection;

	@Override
	public void configure(String pipelineName) {
		this.pipelineName = pipelineName;
		kryoConnection = new KryoConnection<>(pipelineName);
	}

	@Override
	public void start() throws AdapterException {
		LOG.info("Starting Kryo server");
		startServer();
		server.addListener(kryoConnection);
	}

	@Override
	public void stop() {
		LOG.info("KryoServer waiting to shut down");
		kryoConnection.stop();
		server.stop();
	}

	private void startServer() throws AdapterException {
		new Thread(server).start();

		String prefix = pipelineName;
		String host = HostInfo.getHostname();

		PortFinder.aquireBindingLock();
		int port = HostInfo.getPort(30000);
		try {
			server.bind(port);
		} catch (IOException e) {
			throw new AdapterException("Could not bin zu server " + host + ":"
					+ port);
		}
		PortFinder.releaseBindingLock();

		Environment.setPrefixed(prefix, Environment.KRYO_SOCKET_HOST, host);
		Environment.setPrefixed(prefix, Environment.KRYO_SOCKET_PORT,
				Integer.toString(port));

		Kryo kryo = server.getKryo();
		registerObjects(kryo);

		LOG.info("host: {} port: {} pipeline: {}", HostInfo.getHostname(), port,
				pipelineName);
	}

	private void registerObjects(Kryo kryo) throws AdapterException {
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
					throw new AdapterException(
							"Could not register objects for Kryo serialization, KryoRegistrator="
									+ kryoRegistratorClassName, e);
				}
			}
		}
	}

}
