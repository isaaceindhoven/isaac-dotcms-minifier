package nl.isaac.dotcms.minify.viewtool;
/**
* dotCMS minifier by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
* 
* @copyright Copyright (c) 2011 ISAAC Software Solutions B.V. (http://www.isaac.nl)
*/

import nl.isaac.dotcms.minify.MinifyCacheHandler;

import org.apache.velocity.tools.view.tools.ViewTool;

/**
 * DotCMS viewtool that initializes the cache on startup. The other methods
 * are convenience methods.
 * 
 * @author Xander
 *
 */
public class MinifyCacheTool implements ViewTool {
	
	@Override
	public void init(Object arg0) {
		MinifyCacheHandler.getInstance().fillInitialCache();
	}
	
	public void updateCache() {
		MinifyCacheHandler.getInstance().fillInitialCache();
	}

}
