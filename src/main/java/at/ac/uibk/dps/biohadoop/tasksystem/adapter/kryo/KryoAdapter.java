package at.ac.uibk.dps.biohadoop.tasksystem.adapter.kryo;

import java.util.Map;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.AdapterException;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoObjectRegistration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoRegistrator;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.Worker;

import com.esotericsoftware.kryo.Kryo;

public class KryoAdapter implements Adapter {
	@Override
	public Class<? extends Worker> getMatchingWorkerClass() {
		// TODO Auto-generated method stub
		return null;
	}
//	private static final Logger LOG = LoggerFactory
//			.getLogger(KryoAdapter.class);
//
//	private final Server server = new Server(64 * 1024, 64 * 1024);
//
//	private String pipelineName;
//	private KryoConnection<?, ?, ?> kryoConnection;

//	@Override
//	public void configure(String pipelineName) {
//		this.pipelineName = pipelineName;
//		kryoConnection = new KryoConnection<>(pipelineName);
//	}

	@Override
	public void start(String pipelineName) throws AdapterException {
//		LOG.info("Starting Kryo server");
//		startServer();
//		server.addListener(kryoConnection);
	}

	@Override
	public void stop() {
//		LOG.info("KryoServer waiting to shut down");
//		kryoConnection.stop();
//		server.stop();
	}

	private void startServer() throws AdapterException {
//		new Thread(server).start();
//
//		String prefix = pipelineName;
//		String host = HostInfo.getHostname();
//
//		PortFinder.aquireBindingLock();
//		int port = HostInfo.getPort(30000);
//		try {
//			server.bind(port);
//		} catch (IOException e) {
//			throw new AdapterException("Could not bin zu server " + host + ":"
//					+ port);
//		}
//		PortFinder.releaseBindingLock();
//
//		Environment.setPrefixed(prefix, Environment.KRYO_SOCKET_HOST, host);
//		Environment.setPrefixed(prefix, Environment.KRYO_SOCKET_PORT,
//				Integer.toString(port));
//
//		Kryo kryo = server.getKryo();
//		registerObjects(kryo);
//
//		LOG.info("host: {} port: {} pipeline: {}", HostInfo.getHostname(), port,
//				pipelineName);
	}

	private void registerObjects(Kryo kryo) throws AdapterException {
		KryoObjectRegistration.registerDefaultObjects(kryo);
		Map<String, String> properties = Environment
				.getBiohadoopConfiguration().getGlobalProperties();
		if (properties != null) {
			String kryoRegistratorClassName = properties
					.get(KryoRegistrator.KRYO_REGISTRATOR);
			if (kryoRegistratorClassName != null) {
//				LOG.info("Registering additional objects for Kryo serialization");
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

//	@Override
//	public int getPort(String pipelineName) throws AdapterException {
//		// TODO Auto-generated method stub
//		return 0;
//	}

}
