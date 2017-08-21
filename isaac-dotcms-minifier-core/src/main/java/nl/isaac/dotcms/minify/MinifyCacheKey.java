package nl.isaac.dotcms.minify;

/*
 * Dotcms minifier by ISAAC is licensed under a
 * Creative Commons Attribution 3.0 Unported License
 *
 * - http://creativecommons.org/licenses/by/3.0/
 *
 * ISAAC Software Solutions B.V. (http://www.isaac.nl)
 */

import nl.isaac.dotcms.minify.util.ParamValidationUtil;

/**
 * Immutable class to be used as the cache key by MinifyCacheHandler. Uniquely identify a minified file.
 *
 * @author Koen Peters, ISAAC
 * @author Xander Steinmann, ISAAC
 */
public class MinifyCacheKey {
	private final String hostName;
	private final String uriPath;
	private final Boolean isLive;

	public MinifyCacheKey(String uriPath, String hostName, boolean isLive) {
		ParamValidationUtil.validateParamNotNull(uriPath, "uriPath");
		ParamValidationUtil.validateParamNotNull(hostName, "hostName");

		this.hostName = hostName;
		this.uriPath = uriPath;
		this.isLive = isLive;
	}

	public String getHostName() {
		return hostName;
	}

	public String getUriPath() {
		return uriPath;
	}

	public Boolean getLive() {
		return isLive;
	}

	@Override
	public String toString() {
		return hostName + " - " + isLive + " - " + uriPath;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof MinifyCacheKey))
			return false;
		MinifyCacheKey that = (MinifyCacheKey) obj;
		return toString().equals(that.toString());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
