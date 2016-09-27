package nl.isaac.dotcms.minify;
/**
* dotCMS minifier by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
*/

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.beans.Permission;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.query.ValidationException;
import com.dotmarketing.business.query.GenericQueryFactory.Query;
import com.dotmarketing.common.model.ContentletSearch;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHook;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.files.model.File;
import com.dotmarketing.portlets.folders.model.Folder;
import com.dotmarketing.portlets.links.model.Link;
import com.dotmarketing.portlets.structure.model.ContentletRelationships;
import com.dotmarketing.portlets.structure.model.Field;
import com.dotmarketing.portlets.structure.model.Relationship;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.portlets.structure.model.ContentletRelationships.ContentletRelationshipRecords;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;

/**
 * Posthook that updates the cache when a js or css file is updated.
 * The only exception is when a file is uploaded by WEBDAV. The MinifyCacheWebdavFilter is 
 * triggered to handle that case.
 * 
 * @author Xander
 *
 */
public class MinifyContentletPostHook implements ContentletAPIPostHook {

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
	public void UpdateContentWithSystemHost(String arg0)
			throws DotDataException {
	}

	@Override
	public void addFileToContentlet(Contentlet arg0, String arg1, String arg2,
			User arg3, boolean arg4) {
	}

	@Override
	public void addImageToContentlet(Contentlet arg0, String arg1, String arg2,
			User arg3, boolean arg4) {
	}

	@Override
	public void addLinkToContentlet(Contentlet arg0, String arg1, String arg2,
			User arg3, boolean arg4) {
	}

	@Override
	public void applyStructurePermissionsToChildren(Structure arg0, User arg1,
			List<Permission> arg2, boolean arg3) {
	}

	@Override
	public void archive(Contentlet arg0, User arg1, boolean arg2) {
	}

	@Override
	public void archive(List<Contentlet> arg0, User arg1, boolean arg2) {
	}

	@Override
	public void checkin(Contentlet arg0, User arg1, boolean arg2,
			Contentlet arg3) {
	}

	@Override
	public void checkin(Contentlet arg0, List<Permission> arg1, User arg2,
			boolean arg3, Contentlet arg4) {
	}

	@Override
	public void checkin(Contentlet arg0, User arg1, boolean arg2,
			List<Category> arg3, Contentlet arg4) {
	}

	@Override
	public void checkin(Contentlet arg0,
			Map<Relationship, List<Contentlet>> arg1, User arg2, boolean arg3,
			Contentlet arg4) {
	}

	@Override
	public void checkin(Contentlet arg0, List<Category> arg1,
			List<Permission> arg2, User arg3, boolean arg4, Contentlet arg5) {
	}

	@Override
	public void checkin(Contentlet arg0,
			Map<Relationship, List<Contentlet>> arg1, List<Category> arg2,
			User arg3, boolean arg4, Contentlet arg5) {
	}

	@Override
	public void checkin(Contentlet arg0,
			Map<Relationship, List<Contentlet>> arg1, List<Category> arg2,
			List<Permission> arg3, User arg4, boolean arg5, Contentlet arg6) {
	}

	@Override
	public void checkin(Contentlet arg0, ContentletRelationships arg1,
			List<Category> arg2, List<Permission> arg3, User arg4,
			boolean arg5, Contentlet arg6) {
	}

	@Override
	public void checkinWithNoIndex(Contentlet arg0, User arg1, boolean arg2,
			Contentlet arg3) {
	}

	@Override
	public void checkinWithNoIndex(Contentlet arg0, List<Permission> arg1,
			User arg2, boolean arg3, Contentlet arg4) {
	}

	@Override
	public void checkinWithNoIndex(Contentlet arg0, User arg1, boolean arg2,
			List<Category> arg3, Contentlet arg4) {
	}

	@Override
	public void checkinWithNoIndex(Contentlet arg0,
			Map<Relationship, List<Contentlet>> arg1, User arg2, boolean arg3,
			Contentlet arg4) {
	}

