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
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.isaac.dotcms.minify.MinifyCacheFileKey;
import nl.isaac.dotcms.minify.MinifyCacheHandler;

import com.dotmarketing.beans.Host;
import com.dotmarketing.factories.HostFactory;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

/**
 * Servlet that uses the MinifyCache to retrieve minified CSS and Javascript.
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
		String urisParam = request.getParameter("uris");
		
		if(UtilMethods.isSet(request.getParameter("uris"))) {
			List<String> uris = Arrays.asList(urisParam.split(","));
			
			//check uris 
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
			
			//retrieve live or working cached files
			Boolean live = Boolean.TRUE;
			String liveString = request.getParameter("live");
			if(liveString != null) {
				live = Boolean.valueOf(liveString);
			} else {
				live = getIsLiveFromRequest(request);
			}
			
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
						host = HostFactory.getHostByHostName(hostName);
					} else {
						Logger.warn(this.getClass(), "Bad uri: " + uri);
					}
				}
				
				//create a key for this uri and retrieve from the cache
				String key = new MinifyCacheFileKey(modifiedUri, live, host).getKey();
				response.getWriter().write(MinifyCacheHandler.getInstance().get(key).getFileData());
			}
		}
		
		Calendar end = Calendar.getInstance();
		Logger.debug(this.getClass(), "MinifyServlet took " + (end.getTimeInMillis() - start.getTimeInMillis()) + "ms for uris " + urisParam);
		
	}
	
	private Host getCurrentHost(ServletRequest request) throws ServletException {
		return HostFactory.getCurrentHost(request);
	}
	
	private Boolean getIsLiveFromRequest(HttpServletRequest request) {
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
