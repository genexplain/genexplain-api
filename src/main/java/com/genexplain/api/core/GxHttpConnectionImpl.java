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

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

	
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genexplain.util.GxUtil;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Implementation of {@link com.genexplain.api.core.GxHttpConnection}
 * 
 * @see com.genexplain.api.core.GxHttpConnection
 * 
 * @author pst
 *
 */
public class GxHttpConnectionImpl implements GxHttpConnection {
    
	private static final String TEST_URL = "http://genexplain.com"; 
	
	private Logger logger;
	
	private BasicCookieStore    cookieStore;
	private CloseableHttpClient httpClient;
	
	private String server   = "";
	private String username = "";
	private String password = "";
	private String basePath = GxHttpConnection.Path.BASE_PATH.getPath();
	
	private boolean loggedIn  = false;
	private boolean reconnect = false;
	private boolean verbose   = false;
	
	private boolean triedReconnect = false;
	
	private UrlValidator urlValidator;
	
	public GxHttpConnectionImpl() {
		logger      = LoggerFactory.getLogger(this.getClass());
		cookieStore = new BasicCookieStore();
	    httpClient  = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
	    
	    urlValidator = new UrlValidator(new String[]{"http","https"},UrlValidator.ALLOW_LOCAL_URLS);

	}
	
	/*
	 * (non-Javadoc)
	 * @see com.genexplain.api.core.GxHttpConnection#getBasePath()
	 */
	@Override
	public String getBasePath() {
		return basePath;
	}
    
	/**
	 * Sets the base path for all requests to the platform.
	 * <ul>
	 * <li>The specified basepath is prefixed with a slash if missing.
	 * <li>Trailing slashes are removed.
	 * <li>The specified path can be empty.
	 * <li>A null path is assumed to mean empty.
	 * </ul>
	 * 
	 * @throws IllegalArgumentException
	 *           If the specified base path does not form a valid
	 *           URL as determined by {@link org.apache.commons.validator.routines.UrlValidator}
	 *           
	 * @see com.genexplain.api.core.GxHttpConnection#setBasePath(String)
	 */
	@Override
	public GxHttpConnection setBasePath(String basePath) throws IllegalArgumentException {
		if (basePath == null) {
			basePath = "";
		}
		if (!basePath.startsWith("/")) {
			basePath = "/" + basePath;
		}
		basePath = basePath.replaceAll("[\\/]+$", "");
		if (!urlValidator.isValid(TEST_URL + basePath)) {
			throw new IllegalArgumentException("Invalid basepath: " + basePath);
		}
		this.basePath = basePath;
		return this;
	}
    
