package nl.isaac.dotcms.minify.viewtool;

import nl.isaac.dotcms.minify.conf.Configuration;

public final class StaticJsMinifyViewTool extends JsMinifyViewTool {

	@Override
	public String getProxyServletUrl(String hostname, String fullPath) {
		return Configuration.getProxyServletUrl(hostname, fullPath);
	}



}
