package io.firstwave.generator.viewer;

import io.firstwave.generator.Configuration;

import java.util.Properties;

/**
 * Created by waxwing on 2/4/14.
 */
public class ConfigurationUtil {
	public static Configuration fromProperties(Properties p) {
		Configuration rv = new Configuration();
		for (String key : p.stringPropertyNames()) {
			String value = p.getProperty(key);
			rv.put(key, value);
		}
		return rv;
	}

}
