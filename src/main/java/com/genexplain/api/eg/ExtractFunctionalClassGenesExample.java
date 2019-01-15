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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.genexplain.api.core.GxHttpClientImpl;
import com.genexplain.api.core.GxHttpConnectionImpl;

/**
 * <p>This example creates gene sets for the hits of functional gene classes as created by
 * <a href="https://platform.genexplain.com/bioumlweb/#de=analyses/Methods/Functional%20classification/Functional%20classification">the Functional classication tool</a>
 * using the <a href="https://geneontology.org">Gene Ontology</a>.</p>
 * <p>
 * The following table describes available options and parameters.
 * </p>
 * <table style="border: 1px solid black; border-collapse: collapse;">
 * <tr style="font-size: 12pt;">
 *   <th>Parameter</th><th>Description</th>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">user</td><td>login user (default: empty)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">password</td><td>login password (default: empty)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">server</td><td>server URL, (default: <a href="https://platform.genexplain.com/bioumlweb">https://platform.genexplain.com</a>)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">funClassTable</td><td>platform path of functional classification table, required, no default</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">geneSetPath</td><td>platform path of folder for output gene sets, required, no default</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">geneSetFolder</td><td>name of folder for output gene sets, (default: fun_class_gene_sets)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">outputFolder</td><td>name of platform folder to which gene sets are imported, (default: fun_class_gene_sets)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">species</td><td>species to specify for import (default: Human (Homo sapiens)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">localTableFile</td><td>local export path for classification table, table will be exported here, (default: temp_fc_table.txt)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">minGroupSize</td><td>minimal size of groups to consider (default: 1)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">maxGroupSize</td><td>maximal size groups to consider (default: 1e10)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">minHits</td><td>min. number of hits to consider (default: 1)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">maxHits</td><td>max. number of hits to consider (default: 1e10)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">maxAdjustedPval</td><td>cutoff for adjusted P-value (default: 1)</td>
 * </tr>
 * </table>

 * @author Philip Stegmaier
 *
 */
@GxAPIExample(name="extractGeneClasses", description="Extracts gene classes from a functional classification result applying criteria for filtering")
public class ExtractFunctionalClassGenesExample extends AbstractAPIExample {
    
    public enum Parameter {
        user(Json.value("")),
        password(Json.value("")),
        server(Json.value(PUBLIC_SERVER)),
        funClassTable(Json.value("")),
        localTableFile(Json.value("temp_fc_table.txt")), 
        minGroupSize(Json.value(0)),
        maxGroupSize(Json.value(1e10)),
        minHits(Json.value(0)),
        maxHits(Json.value(1e10)),
        maxAdjustedPval(Json.value(1.0)),
        geneSetFolder(Json.value("fun_class_gene_sets")),
        outputFolder(Json.value("fun_class_gene_sets")),
        geneSetPath(Json.value("")),
        species(Json.value("Human (Homo sapiens)"));
        
        private JsonValue def;
        
        private Parameter(JsonValue deFault) {
            def = deFault;
        }
        
        public JsonValue getDefault() { return def; } 
    }
    
    private JsonObject config;
    private String localTableFile;
    private String geneSetFolder;
    private String geneSetPath;
    
    public ExtractFunctionalClassGenesExample() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    @Override
    public void run() throws Exception {
        if (config == null) {
            throw new IllegalArgumentException("Missing configuration object");
        }
        connect();
        localTableFile = config.getString(Parameter.localTableFile.name(), Parameter.localTableFile.def.asString());
        geneSetFolder  = config.getString(Parameter.geneSetFolder.name(), Parameter.geneSetFolder.def.asString());
        geneSetPath  = config.getString(Parameter.geneSetPath.name(), Parameter.geneSetPath.def.asString());
        if (geneSetPath.isEmpty()) {
            throw new IllegalArgumentException("Missing platform path for gene set folder");
        }
        getExport();
        importGeneSets(getFunctionalGroups());
        connection.logout();
    }
    
    @Override
    void run(String[] args) throws Exception {
        if (args.length > 0) {
            setConfig(new FileReader(args[0]));
        }
        run();
    }
    