	@Override
	public void checkinWithNoIndex(Contentlet arg0, List<Category> arg1,
			List<Permission> arg2, User arg3, boolean arg4, Contentlet arg5) {
	}

	@Override
	public void checkinWithNoIndex(Contentlet arg0,
			Map<Relationship, List<Contentlet>> arg1, List<Category> arg2,
			User arg3, boolean arg4, Contentlet arg5) {
	}

	@Override
	public void checkinWithNoIndex(Contentlet arg0,
			Map<Relationship, List<Contentlet>> arg1, List<Category> arg2,
			List<Permission> arg3, User arg4, boolean arg5, Contentlet arg6) {
	}

	@Override
	public void checkinWithNoIndex(Contentlet arg0,
			ContentletRelationships arg1, List<Category> arg2,
			List<Permission> arg3, User arg4, boolean arg5, Contentlet arg6) {
	}

	@Override
	public void checkinWithoutVersioning(Contentlet arg0,
			Map<Relationship, List<Contentlet>> arg1, List<Category> arg2,
			List<Permission> arg3, User arg4, boolean arg5, Contentlet arg6) {
	}

	@Override
	public void checkout(String arg0, User arg1, boolean arg2, Contentlet arg3) {
	}

	@Override
	public void checkout(List<Contentlet> arg0, User arg1, boolean arg2,
			List<Contentlet> arg3) {
	}

	@Override
	public void checkout(String arg0, User arg1, boolean arg2,
			List<Contentlet> arg3) {
	}

	@Override
	public void checkout(String arg0, User arg1, boolean arg2, int arg3,
			int arg4, List<Contentlet> arg5) {
	}

	@Override
	public void cleanField(Structure arg0, Field arg1, User arg2, boolean arg3) {
	}

	@Override
	public void cleanHostField(Structure arg0, User arg1, boolean arg2) {
	}

	@Override
	public long contentletCount(long arg0) throws DotDataException {
		return 0;
	}

	@Override
	public long contentletIdentifierCount(long arg0) throws DotDataException {
		return 0;
	}

	@Override
	public void convertContentletToFatContentlet(Contentlet arg0,
			com.dotmarketing.portlets.contentlet.business.Contentlet arg1,
			com.dotmarketing.portlets.contentlet.business.Contentlet arg2) {
	}

	@Override
	public void convertFatContentletToContentlet(
			com.dotmarketing.portlets.contentlet.business.Contentlet arg0,
			Contentlet arg1) {
	}

	@Override
	public void copyContentlet(Contentlet arg0, User arg1, boolean arg2,
			Contentlet arg3) {
	}

	@Override
	public void copyContentlet(Contentlet arg0, Host arg1, User arg2,
			boolean arg3, Contentlet arg4) {
	}

	@Override
	public void copyContentlet(Contentlet arg0, Folder arg1, User arg2,
			boolean arg3, Contentlet arg4) {
	}

	@Override
	public void copyProperties(Contentlet arg0, Map<String, Object> arg1) {
	}

	@Override
	public void delete(Contentlet arg0, User arg1, boolean arg2) {
	}

	@Override
	public void delete(List<Contentlet> arg0, User arg1, boolean arg2) {
	}

	@Override
	public void delete(Contentlet arg0, User arg1, boolean arg2, boolean arg3) {
	}

	@Override
	public void delete(List<Contentlet> arg0, User arg1, boolean arg2,
			boolean arg3) {
	}

	@Override
	public void deleteOldContent(Date arg0, int arg1, int arg2) {
	}

	@Override
	public void deleteRelatedContent(Contentlet arg0, Relationship arg1,
			User arg2, boolean arg3) {
	}

	@Override
	public void deleteRelatedContent(Contentlet arg0, Relationship arg1,
			boolean arg2, User arg3, boolean arg4) {
	}

	@Override
	public void find(String arg0, User arg1, boolean arg2, Contentlet arg3) {
	}

	@Override
	public void find(Category arg0, long arg1, boolean arg2, String arg3,
			User arg4, boolean arg5, List<Contentlet> arg6) {
	}

