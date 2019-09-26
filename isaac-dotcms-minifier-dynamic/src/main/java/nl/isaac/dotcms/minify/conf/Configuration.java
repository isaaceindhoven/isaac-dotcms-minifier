package nl.isaac.dotcms.minify.conf;

import nl.isaac.dotcms.util.osgi.PropertiesManager;

public class Configuration {

	private static PropertiesManager propertiesManager;

	private static String get(String key) {
		if(propertiesManager == null) {
			propertiesManager = new PropertiesManager();
		}

		return propertiesManager.get(key);
	}

	public static String getBrowserCacheMaxAge() {
		return get("browserCacheMaxAge");
	}

	public static String getProxyServletUrl(String host, String filePath) {
		return get("proxyServletUrl").replace("{host}", host).replace("{filePath}", filePath);
	}

}
