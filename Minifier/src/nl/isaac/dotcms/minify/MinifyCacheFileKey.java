package nl.isaac.dotcms.minify;
/**
* dotCMS minifier by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
*/

import com.dotmarketing.beans.Host;

/**
 * Immutable class to uniquely identify a minified file.
 * 
 * @author Xander
 *
 */
public class MinifyCacheFileKey {
	private static final String SEPARATOR = "-xXx-";
	private final String hostName;
	private final String hostId;
	private final String uri;
	private final Boolean live;
	
	public MinifyCacheFileKey(String uri, boolean live, Host host) {
		this(uri, live, host.getIdentifier(), host.getHostname()); 
	}
	
	private MinifyCacheFileKey(String uri, boolean live, String hostId, String hostName) {
		this.hostId = hostId;
		this.hostName = hostName;
		this.uri = uri;
		this.live = live;
	}

	public static MinifyCacheFileKey createInstanceWithKey(String key) {
		String[] parts = key.split(SEPARATOR);  
		if(parts.length != 4) {
			throw new RuntimeException("Illegal key: " + key);
		}
		return new MinifyCacheFileKey(parts[2], Boolean.valueOf(parts[1]), parts[0], parts[3]);
	}

	public String getHostName() {
		return hostName;
	}

	public String getHostId() {
		return hostId;
	}

	public String getUri() {
		return uri;
	}

	public Boolean getLive() {
		return live;
	}

	public String getKey() {
		return hostId + SEPARATOR + live + SEPARATOR + uri + SEPARATOR + hostName;		
	}
	
	public String getReadableString() {
		return hostName + " - " + live + " - " + uri;		
	}

	public boolean equals(Object obj) {
		if(obj instanceof MinifyCacheFileKey) {
			return ((MinifyCacheFileKey)obj).toString().equals(toString());
		}
		return false;
	}
	
	public int hashCode() {
		return getKey().hashCode();
	}
}
