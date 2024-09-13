package nl.isaac.dotcms.minify.conf;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;


public class Configuration {

	// Plugin name
	public static final String PLUGIN_NAME = "isaac-dotcms-minifier-static";

	//Configuration from plugin.properties
	private static String get(String key) {
		try {
			return APILocator.getPluginAPI().loadProperty(PLUGIN_NAME, key);
		} catch (DotDataException e) {
			throw new RuntimeException("Unable to load property with key " + key, e);
		}
	}

	public static Boolean doRemoveImportantComments() {
      return Boolean.parseBoolean(get("removeImportantComments"));
  }

}