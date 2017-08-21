package nl.isaac.dotcms.minify;

/*
 * Dotcms minifier by ISAAC is licensed under a
 * Creative Commons Attribution 3.0 Unported License
 *
 * - http://creativecommons.org/licenses/by/3.0/
 *
 * ISAAC Software Solutions B.V. (http://www.isaac.nl)
 */

import java.util.List;
import java.util.Map;

import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.beans.Permission;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHookAbstractImp;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.fileassets.business.FileAsset;
import com.dotmarketing.portlets.folders.model.Folder;
import com.dotmarketing.portlets.structure.model.ContentletRelationships;
import com.dotmarketing.portlets.structure.model.Field;
import com.dotmarketing.portlets.structure.model.Relationship;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;

public class FileChangedPostHook extends ContentletAPIPostHookAbstractImp {

	@Override
	public void checkin(Contentlet contentlet, List<Category> cats,
			List<Permission> permissions, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		handleContentlet(contentlet);
	}

	@Override
	public void checkin(Contentlet contentlet, List<Permission> permissions,
			User user, boolean respectFrontendRoles, Contentlet returnValue) {
		handleContentlet(contentlet);
	}

	@Override
	public void checkin(Contentlet contentlet,
			Map<Relationship, List<Contentlet>> contentRelationships,
			List<Category> cats, List<Permission> permissions, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		handleContentlet(contentlet);
	}

	@Override
	public void checkin(Contentlet contentlet,
			Map<Relationship, List<Contentlet>> contentRelationships,
			List<Category> cats, User user, boolean respectFrontendRoles,
			Contentlet returnValue) {
		handleContentlet(contentlet);
	}

	@Override
	public void checkin(Contentlet contentlet,
			Map<Relationship, List<Contentlet>> contentRelationships,
			User user, boolean respectFrontendRoles, Contentlet returnValue) {
		handleContentlet(contentlet);
	}

	@Override
	public void checkin(Contentlet contentlet, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		handleContentlet(contentlet);
	}

	@Override
	public void checkin(Contentlet contentlet, User user,
			boolean respectFrontendRoles, List<Category> cats,
			Contentlet returnValue) {
		handleContentlet(contentlet);
	}

	@Override
	public void checkin(Contentlet currentContentlet,
			ContentletRelationships relationshipsData, List<Category> cats,
			List<Permission> selectedPermissions, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		handleContentlet(currentContentlet);
	}

	private void handleContentlet(Contentlet contentlet) {
		Boolean isFileAsset = APILocator.getFileAssetAPI().isFileAsset(contentlet);

		Logger.debug(this, "handleContentlet = " + contentlet.getTitle());
		Logger.debug(this, "File isFileAsset = " + isFileAsset);

		if (isFileAsset) {
			FileAsset file = APILocator.getFileAssetAPI().fromContentlet(contentlet);

			try {

				if (contentlet.getTitle().endsWith("css") || contentlet.getTitle().endsWith("js")) {
					Host host = getHostOfFile(file);
					MinifyCacheKey keyEditPreview = new MinifyCacheKey(file.getURI(), host.getHostname(), false);
					MinifyCacheKey keyLive = new MinifyCacheKey(file.getURI(), host.getHostname(), true);

					MinifyCacheHandler.INSTANCE.remove(keyEditPreview);
					MinifyCacheHandler.INSTANCE.remove(keyLive);

					Logger.debug(this, "File '" + file.getFileName() + "' (uri: " + file.getURI() + ") has changed, cache has been flushed.");
				}
			} catch (DotDataException ex) {
				throw new RuntimeException(ex);
			} catch (DotStateException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * Utility method that retrieves the host of a file.
	 */
	private Host getHostOfFile(FileAsset file) {
		try {
			Identifier identifier = APILocator.getIdentifierAPI().findFromInode(file.getIdentifier());
			return APILocator.getHostAPI().find(identifier.getHostId(), APILocator.getUserAPI().getSystemUser(), false);
		} catch (DotSecurityException e) {
			Logger.error(this.getClass(), "DotSecurityException while getting Host", e);
			throw new RuntimeException(e);
		} catch (DotDataException e) {
			Logger.error(this.getClass(), "DotSecurityException while getting Host", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 *******************************************************************************
	 * Dummy implemented methods *
	 *******************************************************************************
	 */
	@Override
	public void copyContentlet(Contentlet arg0, Folder arg1, User arg2,
			boolean arg3, boolean arg4, Contentlet arg5) {
		// TODO Auto-generated method stub

	}

	@Override
	public void indexCount(String arg0, User arg1, boolean arg2, long arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadField(String arg0, Field arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void searchIndexCount(String arg0, User arg1, boolean arg2, long arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void isInodeIndexed(String arg0, boolean arg1, boolean arg2) {
		// TODO Auto-generated method stub

	}

}
