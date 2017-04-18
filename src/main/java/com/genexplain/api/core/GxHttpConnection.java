/**
 * Copyright (C) 2017 geneXplain GmbH, Wolfenbuettel, Germany
 *
 * Author: Philip Stegmaier
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY 
 * OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.genexplain.api.core;

import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

import com.eclipsesource.json.JsonObject;

/**
 * This interface provides basic methods to communicate with
 * the geneXplain platform through the BioUML web service.
 * <p>
 * It covers functionality such as login and logout, ping, or
 * low-level methods to submit queries.
 * </p>
 * 
 * @author pst
 */
public interface GxHttpConnection {
	
    /**
     * Platform request paths used by 
     * {@link com.genexplain.api.core.GxHttpConnection GxHttpConnection}.
     *  
     * @author pst
     */
	public enum Path {
		BASE_PATH("/biouml"),
		DATA("/web/data"),
		LOGIN("/web/login"),
		LOGOUT("/web/logout"),
		TABLE_COLUMNS("/web/table/columns"),
		PING("/web/ping");
		
		private String path;
		
		private Path(String path) {
			this.path = path;
		}
		
		public String getPath() {
			return path;
		}
	}
	
	/**
	 * Set <code>true</code> for more logging output.
	 * 
	 * @param verbose
	 *           Switch logging output on or off
	 *           
	 * @return This object to enable fluent calls
	 */
	public GxHttpConnection setVerbose(boolean verbose);
	
	
	/**
	 * Shows whether this connection is currently in <i>verbose</i> mode.
	 * 
	 * @return <code>true</code> if verbose
	 */
	public boolean isVerbose();
	
	/**
	 * Shows whether the login method was invoked.
	 * 
	 * @return <code>true</code> if the login method was invoked
	 */
	public boolean hasLoggedIn();
	
	/**
	 * Set whether a query method may try to reconnect if
	 * the response signals that the session has expired.
	 * 
	 * @param rec
	 *           Set true if a query method may try to reconnect,
	 *           or false if not.
	 *           
	 * @return This object to enable fluent calls
	 */
	public GxHttpConnection setReconnect(boolean rec);
	
	/**
	 * Returns whether a query method may try to reconnect if
	 * the response signals that the session has expired.
	 * 
	 * @return true if a method may reconnect
	 */
	public boolean getReconnect();
	
	/**
	 * Set server, username and password in one command.
	 * <p>
	 * See also {@link #getServer()}, {@link #setUsername(String)}, {@link #getPassword()}
	 * </p>
	 * 
	 * @param server
	 *           The server string consisting of protocol and host, 
	 *           e.g. https://platform.genexplain.com
	 * @param username
	 *           The username to submit for log-in
	 * @param password
	 *           The password that belongs to the username
	 *           
	 * @return This object to enable fluent calls
	 * 
	 * @throws Exception
	 *           May throw or cause an exception by forwarding 
	 *           specified parameters to other methods
	 */
	default GxHttpConnection setLoginParameters(String server, String username, String password) throws Exception {
		setServer(server);
		setUsername(username);
		setPassword(password);
		return this;
	}
	
	/**
	 * Returns the base path of the service.
	 * <p>
	 * The base path is the first component of the request path.
	 * </p>
	 * 
	 * @return The base path
	 */
	public String getBasePath();
	
	/**
	 * Sets the base path.
	 * <p>
     * The base path is the first component of the request path.
     * </p>
	 * 
	 * @param basePath
	 *           A valid base path
	 *           
	 * @return This object to enable fluent calls
	 * 
	 * @throws Exception
	 *           May throw or cause an exception by forwarding the 
     *           specified parameter to other methods
	 */
	public GxHttpConnection setBasePath(String basePath) throws Exception;
	
	/**
	 * Returns the username.
	 * 
	 * @return The username
	 */
	public String getUsername();
	
	/**
	 * Sets the username.
	 * 
	 * @param username
	 *           The username
	 * 
	 * @return This object to enable fluent calls
	 * 
	 * @throws Exception
     *           May throw or cause an exception by forwarding 
     *           specified parameters to other methods
	 */
	public GxHttpConnection setUsername(String username) throws Exception;
	
	/**
	 * Returns the password.
	 * 
	 * @return The password
	 */
    public String getPassword();
	
    /**
     * Sets the password.
     * 
     * @param password
     *           The password
     * 
     * @return This object to enable fluent calls
     * 
     * @throws Exception
     *           May throw or cause an exception by forwarding 
     *           specified parameters to other methods
     */
	public GxHttpConnection setPassword(String password) throws Exception;
	
	/**
	 * Returns the server string consisting of protocol and hostname.
	 * 
	 * @return The server
	 */
	public String getServer();
	
	/**
	 * Sets the server URL part.
	 * <p>The argument should contain the scheme/protocol and the hostname.</p>
	 * <p>E.g.: <i>https://platform.genexplain.com</i>
	 *  
	 * @param server 
	 *           the server (hostname) URL string
	 *           
	 * @return This object to enable fluent calls
	 * 
	 * @throws Exception 
	 *           For an invalid server string
	 */
	public GxHttpConnection setServer(String server) throws Exception;
	
	/**
	 * Sends a query to the BioUML service and returns the HTTP response. The response
	 * object should be closed by the user of this method to avoid resource leaks.

	 * @param path
	 *           The request path
	 *           
	 * @param params
	 *           Request parameters
	 * 
	 * @return The response received from the service
	 * 
	 * @throws Exception
     *           May throw or cause an exception by forwarding 
     *           specified parameters to other methods
	 */
	public CloseableHttpResponse queryBioUML(String path, Map<String,String> params) throws Exception;
	
	/**
	 * Sends a query to the service using {@link #queryBioUML(String, Map) queryBioUML}
	 * and tries to convert the response to a JSON object which is returned from this method.
	 * 
	 * @param path
	 *           The request path
	 *           
	 * @param params
	 *           Request parameters
	 *           
	 * @return The server response
	 * 
	 * @throws Exception
     *           May throw or cause an exception by forwarding 
     *           specified parameters to other methods
	 */
	public JsonObject queryJSON(String path, Map<String,String> params) throws Exception;
    
	
	/**
	 * Returns the HTTP client used by this connection.
	 * 
	 * @return The HTTP client
	 */
	public CloseableHttpClient getHttpClient();
	
	
	/**
	 * Sends a ping request to the server.
	 * 
	 * @return The server response
	 * 
	 * @throws Exception
     *           May throw or cause an exception
	 */
	public JsonObject ping() throws Exception;
	
	/**
	 * Logs into the specified service.
	 * 
	 * @return The server response
	 * 
	 * @throws Exception
     *           May throw or cause an exception
	 */
	public JsonObject login() throws Exception;
	
	/**
	 * Closes the current session and returns the web server response.
	 * 
	 * @return The server response as HTTP client object. This response must be
	 *         closed by the caller when it is not needed anymore.
	 * 
	 * @throws Exception
     *           May throw or cause an exception
	 */
	public CloseableHttpResponse logout() throws Exception;
}
