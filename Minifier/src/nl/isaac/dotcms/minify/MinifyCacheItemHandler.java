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

import nl.isaac.dotcms.minify.dependencies.com.google.javascript.jscomp.CompilationLevel;
import nl.isaac.dotcms.minify.dependencies.com.google.javascript.jscomp.CompilerOptions;
import nl.isaac.dotcms.minify.dependencies.com.google.javascript.jscomp.JSSourceFile;
import nl.isaac.dotcms.minify.dependencies.com.yahoo.platform.yui.compressor.CssCompressor;
import nl.isaac.dotcms.minify.shared.ItemHandler;

import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.files.factories.FileFactory;
import com.dotmarketing.portlets.files.model.File;
import com.dotmarketing.util.Logger;

/**
 * The MinifyCacheItemHandler contains the code that retrieves a new/updated file from the cache.
 * 
 * @author Xander
 *
 */
public class MinifyCacheItemHandler implements ItemHandler<MinifyCacheFile> {
	
	public Map<String, MinifyCacheFile> getInitialCache() {
		return getAllFilesFromDotCMS(true);
	}
	
	private MinifyCacheFile get(MinifyCacheFileKey minifyCacheFileKey, Host host) {
		File file = FileFactory.getFileByURI(minifyCacheFileKey.getUri(), host, minifyCacheFileKey.getLive());
		return get(file, host);
	}
	
	public MinifyCacheFile get(String key) {
		MinifyCacheFileKey minifyCacheFileKey = MinifyCacheFileKey.createInstanceWithKey(key);
		
		//find the host by name
		Host host = null;
		try {
			host = APILocator.getHostAPI().findByName(minifyCacheFileKey.getHostName(), APILocator.getUserAPI().getSystemUser(), false);
		} catch(Exception e) {
			//no problem, we'll try to find the alias
		}
		
		//find the host by alias
		if(host == null) {
			try {
				host = APILocator.getHostAPI().findByAlias(minifyCacheFileKey.getHostName(), APILocator.getUserAPI().getSystemUser(), false);
			} catch (Exception e1) {
				Logger.error(this.getClass(), "Can't find host with name " + minifyCacheFileKey.getHostName(), e1);
				throw new RuntimeException("Can't find host with name " + minifyCacheFileKey.getHostName(), e1);
			}
		}
		
		return get(minifyCacheFileKey, host);
	}
	
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
					nl.isaac.dotcms.minify.dependencies.com.google.javascript.jscomp.Compiler compiler = new nl.isaac.dotcms.minify.dependencies.com.google.javascript.jscomp.Compiler();
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
	 * @param live if it should update only live files (true) or only working files (false)
	 */
	public Map<String, MinifyCacheFile> getAllFilesFromDotCMS(boolean live) {
		Map<String, MinifyCacheFile> cache = new ConcurrentHashMap<String, MinifyCacheFile>();
		Logger.info(MinifyCacheItemHandler.class, "get all files from DotCMS for cache, live=" + live);
		//iterate through all files of all hosts
		List<Host> hosts;
		try {
			hosts = APILocator.getHostAPI().findAll(APILocator.getUserAPI().getSystemUser(), false);
			for(Host host: hosts) {
				List<File> files = APILocator.getFileAPI().getAllHostFiles(host, live, APILocator.getUserAPI().getSystemUser(), false);
				for(File file: files) {
					if((file.getExtension().equalsIgnoreCase("css") || file.getExtension().equalsIgnoreCase("js"))) {
						MinifyCacheFileKey fileKey = new MinifyCacheFileKey(file.getURI(), live, host);
						MinifyCacheFile minifyCacheFile = get(fileKey, host);
						cache.put(fileKey.getKey(), minifyCacheFile);
					}
				}
			}
		} catch (DotDataException e) {
			throw new RuntimeException(e);
		} catch (DotSecurityException e) {
			throw new RuntimeException(e);
		}
		
		return cache;
	}
}
