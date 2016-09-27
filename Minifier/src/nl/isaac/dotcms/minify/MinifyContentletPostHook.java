package nl.isaac.dotcms.minify;
/**
* dotCMS minifier by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
* 
* @copyright Copyright (c) 2011 ISAAC Software Solutions B.V. (http://www.isaac.nl)
*/

import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHookAbstractImp;
import com.dotmarketing.portlets.contentlet.business.DotReindexStateException;
import com.dotmarketing.portlets.files.model.File;
import com.dotmarketing.portlets.folders.model.Folder;
import com.dotmarketing.util.Logger;

/**
 * Posthook that updates the cache when a js or css file is updated.
 * The only exception is when a file is uploaded by WEBDAV. The MinifyCacheWebdavFilter is 
 * triggered to handle that case.
 * 
 * @author Xander
 *
 */
public class MinifyContentletPostHook extends ContentletAPIPostHookAbstractImp {

	/**
	 * When an asset is saved and/or published, this method is called to update the minified js/css cache
	 * (when a js/css file is saved)
	 * Note: It's a bit of a hack, but this method is called after a change and there are no 
	 * posthook methods specifically for file assets.  
	 */
	public void refreshReferencingContentlets(
			com.dotmarketing.portlets.files.model.File file, boolean live) {
		if(file.getExtension().equalsIgnoreCase("css") || file.getExtension().equalsIgnoreCase("js")) {
			Host host = getHostOfFile(file);
			String key = new MinifyCacheFileKey(file.getURI(), live, host).getKey(); 
			MinifyCacheHandler.getInstance().put(key, MinifyCacheItemHandler.get(file, host));
		}
	}
	
	/**
	 * Utility method that retrieves the host of a file. See also jira item EE-1572
	 */
	private Host getHostOfFile(File file) {
		try {
			Identifier identifier = APILocator.getIdentifierAPI().findFromInode(file.getIdentifier());
			return APILocator.getHostAPI().find(identifier.getHostId(), APILocator.getUserAPI().getSystemUser(), false);
		} catch (DotSecurityException e) {
			Logger.info(this.getClass(), "DotSecurityException while getting Host");
			throw new RuntimeException(e);
		} catch (DotDataException e) {
			Logger.info(this.getClass(), "DotSecurityException while getting Host");
			throw new RuntimeException(e);
		}
	}

	/**
	 *******************************************************************************
	 *                       Dummy implemented methods                             *
	 *******************************************************************************
	 */
	
	@Override
	public void refreshContentUnderFolder(Folder arg0)
			throws DotReindexStateException {
	}

	@Override
	public void refreshContentUnderHost(Host arg0)
			throws DotReindexStateException {
	}

}