package at.ac.uibk.dps.biohadoop.server.deployment;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.CDI;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.jboss.weld.context.bound.BoundRequestContext;

public class CDIListener implements ServletRequestListener {
	@Override
	public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
		BoundRequestContext requestContext = (BoundRequestContext) servletRequestEvent
				.getServletRequest().getAttribute("cdiRequestContext");
		Map<String, Object> requestMap = (Map<String, Object>) servletRequestEvent
				.getServletRequest().getAttribute("cdiRequestMap");
		requestContext.invalidate();
		requestContext.deactivate();
		requestContext.dissociate(requestMap);
	}

	@Override
	public void requestInitialized(ServletRequestEvent servletRequestEvent) {
		BoundRequestContext requestContext = CDI.current()
				.select(BoundRequestContext.class).get();
		Map<String, Object> requestMap = new HashMap<>();
		requestContext.associate(requestMap);
		requestContext.activate();
		servletRequestEvent.getServletRequest().setAttribute(
				"cdiRequestContext", requestContext);
		servletRequestEvent.getServletRequest().setAttribute("cdiRequestMap",
				requestMap);
	}
}