	@Override
	public void find(List<Category> arg0, long arg1, boolean arg2, String arg3,
			User arg4, boolean arg5, List<Contentlet> arg6) {
	}

	@Override
	public void findAllContent(int arg0, int arg1, List<Contentlet> arg2) {
	}

	@Override
	public void findAllUserVersions(Identifier arg0, User arg1, boolean arg2,
			List<Contentlet> arg3) {
	}

	@Override
	public void findAllVersions(Identifier arg0, User arg1, boolean arg2,
			List<Contentlet> arg3) {
	}

	@Override
	public void findByStructure(Structure arg0, User arg1, boolean arg2,
			int arg3, int arg4, List<Contentlet> arg5) {
	}

	@Override
	public void findByStructure(String arg0, User arg1, boolean arg2, int arg3,
			int arg4, List<Contentlet> arg5) {
	}

	@Override
	public void findContentletByIdentifier(String arg0, boolean arg1,
			long arg2, User arg3, boolean arg4, Contentlet arg5) {
	}

	@Override
	public void findContentletForLanguage(long arg0, Identifier arg1,
			Contentlet arg2) {
	}

	@Override
	public void findContentlets(List<String> arg0, List<Contentlet> arg1) {
		
		
	}

	@Override
	public void findContentletsByFolder(Folder arg0, User arg1, boolean arg2)
			throws DotDataException {
		
		
	}

	@Override
	public void findContentletsByHost(Host arg0, User arg1, boolean arg2)
			throws DotDataException {
		
		
	}

	@Override
	public void findContentletsByIdentifiers(String[] arg0, boolean arg1,
			long arg2, User arg3, boolean arg4, List<Contentlet> arg5) {
		
		
	}

	@Override
	public void findFieldValues(String arg0, Field arg1, User arg2,
			boolean arg3, List<String> arg4) {
		
		
	}

	@Override
	public void findPageContentlets(String arg0, String arg1, String arg2,
			boolean arg3, long arg4, User arg5, boolean arg6,
			List<Contentlet> arg7) {
	}

	@Override
	public void getAllLanguages(Contentlet arg0, Boolean arg1, User arg2,
			boolean arg3, List<Contentlet> arg4) {
	}

	@Override
	public void getAllRelationships(Contentlet arg0,
			ContentletRelationships arg1) {
	}

	@Override
	public void getAllRelationships(String arg0, User arg1, boolean arg2,
			ContentletRelationships arg3) {
	}

	@Override
	public void getBinaryFile(String arg0, String arg1, User arg2,
			java.io.File arg3) {
	}

	@Override
	public void getContentletReferences(Contentlet arg0, User arg1,
			boolean arg2, List<Map<String, Object>> arg3) {
	}

	@Override
	public void getFieldValue(Contentlet arg0, Field arg1, Object arg2) {
	}

	@Override
	public void getName(Contentlet arg0, User arg1, boolean arg2, String arg3) {
	}

	@Override
	public void getNextReview(Contentlet arg0, User arg1, boolean arg2,
			Date arg3) {
	}

	@Override
	public void getReferencingContentlet(File arg0, boolean arg1, User arg2,
			boolean arg3, List<Contentlet> arg4) {
	}

	@Override
	public void getRelatedContent(Contentlet arg0, Relationship arg1,
			User arg2, boolean arg3, List<Contentlet> arg4) {
	}

	@Override
	public void getRelatedContent(Contentlet arg0, Relationship arg1,
			boolean arg2, User arg3, boolean arg4, List<Contentlet> arg5) {
	}

	@Override
	public void getRelatedFiles(Contentlet arg0, User arg1, boolean arg2,
			List<File> arg3) {
	}

	@Override
	public void getRelatedIdentifier(Contentlet arg0, String arg1, User arg2,
			boolean arg3, Identifier arg4) {
	}

	@Override
	public void getRelatedLinks(Contentlet arg0, User arg1, boolean arg2,
			List<Link> arg3) {
	}

	@Override
	public List<Contentlet> getSiblings(String arg0) throws DotDataException {
		return null;
	}

