package at.ac.uibk.dps.biohadoop.webserver.deployment;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.CDI;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.jboss.weld.context.bound.BoundRequestContext;

public class CDIListener implements ServletRequestListener {

	private static final String CDI_REQUEST_CONTEXT = "cdiRequestContext";
	private static final String CDI_REQUEST_MAP = "cdiRequestMap";

	@Override
	public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
		BoundRequestContext requestContext = (BoundRequestContext) servletRequestEvent
				.getServletRequest().getAttribute(CDI_REQUEST_CONTEXT);
		@SuppressWarnings("unchecked")
		Map<String, Object> requestMap = (Map<String, Object>) servletRequestEvent
				.getServletRequest().getAttribute(CDI_REQUEST_MAP);
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
				CDI_REQUEST_CONTEXT, requestContext);
		servletRequestEvent.getServletRequest().setAttribute(CDI_REQUEST_MAP,
				requestMap);
	}
}