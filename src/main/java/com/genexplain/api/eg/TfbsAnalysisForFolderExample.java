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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.genexplain.api.core.GxHttpClientImpl;
import com.genexplain.api.core.GxHttpConnectionImpl;

/**
 *  This example analyzes enrichment of binding sites in gene promoters of all gene sets of specified
 *  input folder. The binding site analysis is 
 * <a href="https://platform.genexplain.com/bioumlweb/#de=analyses/Methods/Site%20analysis/Search%20for%20enriched%20TFBSs%20(genes)">Search for enriched TFBSs (genes)</a>.
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
 *   <td style="font-weight: bold;">species</td><td>species to specify for import (default: Human (Homo sapiens)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">inputFolder</td><td>platform path of input folder, required, no default</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">inputRegex</td><td>regular expression to filter folder contents for input files</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">noSet</td><td>path of NO set, required, no default</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">profile</td><td>PWM profile to use</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">species</td><td>species of gene sets in folder (default: Human (Homo sapiens) )</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">outputFolder</td><td>folder for output (default: input folder)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">doSample</td><td>true or false to draw subsamples from the NO set (default: false)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">sampleNum</td><td>number of NO set samples (default: 5)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">sampleSize</td><td>size of NO set samples (default: 1000)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">siteFeCutoff</td><td>fold enrichment cutoff for sites (default: 1.0)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">siteFdrCutoff</td><td>FDR cutoff for site enrichment (default: 1.0)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">seqFeCutoff</td><td>fold enrichment cutoff for sequences with sites (default: 0.0)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">seqFdrCutoff</td><td>FDR cutoff for sequences with sites (default: 1.0)</td>
 * </tr>
 * </table>

 * @author Philip Stegmaier
 *
 */
@GxAPIExample(name="tfbsAnalysisForFolder", description="Analyzes binding site enrichment for all gene sets in a folder")
public class TfbsAnalysisForFolderExample extends AbstractAPIExample {
    
    public enum Parameter {
        user(Json.value("")),
        password(Json.value("")),
        server(Json.value(PUBLIC_SERVER)),
        inputFolder(Json.value("")),
        inputRegex(Json.value(".+")),
        noSet(Json.value("")),
        profile(Json.value("databases/TRANSFAC(R) (public)/Data/profiles/vertebrate_human_p0.001_non3d")),
        species(Json.value("Human (Homo sapiens)")),
        outputFolder(Json.value("")),
        doSample(Json.value(false)),
        sampleNum(Json.value(5)),
        sampleSize(Json.value(1000)),
        siteFeCutoff(Json.value(1.0)),
        siteFdrCutoff(Json.value(1.0)),
        seqFeCutoff(Json.value(0.0)),
        seqFdrCutoff(Json.value(1.0)), 
        fromTss(Json.value(-1000)),
        toTss(Json.value(100));
        
        private JsonValue def;
        
        private Parameter(JsonValue deFault) {
            def = deFault;
        }
        
        public JsonValue getDefault() { return def; } 
    }
    
    private JsonObject config;
    private String     inputFolder;
    
    public TfbsAnalysisForFolderExample() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    @Override
    public void run() throws Exception {
        if (config == null) {
            throw new IllegalArgumentException("Missing configuration object");
        }
        inputFolder  = config.getString(Parameter.inputFolder.name(), Parameter.inputFolder.def.asString());
        connect();
        analyzeTFSites(getGeneSetNames());
        connection.logout();
    }
    
    @Override
    void run(String[] args) throws Exception {
        if (args.length > 0) {
            setConfig(new FileReader(args[0]));
        }
        run();
    }
    
    private void analyzeTFSites(List<String> geneSets) throws Exception {
        if (geneSets.isEmpty())
            return;
        String noSet = config.getString(Parameter.noSet.name(), Parameter.noSet.def.asString());
        if (noSet.isEmpty())
            throw new IllegalArgumentException("Missing NO-set path");
        String profile = config.getString(Parameter.profile.name(), Parameter.profile.def.asString());
        if (profile.isEmpty())
            throw new IllegalArgumentException("Missing profile path");
        String species = config.getString(Parameter.species.name(), Parameter.species.def.asString());
        if (species.isEmpty())
            throw new IllegalArgumentException("Missing species argument");
        String outputFolder = config.getString(Parameter.outputFolder.name(), inputFolder);
        if (outputFolder.isEmpty()) {
            throw new IllegalArgumentException("Missing output folder");
        } else {
            String[] of = outputFolder.split("\\/");
            logger.info(outputFolder);
            client.createFolder(String.join("/", Arrays.copyOfRange(of,0,of.length-2)), of[of.length-1]);
        }
        boolean doSample = config.getBoolean(Parameter.doSample.name(), Parameter.doSample.def.asBoolean());
        int sampleNum = config.getInt(Parameter.sampleNum.name(), Parameter.sampleNum.def.asInt());
        int sampleSize = config.getInt(Parameter.sampleSize.name(), Parameter.sampleSize.def.asInt());
        double siteFeCutoff = config.getDouble(Parameter.siteFeCutoff.name(), Parameter.siteFeCutoff.def.asDouble());
        double siteFdrCutoff = config.getDouble(Parameter.siteFdrCutoff.name(), Parameter.siteFdrCutoff.def.asDouble());
        double seqFeCutoff = config.getDouble(Parameter.seqFeCutoff.name(), Parameter.seqFeCutoff.def.asDouble());
        double seqFdrCutoff = config.getDouble(Parameter.seqFdrCutoff.name(), Parameter.seqFdrCutoff.def.asDouble());
        int fromTss = config.getInt(Parameter.fromTss.name(), Parameter.fromTss.def.asInt());
        int toTss = config.getInt(Parameter.toTss.name(), Parameter.toTss.def.asInt());
        for (String gs : geneSets) {
            try {
              client.analyze("Search for enriched TFBSs (genes)",
                      new JsonObject()
                      .add("yesSetPath", inputFolder + "/" + gs)
                      .add("noSetPath", noSet)
                      .add("profilePath", profile)
                      .add("species", species)
                      .add("from", fromTss)
                      .add("to", toTss)
                      .add("doSample", doSample)
                      .add("sampleNum", sampleNum)
                      .add("sampleSize", sampleSize)
                      .add("siteFeCutoff", siteFeCutoff)
                      .add("siteFdrCutoff", siteFdrCutoff)
                      .add("seqFeCutoff", seqFeCutoff)
                      .add("seqFdrCutoff", seqFdrCutoff)
                      .add("output",  outputFolder + "/" + gs + "_Enriched_motifs"),
                      false, true, true);
            } catch (Exception e) {
                logger.error("An error occurred with gene set " + gs);
                e.printStackTrace();
            }
        }
    }
    
    private List<String> getGeneSetNames() throws Exception {
        String inputRegex = config.getString(Parameter.inputRegex.name(), Parameter.inputRegex.def.asString());
        List<String> names = new ArrayList<>();
        JsonObject listing = client.list(inputFolder);
        JsonValue namesVal = listing.get("names");
        if (namesVal != null && namesVal.asArray().isEmpty() == false) {
            namesVal.asArray().forEach(name -> {
                String sn = name.asObject().getString("name", "");
                if (sn.matches(inputRegex)) {
                    names.add(sn);
                }
            });
        }
        return names;
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
    
    
    
    public TfbsAnalysisForFolderExample setConfig(Reader reader) throws Exception {
        return setConfig(Json.parse(reader).asObject());
    }
    
    public TfbsAnalysisForFolderExample setConfig(JsonObject config) throws Exception {
        this.config = config;
        return this;
    }
}
