package nl.isaac.dotcms.minify.viewtool;

import java.util.Set;

import org.apache.velocity.tools.view.tools.ViewTool;

import com.dotmarketing.beans.Host;
import com.dotmarketing.portlets.fileassets.business.FileAsset;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.google.javascript.jscomp.CompilationLevel;

import nl.isaac.dotcms.minify.api.MinifierAPI;

/**
 * Viewtool that provides functionality to minify and combine JS files.
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
	 * @param extraAttributes
	 *            (Optional) extra attributes/HTML that you can add to the generated
	 *            SCRIPT tag. Examples of these are "defer" and "async". Use ""
	 *            to skip this parameter.
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
	public String toScriptTag(String filesOrGroups, String domain, Host host, String cookieOptInLevel, String extraAttributes) {

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
	      					.append(" data-ics-src=\"").append(getMinifierDebugUrl(file, host, cleanDomain)).append("\" ")
	      					.append(extraAttributes)
	      					.append(">")
	      					.append("</script>\n");
	      		} else {
	      			result	.append("<script type=\"text/javascript\"")
		      				.append(" src=\"").append(getMinifierDebugUrl(file, host, cleanDomain)).append("\" ")
	      					.append(extraAttributes)
	      					.append(">")
		      				.append("</script>\n");
	      		}
	      	}
		} else {

		    if (UtilMethods.isSet(cookieOptInLevel)) {
		    	String cleanLevel = UtilMethods.isSet(cookieOptInLevel)? cookieOptInLevel: "";
      			result	.append("<script type=\"text/plain\"")
		      			.append(" data-ics-level=\"").append(cleanLevel).append("\"")
		      			.append(" data-ics-src=\"").append(getMinifierUrl(fileAssets, cleanDomain)).append("\" ")
      					.append(extraAttributes)
      					.append(">")
		      			.append("</script>");
		    } else {
		    	result	.append("<script type=\"text/javascript\"")
				    	.append(" src=\"").append(getMinifierUrl(fileAssets, cleanDomain)).append( "\" ")
      					.append(extraAttributes)
      					.append(">")
				    	.append("</script>");
		    }
		}

		return result.toString();
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toScriptTag(String, String, Host, String, String)}, but with
	 * domain, host and cookieOptInLevel defaulted to null.
	 */
	public String toScriptTag(String filesOrGroups) {
		return toScriptTag(filesOrGroups, null, null, null, "");
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toScriptTag(String)}, but with extraAttributes added.
	 */
	public String toCustomScriptTag(String filesOrGroups, String extraAttributes) {
		return toScriptTag(filesOrGroups, null, null, null, extraAttributes);
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toScriptTag(String, String, Host, String, String)}, but with
	 * host and  cookieOptInLevel defaulted to null.
	 */
	public String toScriptTag(String filesOrGroups, String domain) {
		return toScriptTag(filesOrGroups, domain, null, null, "");
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toScriptTag(String, String)}, but with
	 * extraAttributes added.
	 */
	public String toCustomScriptTag(String filesOrGroups, String domain, String extraAttributes) {
		return toScriptTag(filesOrGroups, domain, null, null, extraAttributes);
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toScriptTag(String, String, Host, String, String)}, but with
	 * domain and cookieOptInLevel defaulted to null.
	 */
	public String toScriptTag(String filesOrGroups, Host host) {
		return toScriptTag(filesOrGroups, null, host, null, "");
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toScriptTag(String, Host)}, but with
	 * extraAttributes added.
	 */
	public String toCustomScriptTag(String filesOrGroups, Host host, String extraAttributes) {
		return toScriptTag(filesOrGroups, null, host, null, extraAttributes);
	}

	public String testWhitespaceJsMinifier() {
		MinifierAPI minAPI = new MinifierAPI();

		String whiteSpaceMinifiedJs1 = minAPI.getMinifiedJavascript(getJs1(), CompilationLevel.WHITESPACE_ONLY);
		String whiteSpaceMinifiedJs2 = minAPI.getMinifiedJavascript(getJs2(), CompilationLevel.WHITESPACE_ONLY);
		String actualWhiteSpaceMinifiedJs = whiteSpaceMinifiedJs1 + whiteSpaceMinifiedJs2;
		String expectedWhiteSpaceMinifiedJs = "var unusedVar;function helloWorld(){alert(\"Hello World!\")}helloWorld();function append(string,append){console.log(string+append)};";

		if (actualWhiteSpaceMinifiedJs.equals(expectedWhiteSpaceMinifiedJs)) {
			return actualWhiteSpaceMinifiedJs;
		}

		return null;
	}

	public String testSimpleJsMinifier() {
		MinifierAPI minAPI = new MinifierAPI();

		String simpleMinifiedJs1 = minAPI.getMinifiedJavascript(getJs1(), CompilationLevel.SIMPLE_OPTIMIZATIONS);
		String simpleMinifiedJs2 = minAPI.getMinifiedJavascript(getJs2(), CompilationLevel.SIMPLE_OPTIMIZATIONS);
		String actualSimpleMinifiedJs = simpleMinifiedJs1 + simpleMinifiedJs2;
		String expectedSimpleMinifiedJs = "var unusedVar;function helloWorld(){alert(\"Hello World!\")}helloWorld();function append(a,b){console.log(a+b)};";

		if (actualSimpleMinifiedJs.equals(expectedSimpleMinifiedJs)) {
			return actualSimpleMinifiedJs;
		}

		return null;
	}

	public String testAdvancedJsMinifier() {
		MinifierAPI minAPI = new MinifierAPI();

		String advancedMinifiedJs1 = minAPI.getMinifiedJavascript(getJs1(), CompilationLevel.ADVANCED_OPTIMIZATIONS);
		String advancedMinifiedJs2 = minAPI.getMinifiedJavascript(getJs2(), CompilationLevel.ADVANCED_OPTIMIZATIONS);
		String actualAdvancedMinifiedJs = advancedMinifiedJs1 + advancedMinifiedJs2;
		String expectedAdvancedMinifiedJs = "alert(\"Hello World!\");";

		if (actualAdvancedMinifiedJs.equals(expectedAdvancedMinifiedJs)) {
			return actualAdvancedMinifiedJs;
		}

		return null;
	}
	private String getJs1() {
		StringBuilder sb = new StringBuilder();
		sb.append("var unusedVar;");
		sb.append("\nfunction helloWorld() {");
		sb.append("\n\talert('Hello World!');");
		sb.append("\n}");
		sb.append("\n\n\thelloWorld();");
		return sb.toString();
	}

	private String getJs2() {
		StringBuilder sb = new StringBuilder();
		sb.append("function append(string, append) {");
		sb.append("\n\tconsole.log(string + append);");
		sb.append("\n}");
		return sb.toString();
	}

}