	@Override
	public void getUrlMapForContentlet(Contentlet arg0, User arg1, boolean arg2)
			throws DotSecurityException, DotDataException {
	}

	@Override
	public void isContentEqual(Contentlet arg0, Contentlet arg1, User arg2,
			boolean arg3, boolean arg4) {
	}

	@Override
	public void isContentlet(String arg0, boolean arg1) {
	}

	@Override
	public void isFieldTypeBoolean(Field arg0, boolean arg1) {
	}

	@Override
	public void isFieldTypeDate(Field arg0, boolean arg1) {
	}

	@Override
	public void isFieldTypeFloat(Field arg0, boolean arg1) {
	}

	@Override
	public void isFieldTypeLong(Field arg0, boolean arg1) {
	}

	@Override
	public void isFieldTypeString(Field arg0, boolean arg1) {
	}

	@Override
	public void isInodeIndexed(String arg0, boolean arg1) {
	}

	@Override
	public void isInodeIndexed(String arg0, int arg1, boolean arg2) {
	}

	@Override
	public void lock(Contentlet arg0, User arg1, boolean arg2) {
	}

	@Override
	public void publish(Contentlet arg0, User arg1, boolean arg2) {
	}

	@Override
	public void publish(List<Contentlet> arg0, User arg1, boolean arg2) {
	}

	@Override
	public void publishRelatedHtmlPages(Contentlet arg0) {
	}

	@Override
	public void reIndexForServerNode() {
	}

	@Override
	public void refresh(Structure arg0) {
	}

	@Override
	public void refresh(Contentlet arg0) {
	}

	@Override
	public void refreshAllContent() {
	}

	@Override
	public void reindex() {
	}

	@Override
	public void reindex(Structure arg0) {
	}

	@Override
	public void reindex(Contentlet arg0) {
	}

	@Override
	public void relateContent(Contentlet arg0,
			ContentletRelationshipRecords arg1, User arg2, boolean arg3) {
	}

	@Override
	public void relateContent(Contentlet arg0, Relationship arg1,
			List<Contentlet> arg2, User arg3, boolean arg4) {
	}

	@Override
	public boolean removeContentletFromIndex(String arg0)
			throws DotDataException {
		
		return false;
	}

	@Override
	public void removeUserReferences(String arg0) throws DotDataException {}

	@Override
	public void restoreVersion(Contentlet arg0, User arg1, boolean arg2) {}

	@Override
	public void search(String arg0, int arg1, int arg2, String arg3, User arg4,
			boolean arg5, List<Contentlet> arg6) {}

	@Override
	public void search(String arg0, int arg1, int arg2, String arg3, User arg4,
			boolean arg5, int arg6, List<Contentlet> arg7) {}

	@Override
	public void setContentletProperty(Contentlet arg0, Field arg1, Object arg2) {}

	@Override
	public void unarchive(List<Contentlet> arg0, User arg1, boolean arg2) {}

	@Override
	public void unarchive(Contentlet arg0, User arg1, boolean arg2) {}

	@Override
	public void unlock(Contentlet arg0, User arg1, boolean arg2) {}

	@Override
	public void unpublish(Contentlet arg0, User arg1, boolean arg2) {}

	@Override
	public void unpublish(List<Contentlet> arg0, User arg1, boolean arg2) {}

	@Override
	public void validateContentlet(Contentlet arg0, List<Category> arg1) {}

	@Override
	public void validateContentlet(Contentlet arg0,
			Map<Relationship, List<Contentlet>> arg1, List<Category> arg2) {}

	@Override
	public void validateContentlet(Contentlet arg0,
			ContentletRelationships arg1, List<Category> arg2) {}

	@Override
	public void DBSearch(Query arg0, User arg1, boolean arg2,
			List<Map<String, Serializable>> arg3) throws ValidationException,
			DotDataException {}

	@Override
	public void searchIndex(String arg0, int arg1, int arg2, String arg3,
			User arg4, boolean arg5, List<ContentletSearch> arg6) {}

}