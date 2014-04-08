package at.ac.uibk.dps.biohadoop.standalone;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InjectionBeanEncoder implements Encoder.Text<InjectionBean> {

	@Override
	public void init(EndpointConfig config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String encode(InjectionBean object) throws EncodeException {
		ObjectMapper om = new ObjectMapper();
		try {
			return om.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "JSON ERROR";
	}


}
