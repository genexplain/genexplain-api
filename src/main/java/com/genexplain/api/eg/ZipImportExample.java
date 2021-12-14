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

    public static final String ZIP_IMPORTER_NAME = "ZIP-archive (*.zip)";
    
    
    public ZipImportExample() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    
    @Override
    public void run() throws Exception {
        if (config == null) {
            System.out.println("Please provide configuration parameters");
            return;
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
}
