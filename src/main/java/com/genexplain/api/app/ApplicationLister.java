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
package com.genexplain.api.app;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.genexplain.api.core.GxHttpClient;
import com.genexplain.api.core.GxHttpClientImpl;
import com.genexplain.api.core.GxHttpConnection;
import com.genexplain.api.core.GxHttpConnectionImpl;

import prophecy.common.JSONMinify;

/**
 * @author pst
 *
 */
@Command(name="apps", description="Lists available analysis applications")
public class ApplicationLister implements ApplicationCommand {
    
    public static final String NO_ARGS_MESSAGE = "Please provide an input file in JSON format as first argument";
    
    private JsonObject config;
    
    private GxHttpConnection connection;
    private GxHttpClient     client;
    
    public ApplicationLister() {
        connection = new GxHttpConnectionImpl();
        client     = new GxHttpClientImpl();
    }
    
    public GxHttpClient getClient() { return client; }
    
    public GxHttpConnection getConnection() { return connection; }
    
    public void login() throws Exception {
        if (!config.getString("connection","").isEmpty()) {
            connection = (GxHttpConnection) Class.forName(config.getString("connection","")).newInstance();
        }
        connection.setServer(config.getString("server", ""));
        connection.setUsername(config.getString("user", ""));
        connection.setPassword(config.getString("password", ""));
        connection.login();
        
        if (!config.getString("client","").isEmpty()) {
            client = (GxHttpClient) Class.forName(config.getString("client","")).newInstance();
        }
        client.setConnection(connection);
    }
    
    public void getApplications() throws Exception {
        JsonObject apps = client.listApplications();
        if (apps.getInt("type",-1) == 0) {
            apps.get("values").asArray().forEach(app -> {
                if (config.getBoolean("with-parameters", false)) {
                    try {
                        getAppParameters(app.asString());
                    } catch (Exception e) {
                        throw new ApplicationListerException(e);
                    }
                } else {
                    System.out.println(app.asString());
                }
            });
        } else {
            throw new ApplicationListerException("An error occurred: " + apps.toString());
        }
    }
    
    public void getAppParameters(String app) throws Exception {
        JsonObject params = client.getAnalysisParameters(app);
        if (params.getInt("type",-1) == 0) {
            params.get("values").asArray().forEach(param -> {
                System.out.println(app + "\t" + param.asObject().get("name").asString() + "\t" +
                                   param.asObject().get("displayName").asString() + "\t" + 
                                   param.asObject().get("description").asString());
            });
        } else {
            System.err.println("An error occurred when retrieving parameters for " + app +
                               ": " + params.toString());
        }
    }
    
    public void setParameters(String config) throws Exception {
        this.config = Json.parse(config).asObject();
    }
    
    public void setParameters(InputStream is) throws Exception {
        StringWriter sw = new StringWriter();
        IOUtils.copy(is, sw, "UTF-8");
        setParameters(JSONMinify.minify(sw.toString()));
    }
    
    public JsonObject getParameters() {
        return this.config;
    }
    
    /* (non-Javadoc)
     * @see com.genexplain.api.app.ApplicationCommand#run(java.lang.String[])
     */
    @Override
    public void run(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println(NO_ARGS_MESSAGE);
            return;
        }
        setParameters(new FileInputStream(args[0]));
        login();
        getApplications();
    }

}
