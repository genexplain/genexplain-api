/**
 * Copyright (C) 2019 geneXplain GmbH, Wolfenbuettel, Germany
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

import java.io.FileReader;
import java.io.Reader;

import org.slf4j.LoggerFactory;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.genexplain.api.core.GxHttpClientImpl;
import com.genexplain.api.core.GxHttpConnectionImpl;
import com.genexplain.api.eg.TfbsAnalysisForFolderExample.Parameter;

/**
 *  This example uploads a ZIP archive containing one or more files of a certain type
 *  into the platform and imports the files into the workspace using a dedicated importer. 
 * <p>
 * The following table describes available options and parameters.
 * </p>
 * <table style="border: 1px solid black; border-collapse: collapse;">
 * <tr style="font-size: 12pt;">
 *   <th>Parameter</th><th>Description</th>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">user</td><td>Login user (default: empty)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">password</td><td>Login password (default: empty)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">server</td><td>Server URL, (default: <a href="https://platform.genexplain.com/bioumlweb">https://platform.genexplain.com</a>)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">zipArchive</td><td>Local path of ZIP archive, required, no default</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">outputFolder</td><td>Destination platform folder, required, no default</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">importParams</td><td>Parameters for the file type-specific importer, required, no default</td>
 * </tr>
 * </table>
 * 
 * @author Philip Stegmaier
 *
 */
@GxAPIExample(name="zipImport", description="Imports multiple files of same type as a ZIP archive")
public class ZipImportExample extends AbstractAPIExample {

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
    
    public static final String ZIP_IMPORTER_NAME = "ZIP-archive (*.zip)";
    
    public enum Parameter {
        user(Json.value("")),
        password(Json.value("")),
        server(Json.value(PUBLIC_SERVER)),
        zipArchive(Json.value("")),
        importParams(Json.parse(DEFAULT_IMPORT_PARAMS)),
        outputFolder(Json.value(""));
        
        private JsonValue def;
        
        private Parameter(JsonValue deFault) {
            def = deFault;
        }
        
        public JsonValue getDefault() { return def; } 
    }
    
    private JsonObject config;
    
    
    public ZipImportExample() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    @Override
    public void run() throws Exception {
        if (config == null) {
            throw new IllegalArgumentException("Missing configuration object");
        }
        String outputFolder = config.getString(Parameter.outputFolder.name(), Parameter.outputFolder.def.asString());
        if (outputFolder.isEmpty()) {
            throw new IllegalArgumentException("Missing output folder");
        }
        String zipArchive = config.getString(Parameter.zipArchive.name(), Parameter.zipArchive.def.asString());
        if (zipArchive.isEmpty()) {
            throw new IllegalArgumentException("Missing input ZIP archive");
        }
        JsonValue params = config.get(Parameter.importParams.name());
        if (params == null) {
            params = Parameter.importParams.getDefault();
        }
        connect();
        JsonObject res = client.imPort(zipArchive, 
                outputFolder,
                ZIP_IMPORTER_NAME,
                params);
        logger.info(res.toString());
    }
    
    @Override
    void run(String[] args) throws Exception {
        if (args.length > 0) {
            setConfig(new FileReader(args[0]));
        }
        run();
    }
    
    public ZipImportExample setConfig(Reader reader) throws Exception {
        return setConfig(Json.parse(reader).asObject());
    }
    
    public ZipImportExample setConfig(JsonObject config) throws Exception {
        this.config = config;
        return this;
    }
    
    @Override
    protected void connect() throws Exception {
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
}
