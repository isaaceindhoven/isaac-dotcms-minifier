package nl.isaac.dotcms.minify;
/**
* dotCMS minifier by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
* 
* @copyright Copyright (c) 2011 ISAAC Software Solutions B.V. (http://www.isaac.nl)
*/

import nl.isaac.dotcms.minify.shared.CacheGroupHandler;

/**
 * Implementation of the CacheGroupHandler&lt;MinifyCacheFile&gt;. This Singleton class 
 * manages the cache for the minifier.
 * 
 * @author Xander
 */
public class MinifyCacheHandler extends CacheGroupHandler<MinifyCacheFile>{
	public static final String CACHE_GROUP_NAME = "ISAAC_MinifyCache";
	private static MinifyCacheHandler minifyCacheHandler;
	
	private MinifyCacheHandler() {
		super(CACHE_GROUP_NAME, new MinifyCacheItemHandler());
	}
	
	public static MinifyCacheHandler getInstance() {
		if(minifyCacheHandler == null) {
			minifyCacheHandler = new MinifyCacheHandler();
		}
		return minifyCacheHandler;
	}
	
}
