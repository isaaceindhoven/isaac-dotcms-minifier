package nl.isaac.dotcms.minify;
/**
* dotCMS minifier by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
*/

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nl.isaac.dotcms.minify.shared.ItemHandler;

import com.dotmarketing.beans.Host;
import com.dotmarketing.factories.HostFactory;
import com.dotmarketing.portlets.files.factories.FileFactory;
import com.dotmarketing.portlets.files.model.File;
import com.dotmarketing.util.Logger;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.yahoo.platform.yui.compressor.CssCompressor;
/**
 * The class that defines the methods that collect new data from dotCMS.
 * It includes a class to get a single file and to get the initial load.
 * 
 * @author Xander
 *
 */
public class MinifyCacheItemHandler implements ItemHandler<MinifyCacheFile> {
	
	public Map<String, MinifyCacheFile> getInitialCache() {
		return getAllFilesFromDotCMS(true);
	}
	
	/**
	 * Get the file that is identified by the given key and host.
	 * (The host is a parameter to avoid looking up the host twice) 
	 * @return the minified file
	 */
	private MinifyCacheFile get(MinifyCacheFileKey minifyCacheFileKey, Host host) {
		File file = FileFactory.getFileByURI(minifyCacheFileKey.getUri(), host, minifyCacheFileKey.getLive());
		return get(file, host);
	}
	
	/**
	 * Get the file that is identified by the given key (getKey() from a MinifyCacheFileKey)
	 */
	public MinifyCacheFile get(String key) {
		MinifyCacheFileKey minifyCacheFileKey = MinifyCacheFileKey.createInstanceWithKey(key);
		Host host = HostFactory.getHostByHostName(minifyCacheFileKey.getHostName());
		return get(minifyCacheFileKey, host);
	}
	
	/**
	 * Get a file from the host
	 */
	public static MinifyCacheFile get(File file, Host host) {
		MinifyCacheFileKey minifyCacheKey = new MinifyCacheFileKey(file.getURI(), file.isLive(), host);
		String result = null;
		try {
			InputStream input = new ByteArrayInputStream(FileFactory.getFileData(file));
			try {
				if(file.getFileName().contains(".min") || file.getFileName().equalsIgnoreCase("jquery.js")) {
					//not minifying already minified file to avoid parser errors
				} else if(file.getExtension().equalsIgnoreCase("css")) {
					Logger.info(MinifyCacheItemHandler.class, "Compressing file: " + minifyCacheKey.getReadableString());
					Reader reader = new InputStreamReader(input);
					CssCompressor cssCompressor = new CssCompressor(reader);
					StringWriter writer = new StringWriter();
					cssCompressor.compress(writer, 80);
					result = writer.getBuffer().toString();
					reader.close();
				} else if(file.getExtension().equalsIgnoreCase("js")) {
					Logger.info(MinifyCacheItemHandler.class, "Compressing file: " + minifyCacheKey.getReadableString());
					com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler();
					CompilerOptions options = new CompilerOptions();
					CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);
					
					List<JSSourceFile> jsFiles = new LinkedList<JSSourceFile>();
					jsFiles.add(JSSourceFile.fromInputStream(file.getFileName(), input));
					List<JSSourceFile> externalJsfiles = new LinkedList<JSSourceFile>();
					compiler.compile(externalJsfiles, jsFiles, options);
					result = compiler.toSource();
				} else {
					throw new RuntimeException("Uncompressable file extension: " + file.getExtension());
				}
			} catch (IOException ioe) {
				Logger.warn(MinifyCacheItemHandler.class, "Cant compress file " + minifyCacheKey.getReadableString());
			} catch (Throwable t) {
				Logger.warn(MinifyCacheItemHandler.class, "Cant compress file " + minifyCacheKey.getReadableString() + ", caught Throwable" , t);
			} finally {
				input.close();
			}
		} catch (IOException ioe) {
			Logger.warn(MinifyCacheItemHandler.class, "Cant compress file, problem witn input stream: " + minifyCacheKey.getReadableString());
		}
		
		//If there's no result, get the non-minified data
		if (result == null) {
			Logger.info(MinifyCacheItemHandler.class, "Storing raw file: " + minifyCacheKey.getReadableString());
			try {
				result = new String(FileFactory.getFileData(file), "UTF-8");
			} catch (IOException e) {
				throw new RuntimeException("Can't store file in cache: " + minifyCacheKey.getReadableString(), e);
			}
		}
		
		return new MinifyCacheFile(file.getURI(), file.isLive(), result, file.getModDate(), host);
	}
	
	/**
	 * Update the cache with all the files from DotCMS (all css and js files)
	 * @param live if it should update live files or working files
	 */
	public Map<String, MinifyCacheFile> getAllFilesFromDotCMS(boolean live) {
		Map<String, MinifyCacheFile> cache = new ConcurrentHashMap<String, MinifyCacheFile>();
		Logger.info(MinifyCacheItemHandler.class, "get all files from DotCMS for cache, live=" + live);
		//iterate through all files of all hosts
		
		List<File> files;
		if (live) {
			files = FileFactory.getLiveFiles();
		} else {
			files = FileFactory.getWorkingFiles();
		}

		for(File file: files) {
			if((file.getExtension().equalsIgnoreCase("css") || file.getExtension().equalsIgnoreCase("js"))) {
				Host host = HostFactory.getParentHost(file);
				MinifyCacheFileKey fileKey = new MinifyCacheFileKey(file.getURI(), live, host);
				MinifyCacheFile minifyCacheFile = get(fileKey, host);
				cache.put(fileKey.getKey(), minifyCacheFile);
			}
		}
		
		return cache;
	}
}
