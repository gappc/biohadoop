package at.ac.uibk.dps.biohadoop.profile.net;

public class NetData {
	private final double rx;
	private final double tx;

	public NetData(double rx, double tx) {
		this.rx = rx;
		this.tx = tx;
	}

	public double getRx() {
		return rx;
	}

	public double getTx() {
		return tx;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("rx=").append(rx).append("|tx=").append(tx);
		return sb.toString();
	}
}
