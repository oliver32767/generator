package io.firstwave.generator;

/**
 * Created by waxwing on 2/4/14.
 */
public class Configuration extends ImmutableConfiguration {

	public void clear() {
		this.values.clear();
	}

	public void putAll(Configuration value) {
		this.values.putAll(value.values);
	}

	public void put(String key, String value) {
		this.values.put(key, value);
	}

	public void put(String key, int value) {
		this.values.put(key, value);
	}

	public void put(String key, float value) {
		this.values.put(key, value);
	}

	public void put(String key, double value) {
		this.values.put(key, value);
	}

	public void put(String key, boolean value) {
		this.values.put(key, value);
	}
}
