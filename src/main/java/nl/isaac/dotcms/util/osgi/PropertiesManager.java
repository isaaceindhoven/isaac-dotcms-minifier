package nl.isaac.dotcms.util.osgi;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import com.dotcms.repackage.org.apache.commons.io.FilenameUtils;
import com.dotcms.repackage.org.osgi.framework.Bundle;
import com.dotcms.repackage.org.osgi.framework.FrameworkUtil;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.util.Logger;

public class PropertiesManager {
	private boolean getPropertiesFromPluginAPI;
	private String pluginName;
	private final Bundle bundle;
	private Map<String, String> properties;
	
	public PropertiesManager() {
		this.bundle = FrameworkUtil.getBundle(this.getClass());
		this.pluginName = FilenameUtils.getBaseName(bundle.getLocation());
		this.getPropertiesFromPluginAPI = true;
		Logger.info(this, "Using plugin name '" + pluginName + "', use-plugin-api=" + getPropertiesFromPluginAPI);
	}
	
	public String get(String key) {
		if(getPropertiesFromPluginAPI) {
			try {
				String value = APILocator.getPluginAPI().loadProperty(pluginName, key);
				if (value != null) {
					return value;
				}
				Logger.info(this, "Value for " + key + "=null in " + pluginName + ", so assuming the Configuration plugin is not available");
			} catch (Exception e) {
				Logger.info(this, "Can't retrieve property " + key + " in " + pluginName + ", so assuming the Configuration plugin is not available");
			}
			
			getPropertiesFromPluginAPI = false;
			Logger.info(this, "Reading properties from own properties file for plugin " + pluginName);
			properties = getLocalProperties();
		}
		
		return properties.get(key);
	}

	private Map<String, String> getLocalProperties() {
		Map<String, String> localProperties = new ConcurrentHashMap<String, String>();
		try {
			
			// Read all the properties from the properties file
			URL resourceURL = bundle.getResource("ext/plugin.properties");
			PropertyResourceBundle resourceBundle = new PropertyResourceBundle(resourceURL.openStream());
			
			// Put the properties in the map
			for(String key: resourceBundle.keySet()) {
				localProperties.put(key, resourceBundle.getString(key));
			}
			
		} catch (IOException e) {
			Logger.warn(this, "Exception while retrieving properties from plugin.properties", e);
		}
		
		return localProperties;
	}
}
