package nl.isaac.dotcms.minify.conf;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

	public static String getBrowserCacheMaxAge() {
		return get("browserCacheMaxAge");
	}

	public static String getProxyServletUrl(String host, String filePath) {

		String encodedFilePath;
		try {
			encodedFilePath = URLEncoder.encode(filePath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 not supported :/");
		}

		return get("proxyServletUrl").replace("{host}", host).replace("{filePath}", encodedFilePath);
	}

}