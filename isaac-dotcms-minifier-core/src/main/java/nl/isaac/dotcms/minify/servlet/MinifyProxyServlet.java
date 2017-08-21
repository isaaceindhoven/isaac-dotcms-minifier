package nl.isaac.dotcms.minify.servlet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dotmarketing.beans.Host;
import com.dotmarketing.util.Logger;

import nl.isaac.dotcms.minify.exception.DotCMSFileNotFoundException;
import nl.isaac.dotcms.minify.shared.HostTools;

/**
 * This servlet is used in debug mode, if the file that must be retrieved is on another host. It just
 * gets the file contents from the given host on the given path and returns this to the browser.
 *
 * @author Jorith van den Heuvel, ISAAC
*/
public class MinifyProxyServlet extends AbstractMinifyServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String hostname = request.getParameter("host");
		String uriAsString = request.getParameter("uri");
		boolean isLiveMode = HostTools.isLiveMode(request);

		URI uri;
		try {
			uri = new URI(uriAsString);
		} catch (URISyntaxException e) {
			Logger.error(MinifyProxyServlet.class, "Cannot parse URI", e);
			response.sendError(404);
			return;
		}

		Host host = HostTools.getHostByDomainName(hostname);

		if (host == null) {
			Logger.error(this, "Cannot find host with hostname '" + hostname + "'");
			response.sendError(404);
			return;
		}

		ContentType currentContentType;
		try {
			currentContentType = ContentType.getContentType(uri);
		} catch (IllegalArgumentException e) {
			Logger.error(this, "Unallowed content type for file '" + uriAsString + "'. Only Javascript and CSS is allowed.");
			response.sendError(400);
			return;
		}

		String fileContent;
		try {
			fileContent = getFileContent(uri, host, isLiveMode);
		} catch (DotCMSFileNotFoundException e) {
			Logger.error(this, "Cannot find file '" + uriAsString + "' on host '" + hostname + "'");
			response.sendError(404);
			return;
		}

		response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		response.addHeader("Pragma", "no-cache");
		response.addHeader("Expires", "0");

		response.setContentType(currentContentType.getContentTypeString());
		response.getWriter().print(fileContent);
	}
}
