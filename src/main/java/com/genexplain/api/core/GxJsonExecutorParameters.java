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

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.genexplain.util.GxUtil;

import prophecy.common.JSONMinify;

/**
 * Parameter object that configures a {@link com.genexplain.api.core.GxJsonExecutor}.
 * 
 * @author pst
 *
 */
public class GxJsonExecutorParameters {
    
    /**
     * Names of JSON properties that are read
     * from input file or object.
     * 
     * @author pst
     *
     */
    public enum JsonField {
        TASKS("tasks"),
        SERVER("server"),
        USER("user"),
        VERBOSE("verbose"),
        RECONNECT("reconnect"),
        PASSWORD("password"),
        CONNECTION("connection-class"),
        HTTP_CLIENT("client-class");
        
        private String name;
        
        private JsonField(String name) {
            this.name = name;
        }
        
        public String get() { return name; }
    }
    
    private JsonObject config;
    
    private String server;
    private String user;
    private String password;
    
    private boolean verbose   = false;
    private boolean reconnect = false;
    
    private JsonArray tasks;
    
    private GxHttpClient     client;
    private GxHttpConnection connection;
    
    
    public GxJsonExecutorParameters() {
        client     = new GxHttpClientImpl();
        connection = new GxHttpConnectionImpl();
    }
    
    /**
     * Configures connection and client according to
     * specified parameters.
     * 
     * @return This parameter object to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    public GxJsonExecutorParameters connect() throws Exception {
        connection.setLoginParameters(getServer(), getUser(), getPassword());
        connection.setVerbose(isVerbose());
        connection.setReconnect(withReconnect());
        connection.login();
        client.setConnection(connection);
        client.setVerbose(isVerbose());
        return this;
    }
    
    /**
     * Returns the current {@link com.genexplain.api.core.GxHttpClient GxHttpClient}.
     * 
     * @return The {@link com.genexplain.api.core.GxHttpClient GxHttpClient}
     */
    public GxHttpClient getHttpClient() { 
        return client;
    }
    
    /**
     * Sets the client used to access the platform.
     * 
     * @param client
     *           Sets the {@link com.genexplain.api.core.GxHttpClient GxHttpClient}
     * 
     * @return This parameter object to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    public GxJsonExecutorParameters setHttpClient(GxHttpClient client) throws Exception {
        this.client = client;
        return this;
    }
    
    /**
     * Sets the client type used to access the platform and creates a new
     * instance of the specified client class.
     * 
     * @param name
     *           Sets the type of {@link com.genexplain.api.core.GxHttpClient GxHttpClient}
     *           specified by class name
     * 
     * @return This parameter object to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    public GxJsonExecutorParameters setHttpClient(String name) throws Exception {
        return setHttpClient((GxHttpClient) Class.forName(name).newInstance());
    }
    
    /**
     * Returns the {@link com.genexplain.api.core.GxHttpConnection GxHttpConnection} used 
     * to access the platform
     * 
     * @return the {@link com.genexplain.api.core.GxHttpConnection GxHttpConnection}
     */
    public GxHttpConnection getConnection() {
        return connection;
    }
    
    /**
     * Sets the connection used to access the platform.
     * 
     * @param client
     *           Sets the {@link com.genexplain.api.core.GxHttpConnection connection}
     * 
     * @return This parameter object to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    public GxJsonExecutorParameters setHttpConnection(GxHttpConnection connection) throws Exception {
        this.connection = connection;
        return this;
    }
    
    /**
     * Sets the connection type used to access the platform and creates a new
     * instance of the specified connection class.
     * 
     * @param name
     *           Sets the type of {@link com.genexplain.api.core.GxHttpConnection GxHttpConnection}
     *           specified by class name
     * 
     * @return This parameter object to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    public GxJsonExecutorParameters setHttpConnection(String name) throws Exception {
        return setHttpConnection((GxHttpConnection) Class.forName(name).newInstance());
    }
    
    /**
     * Returns whether methods may reconnect in case of time out.
     * 
     * @return <code>true</code> if methods may reconnect
     */
    public boolean withReconnect() {
        return reconnect;
    }

