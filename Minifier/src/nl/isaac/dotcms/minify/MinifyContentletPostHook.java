package nl.isaac.dotcms.minify;
/**
* dotCMS minifier by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
*/

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.beans.Permission;
import com.dotmarketing.business.IdentifierAPIImpl;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.factories.HostFactory;
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
import com.dotmarketing.util.LuceneHits;
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
		IdentifierAPIImpl api = new IdentifierAPIImpl();
		Identifier identifier;
		try {
			identifier = api.findFromInode(file.getIdentifier());
			return HostFactory.getHost(identifier.getHostInode());
		} catch (DotDataException e) {
			Logger.info(this.getClass(), "DotSecurityException while getting Host");
			throw new RuntimeException(e);
		}
	}
	
	
	public void UpdateContentWithSystemHost(String hostIdentifier)
			throws DotDataException {
		// TODO Auto-generated method stub
		
	}

	
	public void addFileToContentlet(Contentlet contentlet, String fileInode,
			String relationName, User user, boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void addImageToContentlet(Contentlet contentlet, String imageInode,
			String relationName, User user, boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void addLinkToContentlet(Contentlet contentlet, String linkInode,
			String relationName, User user, boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void applyStructurePermissionsToChildren(Structure structure,
			User user, List<Permission> permissions,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void archive(Contentlet contentlet, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void archive(List<Contentlet> contentlets, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkin(Contentlet contentlet, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkin(Contentlet contentlet, List<Permission> permissions,
			User user, boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkin(Contentlet contentlet, User user,
			boolean respectFrontendRoles, List<Category> cats,
			Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkin(Contentlet contentlet,
			Map<Relationship, List<Contentlet>> contentRelationships,
			User user, boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkin(Contentlet contentlet, List<Category> cats,
			List<Permission> permissions, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkin(Contentlet contentlet,
			Map<Relationship, List<Contentlet>> contentRelationships,
			List<Category> cats, User user, boolean respectFrontendRoles,
			Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkin(Contentlet contentlet,
			Map<Relationship, List<Contentlet>> contentRelationships,
			List<Category> cats, List<Permission> permissions, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkin(Contentlet currentContentlet,
			ContentletRelationships relationshipsData, List<Category> cats,
			List<Permission> selectedPermissions, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkinWithNoIndex(Contentlet contentlet, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkinWithNoIndex(Contentlet contentlet,
			List<Permission> permissions, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkinWithNoIndex(Contentlet contentlet, User user,
			boolean respectFrontendRoles, List<Category> cats,
			Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkinWithNoIndex(Contentlet contentlet,
			Map<Relationship, List<Contentlet>> contentRelationships,
			User user, boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkinWithNoIndex(Contentlet contentlet, List<Category> cats,
			List<Permission> permissions, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkinWithNoIndex(Contentlet contentlet,
			Map<Relationship, List<Contentlet>> contentRelationships,
			List<Category> cats, User user, boolean respectFrontendRoles,
			Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkinWithNoIndex(Contentlet contentlet,
			Map<Relationship, List<Contentlet>> contentRelationships,
			List<Category> cats, List<Permission> permissions, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkinWithNoIndex(Contentlet currentContentlet,
			ContentletRelationships relationshipsData, List<Category> cats,
			List<Permission> selectedPermissions, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkinWithoutVersioning(Contentlet contentlet,
			Map<Relationship, List<Contentlet>> contentRelationships,
			List<Category> cats, List<Permission> permissions, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkout(String contentletInode, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkout(List<Contentlet> contentlets, User user,
			boolean respectFrontendRoles, List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkout(String luceneQuery, User user,
			boolean respectFrontendRoles, List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkout(String luceneQuery, User user,
			boolean respectFrontendRoles, int offset, int limit,
			List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void cleanField(Structure structure, Field field, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void cleanHostField(Structure structure, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public long contentletCount(long returnValue) throws DotDataException {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public long contentletIdentifierCount(long returnValue)
			throws DotDataException {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public void convertContentletToFatContentlet(Contentlet cont,
			com.dotmarketing.portlets.contentlet.business.Contentlet fatty,
			com.dotmarketing.portlets.contentlet.business.Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void convertFatContentletToContentlet(
			com.dotmarketing.portlets.contentlet.business.Contentlet fatty,
			Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void copyContentlet(Contentlet currentContentlet, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void copyContentlet(Contentlet currentContentlet, Host host,
			User user, boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void copyContentlet(Contentlet currentContentlet, Folder folder,
			User user, boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void copyProperties(Contentlet contentlet,
			Map<String, Object> properties) {
		// TODO Auto-generated method stub
		
	}

	
	public void delete(Contentlet contentlet, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void delete(List<Contentlet> contentlets, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void delete(Contentlet contentlet, User user,
			boolean respectFrontendRoles, boolean allVersions) {
		// TODO Auto-generated method stub
		
	}

	
	public void delete(List<Contentlet> contentlets, User user,
			boolean respectFrontendRoles, boolean allVersions) {
		// TODO Auto-generated method stub
		
	}

	
	public void deleteOldContent(Date deleteFrom, int offset, int returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void deleteRelatedContent(Contentlet contentlet,
			Relationship relationship, User user, boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void deleteRelatedContent(Contentlet contentlet,
			Relationship relationship, boolean hasParent, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void find(String inode, User user, boolean respectFrontendRoles,
			Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void find(Category category, long languageId, boolean live,
			String orderBy, User user, boolean respectFrontendRoles,
			List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void find(List<Category> categories, long languageId, boolean live,
			String orderBy, User user, boolean respectFrontendRoles,
			List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void findAllContent(int offset, int limit,
			List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void findAllUserVersions(Identifier identifier, User user,
			boolean respectFrontendRoles, List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void findAllVersions(Identifier identifier, User user,
			boolean respectFrontendRoles, List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void findByStructure(Structure structure, User user,
			boolean respectFrontendRoles, int limit, int offset,
			List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void findByStructure(String structureInode, User user,
			boolean respectFrontendRoles, int limit, int offset,
			List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void findContentletByIdentifier(String identifier, boolean live,
			long languageId, User user, boolean respectFrontendRoles,
			Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void findContentletForLanguage(long languageId,
			Identifier contentletId, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}


	
	public void findContentletsByFolder(Folder parentFolder, User user,
			boolean respectFrontendRoles) throws DotDataException {
		// TODO Auto-generated method stub
		
	}

	
	public void findContentletsByHost(Host parentHost, User user,
			boolean respectFrontendRoles) throws DotDataException {
		// TODO Auto-generated method stub
		
	}

	
	public void findContentletsByIdentifiers(String[] identifiers,
			boolean live, long languageId, User user,
			boolean respectFrontendRoles, List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void findFieldValues(String structureInode, Field field, User user,
			boolean respectFrontEndRoles, List<String> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void findPageContentlets(String HTMLPageIdentifier,
			String containerIdentifier, String orderby, boolean working,
			long languageId, User user, boolean respectFrontendRoles,
			List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void getAllLanguages(Contentlet contentlet, Boolean isLiveContent,
			User user, boolean respectFrontendRoles,
			List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void getAllRelationships(Contentlet contentlet,
			ContentletRelationships returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void getAllRelationships(String contentletInode, User user,
			boolean respectFrontendRoles, ContentletRelationships returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void getBinaryFile(String contentletInode,
			String velocityVariableName, User user, File returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void getContentletReferences(Contentlet contentlet, User user,
			boolean respectFrontendRoles, List<Map<String, Object>> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void getFieldValue(Contentlet contentlet, Field theField,
			Object returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void getName(Contentlet contentlet, User user,
			boolean respectFrontendRoles, String returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void getNextReview(Contentlet content, User user,
			boolean respectFrontendRoles, Date returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void getReferencingContentlet(
			com.dotmarketing.portlets.files.model.File file, boolean live,
			User user, boolean respectFrontendRoles,
			List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void getRelatedContent(Contentlet contentlet, Relationship rel,
			User user, boolean respectFrontendRoles,
			List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void getRelatedContent(Contentlet contentlet, Relationship rel,
			boolean pullByParent, User user, boolean respectFrontendRoles,
			List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void getRelatedFiles(Contentlet contentlet, User user,
			boolean respectFrontendRoles,
			List<com.dotmarketing.portlets.files.model.File> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void getRelatedIdentifier(Contentlet contentlet,
			String relationshipType, User user, boolean respectFrontendRoles,
			Identifier returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void getRelatedLinks(Contentlet contentlet, User user,
			boolean respectFrontendRoles, List<Link> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public List<Contentlet> getSiblings(String identifier)
			throws DotDataException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void isContentEqual(Contentlet contentlet1, Contentlet contentlet2,
			User user, boolean respectFrontendRoles, boolean returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void isContentlet(String inode, boolean returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void isFieldTypeBoolean(Field field, boolean returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void isFieldTypeDate(Field field, boolean returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void isFieldTypeFloat(Field field, boolean returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void isFieldTypeLong(Field field, boolean returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void isFieldTypeString(Field field, boolean returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void isInodeIndexed(String inode, boolean returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void isInodeIndexed(String inode, int secondsToWait,
			boolean returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void lock(Contentlet contentlet, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void publish(Contentlet contentlet, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void publish(List<Contentlet> contentlets, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void publishRelatedHtmlPages(Contentlet contentlet) {
		// TODO Auto-generated method stub
		
	}

	
	public void reIndexForServerNode() {
		// TODO Auto-generated method stub
		
	}

	
	public void refresh(Structure structure) {
		// TODO Auto-generated method stub
		
	}

	
	public void refresh(Contentlet contentlet) {
		// TODO Auto-generated method stub
		
	}

	
	public void refreshAllContent() {
		// TODO Auto-generated method stub
		
	}

	
	
	public void reindex() {
		// TODO Auto-generated method stub
		
	}

	
	public void reindex(Structure structure) {
		// TODO Auto-generated method stub
		
	}

	
	public void reindex(Contentlet contentlet) {
		// TODO Auto-generated method stub
		
	}

	
	public void relateContent(Contentlet contentlet,
			ContentletRelationshipRecords related, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void relateContent(Contentlet contentlet, Relationship rel,
			List<Contentlet> related, User user, boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public boolean removeContentletFromIndex(String contentletInodeOrIdentifier)
			throws DotDataException {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void removeUserReferences(String userId) throws DotDataException {
		// TODO Auto-generated method stub
		
	}

	
	public void restoreVersion(Contentlet contentlet, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void search(String luceneQuery, int limit, int offset,
			String sortBy, User user, boolean respectFrontendRoles,
			List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	
	public void search(String luceneQuery, int limit, int offset,
			String sortBy, User user, boolean respectFrontendRoles,
			int requiredPermission, List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	public void setContentletProperty(Contentlet contentlet, Field field,
			Object value) {
		// TODO Auto-generated method stub
		
	}

	
	public void unarchive(List<Contentlet> contentlets, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void unarchive(Contentlet contentlet, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void unlock(Contentlet contentlet, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void unpublish(Contentlet contentlet, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void unpublish(List<Contentlet> contentlets, User user,
			boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	
	public void validateContentlet(Contentlet contentlet, List<Category> cats) {
		// TODO Auto-generated method stub
		
	}

	
	public void validateContentlet(Contentlet contentlet,
			Map<Relationship, List<Contentlet>> contentRelationships,
			List<Category> cats) {
		// TODO Auto-generated method stub
		
	}

	
	public void validateContentlet(Contentlet contentlet,
			ContentletRelationships contentRelationships, List<Category> cats) {
		// TODO Auto-generated method stub
		
	}

	
	public void getUrlMapForContentlet(Contentlet arg0, User arg1, boolean arg2)
			throws DotSecurityException, DotDataException {
		// TODO Auto-generated method stub
		
	}	
	
	public void addImageToContentlet(Contentlet contentlet, long imageInode,
			String relationName, User user, boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	public void addLinkToContentlet(Contentlet contentlet, long linkInode,
			String relationName, User user, boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}

	public void checkout(long contentletInode, User user,
			boolean respectFrontendRoles, Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	public void find(long inode, User user, boolean respectFrontendRoles,
			Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	public void findByStructure(long structureInode, User user,
			boolean respectFrontendRoles, int limit, int offset,
			List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	public void findContentletByIdentifier(long identifier, boolean live,
			long languageId, User user, boolean respectFrontendRoles,
			Contentlet returnValue) {
		// TODO Auto-generated method stub
		
	}

	public void findContentlets(List<Long> inodes, List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	public void findFieldValues(long structureInode, Field field, User user,
			boolean respectFrontEndRoles, List<String> returnValue) {
		// TODO Auto-generated method stub
		
	}

	public void findPageContentlets(long HTMLPageIdentifier,
			long containerIdentifier, String orderby, boolean working,
			long languageId, User user, boolean respectFrontendRoles,
			List<Contentlet> returnValue) {
		// TODO Auto-generated method stub
		
	}

	public void getAllRelationships(long contentletInode, User user,
			boolean respectFrontendRoles, ContentletRelationships returnValue) {
		// TODO Auto-generated method stub
		
	}

	public void getBinaryFile(Long contentletInode,
			String velocityVariableName, User user, java.io.File returnValue) {
		// TODO Auto-generated method stub
		
	}

	public void indexSearch(String luceneQuery, int limit, int offset,
			String sortBy, User user, boolean respectFrontendRoles,
			LuceneHits returnValue) {
		// TODO Auto-generated method stub
		
	}

	public void isContentlet(long inode, boolean returnValue) {
		// TODO Auto-generated method stub
		
	}

	public boolean removeContentletFromIndex(long contentletInodeOrIdentifier)
			throws DotDataException {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void addFileToContentlet(Contentlet contentlet, long fileInode,
			String relationName, User user, boolean respectFrontendRoles) {
		// TODO Auto-generated method stub
		
	}
	
	public List<Contentlet> getSiblings(long fileInode) {
		// TODO Auto-generated method stub
		return new ArrayList<Contentlet>();
	}

	


}