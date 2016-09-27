package nl.isaac.dotcms.minify.shared;

import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.fileassets.business.FileAsset;
import com.liferay.portal.model.User;

public class FileTools {

	public static FileAsset getFileAssetByURI(String URI, Host host, boolean isLive) {
		try {
			User systemUser = APILocator.getUserAPI().getSystemUser();
			long defaultLanguage = APILocator.getLanguageAPI().getDefaultLanguage().getId();
			
			Identifier identifier = APILocator.getIdentifierAPI().find(host, URI);
			Contentlet fileContentlet = APILocator.getContentletAPI().findContentletByIdentifier(identifier.getId(), isLive, defaultLanguage, systemUser, false );
			
			return APILocator.getFileAssetAPI().fromContentlet(fileContentlet);
		} catch (DotSecurityException e) {
			throw new RuntimeException(e);
		} catch (DotDataException e) {
			throw new RuntimeException(e);
		}
	}
}
