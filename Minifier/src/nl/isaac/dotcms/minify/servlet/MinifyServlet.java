package nl.isaac.dotcms.minify.servlet;
/**
* dotCMS minifier by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
* 
* @copyright Copyright (c) 2011 ISAAC Software Solutions B.V. (http://www.isaac.nl)
*/

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.isaac.dotcms.minify.MinifyCacheFile;
import nl.isaac.dotcms.minify.MinifyCacheFileKey;
import nl.isaac.dotcms.minify.MinifyCacheHandler;
import nl.isaac.dotcms.minify.exception.DotCMSFileNotFoundException;
import nl.isaac.dotcms.minify.shared.Configuration;

import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;

/**
 * Servlet that uses the MinifyCacheHandler to retrieve minified CSS and Javascript.
 * 
 * @author Xander
 *
 */
public class MinifyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    public MinifyServlet() {
    	
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Calendar start = Calendar.getInstance();
		Host currentHost = getCurrentHost(request);
		
		//validate uris parameter
		if(!UtilMethods.isSet(request.getParameter("uris"))) {
			throw new ServletException("Invalid uris parameter");
		}
		List<String> uris = Arrays.asList(request.getParameter("uris").split(","));

		//set content type if uri's is a css or js file
		if(uris.size() < 1) {
			throw new RuntimeException("Missing uri's");
		} else {
			String firstUri = uris.iterator().next().toLowerCase().trim();
			if(firstUri.endsWith(".css")) {
				response.setContentType("text/css;charset=UTF-8");
			} else if (firstUri.endsWith(".js")){
				response.setContentType("application/javascript;charset=UTF-8");
			} else {
				throw new RuntimeException("Expecting only files with the .css or .js extension");
			}
		}
		
		//check if we need the live or working version
		boolean live = isLive(request);
		
		boolean contentModified = false;
		Date ifModifiedSince = new Date(request.getDateHeader("If-Modified-Since"));
		StringBuilder output = new StringBuilder();
		
		for(String uri: uris) {
			String modifiedUri = uri.trim().replaceAll("(\\r|\\n)", "");
			if(modifiedUri != null && !modifiedUri.isEmpty()) {
				Host host = currentHost;
				
				//check if this file needs to be retrieved from a different host 
				if(modifiedUri.startsWith("http")) {
					modifiedUri = modifiedUri.replaceAll("http://", "");
					modifiedUri = modifiedUri.replaceAll("https://", "");
					int dashIndex = modifiedUri.indexOf('/');
					if(dashIndex > 0) {
						String hostName = modifiedUri.substring(0, dashIndex);
						modifiedUri = modifiedUri.substring(dashIndex);
						host = findHostByNameOrAlias(hostName);
					} else {
						Logger.warn(this.getClass(), "Bad uri: '" + uri + "'");
					}
				}
				
				//create a key for this uri and retrieve from the cache
				String key = new MinifyCacheFileKey(modifiedUri, live, host).getKey();
				MinifyCacheFile file = MinifyCacheHandler.getInstance().get(key);

				Date modDate = file.getModDate();
				if (modDate.compareTo(ifModifiedSince) >= 0) contentModified = true;
				
				try {
					output.append(file.getFileData());
				} catch (DotCMSFileNotFoundException e) {
					Logger.warn(this.getClass(), "Not adding file with URI " + modifiedUri + " and host " + host.getHostname() + " since it can't be found in dotCMS");
				}
				
			} else {
				Logger.warn(this.getClass(), "Can't minify uri that is empty. Maybe there's an error in the Minifier call? uris='" + request.getParameter("uris") + "'");
			}
		}
		int maxAge = new Integer(Configuration.getBrowserCacheMaxAge()).intValue();
		response.addHeader("Cache-Control", "public, max-age=" + maxAge);
		response.setDateHeader("Expires", new Date().getTime() + (maxAge * 1000));
		
		if (contentModified) {
			response.setDateHeader("Last-Modified", new Date().getTime());
			response.getWriter().write(output.toString());

		} else {
			// No files are modified since the browser cached it, so send status 304
			// Browser will then use the file from his cache
			response.setStatus(304);
		}
		
		Calendar end = Calendar.getInstance();
		Logger.debug(this.getClass(), "MinifyServlet took " + (end.getTimeInMillis() - start.getTimeInMillis()) + "ms for uris " + uris);
	}
	
	private Host findHostByNameOrAlias(String hostName) {
		Host host = null;
		Exception e1 = null;
		Exception e2 = null;
		
		try {
			host = APILocator.getHostAPI().findByName(hostName, APILocator.getUserAPI().getSystemUser(), false);
		} catch (DotDataException e) {
			e1 = e;
		} catch (DotSecurityException e) {
			e1 = e;
		}
		
		if(host == null) {
			try {
				host = APILocator.getHostAPI().findByAlias(hostName, APILocator.getUserAPI().getSystemUser(), false);
			} catch (DotDataException e) {
				e2 = e;
			} catch (DotSecurityException e) {
				e2 = e;
			}
		}
		
		if(host == null) {
			Logger.error(this.getClass(), "Unable to find host with name " + hostName + " (see the following exceptions");
			Logger.error(this.getClass(), "Exception lookup up hostName", e1);
			Logger.error(this.getClass(), "Exception looking up alias", e2);
			throw new RuntimeException("Unable to find host with name " + hostName + " (see previous exceptions)");			
		}
		
		return host;
	}

	/**
	 * Retrieve the current host from the request
	 * @return the current host
	 * @throws ServletException an exception that wraps the actual dotCMS exception when the host can't be found
	 */
	private Host getCurrentHost(HttpServletRequest request) throws ServletException {
		try {
			return WebAPILocator.getHostWebAPI().getCurrentHost(request);
		} catch (PortalException e) {
			throw new ServletException(e);
		} catch (SystemException e) {
			throw new ServletException(e);
		} catch (DotDataException e) {
			throw new ServletException(e);
		} catch (DotSecurityException e) {
			throw new ServletException(e);
		}
	}
	
	/**
	 * @param request
	 * @return whether the request is coming from live or edit/preview mode
	 */
	private Boolean isLive(HttpServletRequest request) {
		String liveAsString = request.getParameter("live");
		Boolean live = Boolean.TRUE;
		if (liveAsString != null) {
			if (!liveAsString.equals("null") && liveAsString.length() > 0) {
				if (liveAsString.equalsIgnoreCase("1") ||
					liveAsString.equalsIgnoreCase("true") ||
					liveAsString.equalsIgnoreCase("on")) {
					live = Boolean.TRUE;
				} else {
					live = Boolean.FALSE;
				}
			}
		}
		return live;
	}
	
}
