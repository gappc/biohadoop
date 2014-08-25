package at.ac.uibk.dps.biohadoop.metrics;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class MetricsResource {

	@GET
	@Path("overview")
	public MetricsData getOverview() {
		return Metrics.getMetricsData();
	}

	@GET
	@Path("counters")
	public Map<String, Counter> getCounters() {
		return Metrics.getInstance().getCounters();
	}

	@GET
	@Path("gauges")
	public Map<String, Gauge> getGauges() {
		return Metrics.getInstance().getGauges();
	}

}
