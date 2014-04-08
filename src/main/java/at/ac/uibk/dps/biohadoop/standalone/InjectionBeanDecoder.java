package at.ac.uibk.dps.biohadoop.standalone;

import java.io.IOException;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InjectionBeanDecoder implements Decoder.Text<InjectionBean> {

	@Override
	public void init(EndpointConfig config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InjectionBean decode(String s) throws DecodeException {
		ObjectMapper om = new ObjectMapper();
		try {
			return om.readValue(s, InjectionBean.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean willDecode(String s) {
		// TODO Auto-generated method stub
		return true;
	}


}
