package nl.isaac.dotcms.minify.shared;
/**
* dotCMS minifier by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
*/

import java.util.Map;

/**
 * An interface that is used by the CacheGroupHandler. It has 2 methods, one
 * that return an item by key, and a method that returns an initial load.
 * 
 * @author Xander
 *
 * @param <T> the type to return
 */
public interface ItemHandler<T> {
	public T get(String key);
	public Map<String,T> getInitialCache();
}