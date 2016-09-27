package nl.isaac.dotcms.minify.viewtool;

import org.apache.velocity.tools.view.tools.ViewTool;

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
}