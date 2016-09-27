package nl.isaac.dotcms.minify.osgi;

import javax.servlet.ServletException;

import nl.isaac.dotcms.minify.FileChangedPostHook;
import nl.isaac.dotcms.minify.MinifyCacheHandler;
import nl.isaac.dotcms.minify.servlet.MinifyServlet;
import nl.isaac.dotcms.minify.viewtool.AbstractMinifyViewTool;
import nl.isaac.dotcms.minify.viewtool.CssMinifyViewTool;
import nl.isaac.dotcms.minify.viewtool.JsMinifyViewTool;
import nl.isaac.dotcms.util.osgi.ExtendedGenericBundleActivator;
import nl.isaac.dotcms.util.osgi.ViewToolScope;

import com.dotcms.repackage.org.apache.felix.http.api.ExtHttpService;
import com.dotcms.repackage.org.osgi.framework.BundleContext;
import com.dotcms.repackage.org.osgi.framework.ServiceReference;
import com.dotcms.repackage.org.osgi.service.http.NamespaceException;
import com.dotcms.repackage.org.osgi.util.tracker.ServiceTracker;
import com.dotmarketing.filters.CMSFilter;
import com.dotmarketing.util.Logger;

public class MinifierActivator extends ExtendedGenericBundleActivator {

	private MinifyServlet minifyServlet;
	private ServiceTracker<ExtHttpService, ExtHttpService> tracker;

	public void start(BundleContext context) throws Exception {

		initializeServices(context);

		// Register the contentlet posthook
		addPostHook(new FileChangedPostHook());

		// Register ViewTools
		addViewTool(context,        JsMinifyViewTool.class,  "jsMinifyTool", ViewToolScope.REQUEST);
		addViewTool(context,       CssMinifyViewTool.class, "cssMinifyTool", ViewToolScope.REQUEST);

		// Register the servlet and filter
		registerServlet(context);

		final String filterPattern = ".*\\" + AbstractMinifyViewTool.FILTER_PATTERN + "(j|cs)s$";
		addRewriteRule(filterPattern, "/app/servlets/MinifyServlet", "forward", "MinifyServletRedirectFilter");

		addRewriteRule("^/servlets/MinifyServlet$", "/app/servlets/MinifyServlet", "forward", "MinifyServletBackwardsCompatiblity");

		CMSFilter.addExclude("/servlets/MinifyServlet");
		CMSFilter.addExclude("/app/servlets/MinifyServlet");
		CMSFilter.addExclude("/app/servlets/MinifyServlet");
		CMSFilter.addExclude( filterPattern);
	}
	

	/**
	 * Mostly boilerplate to get the httpservice where we can register the {@link #minifyServlet}, we also create the servlet here.
	 * @param context
	 */
	private void registerServlet(BundleContext context) {
		tracker = new ServiceTracker<ExtHttpService, ExtHttpService>(context, ExtHttpService.class, null) {
			@Override public ExtHttpService addingService(ServiceReference<ExtHttpService> reference) {
				ExtHttpService extHttpService = super.addingService(reference);

				minifyServlet = new MinifyServlet();

				try {

					extHttpService.registerServlet("/servlets/MinifyServlet", minifyServlet, null, null);

				} catch (ServletException e) {
					throw new RuntimeException("Failed to register servlet and filter", e);
				} catch (NamespaceException e) {
					throw new RuntimeException("Failed to register servlet and filter", e);
				}

				Logger.info(this, "Registered servlet and filter");

				return extHttpService;
			}
			@Override public void removedService(ServiceReference<ExtHttpService> reference, ExtHttpService extHttpService) {
				extHttpService.unregisterServlet(minifyServlet);
				super.removedService(reference, extHttpService);
			}
		};
		tracker.open();
	}

	public void stop(BundleContext context) throws Exception {

		CMSFilter.removeExclude("/servlets/MinifyServlet");
		CMSFilter.removeExclude("/app/servlets/MinifyServlet");
		tracker.close();

		// Clear the cache
		MinifyCacheHandler.INSTANCE.removeAll();

		unregisterServices(context);

	}
}

