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

import java.io.FileReader;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.genexplain.base.JsonConfigurable;

/**
 * Parameter object that configures a {@link com.genexplain.api.core.GxJsonExecutor}.
 * 
 * @author pst
 *
 */
public class GxJsonExecutorParameters implements JsonConfigurable{
    
    /**
     * Names of JSON properties that are read
     * from input file or object.
     * 
     * @author pst
     *
     */
    public enum JsonProperty {
        TASKS("tasks"),
        SERVER("server"),
        USER("user"),
        VERBOSE("verbose"),
        RECONNECT("reconnect"),
        NOCONNECT("withoutConnect"),
        PASSWORD("password"),
        CREDENTIALS("credentials"),
        CONNECTION("connection-class"),
        HTTP_CLIENT("client-class"),
        REPLACE_STRINGS("replaceStrings"),
        REPLACE_LISTS("replaceLists"), 
        REPLACE_NUMS("replaceNumbers"),
        NEXT_TASK("nextTask");
        
        
        private String name;
        
        private JsonProperty(String name) {
            this.name = name;
        }
        
        public String get() { return name; }
    }
    
    private JsonObject config;
    
    private String server;
    private String user;
    private String password;
    private String credentials;
    
    private boolean verbose   = false;
    private boolean reconnect = false;
    
    private JsonValue taskItem;
    
    private GxHttpClient     client;
    private GxHttpConnection connection;
    
    private boolean withoutConnect;
    
    private JsonArray replaceStrings;
    private JsonArray replaceLists;
    private JsonArray replaceNums;
    
    private String nextTaskItem;
    
    public GxJsonExecutorParameters() {
        client     = new GxHttpClientImpl();
        connection = new GxHttpConnectionImpl();
        replaceStrings = new JsonArray();
        replaceLists = new JsonArray();
        replaceNums = new JsonArray();
        nextTaskItem = "";
        withoutConnect = false;
    }
    
    public String replaceStrings(String input) {
        String output = input;
        if (replaceStrings != null && replaceStrings.isArray()) {
            JsonArray rep;
            for (int r = 0;r < replaceStrings.size(); ++r) {
                rep = replaceStrings.get(r).asArray();
                output = output.replace(rep.get(0).asString(),
                        rep.get(1).asString());
            }
        }
        return output;
    }
    
    public String replaceLists(String input) {
        String output = input;
        if (replaceLists != null && replaceLists.isArray()) {
            JsonObject rep;
            for (int r = 0;r < replaceLists.size(); ++r) {
                rep = replaceLists.get(r).asObject();
                output = output.replace("\"" + rep.get("label").asString() + "\"",
                        rep.get("replace").asArray().toString());
            }
        }
        return output;
    }
    