    /**
     * Sets whether methods may reconnect in case of time out.
     * 
     * @return This parameter object to enable fluent calls
     */
    public GxJsonExecutorParameters setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
        return this;
    }
    
    /**
     * Returns whether executing objects are in verbose mode.
     * 
     * @return <code>true</code> if verbose
     */
    public boolean isVerbose() {
        return verbose;
    }

    
    /**
     * Sets executing objects are in verbose mode.
     * 
     * @return This parameter object to enable fluent calls
     */
    public GxJsonExecutorParameters setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    /**
     * Returns tasks that can be executed by methods of 
     * {@link com.genexplain.api.core.GxJsonExecutor GxJsonExecutor}. 
     * 
     * @return JsonArray with task definitions
     */
    public JsonArray getTasks() {
        return tasks;
    }

    /**
     * Sets tasks that can be executed by methods of 
     * {@link com.genexplain.api.core.GxJsonExecutor GxJsonExecutor}. 
     * 
     * @return This parameter object to enable fluent calls
     */
    public GxJsonExecutorParameters setTasks(JsonArray tasks) {
        this.tasks = tasks;
        return this;
    }
    
    /**
     * Returns the server used to configure the connection.
     * 
     * @return The server
     * 
     * @see com.genexplain.api.core.GxHttpConnection#getServer()
     */
    public String getServer() {
        return server;
    }

    /**
     * Sets the server used to configure this connection.
     * 
     * @return This parameter object to enable fluent calls
     * 
     * @see com.genexplain.api.core.GxHttpConnection#setServer(String)
     */
    public GxJsonExecutorParameters setServer(String server) {
        this.server = server;
        return this;
    }

    /**
     * Returns the user applied for log in.
     * 
     * @return The user
     * 
     * @see com.genexplain.api.core.GxHttpConnection#setUsername(String)
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the user applied for log in.
     * 
     * @return This parameter object to enable fluent calls
     * 
     * @see com.genexplain.api.core.GxHttpConnection#setUsername(String)
     */
    public GxJsonExecutorParameters setUser(String user) {
        this.user = user;
        return this;
    }

    /**
     * Returns the password applied for log in.
     * 
     * @return The password
     * 
     * @see com.genexplain.api.core.GxHttpConnection#setPassword(String)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password applied for log in.
     * 
     * @return This parameter object to enable fluent calls
     * 
     * @see com.genexplain.api.core.GxHttpConnection#setPassword(String)
     */
    public GxJsonExecutorParameters setPassword(String password) {
        this.password = password;
        return this;
    }
    
    /**
     * Returns the config object.
     * 
     * @return the JsonObject with configuration parameters, may be null if
     *         parameters were not configured using a JsonObject
     */
    public JsonObject getConfig() { return config; }
    
    /**
     * Sets configuration from an input stream. The input stream is extracted
     * to a string which is parsed by a JSON parser. The JSON is minified before
     * parsing. The minifier removes C/C++-style comments.
     * 
     * @param is
     *           Inputstream from which a JsonObject can be read
     *           
     * @return This parameter object to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    public GxJsonExecutorParameters setConfig(InputStream is) throws Exception {
        StringWriter sw = new StringWriter();
        IOUtils.copy(is, sw, "UTF-8");
        return setConfig(JSONMinify.minify(sw.toString()));
    }
    
    /**
     * Sets configuration from a string which is parsed by a JSON parser.
     * The JSON is minified before parsing. The minifier removes C/C++-style comments.
     * 
     * @param config
     *           String that can be parsed into a JsonObject
     *           
     * @return This parameter object to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    public GxJsonExecutorParameters setConfig(String config) throws Exception {
        return setConfig(Json.parse(config).asObject());
    }
    
    /**
     * Sets configuration object.
     * 
     * @param config
     *           JsonObject containing configuration parameters
     *           
     * @return This parameter object to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    public GxJsonExecutorParameters setConfig(JsonObject config) throws Exception {
        this.config = config;
        setServer(config.getString(JsonField.SERVER.get(),""));
        setUser(config.getString(JsonField.USER.get(),""));
        setPassword(config.getString(JsonField.PASSWORD.get(),""));
        setVerbose(config.getBoolean(JsonField.VERBOSE.get(),false));
        setReconnect(config.getBoolean(JsonField.RECONNECT.get(),false));
        setHttpConnection(config.getString(JsonField.CONNECTION.get(), GxHttpConnectionImpl.class.getName()));
        setHttpClient(config.getString(JsonField.HTTP_CLIENT.get(), GxHttpClientImpl.class.getName()));
        
        
        if (config.get(JsonField.TASKS.get()) != null) {
            setTasks(config.get(JsonField.TASKS.get()).asArray());
        }
        
        this.config = config;
        
        return this;
    }
}
