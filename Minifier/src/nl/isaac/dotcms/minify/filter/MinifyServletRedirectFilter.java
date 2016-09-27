package nl.isaac.dotcms.minify.filter;
/**
* dotCMS minifier by ISAAC - The Full Service Internet Agency is licensed 
* under a Creative Commons Attribution 3.0 Unported License
* - http://creativecommons.org/licenses/by/3.0/
* - http://www.geekyplugins.com/
*/

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * The MinifyServletRedirectFilter redirects calls that are meant for minifier.js 
 * and minifier.css. Those calls are redirected to the MinifyServlet. (the servlet url-pattern
 * is not sufficient to handle this). This is done so paths aren't too messed up when calling the MinifyServlet.
 * 
 * When a css file uses an image with url(./img/picture.jpg), directly calling /servlet/MinifyServlet
 * causes the image URL to become /servlet/img/picture.jpg. When calling /css/minifier.css the /css/
 * remains in the URL to avoid bad URLs.
 * 
 * @author Xander
 *
 */
public class MinifyServletRedirectFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)req;
		
		if(request.getRequestURI().contains("minifier")) {
			req.getRequestDispatcher("/servlets/MinifyServlet").include(req, res);
		} else {
			chain.doFilter(req, res);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
