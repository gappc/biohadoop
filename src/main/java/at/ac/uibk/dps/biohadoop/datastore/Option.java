package at.ac.uibk.dps.biohadoop.datastore;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Option<T> implements Serializable {

	private static final long serialVersionUID = -1564427329140182760L;

	private static final Logger LOG = LoggerFactory.getLogger(Option.class);

	private final String name;
	private final Class<T> clazz;
	
	public Option(String name, Class<T> clazz) {
		this.name = name;
		this.clazz = clazz;
	}
}
