package io.firstwave.generator;

import java.util.Properties;

/**
 * Created by waxwing on 2/1/14.
 */
public class LevelConfiguration {
	public static final String SEED = "seed";

	private final Properties data;

	public LevelConfiguration(Properties data) {
		this.data = data;
	}

	public void set(String key, Object value) {
		data.setProperty(key, String.valueOf(value));
	}

	public String getString(String key) {
		return getString(key, null);
	}

	public String getString(String key, String defaultValue) {
		return data.getProperty(key, defaultValue);
	}

	public Integer getInteger(String key) {
		return getInteger(key, null);
	}

	public Integer getInteger(String key, Integer defaultValue) {
		return (data.getProperty(key) != null) ? Integer.valueOf(data.getProperty(key)) : defaultValue;
	}

	public Float getFloat(String key) {
		return getFloat(key, null);
	}

	public Float getFloat(String key, Float defaultValue) {
		return (data.getProperty(key) != null) ? Float.valueOf(data.getProperty(key)) : defaultValue;
	}

	public Double getDouble(String key) {
		return getDouble(key, null);
	}

	public Double getDouble(String key, Double defaultValue) {
		return (data.getProperty(key) != null) ? Double.valueOf(data.getProperty(key)) : defaultValue;
	}

	public Boolean getBoolean(String key) {
		return getBoolean(key, null);
	}

	public Boolean getBoolean(String key, Boolean defaultValue) {
		return (data.getProperty(key) != null) ? Boolean.valueOf(data.getProperty(key)) : defaultValue;
	}
}
