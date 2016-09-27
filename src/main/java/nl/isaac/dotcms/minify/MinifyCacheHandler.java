package nl.isaac.dotcms.minify;

/*
 * Dotcms minifier by ISAAC is licensed under a 
 * Creative Commons Attribution 3.0 Unported License
 * 
 * - http://creativecommons.org/licenses/by/3.0/
 * - http://www.geekyplugins.com/
 * 
 * ISAAC Software Solutions B.V. (http://www.isaac.nl)
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import nl.isaac.dotcms.minify.exception.DotCMSFileNotFoundException;
import nl.isaac.dotcms.minify.shared.FileTools;
import nl.isaac.dotcms.minify.shared.HostTools;
import nl.isaac.dotcms.minify.util.ParamValidationUtil;

import org.apache.commons.io.IOUtils;

import com.dotmarketing.beans.Host;
import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.business.DotCacheAdministrator;
import com.dotmarketing.business.DotCacheException;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.portlets.fileassets.business.FileAsset;
import com.dotmarketing.util.Logger;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.yahoo.platform.yui.compressor.CssCompressor;

/**
 * The Singleton that handles the cache of the minified files. 
 * 
 * @author Koen Peters, ISAAC
 * @author Xander Steinmann, ISAAC
 */
public enum MinifyCacheHandler {

	/* 
	 * The single instance, making this class a singleton 
	 */
	INSTANCE;

	/**
	 * The name of the dotCMS cache group that is used for storing the cache
	 */
	public static final String CACHE_GROUP_NAME = "ISAAC_MinifyCache";

	/**
	 * @param key
	 *            A non null MinifyCacheKey instance that will be used to fetch the MinifyCacheFile from the cache
	 * @return The MinifyCacheFile that is related to the given key. If it was
	 *         not already in the cache it was added.
	 */
	public MinifyCacheFile get(MinifyCacheKey key) {
		ParamValidationUtil.validateParamNotNull(key, "key");

		DotCacheAdministrator cache = CacheLocator.getCacheAdministrator();
		Object o = null;

		// First we try to get the item from the cache
		try {
			o = cache.get(key.toString(), CACHE_GROUP_NAME);
		} catch (DotCacheException e) {
			Logger.info(this.getClass(), String.format("DotCacheException for Group '%s', key '%s', message: %s", CACHE_GROUP_NAME, key.toString(), e.getMessage()));
		}

		// If the cache did not have the item we look it up and add it to the cache
		// And in the case the cache contains values of a previous version of the Minifier plugin,
		// then the type of those values will not be compatible with our current types
		if(o == null && !(o instanceof MinifyCacheFile)) {
			MinifyCacheFile minifyCacheFile = loadFile(key);
			put(key, minifyCacheFile);
			return minifyCacheFile;
		} else {
			return (MinifyCacheFile)o;
		}
	}

	/**
	 * Stores the given MinifyCacheFile in the cache under the given
	 * MinifyCacheKey. Overwrites the old values if they already existed.
	 * 
	 * @param key
	 *            A non null MinifyCacheKey
	 * @param value
	 *            A non null MinifyCacheFile
	 */
	public void put(MinifyCacheKey key, MinifyCacheFile value) {
		ParamValidationUtil.validateParamNotNull(key, "key");
		ParamValidationUtil.validateParamNotNull(value, "value");

		DotCacheAdministrator cache = CacheLocator.getCacheAdministrator();
		cache.put(key.toString(), value, CACHE_GROUP_NAME);
	}

	/**
	 * Removes the entry with the given MinifyCacheKey if it exists.
	 * 
	 * @param key
	 *            A non null MinifyCacheKey
	 */
	public void remove(MinifyCacheKey key) {
		ParamValidationUtil.validateParamNotNull(key, "key");

		DotCacheAdministrator cache = CacheLocator.getCacheAdministrator();
		cache.remove(key.toString(), CACHE_GROUP_NAME);
	}

	/**
	 * Clears the entire cache.
	 */
	public void removeAll() {
		DotCacheAdministrator cache = CacheLocator.getCacheAdministrator();
		cache.flushGroup(CACHE_GROUP_NAME);
	}

	/*
	 * Loads the file from disk, tries to minify it, and returns the result as an MinifyCacheFile. 
	 */
	private MinifyCacheFile loadFile(MinifyCacheKey key) {
		String result = null;
		FileAsset file = getFile(key);

		try {

			InputStream input = file.getFileInputStream();
			try {
				if (file.getFileName().contains(".min") || file.getFileName().equalsIgnoreCase("jquery.js")) {
					Logger.info(MinifyCacheHandler.class, "Skipping minification of file: " +  key.toString() + ". Filename contains '.min' or is 'jquery.js', so " +
							"we assume the file is already minified.");

				} else if (file.getExtension().equalsIgnoreCase("css")) {
					Logger.info(MinifyCacheHandler.class, "Compressing css file: " + key.toString());
					Reader reader = new InputStreamReader(input);
					CssCompressor cssCompressor = new CssCompressor(reader);
					StringWriter writer = new StringWriter();
					cssCompressor.compress(writer, 80);
					result = writer.getBuffer().toString();
					reader.close();

				} else if (file.getExtension().equalsIgnoreCase("js")) {
					Logger.info(MinifyCacheHandler.class, "Compressing js file: " + key.toString());
					Compiler compiler = new Compiler();
					CompilerOptions options = new CompilerOptions();
					CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);

					List<SourceFile> jsFiles = new LinkedList<SourceFile>();
					jsFiles.add(SourceFile.fromInputStream(file.getFileName(), input));
					List<SourceFile> externalJsfiles = new LinkedList<SourceFile>();
					compiler.compile(externalJsfiles, jsFiles, options);
					result = compiler.toSource();

				} else {
					throw new RuntimeException("Uncompressable file extension: " + file.getExtension());
				}
			} catch (IOException e) {
				Logger.warn(MinifyCacheHandler.class, "Can't compress file " + key.toString());
			} finally {
				input.close();
			}
		} catch (IOException e) { 
			Logger.warn(MinifyCacheHandler.class, "Can't compress file, problem with input stream: " + key.toString());
		}

		// If there's no result, get the non-minified data
		if (result == null || result.trim().isEmpty()) {
			Logger.info(MinifyCacheHandler.class, "Nothing was minified, so using raw file: " + key.toString());
			try {
				result = new String(IOUtils.toByteArray(file.getFileInputStream()), "UTF-8");
			} catch (Throwable t) {
				throw new RuntimeException("Can't store file in cache: " + key.toString(), t);
			}
		}

		try {
			return new MinifyCacheFile(result, file.getModDate());
		} catch (DotStateException e) {
			throw new RuntimeException(e);
		}
	}

	private FileAsset getFile(MinifyCacheKey key) throws DotCMSFileNotFoundException {

		Host host = HostTools.getHostByDomainName(key.getHostName());
		try {
			FileAsset file = FileTools.getFileAssetByURI(key.getUriPath(), host, key.getLive());
			if (file == null || file.getURI() == null) {
				Logger.error(MinifyCacheHandler.class, "Can't find file for key: " + key.toString());

				if (key.getUriPath().startsWith("dotcms")) {
					Logger.warn(this.getClass(), "A filename may not start with 'dotcms'... This is a dotCMS issue!");
				}
				throw new DotCMSFileNotFoundException("File with uri " + key.getUriPath() + " not found");
			}
			return file;
		} catch (DotDataException ex) {
			throw new RuntimeException(ex);
		}
	}

}