package nl.isaac.dotcms.minify.viewtool;
/**
* dotCMS minifier by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
* 
* @copyright Copyright (c) 2011 ISAAC Software Solutions B.V. (http://www.isaac.nl)
*/

import java.util.zip.Adler32;

import javax.servlet.http.HttpServletRequest;

import nl.isaac.dotcms.minify.shared.HostTools;

import org.apache.velocity.tools.view.tools.ViewTool;

import com.dotmarketing.beans.Host;
import com.dotmarketing.portlets.files.factories.FileFactory;
import com.dotmarketing.portlets.files.model.File;

public class UtilTool implements ViewTool{

	@Override
	public void init(Object initData) {
		// TODO Auto-generated method stub
		
	}

	public String removeNewlinesTabsAndSpaces(String input) {
		String output = input;
		
		//remove newlines
		output = output.replace("\r\n", "");

		//remove tabs
		output = output.replace("\t", "");

		//remove whitespace before and after a comma
		output = output.replaceAll("( *)(,)( *)", "$2");
		
		//remove whitespace before and after the string, and return it
		return output.trim();
	}
	
	/**
	 * 
	 * @param request	The request we use to retrieve the host from
	 * @param fileUri	A file path of a file can can be retrieved using FileTools.getFileAssetByURI. 
	 * @return 			a hash of the modification date of the given file. 
	 */
	public String createCacheBuster(HttpServletRequest request, String fileUri) {
		String [] arr = {fileUri};
		return createCacheBuster(request, arr);
	}
	
	/**
	 * 
	 * @param request	The request we use to retrieve the host from
	 * @param fileUris	A String array containing a list of file paths of which the files 
	 * 					can can be retrieved using FileTools.getFileAssetByURI. 
	 * @return 			a hash of the concatinated modification dates of the given files. 
	 */
	public String createCacheBuster(HttpServletRequest request, String[] fileUris) {
	    Host host = HostTools.getCurrentHost(request);
	    
	    String modDates = "";
	    
	    for (String fileUri : fileUris) {
	    	File file = FileFactory.getFileByURI(fileUri, host, true);
	    	long modDate = file.getModDate().getTime();
	    	
	    	modDates += "_" + modDate;
	    }
	    
	    // Since the modDates String can be very long, we create a much shorter Adler32 checksum string
	    // We do not use a hash here because checksums are faster (and simpler).
	    Adler32 adler = new Adler32();
	    byte[] bytes = modDates.getBytes();
		adler.update(bytes);
		return "" + adler.getValue();
	}
}