    public String replaceNums(String input) {
        String output = input;
        if (replaceNums != null && replaceNums.isArray()) {
            JsonArray rep;
            for (int r = 0;r < replaceNums.size(); ++r) {
                rep = replaceNums.get(r).asArray();
                output = output.replace("\"" + rep.get(0).asString() + "\"",
                        rep.get(1).toString());
            }
        }
        return output;
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
        if (!withoutConnect) {
            connection.setLoginParameters(getServer(), getUser(), getPassword());
            connection.setVerbose(isVerbose());
            connection.setReconnect(withReconnect());
            connection.login();
            client.setConnection(connection);
            client.setVerbose(isVerbose());
        }
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
    
    public boolean isWithoutConnect() {
        return withoutConnect;
    }

    public void setWithoutConnect(boolean withoutConnect) {
        this.withoutConnect = withoutConnect;
    }

    public String getNextTaskItem() { return nextTaskItem; }
    
    public GxJsonExecutorParameters setNextTaskItem(String item) {
        nextTaskItem = item;
        return this;
    }

    /**
     * Returns tasks that can be executed by methods of 
     * {@link com.genexplain.api.core.GxJsonExecutor GxJsonExecutor}. 
     * 
     * @return JsonArray with task definitions
     */
    public JsonValue getTasks() {
        return taskItem;
    }

    /**
     * Sets tasks that can be executed by methods of 
     * {@link com.genexplain.api.core.GxJsonExecutor GxJsonExecutor}. 
     * 
     * @return This parameter object to enable fluent calls
     */
    public GxJsonExecutorParameters setTasks(JsonValue tasks) {
        this.taskItem = tasks;
        return this;
    }
    
    /**
     * Returns the string replacement array.
     * 
     * @return The array of replacements if defined
     */
    public JsonArray getReplaceStrings() {
        return replaceStrings;
    }
    
    /**
     * Sets string replacements that can be exchanged in task
     * objects before execution.
     * 
     * @param  reps - an array of two-element arrays defining string replacements
     * @return This parameter object to enable fluent calls
     */
    public GxJsonExecutorParameters setReplaceStrings(JsonArray reps) {
        this.replaceStrings = reps;
        return this;
    }
    
    /**
     * Returns the number/boolean replacement array.
     * 
     * @return The array of replacements if defined
     */
    public JsonArray getReplaceNums() {
        return replaceNums;
    }

    /**
     * Sets number or boolean replacements that can be exchanged in task
     * objects before execution.
     * 
     * @param  reps - an array of two-element arrays defining number/boolean replacements
     * @return This parameter object to enable fluent calls
     */
    public GxJsonExecutorParameters setReplaceNums(JsonArray replaceNums) {
        this.replaceNums = replaceNums;
        return this;
    }

    /**
     * Returns the list replacement array.
     * 
     * @return The array of replacements if defined
     */
    public JsonArray getReplaceLists() {
        return replaceLists;
    }
    
    /**
     * Sets list replacements that can be exchanged in task
     * objects before execution.
     * 
     * @param  reps - an array of two-element arrays defining string-by-list replacements
     * @return This parameter object to enable fluent calls
     */
    public GxJsonExecutorParameters setReplaceLists(JsonArray reps) {
        this.replaceLists = reps;
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
     * Returns the filepath with credentials applied for log in.
     * 
     * @return The credential file path if defined
     * 
     */
    public String getCredentials() {
        return credentials;
    }
    
    /**
     * Sets the filepath with credentials applied for log in.
     * 
     * @return This parameter object to enable fluent calls
     */
    public void setCredentials(String credentials) throws Exception {
        this.credentials = credentials;
        if (credentials.isEmpty())
            return;
        JsonObject crs = Json.parse(new FileReader(credentials)).asObject();
        setUser(crs.getString(JsonProperty.USER.get(),""));
        setPassword(crs.getString(JsonProperty.PASSWORD.get(),""));
    }

    /**
     * Sets configuration object.
     * 
     * @see com.genexplain.base.JsonConfigurable#setConfig(JsonObject)
     */
    @Override
    public JsonConfigurable setConfig(JsonObject config) throws Exception {
        this.config = config;
        setServer(config.getString(JsonProperty.SERVER.get(),""));
        setUser(config.getString(JsonProperty.USER.get(),""));
        setPassword(config.getString(JsonProperty.PASSWORD.get(),""));
        setCredentials(config.getString(JsonProperty.CREDENTIALS.get(),""));
        setVerbose(config.getBoolean(JsonProperty.VERBOSE.get(),false));
        setReconnect(config.getBoolean(JsonProperty.RECONNECT.get(),false));
        setHttpConnection(config.getString(JsonProperty.CONNECTION.get(), GxHttpConnectionImpl.class.getName()));
        setHttpClient(config.getString(JsonProperty.HTTP_CLIENT.get(), GxHttpClientImpl.class.getName()));
        setNextTaskItem(config.getString(JsonProperty.NEXT_TASK.get(), ""));
        setWithoutConnect(config.getBoolean(JsonProperty.NOCONNECT.get(),false));
        if (config.get(JsonProperty.REPLACE_STRINGS.get()) != null) {
            setReplaceStrings(config.get(JsonProperty.REPLACE_STRINGS.get()).asArray());
        }
        if (config.get(JsonProperty.REPLACE_LISTS.get()) != null) {
            setReplaceLists(config.get(JsonProperty.REPLACE_LISTS.get()).asArray());
        }
        if (config.get(JsonProperty.REPLACE_NUMS.get()) != null) {
            setReplaceNums(config.get(JsonProperty.REPLACE_NUMS.get()).asArray());
        }
        
        if (config.get(JsonProperty.TASKS.get()) != null) {
            setTasks(config.get(JsonProperty.TASKS.get()));
        }
        
        this.config = config;
        
        return this;
    }
}
