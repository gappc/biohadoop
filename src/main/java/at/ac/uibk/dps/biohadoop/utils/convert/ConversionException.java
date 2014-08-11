package at.ac.uibk.dps.biohadoop.utils.convert;

//TODO should be made checked
public class ConversionException extends Exception {

	private static final long serialVersionUID = 2308781144934182306L;

	public ConversionException() {
	}

	public ConversionException(String message) {
		super(message);
	}

	public ConversionException(Throwable cause) {
		super(cause);
	}

	public ConversionException(String message, Throwable cause) {
		super(message, cause);
	}
}
