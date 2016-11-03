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
 * Viewtool that provides functionality to minify and combine JS files.
 *
 * @see http://www.geekyplugins.nl for more information on how to use this plugin.
 *
 * @author Koen Peters, ISAAC
 * @author Xander Steinmann, ISAAC
 */
public final class JsMinifyViewTool extends AbstractMinifyViewTool implements ViewTool {


	@Override
	public void init(Object initData) {
		init(initData, "js");
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
	 * @param cookieOptInLevel
	 *            (Optional) the cookie acceptance level that these JS files
	 *            require to run. Use null or "" to skip this parameter.
	 *
	 * @return <p>
	 *         If not in debug mode: one HTML script tag that links to the
	 *         minify servlet that will combine and minify the JS files in
	 *         "filesOrGroups". The URL used for the src attribute is
	 *         constructed using the {@link #toUrl(String, String, Host)}
	 *         method. Also see that method for how the "domain" and "host"
	 *         parameters are used.
	 *
	 *         <p>
	 *         If in debug mode: a set of script tags (one for each referenced
	 *         JS file in "fileOrGroups") that directly links to the given JS
	 *         file. In this case nothing will be minified or combined. We are
	 *         in debug mode if the URL that was used to do this request has a
	 *         parameter with the name "debug". It's value does not matter.
	 *
	 *         <p>
	 *         If "scriptClearanceLevel" is provided the script tag that is
	 *         returned has some changes:
	 *
	 *         <ul>
	 *         <li>It has an extra attribute: "data-ics-level" that has the
	 *         given level as its value
	 *
	 *         <li>It has "type=text/plain" instead of "type=text/javascript"
	 *
	 *         <li>Its "src" tag is replaced by an "data-ics-src" tag. Under
	 *         European laws some script can only be run in the browser if the
	 *         user explicitly white lists them. To prevent the running of these
	 *         scripts until the user whitelists their type (or alternatively
	 *         called: level) this alternative src attribute will prevent the
	 *         browser from doing so. Some additional client side JS can decide
	 *         afterwards if it needs to change the data-ics-src to src so the
	 *         script is loaded after all.
	 *         </ul>
	 *
	 *         <p>
	 *         Important to know is that all relative references inside of the
	 *         files (such as references to other JS files from within one of
	 *         the JS files) will be relative from the location of the first
	 *         file in "filesOrGroups". So do not mix files that are located in
	 *         different folders.
	 *
	 */
	public String toScriptTag(String filesOrGroups, String domain, Host host, String cookieOptInLevel) {

		Host cleanHost = UtilMethods.isSet(host)? host: currentHost;
		Set<FileAsset> fileAssets = getFileUriSet(filesOrGroups, cleanHost);

		if (fileAssets.isEmpty()) {
			Logger.debug(this, "no JS files for " + filesOrGroups);
			return "";
		}

		String cleanDomain = UtilMethods.isSet(domain)? "//" + domain: "";
		StringBuilder result = new StringBuilder();

		if (isDebugMode) {

			for (FileAsset file: fileAssets) {

	      		if (UtilMethods.isSet(cookieOptInLevel)) {
	      			String cleanLevel = UtilMethods.isSet(cookieOptInLevel)? cookieOptInLevel: "";
	      			result	.append("<script type=\"text/plain\"")
	      					.append(" data-ics-level=\"").append(cleanLevel).append("\"")
	      					.append(" data-ics-src=\"").append(getMinifierDebugUrl(file, host, cleanDomain)).append("\">")
	      					.append("</script>\n");
	      		} else {
	      			result	.append("<script type=\"text/javascript\"")
		      				.append(" src=\"").append(getMinifierDebugUrl(file, host, cleanDomain)).append("\">")
		      				.append("</script>\n");
	      		}

	      	}
		} else {

		    if (UtilMethods.isSet(cookieOptInLevel)) {
		    	String cleanLevel = UtilMethods.isSet(cookieOptInLevel)? cookieOptInLevel: "";
      			result	.append("<script type=\"text/plain\"")
		      			.append(" data-ics-level=\"").append(cleanLevel).append("\"")
		      			.append(" data-ics-src=\"").append(getMinifierUrl(fileAssets, cleanDomain)).append("\">")
		      			.append("</script>");
		    } else {
		    	result	.append("<script type=\"text/javascript\"")
				    	.append(" src=\"").append(getMinifierUrl(fileAssets, cleanDomain)).append( "\">")
				    	.append("</script>");
		    }
		}

		return result.toString();
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toScriptTag(String, String, String, Host)}, but with
	 * domain, host and cookieOptInLevel defaulted to null.
	 */
	public String toScriptTag(String filesOrGroups) {
		return toScriptTag(filesOrGroups, null, null, null);
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toScriptTag(String, String, String, Host)}, but with
	 * host and  cookieOptInLevel defaulted to null.
	 */
	public String toScriptTag(String filesOrGroups, String domain) {
		return toScriptTag(filesOrGroups, domain, null, null);
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toScriptTag(String, String, String, Host)}, but with
	 * domain and cookieOptInLevel defaulted to null.
	 */
	public String toScriptTag(String filesOrGroups, Host host) {
		return toScriptTag(filesOrGroups, null, host, null);
	}
}