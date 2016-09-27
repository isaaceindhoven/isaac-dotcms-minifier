package nl.isaac.dotcms.minify.shared;

import javax.servlet.http.HttpServletRequest;

import com.dotmarketing.beans.Host;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;

public class HostTools {
	/**
	 * Retrieve the current host from the request
	 * @return the current host
	 * @throws RuntimeException an exception that wraps the actual dotCMS exception when the host can't be found
	 */
	public static Host getCurrentHost(HttpServletRequest request) {
		try {
			return WebAPILocator.getHostWebAPI().getCurrentHost(request);
		} catch (PortalException e) {
			throw new RuntimeException(e);
		} catch (SystemException e) {
			throw new RuntimeException(e);
		} catch (DotDataException e) {
			throw new RuntimeException(e);
		} catch (DotSecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
