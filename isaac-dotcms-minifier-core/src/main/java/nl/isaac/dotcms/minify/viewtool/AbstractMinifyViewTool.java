package nl.isaac.dotcms.minify.viewtool;

/*
 * Dotcms minifier by ISAAC is licensed under a
 * Creative Commons Attribution 3.0 Unported License
 *
 * - http://creativecommons.org/licenses/by/3.0/
 *
 * ISAAC Software Solutions B.V. (http://www.isaac.nl)
 */

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Adler32;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.tools.view.context.ViewContext;

import com.dotmarketing.beans.Host;
import com.dotmarketing.portlets.fileassets.business.FileAsset;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import nl.isaac.dotcms.minify.shared.FileTools;
import nl.isaac.dotcms.minify.shared.HostTools;
import nl.isaac.dotcms.minify.util.ParamValidationUtil;
import nl.isaac.dotcms.minify.util.StringListUtil;

/**
 * Abstract class that contains the generic functionality for other Minifier
 * Viewtools that extend this class.
 *
 * @author Koen Peters, ISAAC
 * @author Xander Steinmann, ISAAC
 **/

public abstract class AbstractMinifyViewTool {

	// We use a LinkedHashSet because the order in which the elements are added needs to be preserved
	protected Map<String, LinkedHashSet<FileAsset>> fileMap;
	protected Map<String, LinkedHashSet<String>> inlineMap;

	protected HttpServletRequest request;

	protected Host currentHost;

	/**
	 * The extension that should be used in the minify URL indicating the
	 * different file types. Should be set by the subclass.
	 */
	protected String minifyUrlExtension;

/**
	 * True if the request indicated debug mode, false otherwise
	 */
	protected boolean isDebugMode;

	/* True if we're in live mode based on the request, False otherwise.
	 */
	private boolean isLiveMode;

	/** A String that is used as part of the minify URL path so that the
	 * framework can recognize which requests are meant for the minification
	 * Servlet and which are not. It is also configured in the web.xml as the
	 * url-pattern.
	 */
	public static final String FILTER_PATTERN = ".minifier_filter_";

	/** A String that is used in debug mode to change the filename of the original
	 * files. It will be inserted into the filename together with a cache
	 * busting string. Using this pattern as a recognition string a rewrite rule
	 * can later remove it again so the file can be served.
	 */
	public static final String REWRITE_PATTERN = "minifier_rewrite_";

	/** A String that is used in debug mode to retrieve files from another host
	 */
	public static final String PROXY_SERVLET_URL = "/app/minifier/proxy/";


	// /////////////////
	// PUBLIC METHODS //
	// /////////////////

