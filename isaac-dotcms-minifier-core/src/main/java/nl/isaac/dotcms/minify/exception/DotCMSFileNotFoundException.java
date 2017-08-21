package nl.isaac.dotcms.minify.exception;

/*
 * Dotcms minifier by ISAAC is licensed under a
 * Creative Commons Attribution 3.0 Unported License
 *
 * - http://creativecommons.org/licenses/by/3.0/
 *
 * ISAAC Software Solutions B.V. (http://www.isaac.nl)
 */
@SuppressWarnings("serial")
public class DotCMSFileNotFoundException extends RuntimeException {
	public DotCMSFileNotFoundException(String message) {
		super(message);
	}
}
