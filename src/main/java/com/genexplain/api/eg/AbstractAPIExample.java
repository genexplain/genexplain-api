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
package com.genexplain.api.eg;

import java.io.Reader;

import org.slf4j.Logger;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.genexplain.api.core.GxHttpClient;
import com.genexplain.api.core.GxHttpClientImpl;
import com.genexplain.api.core.GxHttpConnection;
import com.genexplain.api.core.GxHttpConnectionImpl;
import com.genexplain.api.eg.AbstractAPIExample.Parameter;

/**
 * @author pst
 *
 */
public abstract class AbstractAPIExample {
    
	// Default import parameters are for ASCII text files 
    // containing Ensembl gene ids in a column named Gene
    // and possibly other data in tab-separated columns.
    public static final String DEFAULT_IMPORT_PARAMS = "[\n" + 
            "  {\"name\":\"cleanupFolder\",\"value\":false},\n" + 
            "  {\"name\":\"preserveExtension\",\"value\":false},\n" + 
            "  {\"name\":\"preserveArchiveStructure\",\"value\":false},\n" + 
            "  {\"name\":\"importFormat\",\"value\":\"Tabular (*.txt, *.xls, *.tab, etc.)\"},\n" + 
            "  {\"name\":\"importerProperties\",\"value\": [\n" + 
            "    {\"name\":\"delimiterType\",\"value\":\"0\"},\n" + 
            "    {\"name\":\"processQuotes\",\"value\":true},\n" + 
            "    {\"name\":\"headerRow\",\"value\":\"1\"},\n" + 
            "    {\"name\":\"dataRow\",\"value\":\"2\"},\n" + 
            "    {\"name\":\"commentString\",\"value\":\"\"},\n" + 
            "    {\"name\":\"columnForID\",\"value\":\"Gene\"},\n" + 
            "    {\"name\":\"addSuffix\",\"value\":false},\n" + 
            "    {\"name\":\"tableType\",\"value\":\"Genes: Ensembl\"}\n" + 
            "  ]}\n" + 
            "]";
    
    public static final String CEL_IMPORT_PARAMS = "[\n" + 
            "  {\"name\":\"cleanupFolder\",\"value\":false},\n" + 
            "  {\"name\":\"preserveExtension\",\"value\":false},\n" + 
            "  {\"name\":\"preserveArchiveStructure\",\"value\":true},\n" + 
            "  {\"name\":\"importFormat\",\"value\":\"Affymetrix CEL file (*.cel)\"}\n" + 
            "]";
    
	public enum Parameter {
        user(Json.value("")),
        password(Json.value("")),
        server(Json.value(PUBLIC_SERVER)),
        zipArchive(Json.value("")),
        importParams(Json.parse(DEFAULT_IMPORT_PARAMS)),
        outputFolder(Json.value(""));
        
        protected JsonValue def;
        
        private Parameter(JsonValue deFault) {
            def = deFault;
        }
        
        public JsonValue getDefault() { return def; } 
    }
	
    public final static String PUBLIC_SERVER = "https://platform.genexplain.com";
    
    protected Logger logger;
    
    protected GxHttpConnection connection;
    protected GxHttpClient     client;
    protected JsonObject config;
    
    /**
     * This method is called by {@link com.genexplain.api.eg.ExampleRunner} to
     * execute the example code.
     * 
     * @throws Exception
     */
    abstract public void run() throws Exception;
    
    /**
     * Connections to the public server demo account.
     * 
     * @throws Exception
     */
    protected void connect() throws Exception {
    	if (config == null || config.isEmpty()) {
    		throw new IllegalArgumentException("Error: missing configuration parameters");
    	}
    	connection = new GxHttpConnectionImpl();
        connection.setServer(config.getString(Parameter.server.name(), Parameter.server.def.asString()));
        connection.setUsername(config.getString(Parameter.user.name(), Parameter.user.def.asString()));
        connection.setPassword(config.getString(Parameter.password.name(), Parameter.password.def.asString()));
        connection.setVerbose(true);
        connection.login();
         
        client = new GxHttpClientImpl();
        client.setConnection(connection);
        client.setVerbose(true);
    }
    
    
    public AbstractAPIExample setConfig(Reader reader) throws Exception {
        return setConfig(Json.parse(reader).asObject());
    }
    
    
    public AbstractAPIExample setConfig(JsonObject config) throws Exception {
        this.config = config;
        return this;
    }
    
    
    /**
     * This method is called by {@link com.genexplain.api.eg.ExampleRunner} to
     * execute the example code with arguments.
     * 
     * @throws Exception
     */
    abstract void run(String[] args) throws Exception;
}
