package nl.isaac.dotcms.minify.util;

/*
 * Dotcms minifier by ISAAC is licensed under a 
 * Creative Commons Attribution 3.0 Unported License
 * 
 * - http://creativecommons.org/licenses/by/3.0/
 * - http://www.geekyplugins.com/
 * 
 * ISAAC Software Solutions B.V. (http://www.isaac.nl)
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.dotmarketing.util.UtilMethods;

public class StringListUtil {
	private StringListUtil() {};
	
	public static List<String> getCleanStringList(String kommaSeparatedString) {
		return getStringList(removeNewlinesTabsAndSpaces(kommaSeparatedString).split(","));
	}
	
	public static List<String> getStringList(String[] stringArray) {
		if(stringArray.length > 0 && UtilMethods.isSet(stringArray[0])) {
			return Arrays.asList(stringArray);
		} else {
			return Collections.emptyList();
		}
	}
	
	private static String removeNewlinesTabsAndSpaces(String input) {
		String output = input;
		
		//remove newlines
		output = output.replaceAll("\n", "");
		output = output.replaceAll("\r", "");

		//remove tabs
		output = output.replace("\t", "");

		//remove whitespace before and after a comma
		output = output.replaceAll("( *)(,)( *)", "$2");
		
		//remove whitespace before and after the string, and return it
		return output.trim();
	}
}
