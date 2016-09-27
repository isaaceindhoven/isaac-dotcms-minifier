package nl.isaac.dotcms.minify.shared;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;

public class Configuration {

	public static final String PLUGIN_NAME = "Minifier";
	
	private static String getProperty(String propertyName){
		try {
			return APILocator.getPluginAPI().loadProperty(PLUGIN_NAME, propertyName);
		} catch (DotDataException e) {
			throw new RuntimeException("Failed to resolve property " + propertyName);
		}
	}

	public static String getBrowserCacheMaxAge() {
		return getProperty("browserCacheMaxAge");
	}
}
