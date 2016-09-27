package nl.isaac.dotcms.minify.osgi;

import nl.isaac.dotcms.minify.FileChangedPostHook;
import nl.isaac.dotcms.minify.MinifyCacheHandler;
import nl.isaac.dotcms.minify.servlet.MinifyServlet;
import nl.isaac.dotcms.minify.viewtool.AbstractMinifyViewTool;
import nl.isaac.dotcms.minify.viewtool.CssMinifyViewTool;
import nl.isaac.dotcms.minify.viewtool.JsMinifyViewTool;
import nl.isaac.dotcms.util.osgi.ExtendedGenericBundleActivator;
import nl.isaac.dotcms.util.osgi.ViewToolScope;

import com.dotcms.repackage.org.osgi.framework.BundleContext;
import com.dotmarketing.filters.CMSFilter;

public class MinifierActivator extends ExtendedGenericBundleActivator {

	public void start(BundleContext context) throws Exception {

		initializeServices(context);

		// Register the contentlet posthook
		addPostHook(context, FileChangedPostHook.class);

		// Register ViewTools
		addViewTool(context,        JsMinifyViewTool.class,  "jsMinifyTool", ViewToolScope.REQUEST);
		addViewTool(context,       CssMinifyViewTool.class, "cssMinifyTool", ViewToolScope.REQUEST);

		// Register the servlet
		addServlet(context, MinifyServlet.class, "/servlets/MinifyServlet");

		final String filterPattern = ".*\\" + AbstractMinifyViewTool.FILTER_PATTERN + "(j|cs)s$";
		addRewriteRule(filterPattern, "/app/servlets/MinifyServlet", "forward", "MinifyServletRedirectFilter");
		addRewriteRule("^/servlets/MinifyServlet$", "/app/servlets/MinifyServlet", "forward", "MinifyServletBackwardsCompatiblity");

		CMSFilter.excludeURI( filterPattern);
	}
	


	public void stop(BundleContext context) throws Exception {

		// Clear the cache
		MinifyCacheHandler.INSTANCE.removeAll();
		unregisterServices(context);

	}
}

