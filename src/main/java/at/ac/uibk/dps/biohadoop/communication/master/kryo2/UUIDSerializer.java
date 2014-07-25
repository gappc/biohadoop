package at.ac.uibk.dps.biohadoop.communication.master.kryo2;

import java.util.UUID;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class UUIDSerializer extends Serializer<UUID> {

	@Override
	public UUID read(Kryo kryo, Input input, Class<UUID> type) {
		return UUID.fromString(input.readString());
	}

	@Override
	public void write(Kryo kryo, Output output, UUID uuid) {
		output.writeString(uuid.toString());
	}
}
