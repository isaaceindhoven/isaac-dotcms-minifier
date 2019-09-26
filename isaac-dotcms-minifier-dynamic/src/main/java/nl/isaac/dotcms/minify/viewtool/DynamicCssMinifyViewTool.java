package nl.isaac.dotcms.minify.viewtool;

import nl.isaac.dotcms.minify.conf.Configuration;

public final class DynamicCssMinifyViewTool extends CssMinifyViewTool {

	@Override
	public String getProxyServletUrl(String hostname, String fullPath) {
		return Configuration.getProxyServletUrl(hostname, fullPath);
	}

}
