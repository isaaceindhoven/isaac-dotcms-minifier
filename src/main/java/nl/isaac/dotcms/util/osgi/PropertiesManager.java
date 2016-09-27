package nl.isaac.dotcms.util.osgi;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import com.dotcms.repackage.org.osgi.framework.Bundle;
import com.dotcms.repackage.org.osgi.framework.FrameworkUtil;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.util.Logger;

public class PropertiesManager {
	private Boolean configurationPluginAvailable;
	private boolean retrievedLocalProperties;
	private final String pluginName;
	private final Bundle bundle;
	private Map<String, String> properties;
	
	public PropertiesManager() {
		this.bundle = FrameworkUtil.getBundle(this.getClass());
		this.pluginName = "osgi/" + bundle.getHeaders().get("Bundle-Name") + "/" + bundle.getBundleId(); 
		this.retrievedLocalProperties = false;
		properties = new HashMap<String, String>();
		Logger.info(this, "Using plugin name '" + pluginName + "'");
	}
	
	public String get(String key) {
		if(isConfigurationPluginAvailable()) {
			try {
				return APILocator.getPluginAPI().loadProperty(pluginName, key);
			} catch (Exception e) {
				throw new RuntimeException( "Can't retrieve property " + key + " in " + pluginName, e);
			}
		}
		
		if(!retrievedLocalProperties) {
			Logger.info(this, "Reading properties from own properties file for plugin " + pluginName);
			properties = getLocalProperties();
			retrievedLocalProperties = true;
		}
		
		return properties.get(key);
	}
	
	/**
	 * Check for the "environment" key in the pluginAPI
	 */
	private boolean isConfigurationPluginAvailable() {
		if(configurationPluginAvailable == null) {
			try {
				String value = APILocator.getPluginAPI().loadProperty(pluginName, "environment");
				configurationPluginAvailable = (value != null);
			} catch (Exception e) {
				Logger.warn(this, "Exception while trying to get property 'environment' ", e);
				configurationPluginAvailable = false;
			}
			Logger.info(this, "Configuration plugin available: " + configurationPluginAvailable);
		}
		
		return configurationPluginAvailable;
	}

	private Map<String, String> getLocalProperties() {
		Map<String, String> localProperties = new ConcurrentHashMap<String, String>();
		try {
			
			// Read all the properties from the properties file
			URL resourceURL = bundle.getResource("conf/plugin.properties");
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
