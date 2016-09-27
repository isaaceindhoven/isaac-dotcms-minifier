package nl.isaac.dotcms.minify;
/**
* dotCMS minifier by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
*/

import java.util.Date;

import com.dotmarketing.beans.Host;

/**
 * Immutable class that represents a minified file. The files are uniquely identified by a MinifyCacheFileKey.
 *  
 * @author Xander
 *
 */
public class MinifyCacheFile {
	private final String uri;
	private final boolean live;
	private final String fileData;
	private final Date modDate;
	private final MinifyCacheFileKey key;
	
	public MinifyCacheFile(String uri, boolean live, String fileData, Date modDate, Host host) {
		this.uri = uri;
		this.live = live;
		this.fileData = fileData;
		this.modDate = modDate;
		this.key = new MinifyCacheFileKey(uri, live, host);
	}
	
	public String getUri() {
		return uri;
	}
	public boolean isLive() {
		return live;
	}
	public String getFileData() {
		return fileData;
	}
	public Date getModDate() {
		return modDate;
	}
	public String getHostName() {
		return this.key.getHostName();
	}
	public Long getHostId() {
		return this.key.getHostId();
	}
	public MinifyCacheFileKey getKey() {
		return key;
	}

	public boolean equals(Object obj) {
		if(obj instanceof MinifyCacheFile) {
			return getKey().equals(((MinifyCacheFile)obj).getKey()); 
		}
		return false;
	}
	
	public int hashCode() {
		return getKey().hashCode();
	}
	
}
