package at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo;

import com.esotericsoftware.kryo.Kryo;

public class KryoBuilder {

	public static Kryo buildKryo() {
		Kryo kryo = new Kryo();
		kryo.setReferences(false);
		return kryo;
	}
	
	public static Kryo buildKryo(KryoRegistrator kryoRegistrator) {
		Kryo kryo = new Kryo();
		kryo.setReferences(false);
		KryoObjectRegistration.registerDefaultObjects(kryo);

		if (kryoRegistrator != null) {
			KryoObjectRegistration.registerTypes(kryo,
					kryoRegistrator.getRegistrationObjects());
			KryoObjectRegistration.registerTypes(kryo,
					kryoRegistrator.getRegistrationObjectsWithSerializer());
		}
		return kryo;
	}
	
}
