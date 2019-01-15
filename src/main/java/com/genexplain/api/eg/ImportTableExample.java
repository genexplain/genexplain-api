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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import org.slf4j.LoggerFactory;

import com.eclipsesource.json.JsonObject;
import com.genexplain.api.core.GxHttpClient;

/**
 * Imports a table from local text file to specified platform folder.
 * <p>
 * <b>NOTE</b>
 * <br>Running this example creates and deletes a local file with contents extracted from the
 * API jar. The default relative path of the created file is <b>gx_mouse_import_data.txt</b>.
 * An alternative path can be specified using the system property (&minus;D) <b>localImportTable</b>,
 * e.g. <i>&minus;DlocalImportTable=gx_test_import.txt</i>.</p>  
 * @author pst
 */
@GxAPIExample(name="importTable", description="Imports a table to the specified path")
public class ImportTableExample extends AbstractAPIExample {

    public ImportTableExample() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    /* (non-Javadoc)
     * @see com.genexplain.api.eg.AbstractAPIExample#run()
     */
    @Override
    public void run() throws Exception {
        connect();
        
        String localFile = "gx_mouse_import_data.txt";
        if (!(System.getProperty("localImportTable") == null || System.getProperty("localImportTable").isEmpty())) {
            localFile = System.getProperty("localImportTable");
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("data/mouse_import_data.txt")));
             FileWriter fw = new FileWriter(localFile);) {
            String line;
            while ((line = br.readLine()) != null) {
                fw.write(line + System.lineSeparator());
            }
        }
        
        JsonObject result = client.importTable(localFile,
                "data/Projects/Demo project/Data",
                "imported_table",
                false,
                GxHttpClient.ColumnDelimiter.Tab,
                1, 2, "", "ID",
                false, "Genes: Ensembl", "Mus musculus");
        
        new File(localFile).delete();
        logger.info(result.toString());
    }

    @Override
    void run(String[] args) throws Exception {
        run();
    }
}
