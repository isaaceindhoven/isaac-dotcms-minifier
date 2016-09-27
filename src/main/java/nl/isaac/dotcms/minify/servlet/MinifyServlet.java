package nl.isaac.dotcms.minify.servlet;

/*
 * Dotcms minifier by ISAAC is licensed under a 
 * Creative Commons Attribution 3.0 Unported License
 * 
 * - http://creativecommons.org/licenses/by/3.0/
 * - http://www.geekyplugins.com/
 * 
 * ISAAC Software Solutions B.V. (http://www.isaac.nl)
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.isaac.dotcms.minify.MinifyCacheFile;
import nl.isaac.dotcms.minify.MinifyCacheHandler;
import nl.isaac.dotcms.minify.MinifyCacheKey;
import nl.isaac.dotcms.minify.exception.DotCMSFileNotFoundException;
import nl.isaac.dotcms.minify.shared.FileTools;
import nl.isaac.dotcms.minify.shared.HostTools;
import nl.isaac.dotcms.minify.util.StringListUtil;

import org.apache.commons.io.IOUtils;

import com.dotmarketing.beans.Host;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.portlets.fileassets.business.FileAsset;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

/**
 * Servlet that uses the MinifyCacheHandler to retrieve minified CSS and
 * Javascript.
 * 
 * @author Koen Peters, ISAAC
 * @author Xander Steinmann, ISAAC
 */
public class MinifyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final long BROWSER_CACHE_MAX_AGE = 25920000L;
	
	/**
	 * Helper type for all possible content types that the minifier accepts.
	 */
	private enum ContentType {
		JS("js", "application/javascript;charset=UTF-8")
		,CSS("css", "text/css;charset=UTF-8");
		
		private String extension;
		private String contentTypeString;
		
		ContentType(String extension, String contentTypeString) {
			this.extension = extension;
			this.contentTypeString = contentTypeString;
		}
		
		static ContentType getContentType(URI uri) {
			for (ContentType ct: ContentType.values()) {
				if (uri.getPath().endsWith(ct.extension)) {
					return ct;
				}	
			}
			throw new RuntimeException("Cannot determine contentType from URI " + uri);
		}
		
	}

	public MinifyServlet() {

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Make sure the "uris" parameter returns a string
		String urisAsString = request.getParameter("uris") != null ? request.getParameter("uris") : "";
		
		// Define variables whos content will be determined during the for loop.
		StringBuilder fileContentOfUris = new StringBuilder();
		boolean isContentModified = false;
		ContentType overAllContentType = null;

		// Retrieve data that is used in the for loop only once for speed
		boolean isDebugMode = UtilMethods.isSet(request.getParameter("debug"));
		boolean isLiveMode = HostTools.isLiveMode(request);
		Date ifModifiedSince = new Date(request.getDateHeader("If-Modified-Since"));
		Host defaultHost = HostTools.getCurrentHost(request);
		
		try {
			for (String uriAsString : StringListUtil.getCleanStringList(urisAsString)) {
				if(UtilMethods.isSet(uriAsString)) {
					URI uri = new URI(uriAsString);
					
					ContentType currentContentType = ContentType.getContentType(uri);
					overAllContentType = overAllContentType == null? currentContentType: overAllContentType;
					
					if (currentContentType == overAllContentType) {
						Host host = getHostOfUri(uri, defaultHost);
							
						if (isDebugMode) {
	
							fileContentOfUris.append(getOriginalFile(uri, host, isLiveMode));
							isContentModified = true;
							
						} else {
							MinifyCacheKey key = new MinifyCacheKey(uri.getPath(), host.getHostname(), isLiveMode);
	
							MinifyCacheFile file = MinifyCacheHandler.INSTANCE.get(key);
							fileContentOfUris.append(file.getFileData());
	
							Date modDate = file.getModDate();
							isContentModified |=  modDate.compareTo(ifModifiedSince) >= 0;
						}
					} else {
						Logger.warn(MinifyServlet.class, "Encountered uri with different contentType than the others, skipping file. Expected " + overAllContentType.extension + ", found: " + currentContentType.extension);
					}
				} else {
					Logger.info(this, "Skipping empty uri in uris='" + urisAsString + "'");
				}
			}
			
			if(overAllContentType != null) {
				response.setContentType(overAllContentType.contentTypeString);
			}
			
			response.addHeader("Cache-Control", "public, max-age=" + BROWSER_CACHE_MAX_AGE);
			response.setDateHeader("Expires", new Date().getTime() + (BROWSER_CACHE_MAX_AGE * 1000));

			if (isContentModified) {
				response.setDateHeader("Last-Modified", new Date().getTime());
				response.getWriter().write(fileContentOfUris.toString());

			} else {
				// No files are modified since the browser cached it, so send
				// status 304. Browser will then use the file from his cache
				response.setStatus(304);
			}
			
		} catch (DotCMSFileNotFoundException e) {
			Logger.error(MinifyServlet.class, "One or more files can't be found in dotCMS, sending 404 response", e);
			response.sendError(404);

		} catch (URISyntaxException e) {
			Logger.error(MinifyServlet.class, "Cannot parse one or more URIs", e);
			response.sendError(404);
			
		}
	}

	
	private Host getHostOfUri(URI uri, Host defaultHost) {
		
		// If the URI is absolute we determine the host on its domain. (We don't
		// use URI's isAbsolute method because it not understand protocol
		// relative URLs.)
		if (uri.getHost() != null) {
			return HostTools.getHostByDomainName(uri.getHost());
		}

		return defaultHost;
	}
	
	private String getOriginalFile(URI uri, Host host, boolean isLiveMode) throws DotCMSFileNotFoundException {
		FileAsset file = FileTools.getFileAssetByURI(uri.toString(), host, isLiveMode);
		
		try {
			if (file != null && file.getURI() != null) {
				StringWriter stringWriter = new StringWriter();
				InputStream input = file.getFileInputStream();
				IOUtils.copy(input, stringWriter);
				return stringWriter.toString();
			}
		} catch (FileNotFoundException e) {
			Logger.error(MinifyServlet.class, "Could not find file", e);
		} catch (IOException e) {
			Logger.error(MinifyServlet.class, "Could not find file", e);
		} catch (DotDataException e) {
			Logger.error(MinifyServlet.class, "Could not find file", e);
		}
		throw new DotCMSFileNotFoundException("Could not find " + (isLiveMode? "live": "working") + " file '" + uri.toString() + "' on host '" + host.getHostname() + "'.");
	}

}
