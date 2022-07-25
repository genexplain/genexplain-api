/**
 * Copyright (C) 2022 geneXplain GmbH, Wolfenbuettel, Germany
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
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.PrettyPrint;
import com.genexplain.api.core.GxHttpClient;
import com.genexplain.api.core.GxHttpClientImpl;
import com.genexplain.api.core.GxHttpConnection;
import com.genexplain.api.core.GxHttpConnectionImpl;

import prophecy.common.JSONMinify;

/**
 * @author pst
 *
 */
@Command(name="exporter", description="Lists exporters and exporter parameters")
public class ExporterLister implements ApplicationCommand {
    
	public static final String USAGE_HELP = "exporter - Lists available exporters or their parameters\n" +
	                                        "Usage: <java -jar genexplain-api.jar> exporter INPUT.JSON\n\n" +
	                                        "JSON options:\n\n" +
	                                        "   server   - Server URL, e.g. https://platform.genexplain.com (required)\n" +
	                                        "   user     - Login name/email (required)\n" +
	                                        "   password - Login password (required)\n" +
	                                        "   client   - Name of GxHttpClient class to be used instead of default client (optional)\n" +
	                                        "   path     - Platform path of item to export (optional, default: empty string)\n" +
	                                        "   importer - Name of the exporter to show parameters for given path (optional, default: empty string)\n" +
	                                        "   outfile  - Outfile (optional, default: empty string)\n" +
	                                        "\n" +
	                                        "If 'path' and 'exporter' are provided the program prints parameters for the given exporter\n" +
	                                        "on the specified path. Otherwise, if none or only one of the two parameters is specified, the\n" +
	                                        "program produces a list of available exporters." +
	                                        "\n";
	
    private JsonObject config;
    
    private GxHttpConnection connection;
    private GxHttpClient     client;
    
    public ExporterLister() {
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
    
    
    public void listExporters() throws Exception {
    	JsonObject exporters = client.listExporters();
    	if (exporters.getInt("type",-1) == 0) {
    		String outfile = config.getString("outfile", "");
    		Writer writer;
    		if (outfile.isEmpty()) {
    			writer = new StringWriter();
    		} else {
    			writer = new FileWriter(outfile);
    		}
    		for (JsonValue imp : exporters.get("values").asArray()) {
    			writer.write(imp.asString() + "\n");
    		}
    		if (outfile.isEmpty()) {
    			System.out.println(writer.toString());
    		}
    		writer.close();
    	} else {
    		throw new ApplicationListerException("An error occurred: " + exporters.toString());
    	}
    }
    
    
    public void getExporters() throws Exception {
    	if (config.getString("path", "").isEmpty() || config.getString("exporter", "").isEmpty()) {
    		listExporters();
    	} else {
    		JsonObject params = client.getExporterParameters(config.getString("path", ""), 
    				                                         config.getString("exporter",  ""));
    		if (params.getInt("type",-1) == 0) {
    			String outfile = config.getString("outfile", "");
    			Writer writer;
    			if (outfile.isEmpty()) {
    				writer = new StringWriter();
    				params.get("values").asArray().writeTo(writer, PrettyPrint.indentWithSpaces(4));
    				System.out.println(writer.toString());
    			} else {
    				writer = new FileWriter(outfile);
    				params.get("values").asArray().writeTo(writer, PrettyPrint.indentWithSpaces(4));
    			}
    			writer.close();
    		} else {
    			throw new ApplicationListerException("An error occurred: " + params.toString());
    		}
    	}
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
        } else if (APIRunner.helpFlags.contains(args[0])) {
        	System.out.println(USAGE_HELP);
        	return;
        }
        setParameters(new FileInputStream(args[0]));
        login();
        getExporters();
    }

}