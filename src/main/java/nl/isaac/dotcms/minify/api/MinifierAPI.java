package nl.isaac.dotcms.minify.api;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import com.dotmarketing.util.Logger;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.yahoo.platform.yui.compressor.CssCompressor;

/**
 * API that provides functionality to minify JavaScript and CSS. It is exported to
 * other bundles, to make it possible to minify code from within other dynamic plugins.
 * 
 * @author jorith.vandenheuvel
 *
 */
public class MinifierAPI {
	
	/**
	 * Compresses JavaScript
	 * 
	 * @param plainJavascript A String containing plain, uncompressed JavaScript
	 * @param compilationLevel The level of optimization that should be
	 * applied when compiling JavaScript code.
	 * @return A String containing compressed JavaScript
	 */
	public String getMinifiedJavascript(String plainJavascript, CompilationLevel compilationLevel) {
		
		Compiler compiler = new Compiler();
		CompilerOptions options = new CompilerOptions();
		compilationLevel.setOptionsForCompilationLevel(options);

		List<SourceFile> jsFiles = new LinkedList<SourceFile>();
		
		jsFiles.add(SourceFile.fromCode("js", plainJavascript));
		List<SourceFile> externalJsfiles = new LinkedList<SourceFile>();
		
		compiler.compile(externalJsfiles, jsFiles, options);
		
		return compiler.toSource();
	}
	
	/**
	 * Compresses CSS
	 * 
	 * @param plainCss A String containing plain, uncompressed CSS
	 * @return A String containing compressed CSS
	 */
	public String getMinfiedCss(String plainCss) {
		String minifiedCss = "";
		
		try {
			Reader reader = new StringReader(plainCss);
			StringWriter writer = new StringWriter();
			try {
				reader = new StringReader(plainCss);
				CssCompressor cssCompressor = new CssCompressor(reader);
				cssCompressor.compress(writer, 80);
				minifiedCss = writer.getBuffer().toString();
			}  finally {
				reader.close();		
				writer.close();
			}
		} catch (IOException e) {
			Logger.error(this, "Error while minifying CSS content", e);
			throw new RuntimeException(e);
		}
		
		return minifiedCss;
	}
}
