package com.genexplain.api.app;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.PrettyPrint;
import com.genexplain.api.core.GxHttpClient;
import com.genexplain.api.core.GxHttpClientImpl;
import com.genexplain.api.core.GxHttpConnection;
import com.genexplain.api.core.GxHttpConnectionImpl;

import prophecy.common.JSONMinify;

@Command(name="parameters", description="Fetches JSON parameter descriptions for platform tools")
public class ParameterLister implements ApplicationCommand {
	
	public static final String USAGE_HELP = "parameters - Fetches JSON parameter descriptions for platform tools" +
            "Usage: <java -jar genexplain-api.jar> parameters INPUT.JSON\n\n" +
            "JSON options:\n\n" +
            "   server         - Server URL, e.g. https://platform.genexplain.com (required)\n" +
            "   user           - Login name/email (required)\n" +
            "   password       - Login password (required)\n" +
            "   client         - Name of GxHttpClient class to be used instead of default client (optional)\n" +
            "   tools          - Array of tool names (required)\n" +
            "\n";
	
	private JsonObject config;
    private GxHttpConnection connection;
    private GxHttpClient     client;
	
    public ParameterLister() {
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
    
    
    public void getToolParameters() throws Exception {
    	if (config.names().contains("tools")) {
    		JsonArray tools = config.get("tools").asArray();
    		JsonObject params = new JsonObject();
    		JsonObject js;
    		String tname;
    		for (int t = 0; t < tools.size(); ++t) {
    			tname = tools.get(t).asString();
    			if (!tname.isEmpty()) {
    				try {
    					js = client.getAnalysisParameters(tname);
    					if (js.names().contains("parameters")) {
    						params.add(tname, js.get("parameters"));
    					}
    				} catch (Exception e) {
    					System.err.println("An error occurred getting parameters of " + tname + ":\n\n" + e.getMessage());
    				}
    			}
    		}
    		String outfile = config.getString("outfile", "");
    		Writer writer;
    		if (outfile.isEmpty()) {
    			writer = new StringWriter();
    			params.writeTo(writer, PrettyPrint.indentWithSpaces(4));
    			System.out.println(writer.toString());
    		} else {
    			writer = new FileWriter(outfile);
    			params.writeTo(writer, PrettyPrint.indentWithSpaces(4));
    		}
    		writer.close();
    	}
    }
    
    
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
        getToolParameters();
	}
}
