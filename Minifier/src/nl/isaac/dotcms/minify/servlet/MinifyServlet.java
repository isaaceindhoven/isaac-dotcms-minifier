package nl.isaac.dotcms.minify.servlet;
/**
* dotCMS minifier by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
*/

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.isaac.dotcms.minify.MinifyCacheFileKey;
import nl.isaac.dotcms.minify.MinifyCacheHandler;

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
			String firstUri = uris.iterator().next().toLowerCase();
			if(firstUri.endsWith(".css")) {
				response.setContentType("text/css;charset=UTF-8");
			} else if (firstUri.endsWith(".js")){
				response.setContentType("application/javascript;charset=UTF-8");
			} else {
				throw new RuntimeException("Expecting only files with the .css or .js extension");
			}
		}
		
		//check if we need the live or working version
		Boolean live = isLive(request);
		
		for(String uri: uris) {
			String modifiedUri = uri;
			Host host = currentHost;
			
			//check if this file needs to be retrieved from a different host 
			if(modifiedUri.startsWith("http")) {
				modifiedUri = modifiedUri.replaceAll("http://", "");
				modifiedUri = modifiedUri.replaceAll("https://", "");
				int dashIndex = modifiedUri.indexOf('/');
				if(dashIndex > 0) {
					String hostName = modifiedUri.substring(0, dashIndex);
					modifiedUri = modifiedUri.substring(dashIndex);
					try {
						host = APILocator.getHostAPI().find(hostName, APILocator.getUserAPI().getSystemUser(), false);
					} catch (DotDataException e) {
						Logger.warn(this.getClass(), "Can't find host: '" + hostName + "'");
					} catch (DotSecurityException e) {
						Logger.warn(this.getClass(), "Can't find host: '" + hostName + "'");
					}
				} else {
					Logger.warn(this.getClass(), "Bad uri: " + uri);
				}
			}
			
			//create a key for this uri and retrieve from the cache
			String key = new MinifyCacheFileKey(modifiedUri, live, host).getKey();
			response.getWriter().write(MinifyCacheHandler.getInstance().get(key).getFileData());
		}
		
		Calendar end = Calendar.getInstance();
		Logger.debug(this.getClass(), "MinifyServlet took " + (end.getTimeInMillis() - start.getTimeInMillis()) + "ms for uris " + uris);
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
