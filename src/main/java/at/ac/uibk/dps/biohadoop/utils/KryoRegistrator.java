package at.ac.uibk.dps.biohadoop.utils;

import java.util.List;
import java.util.Map;

import com.esotericsoftware.kryo.Serializer;

public interface KryoRegistrator {

	public static String KRYO_REGISTRATOR = "KRYO_REGISTRATOR";
	
	public List<Class<? extends Object>> getRegistrationObjects();

	public Map<Class<? extends Object>, Serializer<?>> getRegistrationObjectsWithSerializer();

}
