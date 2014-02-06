package io.firstwave.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A Configuration is an immutable wrapper over a Map<String, String>
 * Very similar to an Android Bundle, but does not explicitly support as many data types
 * This will eventually support serialization.
 * Objects retrieved by a given key are then cast using valueOf at the time of retrieval, so it is not implicitly type safe
 */
public class ImmutableConfiguration {
	protected final Map<String, Object> values = new HashMap<String, Object>();

	public ImmutableConfiguration() {
		// default constructor
	}

	/**
	 * Copy constructor
	 * @param toCopy
	 */
	public ImmutableConfiguration(ImmutableConfiguration toCopy) {
		this.values.putAll(toCopy.values);
	}

	public String getString(String key) {
		return getString(key, null);
	}

	public int size() {
		return values.size();
	}

	public boolean containsKey(String key) {
		return values.containsKey(key);
	}

	public Set<String> keySet() {
		return values.keySet();
	}

	public Object get(String key) {
		return values.get(key);
	}

	public String getString(String key, String defaultValue) {
		if (!values.containsKey(key)) return defaultValue;
		if (values.get(key) == null) return null;
		return String.valueOf(values.get(key));
	}

	public int getInteger(String key) {
		return getInteger(key, 0);
	}

	public int getInteger(String key, int defaultValue) {
		if (!values.containsKey(key)) return defaultValue;
		try {
			return Integer.parseInt(String.valueOf(values.get(key)));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return defaultValue;
		}
	}

	public float getFloat(String key) {
		return getFloat(key, 0.0f);
	}

	public float getFloat(String key, float defaultValue) {
		if (!values.containsKey(key)) return defaultValue;
		try {
			return Float.parseFloat(String.valueOf(values.get(key)));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return defaultValue;
		}
	}

	public double getDouble(String key) {
		return getDouble(key, 0.0d);
	}

	public double getDouble(String key, double defaultValue) {
		if (!values.containsKey(key)) return defaultValue;
		try {
			return Double.parseDouble(String.valueOf(values.get(key)));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return defaultValue;
		}
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		if (!values.containsKey(key)) return defaultValue;
		try {
			return Boolean.parseBoolean(String.valueOf(values.get(key)));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return defaultValue;
		}
	}
}