	/*
	 * (non-Javadoc)
	 * @see com.genexplain.api.core.GxHttpConnection#getUsername()
	 */
	@Override
	public String getUsername() {
		return username;
	}
    
	
	/**
	 * Sets the username.
	 * <ul>
	 * <li>The username must not be null or empty.
	 * </ul>
	 * 
	 * @throws IllegalArgumentException
	 *           If the argument is an empty string or null
	 *           
	 * @see com.genexplain.api.core.GxHttpConnection#setUsername(String)
	 */
	@Override
	public GxHttpConnection setUsername(String username) throws IllegalArgumentException {
		if (username == null || username.isEmpty()) {
			throw new IllegalArgumentException("Username must not be null or empty");
		} else {
		    this.username = username;
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see com.genexplain.api.core.GxHttpConnection#getPassword()
	 */
	@Override
	public String getPassword() {
		return password;
	}
    
	/**
	 * Sets the password.
	 * <ul>
	 * <li>A null password is assumed to mean empty string.
	 * </ul>
	 * 
	 * @see com.genexplain.api.core.GxHttpConnection#setPassword(String)
	 */
	@Override
	public GxHttpConnection setPassword(String password) throws Exception {
		if (password == null) {
			password = "";
		}
		this.password = password;
		return this;
	}
    
	/*
	 * (non-Javadoc)
	 * @see com.genexplain.api.core.GxHttpConnection#getServer()
	 */
	@Override
	public String getServer() {
		return server;
	}
    
	/**
	 * Sets the server URL.
	 * <ul>
	 * <li>Server URL cannot be null or empty.
	 * <li>The specified server must be a valid URL, e.g. https://platform.genexplain.com.
	 * </ul>
	 * 
	 * @throws IllegalArgumentException
	 *           If specified server is null or empty or is not a valid URL
	 *           
	 * @see com.genexplain.api.core.GxHttpConnection#setServer(String)
	 */
	@Override
	public GxHttpConnection setServer(String server) throws IllegalArgumentException {
	    GxUtil.showMessage(verbose, "Setting server to: " + server, logger, GxUtil.LogLevel.INFO);
	    if (server == null || server.isEmpty()) {
	        throw new IllegalArgumentException("Empty server URL.");
	    } else if (!urlValidator.isValid(server)) {
	        throw new IllegalArgumentException("Invalid server URL: " + server + ". Often this happens for a missing protocol part.");
	    }
		this.server = server;
		return this;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.genexplain.api.core.GxHttpConnection#setReconnect(boolean)
	 */
	@Override
    public GxHttpConnection setReconnect(boolean rec) {
        reconnect = rec;
        return this;
    }
    
	/*
	 * (non-Javadoc)
	 * @see com.genexplain.api.core.GxHttpConnection#getReconnect()
	 */
    @Override
    public boolean getReconnect() {
        return reconnect;
    }

    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#setVerbose(boolean)
     */
    @Override
    public GxHttpConnection setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#isVerbose()
     */
    @Override
    public boolean isVerbose() {
        return verbose;
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#hasLoggedIn()
     */
    @Override
    public boolean hasLoggedIn() {
        return loggedIn;
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#getHttpClient()
     */
    @Override
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#ping()
     */
	@Override
	public JsonObject ping() throws Exception {
		return queryJSON(basePath + GxHttpConnection.Path.PING.getPath(), new LinkedHashMap<String,String>());
		
	}
    
	/**
	 * @throws GxHttpConfigurationException
	 *           If the login path is not valid
	 * 
	 * @throws GxHttpException
	 *           If an unexpected response is received from the server or type is missing
	 *           or indicates an error
	 *
	 * @see com.genexplain.api.core.GxHttpConnection#login()
	 */
	@Override
	public JsonObject login() throws Exception {
        GxUtil.showMessage(verbose, "Trying to log in", logger, GxUtil.LogLevel.INFO);
		String loginPath = server + basePath + GxHttpConnection.Path.LOGIN.getPath();
		if (!urlValidator.isValid(loginPath)) {
		    throw new GxHttpConfigurationException("Resulting login path is not valid URL: " + loginPath);
		}
		HttpUriRequest login = RequestBuilder.post()
                .setUri(new URI(loginPath))
                .addParameter("username", username)
                .addParameter("password", password)
                .build();
		CloseableHttpResponse response = httpClient.execute(login);
		JsonObject js = null;
		try {
		    String resp = EntityUtils.toString(response.getEntity());
		    if (resp.isEmpty()) {
		        throw new GxHttpException("Unexpected error: received empty login response.");
		    }
		    js = Json.parse(resp).asObject();
		    if (js.isEmpty()) {
            	throw new GxHttpException("Unexpected error: received empty response object.");
            } else if (js.get("type") == null) {
            	throw new GxHttpException("Received invalid response without type field: " + js.toString());
            } else if (js.getInt("type",-1) != 0) {
                throw new GxHttpException("Could not login: " + js.toString());
            }
		    loggedIn = true;
	    } finally {
            response.close();
        }
		return js;
	}
    
	/*
	 * (non-Javadoc)
	 * @see com.genexplain.api.core.GxHttpConnection#logout()
	 */
	@Override
	public CloseableHttpResponse logout() throws Exception {
	    GxUtil.showMessage(verbose, "Log out", logger, GxUtil.LogLevel.INFO);
		return queryBioUML(basePath + GxHttpConnection.Path.LOGOUT.getPath(), new LinkedHashMap<String,String>());
		
	}
    
	/*
	 * (non-Javadoc)
	 * @see com.genexplain.api.core.GxHttpConnection#queryBioUML(java.lang.String, java.util.Map)
	 */
	@Override
	public CloseableHttpResponse queryBioUML(String path, Map<String, String> params) throws Exception {
	    GxUtil.showMessage(verbose, "Sending query to path: " + path, logger, GxUtil.LogLevel.INFO);
		RequestBuilder rbuilder = RequestBuilder.post()
                .setUri(new URI(server + path));
		params.forEach((key, val) -> {
			rbuilder.addParameter(key, val);
		});
		return httpClient.execute(rbuilder.build());
	}
    
	/**
     * @throws GxHttpException
     *           If response contains no or an unexpected type value or type content
     * 
     * @see com.genexplain.api.core.GxHttpConnection#queryJSON(String,Map)
     */
	@Override
	public JsonObject queryJSON(String path, Map<String, String> params) throws Exception {
	    GxUtil.showMessage(verbose, "Sending JSON query to path: " + path, logger, GxUtil.LogLevel.INFO);
		CloseableHttpResponse resp = queryBioUML(path, params);
		JsonObject response = Json.parse(EntityUtils.toString(resp.getEntity())).asObject();
		if (response.get("type") != null) {
            JsonValue type = response.get("type");
			if (type.isString()) {
			    switch (type.asString()) {
			    case "ok": response.add("type", 0); break;
			    case "error": response.add("type", 1); break;
			    default: throw new GxHttpException("Unexpected type value neither ok nor error:" + type.asString());
			    }
		    } else if (!type.isNumber()) {
		    	throw new GxHttpException("Content of type field is neither string nor integer");
		    } else if (type.asInt() == 3) {
		        if (!triedReconnect && reconnect) {
		            triedReconnect = true;
				    login();
				    return queryJSON(path, params);
		        }
		    } else {
		        triedReconnect = false;
		    }
		} else {
			throw new GxHttpException("Response does not contain a type field");
		}
		resp.close();
		return response;
	}
}
