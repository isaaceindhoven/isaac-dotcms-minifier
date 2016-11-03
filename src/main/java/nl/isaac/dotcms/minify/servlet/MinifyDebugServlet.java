package nl.isaac.dotcms.minify.servlet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dotmarketing.beans.Host;
import com.dotmarketing.util.Logger;

import nl.isaac.dotcms.minify.shared.HostTools;

public class MinifyDebugServlet extends AbstractMinifyServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uriAsString = request.getParameter("uri");
		boolean isLiveMode = HostTools.isLiveMode(request);

		URI uri;
		try {
			uri = new URI(uriAsString);
		} catch (URISyntaxException e) {
			Logger.error(MinifyServlet.class, "Cannot parse URI", e);
			response.sendError(404);
			return;
		}

		Host defaultHost = HostTools.getCurrentHost(request);

		Host host = getHostOfUri(uri, defaultHost);
		ContentType currentContentType = ContentType.getContentType(uri);
		String fileContent = getFileContent(uri, host, isLiveMode);

		response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		response.addHeader("Pragma", "no-cache");
		response.addHeader("Expires", "0");

		response.setContentType(currentContentType.getContentTypeString());
		response.getWriter().print(fileContent);
	}
}
