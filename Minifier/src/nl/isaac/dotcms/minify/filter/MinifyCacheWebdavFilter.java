package nl.isaac.dotcms.minify.filter;
/**
* dotCMS minifier by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
* 
* @copyright Copyright (c) 2011 ISAAC Software Solutions B.V. (http://www.isaac.nl)
* 
*/

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import nl.isaac.dotcms.minify.MinifyCacheFileKey;
import nl.isaac.dotcms.minify.MinifyCacheHandler;

import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.util.Logger;

/**
 * The MinifyCacheWebdavFilter filters WEBDAV PUT calls and updates the MinifyCache when a js or css
 * file is submitted. 
 * 
 * @author Xander
 *
 */
public class MinifyCacheWebdavFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)req;
		
		chain.doFilter(req, res);
		
		//After submitting a WEBDAV request, check if it was a PUT request
		if(request.getMethod().equalsIgnoreCase("PUT")) {
			Logger.debug(this.getClass(), "Parsing WebDAV PUT request");
			
			//Determine uri, host and 'live' from requestURI
			//the uri is something like /webdav/autopub/companyX.com/css/style.css
			String uri = request.getRequestURI();

			//remove the webdav part
			uri = uri.substring(uri.indexOf("webdav/") + 7);
			
			//determine live/working and remove it from the uri (autopub = live, nonpub = working)
			boolean live = uri.substring(0, uri.indexOf("/")).equalsIgnoreCase("autopub");
			uri = uri.substring(uri.indexOf("/") + 1);
			
			//determine host (the remainder is the file uri)
			String hostName = uri.substring(0, uri.indexOf("/"));
			uri = uri.substring(uri.indexOf("/"));
			
			Logger.debug(this.getClass(), "hostName: " + hostName + ", live: " + live + ", uri: " + uri);
			
			try {
				//find the host
				Host host = APILocator.getHostAPI().findByName(hostName, APILocator.getUserAPI().getSystemUser(), false);
				
				//update the cache
				String key = new MinifyCacheFileKey(uri, live, host).getKey();
				MinifyCacheHandler.getInstance().updateWithItemHandler(key);
			} catch (DotDataException e) {
				throw new RuntimeException("Can't find host", e);
			} catch (DotSecurityException e) {
				throw new RuntimeException("Can't find host", e);
			}
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}