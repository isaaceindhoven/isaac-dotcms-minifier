package nl.isaac.dotcms.minify.viewtool;

/*
 * Dotcms minifier by ISAAC is licensed under a
 * Creative Commons Attribution 3.0 Unported License
 *
 * - http://creativecommons.org/licenses/by/3.0/
 * - http://www.geekyplugins.com/
 *
 * ISAAC Software Solutions B.V. (http://www.isaac.nl)
 */

import java.util.Set;

import org.apache.velocity.tools.view.tools.ViewTool;

import com.dotmarketing.beans.Host;
import com.dotmarketing.portlets.fileassets.business.FileAsset;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

/**
 * Viewtool that provides functionality to minify and combine CSS files.
 *
 * @see http://www.geekyplugins.nl for more information on how to use this plugin.
 *
 * @author Koen Peters, ISAAC
 * @author Xander Steinmann, ISAAC
 */
public final class CssMinifyViewTool extends AbstractMinifyViewTool implements ViewTool {


	@Override
	public void init(Object initData) {
		init(initData, "css");
	}

	/**
	 * @param filesOrGroups
	 *            A comma separated list of file paths, or groups. Each of the
	 *            files should be retrievable by FileTools.getFileAssetByURI and
	 *            the groups should have been created using methods such as
	 *            {@link #addFile(String, String)}.
	 *
	 * @param domain
	 *            (Optional) The domain that should be used as the domain for
	 *            the minifier URL. If no domain is provided the URL will be
	 *            made relative. Setting this does not mean that the files in
	 *            filesOrGroups will be retrieved from that server and minified,
	 *            it just means that the domain of the generated Minify URL will
	 *            point to that server. (For security reasons the Minifier does
	 *            not load and minify files that are not located on one of the
	 *            hosts of this dotCMS instance.). Setting this parameter is
	 *            useful for things like using CDNs to host your content. Use
	 *            null or "" to skip this parameter.
	 *
	 * @param host
	 *            (Optional) The host where the given files can be found. Use
	 *            null to skip this parameter.
	 *
	 * @param extraAttributes
	 *            (Optional) A string containing additional attributes that need
	 *            to be added to the &lt;link&gt; tags, such as "media=print".
	 *            Use null or "" to skip this parameter.
	 *
	 * @return <p>
	 *         If not in debug mode: one HTML &lt;link&gt; tag that links to the
	 *         minify servlet that will combine and minify the CSS files in
	 *         "filesOrGroups". The URL used for the href attribute is
	 *         constructed using the {@link #toUrl(String, String, Host)}
	 *         method. Also see that method for how the "domain" and "host"
	 *         parameters are used.
	 *
	 *         <p>
	 *         If in debug mode: a set of link tags (one for each referenced CSS
	 *         file in "fileOrGroups") that directly links to the given CSS
	 *         file. In this case nothing will be minified or combined. We are
	 *         in debug mode if the URL that was used to do this request has a
	 *         parameter with the name "debug". It's value does not matter.
	 *
	 *         <p>
	 *         If "extraAttributes" is provided its value will be included
	 *         inside the &lt;link&gt; tags. This can be handy in the case of
	 *         additional information such as "media=print".
	 *
	 *         <p>
	 *         Important to know is that all relative references inside of the
	 *         files (such as references to images inside a CSS file) will be
	 *         relative from the location of the first file in "filesOrGroups".
	 *         So do not mix files that are located in different folders.
	 *
	 */
	public String toLinkTag(String filesOrGroups, String domain, Host host, String extraAttributes) {

		Host cleanHost = UtilMethods.isSet(host)? host: currentHost;
		Set<FileAsset> filesAssets = getFileUriSet(filesOrGroups, cleanHost);

		// It could be that no files need to be minified, in that case we just skip processing
		if (filesAssets.isEmpty()) {
			Logger.warn(this, "no CSS files for " + filesOrGroups);
			return "";
		}

		String cleanDomain = UtilMethods.isSet(domain)? "//" + domain: "";
		String extraAttrs = UtilMethods.isSet(extraAttributes)? extraAttributes: "";
		StringBuilder result = new StringBuilder();

		if (isDebugMode) {


 	      	for (FileAsset file: filesAssets) {
	      		result	.append("<link rel=\"stylesheet\"")
			      		.append(" type=\"text/css\"")
			      		.append(" href=\"").append(cleanDomain).append(getMinifierDebugUrl(file, host, cleanDomain)).append("\" ").append(extraAttrs)
			      		.append("/>\n");
	      	}

		} else {

			result	.append("<link rel=\"stylesheet\"")
					.append(" type=\"text/css\"")
					.append(" href=\"").append(getMinifierUrl(filesAssets, cleanDomain)).append("\" ").append(extraAttrs)
					.append("/>");
		}

		return result.toString();
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toLinkTag(String, String, String, Host)}, but with
	 * domain, host and extraAttributes defaulted to null.
	 */
	public String toLinkTag(String filesOrGroups) {
		return toLinkTag(filesOrGroups, null, null, null);
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toLinkTag(String, String, String, Host)}, but with
	 * host and extraAttributes defaulted to null.
	 */
	public String toLinkTag(String filesOrGroups, String domain) {
		return toLinkTag(filesOrGroups, domain, null, null);
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toLinkTag(String, String, String, Host)}, but with
	 * domain and extraAttributes defaulted to null.
	 */
	public String toLinkTag(String filesOrGroups, Host host) {
		return toLinkTag(filesOrGroups, null, host, null);
	}
}