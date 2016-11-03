package nl.isaac.dotcms.minify.servlet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dotmarketing.beans.Host;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import nl.isaac.dotcms.minify.MinifyCacheFile;
import nl.isaac.dotcms.minify.MinifyCacheHandler;
import nl.isaac.dotcms.minify.MinifyCacheKey;
import nl.isaac.dotcms.minify.exception.DotCMSFileNotFoundException;
import nl.isaac.dotcms.minify.shared.HostTools;
import nl.isaac.dotcms.minify.util.StringListUtil;

/**
 * Servlet that uses the MinifyCacheHandler to retrieve minified CSS and
 * Javascript.
 *
 * @author Koen Peters, ISAAC
 * @author Xander Steinmann, ISAAC
 */
public class MinifyServlet extends AbstractMinifyServlet {

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

							fileContentOfUris.append(getFileContent(uri, host, isLiveMode));
							isContentModified = true;

						} else {
							MinifyCacheKey key = new MinifyCacheKey(uri.getPath(), host.getHostname(), isLiveMode);

							MinifyCacheFile file = MinifyCacheHandler.INSTANCE.get(key);
							fileContentOfUris.append(file.getFileData());

							Date modDate = file.getModDate();
							isContentModified |=  modDate.compareTo(ifModifiedSince) >= 0;
						}
					} else {
						Logger.warn(MinifyServlet.class, "Encountered uri with different contentType than the others, skipping file. Expected " + overAllContentType.getExtension() + ", found: " + currentContentType.getExtension());
					}
				} else {
					Logger.debug(this, "Skipping empty uri in uris='" + urisAsString + "'");
				}
			}

			if(overAllContentType != null) {
				response.setContentType(overAllContentType.getContentTypeString());
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



}
