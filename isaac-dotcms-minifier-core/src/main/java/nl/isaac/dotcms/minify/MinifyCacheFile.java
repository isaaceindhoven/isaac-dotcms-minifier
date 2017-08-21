package nl.isaac.dotcms.minify;

/*
 * Dotcms minifier by ISAAC is licensed under a
 * Creative Commons Attribution 3.0 Unported License
 *
 * - http://creativecommons.org/licenses/by/3.0/
 *
 * ISAAC Software Solutions B.V. (http://www.isaac.nl)
 */

import java.util.Date;

import nl.isaac.dotcms.minify.util.ParamValidationUtil;

/**
 * Immutable class to be used as the cache value by MinifyCacheHandler. Stored
 * the minified content of a file and some additional information about the file
 *
 * @author Koen Peters, ISAAC
 * @author Xander Steinmann, ISAAC
 */
public class MinifyCacheFile {
	private final String fileData;
	private final Date modDate;

	public MinifyCacheFile(String fileData, Date modDate) {
		ParamValidationUtil.validateParamNotNull(fileData, "fileData");
		ParamValidationUtil.validateParamNotNull(modDate, "modDate");

		this.fileData = fileData;
		this.modDate = new Date(modDate.getTime());
	}

	public String getFileData() {
		return fileData;
	}

	public Date getModDate() {
		// Defensive copy
		return new Date(modDate.getTime());
	}
}