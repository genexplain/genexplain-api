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
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

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
        	StringBuilder sb = new StringBuilder();
        	if (config.getBoolean("withParameters", false)) {
        		sb.append("Tool folder/name\tAPI name\tParameter name\tShort description\tType\tClass\tRequired\tDescription\n");
        	}
            apps.get("values").asArray().forEach(app -> {
                if (config.getBoolean("withParameters", false)) {
                    try {
                        getAppParameters(app.asString(), sb);
                    } catch (Exception e) {
                        throw new ApplicationListerException(e);
                    }
                } else {
                    sb.append(app.asString() + "\n");
                }
            });
            if (config.getBoolean("withGalaxy", false)) {
            	List<String[]> gtools = getGalaxy();
            	for (String[] gt : gtools) {
            		if (config.getBoolean("withParameters", false)) {
            			getGalaxyParameters(gt, sb);
            		} else {
            			sb.append(gt[2] + "/" + gt[0] + "\n");
            		}
            	}
            }
            String outfile = config.getString("outfile", "");
            if (outfile.isEmpty()) {
            	System.out.print(sb.toString());
            } else {
            	FileWriter fw = new FileWriter(outfile);
            	fw.write(sb.toString());
            	fw.close();
            }
            sb.setLength(0);
        } else {
            throw new ApplicationListerException("An error occurred: " + apps.toString());
        }
    }
    
    
    public void getGalaxyParameters(String[] tool, StringBuilder sb) throws Exception {
        JsonObject params = client.getAnalysisParameters(tool[1]);
        if (params.getInt("type",-1) == 0) {
            params.get("parameters").asArray().forEach(param -> {
            	sb.append(tool[2] + "/" + tool[0] + "\t" + tool[1] + "\t" + 
            					   param.asObject().get("name").asString() + "\t" +
                                   param.asObject().get("displayName").asString() + "\t" + 
                                   param.asObject().getString("type", "") + "\t" +
                                   param.asObject().getString("elementClass", "") + "\t" +
                                   param.asObject().getBoolean("elementMustExist", false) + "\t" +
                                   param.asObject().get("description").asString() + "\n");
            });
        } else {
            System.err.println("An error occurred when retrieving parameters for Galaxy tool " + tool[2] + "/" + tool[0] +
                               ":\n\n" + params.toString());
        }
    }
    
    
    private void collectGalaxyTools(String folder, String displayFolder, List<String[]> tools) throws Exception {
    	JsonObject js = client.list(folder);
    	if (js.names().contains("names")) {
    		js.get("names").asArray().forEach(elem -> {
    			JsonObject emo = elem.asObject();
    			if (emo.getBoolean("hasChildren", false)) {
    				try {
    					collectGalaxyTools(folder + "/" + emo.get("name").asString(),
    									   displayFolder + "/" + emo.get("title").asString(), tools);
    				} catch (Exception e) {
    					System.out.println("An error occurred: " + e.getMessage());
    				}
    			} else {
    				tools.add(new String[] {emo.getString("title", ""),
    										emo.getString("name", ""),
    										displayFolder,
    										folder});
    			}
    		});
    	}
    }
    
    
    public List<String[]> getGalaxy() throws Exception {
    	List<String[]> tools = new ArrayList<>();
    	collectGalaxyTools("analyses/Galaxy", "analyses/Galaxy", tools);
    	return tools;
    }
    
    
    public void getAppParameters(String app, StringBuilder sb) throws Exception {
        JsonObject params = client.getAnalysisParameters(app);
        if (params.getInt("type",-1) == 0) {
        	String[] tname = app.split("/");
            params.get("values").asArray().forEach(param -> {
            	sb.append(app + "\t" + tname[tname.length - 1] + "\t" + 
            					   param.asObject().get("name").asString() + "\t" +
                                   param.asObject().get("displayName").asString() + "\t" + 
                                   param.asObject().getString("type", "") + "\t" +
                                   param.asObject().getString("elementClass", "") + "\t" +
                                   param.asObject().getBoolean("elementMustExist", false) + "\t" +
                                   param.asObject().get("description").asString() + "\n");
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
