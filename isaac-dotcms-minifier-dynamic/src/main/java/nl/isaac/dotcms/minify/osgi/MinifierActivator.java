package nl.isaac.dotcms.minify.osgi;

import org.osgi.framework.BundleContext;

import com.dotmarketing.filters.CMSFilter;

import nl.isaac.dotcms.minify.FileChangedPostHook;
import nl.isaac.dotcms.minify.MinifyCacheHandler;
import nl.isaac.dotcms.minify.servlet.MinifyProxyServlet;
import nl.isaac.dotcms.minify.servlet.MinifyServlet;
import nl.isaac.dotcms.minify.viewtool.AbstractMinifyViewTool;
import nl.isaac.dotcms.minify.viewtool.CssMinifyViewTool;
import nl.isaac.dotcms.minify.viewtool.JsMinifyViewTool;
import nl.isaac.dotcms.util.osgi.ExtendedGenericBundleActivator;
import nl.isaac.dotcms.util.osgi.ViewToolScope;

public class MinifierActivator extends ExtendedGenericBundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {

		initializeServices(context);

		// Register the contentlet posthook
		addPostHook(context, FileChangedPostHook.class);

		// Register ViewTools
		addViewTool(context,        JsMinifyViewTool.class,  "jsMinifyTool", ViewToolScope.REQUEST);
		addViewTool(context,       CssMinifyViewTool.class, "cssMinifyTool", ViewToolScope.REQUEST);

		// Register the servlet
		addServlet(context, MinifyServlet.class, "/servlets/MinifyServlet");
		addServlet(context, MinifyProxyServlet.class, "/servlets/MinifyProxyServlet");

		final String filterPattern = ".*\\" + AbstractMinifyViewTool.FILTER_PATTERN + "(j|cs)s$";
		final String debugFilterPattern = "^/app/minifier/proxy/(.+?)(/.+)$"; // first URL part after /proxy/ is always the host

		addRewriteRule(filterPattern, "/app/servlets/MinifyServlet", "forward", "MinifyServletRedirectFilter");
		addRewriteRule(debugFilterPattern, "/app/servlets/MinifyProxyServlet?host=$1&uri=$2", "forward", "MinifyProxyServletRedirectFilter");
		addRewriteRule("^/servlets/MinifyServlet$", "/app/servlets/MinifyServlet", "forward", "MinifyServletBackwardsCompatiblity");

		CMSFilter.excludeURI( filterPattern);
	}

	@Override
	public void stop(BundleContext context) throws Exception {

		// Clear the cache
		MinifyCacheHandler.INSTANCE.removeAll();
		unregisterServices(context);

	}
}