    private void importGeneSets(List<String> setNames) throws Exception {
        if (setNames.size() == 0)
            return;
        String outputFolder  = config.getString(Parameter.outputFolder.name(), Parameter.outputFolder.def.asString());
        client.createFolder(geneSetPath, outputFolder);
        String file;
        String species = config.getString(Parameter.species.name(), Parameter.species.def.asString());
        for (String name : setNames) {
            logger.info("Importing " + name);
            file = geneSetFolder + File.separator + name + ".txt";
            client.imPort(file, 
                    geneSetPath + "/" + outputFolder, 
                    "Tabular (*.txt, *.xls, *.tab, etc.)",
                    new JsonObject()
                    .add("delimiterType","\t")
                    .add("headerRow",1)
                    .add("dataRow",2)
                    .add("tableType=","Genes: Ensembl")
                    .add("columnForID","Gene")
                    .add("species",species));
        }
    }
    
    private void getExport() throws Exception {
        String funClassTable = config.getString(Parameter.funClassTable.name(), Parameter.funClassTable.def.asString());
        if (funClassTable.isEmpty()) {
            throw new IllegalArgumentException("Functional classification table required");
        }
        OutputStream outStream = new FileOutputStream(new File(localTableFile));
        client.export(funClassTable, 
                "Tab-separated text (*.txt)",
                outStream,
                new JsonObject().add("columns", 
                        new JsonArray().add("ID")
                        .add("Title")
                        .add("Number of hits")
                        .add("Group size")
                        .add("Expected hits")
                        .add("P-value")
                        .add("Adjusted P-value")
                        .add("Hits")
                        ));
        outStream.close();
    }
    
    private List<String> getFunctionalGroups() throws Exception {
        List<String> result = new ArrayList<String>();
        try ( BufferedReader br = new BufferedReader(new FileReader(localTableFile)); ) {
            String line;
            String[] L;
            int ln = 0;
            int minGroupSize = config.getInt(Parameter.minGroupSize.name(), Parameter.minGroupSize.def.asInt());
            int maxGroupSize = config.getInt(Parameter.maxGroupSize.name(), Parameter.maxGroupSize.def.asInt());
            int minHits = config.getInt(Parameter.minHits.name(), Parameter.minHits.def.asInt());
            int maxHits = config.getInt(Parameter.maxHits.name(), Parameter.maxHits.def.asInt());
            double maxAdjustedPval = config.getDouble(Parameter.maxAdjustedPval.name(), Parameter.maxAdjustedPval.def.asDouble());
            
            int  gs, hits;
            double fdr;
            String gsName;
            JsonArray ar;
            JsonObject geneSets = new JsonObject();
            while ((line = br.readLine()) != null) {
                ln++;
                if (ln == 1)
                    continue;
                L = line.split("\t");
                hits  = Integer.parseInt(L[2]);
                gs    = Integer.parseInt(L[3]);
                fdr = Double.parseDouble(L[6]);
                if (hits >= minHits && 
                    hits <= maxHits && 
                    gs >= minGroupSize && 
                    gs <= maxGroupSize && 
                    fdr <= maxAdjustedPval) {
                    gsName = L[0] + "_" + L[1];
                    gsName = gsName.replaceAll("\\W+", "_").substring(0, Math.min(80, gsName.length()));
                    ar = Json.parse(L[7]).asArray();
                    geneSets.add(gsName, ar);
                }
            }
            
            
            if (geneSets.size() > 0) {
                File gsFolder = new File(geneSetFolder);
                if (!gsFolder.exists())
                    gsFolder.mkdirs();
                for (String gn : geneSets.names()) {
                    ar = geneSets.get(gn).asArray();
                    FileWriter fw = new FileWriter(geneSetFolder + File.separator + gn + ".txt");
                    fw.write("Gene\n");
                    ar.forEach(g -> {try {
                        fw.write(g.asString() + "\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }});
                    fw.close();
                    result.add(gn);
                }
            }
        }
        return result;
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
    
    public ExtractFunctionalClassGenesExample setConfig(Reader reader) throws Exception {
        return setConfig(Json.parse(reader).asObject());
    }
    
    public ExtractFunctionalClassGenesExample setConfig(JsonObject config) throws Exception {
        this.config = config;
        return this;
    }
}