	/**
	 *
	 * @param filesOrGroups
	 *            A comma separated list of relative file uris or groups. Each
	 *            of the files should be retrievable by
	 *            {@link nl.isaac.dotcms.minify.shared.FileTools#getFileAssetByURI(String, Host, boolean)}
	 *            and the groups must have been created by calling
	 *            {@link #addFiles(String, String, Host)}.
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
	 *            (Optional) The dotCMS host that will be used to retrieve the
	 *            files in fileOrGroups. This host will not be used for the
	 *            files in the groups because they have been added with their
	 *            own host. If no host is provided the current host is used. Use
	 *            null to skip this parameter.
	 *
	 * @return <p>
	 *         The URL that can be called by the browser that will return a
	 *         minified and combined version of the content of the files (and
	 *         the files in the groups) in "filesOrGroups".
	 *
	 *         <p>
	 *         The URL that is returned consists out of these parts:
	 *
	 *         <pre>
	 * {@code
	 * [domain]/[path]/[cache buster][FILTER_PATTERN][extension]?uris=[uris][debug mode]
	 * }
	 * </pre>
	 *
	 *         <ol>
	 *         <li>[domain]<br>
	 *         If the parameter "domain" is provided, [domain] will be
	 *         constructed as a protocol relative domain:
	 *         "//[the value of the provided domain]". If not, the [domain] part
	 *         will be "/" making the URL relative to the root of the website.
	 *
	 *         <li>[path]<br>
	 *         The path will be constructed by taking the path of the first file
	 *         in "filesOrGroups" and stripping away the file part. This means
	 *         that all other files in "filesOrGroups" will have to be written
	 *         in such as way that references to other resources from the
	 *         content of those files are relative to that same path. For
	 *         instance all references to background images in CSS files must be
	 *         relative to the same path, or else they won't be found.
	 *
	 *         <li>[cache buster]<br>
	 *         This is the result of an Adler32 checksum of all the modification
	 *         dates of all the files included in filesOrGroups. If one of the
	 *         files changes its modification date, this checksum will also
	 *         change, resulting in a different URL, forcing the browser to
	 *         reload. We do not use a http query parameter for this because
	 *         some proxies don't included them in their cache key.
	 *
	 *         <li>[FILTER_PATTERN]<br>
	 *         The value of "FILTER_PATTERN" as defined in this class. It is
	 *         used by the Minify filter to recognize which requests the Minify
	 *         frameworks needs to handle. So never use this pattern in any of
	 *         your own .css or .js files because the minifier will handle it as
	 *         a minifier servlet
	 *
	 *         <li>[extension]<br>
	 *         The extension of the type of files that are being minified, e.g.
	 *         css or js.
	 *
	 *         <li>[uris]<br>
	 *         The list of all files in "filesOrGroups"separated by commas. The
	 *         groups are of course translated to the files that contains them.
	 *
	 *         <li>[debug mode]<br>
	 *         If in debug mode [debug mode] will be "&debug=true", otherwise it
	 *         is empty. Requesting the URL the server will respond with the
	 *         original version of the files in [uris], not the minified
	 *         versions. The files will still be combined into one big file by
	 *         the server, just not minified. We are in debug mode if the URL
	 *         that was used to do this request has a parameter with the name
	 *         "debug", so the value of that parameter does not really matter.
	 *         </ol>
	 */
	public String toUrl(String filesOrGroups, String domain, Host host) {

		Host cleanHost = UtilMethods.isSet(host) ? host : HostTools.getCurrentHost(request);
		Set<FileAsset> fileAssets = getFileUriSet(filesOrGroups, cleanHost);

		if (fileAssets.isEmpty()) {
			Logger.debug(this, "no files for " + filesOrGroups);
			return "";
		}

		String cleanDomain = UtilMethods.isSet(domain) ? "//" + domain : "";
		return getMinifierUrl(fileAssets, cleanDomain);
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toUrl(String, String, Host)}, but with
	 * domain and host defaulted to null.
	 */
	public String toUrl(String filesOrGroups) {
		return toUrl(filesOrGroups, null, null);
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toUrl(String, String, Host)}, but with
	 * host defaulted to null.
	 */
	public String toUrl(String filesOrGroups, String domain) {
		return toUrl(filesOrGroups, domain, null);
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #toUrl(String, String, Host)}, but with
	 * domain defaulted to null.
	 */
	public String toUrl(String filesOrGroups, Host host) {
		return toUrl(filesOrGroups, null, host);
	}

	/**
	 * Removes all files and inline code that have been added so far.
	 */
	public void reset() {
		fileMap = new HashMap<>();
		inlineMap = new HashMap<>();
	}

	/**
	 * Adds the given uris to the given group using the given dotCMS host as the host the uris should be looked up
	 *
	 * @param fileUris
	 *            The fileUris to add, separated by a comma. For instance:
	 *            "/path/to/file.js,/path/to/another/file.js".
	 *
	 * @param group
	 *            The group to add the file to.
	 *
	 * @param host
	 *            (Optional) The dotCMS host that will be used to retrieve the
	 *            filesUris. If no host is provided the current host is used. Use
	 *            null to skip this parameter.
	 *
	 */
	public void addFiles(String fileUris, String group, Host host) {
		ParamValidationUtil.validateParamNotNull(fileUris, "fileUris");
		ParamValidationUtil.validateParamNotNull(group, "group");

		Host cleanHost = UtilMethods.isSet(host) ? host : HostTools.getCurrentHost(request);

		for (String fileUri : StringListUtil.getCleanStringList(fileUris)) {
			FileAsset fileAsset = FileTools.getFileAssetByURI(fileUri, cleanHost, isLiveMode);

			if (fileAsset != null) {
				if (!fileMap.containsKey(group)) {
					fileMap.put(group, new LinkedHashSet<FileAsset>());
				}

				fileMap.get(group).add(fileAsset);
			}
		}
	}

	/**
	 * Convenience method with the same functionality as
	 * {@link #addFiles(String, String, Host)}, but with
	 * domain defaulted to null.
	 */
	public void addFiles(String fileUris, String group) {
		addFiles(fileUris, group, null);
	}

	/**
	 * @param groups
	 *            The groups to get the files from separated by commas.
	 *
	 * @return Set of FileAsset objects that were added to any of the given
	 *         groups.
	 */
	public Set<FileAsset> getFiles(String groups) {
		ParamValidationUtil.validateParamNotNull(groups, "groups");

		List<String> cleangroups = StringListUtil.getCleanStringList(groups);

		Set<FileAsset> result = new LinkedHashSet<>();
		for (String group : cleangroups) {
			if (fileMap.containsKey(group)) {
				result.addAll(fileMap.get(group));
			} else {
				Logger.debug(AbstractMinifyViewTool.class, "Can't find group '" + group + "'");
			}
		}

		return result;
	}

	/**
	 * @return A Set of all created groups used for storing fileUris.
	 */
	public Set<String> getFileGroups() {
		return fileMap.keySet();
	}

	/**
	 * Adds inline code to the given group.
	 *
	 * @param inlineCode
	 *            The inline code to add.
	 *
	 * @param group
	 *            The group to add the inline code to.
	 */
	public void addInlines(String inlineCode, String group) {
		ParamValidationUtil.validateParamNotNull(inlineCode, "inlineCode");
		ParamValidationUtil.validateParamNotNull(group, "group");

		if (!inlineMap.containsKey(group)) {
			inlineMap.put(group, new LinkedHashSet<String>());
		}

		inlineMap.get(group).add(inlineCode);
	}

	/**
	 * @param groups
	 *            The groups to get the inline code from, separated by commas.
	 *
	 * @return A Set of all inline code that was added to the given group.
	 */
	public Set<String> getInlines(String groups) {
		ParamValidationUtil.validateParamNotNull(groups, "groups");

		List<String> cleangroups = StringListUtil.getCleanStringList(groups);

		Set<String> result = new LinkedHashSet<>();
		for (String group : cleangroups) {

			if (inlineMap.containsKey(group)) {
				result.addAll(inlineMap.get(group));
			} else {
				Logger.debug(AbstractMinifyViewTool.class, "Can't find group '" + group + "'");
			}
		}

		return result;
	}

	/**
	 * @return A Set of all created groups for storing inline code.
	 */
	public Set<String> getInlineGroups() {
		return inlineMap.keySet();

	}

	/**
	 * @param hostNameOrAlias
	 *            The name or the alias of one of the hosts on this dotCMS
	 *            server
	 * @return The host or the alias that has the given string as its hostName
	 *         or Alias.
	 */
	public Host getHost(String hostNameOrAlias) {
		return HostTools.getHostByDomainName(hostNameOrAlias);
	}

	// ////////////////////
	// PROTECTED METHODS //
	// ////////////////////

	/**
	 * Initialization method meant to be called by the init methods of the
	 * viewtools that extend this abstract class.
	 *
	 * @param initData
	 *            The initData object that is given as a parameter to the init
	 *            method of the extending viewtools that call this method.
	 * @param servletExtension
	 *            A string indicating the extension (.js or .css) that we should
	 *            use for Servlet.
	 */
	protected void init(Object initData, String servletExtension) {
		ParamValidationUtil.validateParamNotNull(initData, "initData");
		ParamValidationUtil.validateParamNotNull(servletExtension, "servletExtension");

		request = ((ViewContext) initData).getRequest();
		reset();
		this.minifyUrlExtension = servletExtension;
		this.isDebugMode = UtilMethods.isSet(request.getParameter("debug"));
		this.isLiveMode = HostTools.isLiveMode(request);
		this.currentHost = HostTools.getCurrentHost(request);
	}

	/**
	 * @param fileAssets
	 *            A non empty Set containing FileAssets that we want to include
	 *            in the minify URL.
	 *
	 * @param domain
	 *            The domain that should be used as the domain for the URL. If
	 *            it is an empty string the URL will be made relative.
	 *
	 * @return The URL that can be called by the browser that will return a
	 *         minified and combined version of the content of the files in
	 *         "fileUris". If a domain is given it will be used to make the URL
	 *         absolute and protocol relative. If no domain is given the
	 *         returned URL will be relative to the root of the site.
	 */
	protected String getMinifierUrl(Set<FileAsset> fileAssets, String domain) {
		ParamValidationUtil.validateParamNotNull(fileAssets, "fileAssets");
		ParamValidationUtil.validateParamMinCollectionSize(fileAssets, 1, "fileAssets");
		ParamValidationUtil.validateParamNotNull(domain, "domain");

		FileAsset firstFileUri = fileAssets.iterator().next();
		String basePath = firstFileUri.getPath();
		String fileUrisString = fileAssetsToCsvUris(fileAssets);
		String cacheBuster = createCacheBuster(fileAssets);

		return domain + basePath + cacheBuster + FILTER_PATTERN
				+ minifyUrlExtension + "?uris=" + fileUrisString;
	}

	protected String getMinifierDebugUrl(FileAsset fileAsset, Host host, String domain) {
		ParamValidationUtil.validateParamNotNull(fileAsset, "fileAsset");

		String basePath = fileAsset.getPath();
		String fullPath = basePath + fileAsset.getFileName();

		String debugUrl;
		if (host == null) {

			// if the file is on the same host, just use the file's URL
			debugUrl = fullPath;

		} else {

			// if the file is on another host, we have to use a proxy servlet
			// to get the raw file contents

			String hostname;
			try {
				hostname = URLEncoder.encode(host.getHostname(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}

			debugUrl = PROXY_SERVLET_URL + hostname + fullPath;
		}

		// if needed, set the domain in the debug url
		if (UtilMethods.isSet(domain)) {
			debugUrl = "//" + domain + debugUrl;
		}

		return debugUrl;
	}

	/**
	 * @param fileUrisOrGroups
	 *            A comma separated list of relative file uris or groups. Each
	 *            of the files should be retrievable by
	 *            {@link nl.isaac.dotcms.minify.shared.FileTools#getFileAssetByURI(String, Host, boolean)}
	 *            and the groups must have been created by calling
	 *            {@link #addFiles(String, String, Host)}.
	 *
	 * @param host
	 *            The host that will be used to retrieve the files in
	 *            fileOrGroups. This host will not be used for the files in the
	 *            groups because they have been added with their own host.
	 *
	 * @return A Set containing all FileAsset objects in the given
	 *         "fileUrisAndGroups". All groups are exploded to the FilesAsset
	 *         objects they contain.
	 */
	protected Set<FileAsset> getFileUriSet(String fileUrisOrGroups, Host host) {

		Set<FileAsset> fileUriList = new LinkedHashSet<>();

		if (UtilMethods.isSet(fileUrisOrGroups)) {

			for (String fileUriOrGroup : StringListUtil.getCleanStringList(fileUrisOrGroups)) {

				if (fileMap.containsKey(fileUriOrGroup)) {
					// this is a group
					fileUriList.addAll(getFiles(fileUriOrGroup));

				} else if (minifyUrlExtension.equalsIgnoreCase(getFileExtension(fileUriOrGroup))) {
					// this is a file of the right type
					FileAsset file = FileTools.getFileAssetByURI(fileUriOrGroup, host, isLiveMode);
					if (file != null) {
						fileUriList.add(file);
					}

				} else {
					// It's not a group, but also not a recognized file.
					Logger.debug(this, "FileUri " + fileUriOrGroup
							+ " does not have expected extension "
							+ minifyUrlExtension);
				}
			}
		}

		return fileUriList;
	}

	/**
	 * @param fileAsset
	 *            A non null FileAsset
	 *
	 * @return The URI of the FileAsset object with a cachebuster added to the
	 *         filename. This cachebuster will be removed by a rewrite rule also
	 *         included in the Minifier framework so it will not result in a 404
	 */
	protected String addCacheBusterToFile(FileAsset fileAsset) {
		ParamValidationUtil.validateParamNotNull(fileAsset, "fileAsset");

		Collection<FileAsset> asList = Collections.singleton(fileAsset);

		// Check of this fileAsset is located on a different host. If so: use that domain to fetch it.
		String hostName = "";
		if (!fileAsset.getHost().equals(currentHost.getIdentifier())) {
			Host host = HostTools.getHostByIdentifier(fileAsset.getHost());
			hostName = "//" + host.getHostname();
		}

		return hostName + fileAsset.getPath() + fileAsset.getFileName() + "?_=" + createCacheBuster(asList);
	}

	// //////////////////
	// PRIVATE METHODS //
	// //////////////////

	/**
	 *
	 * @param fileAssets
	 *            A Set containing string representing paths to files on the
	 *            given host, eg. "/path/to/file.css". No groups are allowed
	 *            here.
	 * @return An Andler32 checksum of all the modDates of the given files.
	 */
	private String createCacheBuster(Collection<FileAsset> fileAssets) {
		ParamValidationUtil.validateParamNotNull(fileAssets, "fileAssets");

		String modDates = "";

		for (FileAsset fileUri : fileAssets) {
			if (fileUri.getModDate() != null) {
				long modDate = fileUri.getModDate().getTime();
				modDates += "_" + modDate;
			}
		}

		// Since the modDates String can be very long, we create a much shorter
		// Adler32 checksum string. We do not use a hash here because checksums
		// are faster (and simpler).
		Adler32 adler = new Adler32();
		byte[] bytes = modDates.getBytes();
		adler.update(bytes);
		return "" + adler.getValue();
	}

	/**
	 * @param fileUri
	 *            The representation of the path and filename in a string, eg:
	 *            "path/to/file.js"
	 *
	 * @return the extension of the file represented in the string, eg "js".
	 */
	private String getFileExtension(String fileUri) {
		if (fileUri.contains(".")) {
			return fileUri.substring(fileUri.lastIndexOf(".") + 1);
		}

		return null;
	}

	/**
	 * @param fileAssets
	 *            A Set of non null FileAsset objects that will be included in
	 *            the string
	 *
	 * @return A string containing relative paths to all the files in the given
	 *         set separated by commas
	 */
	private String fileAssetsToCsvUris(Set<FileAsset> fileAssets) {
		StringBuilder sb = new StringBuilder();

		for (FileAsset fileAsset : fileAssets) {

			// Check of this fileAsset is located on a different host. If so: use that domain to fetch it.
			String hostName = "";
			if (!fileAsset.getHost().equals(currentHost.getIdentifier())) {
				Host host = HostTools.getHostByIdentifier(fileAsset.getHost());
				hostName = "//" + host.getHostname();
			}
			sb.append(sb.length() > 0? ",": "").append(hostName).append(fileAsset.getPath()).append(fileAsset.getFileName());
		}

		try {
			return URLEncoder.encode(sb.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}