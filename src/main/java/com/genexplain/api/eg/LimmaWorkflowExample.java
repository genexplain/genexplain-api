package com.genexplain.api.eg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.genexplain.api.core.GxHttpClient;
import com.genexplain.api.core.GxHttpClientImpl;
import com.genexplain.api.core.GxHttpConnectionImpl;
import com.genexplain.api.molnets.RegulatorSearch;

/**
 *  Starting from a ZIP archive of CEL files or a file containing normalized expression values,
 *  this example application presents a workflow for gene expression analysis covering functional
 *  characterization of up- and downregulation events, binding site and molecular network analysis.
 *  The binding site analysis is 
 * <a href="https://platform.genexplain.com/bioumlweb/#de=analyses/Methods/Site%20analysis/Search%20for%20enriched%20TFBSs%20(genes)">Search for enriched TFBSs (genes)</a>.
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
 *   <td style="font-weight: bold;">species</td><td>Species to specify for import (default: Human (Homo sapiens)</td>
 * </tr>

 * <tr>
 *   <td style="font-weight: bold;">metadataTable</td>
 *   <td>File with table describing samples (required, no default). The table has three columns named
 *   <b>CEL</b>, <b>Group</b>, and <b>isControl</b> containing CEL file names, group/condition names
 *   and whether a sample represents a control encoded as 1 (true) or 0 (false).</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">parentFolder</td><td>Name of platform folder in which study folder is/will be located, required, no default</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">studyFolderName</td><td>Name of the platform folder for the study, required, no default</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">celZipArchive</td><td>ZIP archive containing CEL files. CEL files should not be located within a folder.</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">normalizedDataFile</td><td>File containing normalized expression values as alternative starting point to CEL files</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">pwmProfile</td><td>Profile of PWMs for binding site analysis (default: the TRANSFAC(&reg;) public profile)</td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">filterUp</td><td>Filter expression for upregulation, default: <i>adj_P_Val &lt; 0.05 && logFC &gt; 0.25</i></td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">filterDown</td><td>Filter expression for downregulation, default: <i>adj_P_Val &lt; 0.05 && logFC &lt; -0.25</i></td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">filterUnch</td><td>Filter expression for no change (unchanged), default: <i>adj_P_Val &gt; 0.5 && logFC &gt; -0.05 && logFC &lt; 0.05</i></td>
 * </tr>
 * <tr>
 *   <td style="font-weight: bold;">outfile</td><td>Output file containing the list of produced results, default: limma_workflow_example_results.txt</td>
 * </tr>
 * </table>
 * 
 * @author Philip Stegmaier
 *
 */
@GxAPIExample(name="limmaWorkflow", description="Uses API and JSON interface for a workflow starting from Limma analysis")
public class LimmaWorkflowExample extends AbstractAPIExample {
    
    public enum Parameter {
        user(Json.value("")),
        password(Json.value("")),
        server(Json.value(PUBLIC_SERVER)),
        metadataTable(Json.value("")),
        parentFolder(Json.value("")),
        studyFolderName(Json.value("")),
        celZipArchive(Json.value("")),
        normalizedDataFile(Json.value("")),
        pwmProfile(Json.value("databases/TRANSFAC(R) (public)/Data/profiles/vertebrate_human_p0.001")),
        filterUp(Json.value("adj_P_Val < 0.05 && logFC > 0.25")),
        filterDown(Json.value("adj_P_Val < 0.05 && logFC < -0.25")),
        filterUnch(Json.value("adj_P_Val > 0.5 && logFC > -0.05 && logFC < 0.05")),
        species(Json.value("Human (Homo sapiens)")),
        outfile(Json.value("limma_workflow_example_results.txt"));
        
        private JsonValue def;
        
        private Parameter(JsonValue deFault) {
            def = deFault;
        }
        
        public JsonValue getDefault() { return def; } 
    }

    public static final String NORMALIZED_DATA   = "Normalized_data";
    public static final String LIMMA_FOLDER_NAME = "limma";
    
    public LimmaWorkflowExample() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    private JsonObject config;
    private JsonObject metaTable;
    private String species;
    private String pwmProfile;
    private String controlGroup;
    private String parentFolder;
    private String studyFolderName;
    private String studyFolder;
    private String celFolderName;
    private String celFolder;
    private String limmaFolder;
    private String normalizedData;
    
    @Override
    public void run() throws Exception {
        if (config == null) {
            throw new IllegalArgumentException("Missing configuration object");
        }
        JsonObject res = analyze();
        String js = new JSONObject(res.toString()).toString(4);
        if (config.get(Parameter.outfile.name()) != null) {
            FileWriter fw = new FileWriter(config.getString(Parameter.outfile.name(), 
                    Parameter.outfile.def.asString()));
            fw.write(js);
            fw.close();
        } else {
            logger.info(js);
        }
    }
    
    private JsonObject analyze() throws Exception {
        species    = config.getString(Parameter.species.name(), Parameter.species.def.asString());
        pwmProfile = config.getString(Parameter.pwmProfile.name(), Parameter.pwmProfile.def.asString());
        parentFolder = config.getString(Parameter.parentFolder.name(), Parameter.parentFolder.def.asString());
        if (parentFolder.isEmpty())
            throw new IllegalArgumentException("Missing parent folder");
        studyFolderName = config.getString(Parameter.studyFolderName.name(), Parameter.studyFolderName.def.asString());
        if (studyFolderName.isEmpty())
            throw new IllegalArgumentException("Missing study folder name");
        studyFolder = parentFolder + "/" + studyFolderName;
        metaTable = getMetaTable();
        logger.info(new JSONObject(metaTable.toString()).toString(4));
        JsonObject createdResults = new JsonObject();
        connect();
        logger.info("Creating study folder in platform workspace");
        client.createFolder(parentFolder, studyFolderName);
        normalizedData = studyFolder + "/" + NORMALIZED_DATA;
        if (config.get(Parameter.celZipArchive.name()) != null) {
            uploadZipArchive();
            normalizeCelFiles();
        } else {
            importNormalizedData();
        }
        makeNormalizationQualityPlots();
        runPCA();
        Map<String, String> limmaResults = runLimma(createdResults);
        analyzeFunctionalClasses(limmaResults, createdResults);
        runLRPath(limmaResults, createdResults);
        analyzeBindingSites(limmaResults, createdResults);
        runRegulatorSearch(limmaResults, createdResults);
        connection.logout();
        return createdResults;
    }
    
    private void importNormalizedData() throws Exception {
        String file = config.getString(Parameter.normalizedDataFile.name(), Parameter.normalizedDataFile.def.asString());
        client.importTable(file, studyFolder, NORMALIZED_DATA, 
                true, GxHttpClient.ColumnDelimiter.Tab, 
                1, 2, "#", "Gene", 
                false, "Probes: Affymetrix", "Human (Homo sapiens)");
        waitForResult(studyFolderName, NORMALIZED_DATA);
    }
    
    private void runRegulatorSearch(Map<String, String> limmaResults, JsonObject createdResults) throws Exception {
        String regSearchFolder = studyFolder + "/regulators";
        client.createFolder(studyFolder, "regulators");
        RegulatorSearch regSearch = new RegulatorSearch();
        limmaResults.forEach((name, path) -> {
            JsonObject params = new JsonObject()
                    .add("sourcePath", path + " Entrez genes UP")
                    .add("weightColumn", "signLog10P")
                    .add("isInputSizeLimited", false)
                    .add("inputSizeLimit", 100000)
                    .add("maxRadius", 2)
                    .add("scoreCutoff", 0.2)
                    .add("bioHub", "GeneWays hub")
                    .add("species", species)
                    .add("calculatingFDR", true)
                    .add("FDRcutoff", 0.01)
                    .add("ZScoreCutoff", 1.05)
                    .add("penalty", 0.1)
                    .add("isoformFactor", true)
                    .add("contextDecorators", new JsonArray()
                            .add(new JsonObject()
                                    .add("tableName", path + " Entrez genes")
                                    .add("tableColumn", "signLog10P")
                                    .add("decayFactor", 0.1)));
            if (createdResults.get(name) == null) {
                createdResults.add(name, new JsonObject());
            }
            JsonObject js = createdResults.get(name).asObject();
            try {
                String output = regSearchFolder + "/" + name + " Regulators UP";
                client.analyze("Regulator search", 
                        regSearch.getSearchParameters(
                                params.add("outputTable", output)),
                        false, true, true);
                js.add("Regulators UP", output);
                
                output = regSearchFolder + "/" + name + " Effectors UP";
                client.analyze("Effector search", 
                        regSearch.getSearchParameters(
                                params.add("outputTable", output)),
                        false, true, true);
                js.add("Effectors UP", output);
                
                params.add("sourcePath", path + " Entrez genes DOWN");
                output = regSearchFolder + "/" + name + " Regulators DOWN";
                client.analyze("Regulator search", 
                        regSearch.getSearchParameters(
                                params.add("outputTable", output)),
                        false, true, true);
                js.add("Regulators DOWN", output);
                
                output = regSearchFolder + "/" + name + " Effectors DOWN";
                client.analyze("Effector search", 
                        regSearch.getSearchParameters(
                                params.add("outputTable", output)),
                        false, true, true);
                js.add("Effectors DOWN", output);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        });

    }
    
    private void analyzeBindingSites(Map<String, String> limmaResults, JsonObject createdResults) throws Exception {
        String siteFolder = studyFolder + "/binding_sites";
        client.createFolder(studyFolder, "binding_sites");
        limmaResults.forEach((name, path) -> {
            String output = siteFolder + "/" + name + " Enriched motifs UP";
            JsonObject params = new JsonObject()
                    .add("yesSetPath", path + " Genes UP")
                    .add("noSetPath", path + " Genes UNCH")
                    .add("from", -1000)
                    .add("to", 100)
                    .add("species",  species)
                    .add("profilePath", pwmProfile)
                    .add("doSample", true)
                    .add("sampleNum", 5)
                    .add("sampleSize", 1000)
                    .add("siteFeCutoff", 1.1)
                    .add("seqFeCutoff", 1.0)
                    .add("output", output);
            if (createdResults.get(name) == null) {
                createdResults.add(name, new JsonObject());
            }
            JsonObject js = createdResults.get(name).asObject();
            try {
                client.analyze("Search for enriched TFBSs (genes)", 
                        params, 
                        false, true, true);
                js.add("Enriched motifs UP", output);
                output = siteFolder + "/" + name + " Enriched motifs DOWN";
                client.analyze("Search for enriched TFBSs (genes)", 
                        params
                        .add("yesSetPath", path + " Genes DOWN")
                        .add("output", output), 
                        false, true, true);
                js.add("Enriched motifs DOWN", output);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        });
    }
    
    private void runLRPath(Map<String, String> limmaResults, JsonObject createdResults) throws Exception {
        String lrpathFolder = studyFolder + "/lrpath";
        client.createFolder(studyFolder, "lrpath");
        limmaResults.forEach((name, path) -> {
            JsonObject params = new JsonObject()
                    .add("inputTablePath", path + " Genes")
                    .add("species", species)
                    .add("significanceColumn", "adj.P.Val")
                    .add("significanceCut", 0.05)
                    .add("isLower", true)
                    .add("onlyClassifiedGenes", true)
                    .add("predictorColumn", "signLog10P")
                    .add("treatAsPvalues", false);
            if (createdResults.get(name) == null) {
                createdResults.add(name, new JsonObject());
            }
            JsonObject js = createdResults.get(name).asObject();
            try {
                String output = lrpathFolder + "/" + name + " Genes GO sign";
                client.analyze("LRPath", 
                        params
                        .add("bioHub", "Full gene ontology classification")
                        .add("outputTable", output), 
                        false, true, true);
                js.add("LRPath GO sign", output);
                output = lrpathFolder + "/" + name + " Genes Reactome sign";
                client.analyze("LRPath", 
                        params
                        .add("bioHub", "Reactome pathways (63)")
                        .add("outputTable", output), 
                        false, true, true);
                js.add("LRPath Reactome sign", output);
                output = lrpathFolder + "/" + name + " Genes GO mod";
                client.analyze("LRPath", 
                        params
                        .add("predictorColumn", "modLog10P")
                        .add("bioHub", "Full gene ontology classification")
                        .add("outputTable", output), 
                        false, true, true);
                js.add("LRPath GO mod", output);
                output = lrpathFolder + "/" + name + " Genes Reactome mod";
                client.analyze("LRPath", 
                        params
                        .add("predictorColumn", "modLog10P")
                        .add("bioHub", "Reactome pathways (63)")
                        .add("outputTable", output), 
                        false, true, true);
                js.add("LRPath Reactome mod", output);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        });
    }
    
    private void analyzeFunctionalClasses(Map<String, String> limmaResults, JsonObject createdResults) throws Exception {
        String funClassFolder = studyFolder + "/fun_class";
        client.createFolder(studyFolder, "fun_class");
        limmaResults.forEach((name, path) -> {
            JsonObject params = new JsonObject()
                    .add("sourcePath", path + " Genes UP")
                    .add("species", species)
                    .add("minHits", 2)
                    .add("pvalueThreshold", 1.0);
            if (createdResults.get(name) == null) {
                createdResults.add(name, new JsonObject());
            }
            JsonObject js = createdResults.get(name).asObject();
            try {
                String output = funClassFolder + "/" + name + " Genes UP GO";
                client.analyze("Functional classification", 
                        params
                        .add("bioHub", "Full gene ontology classification")
                        .add("outputTable", output), 
                        false, true, true);
                js.add("Funclass GO UP", output);
                output = funClassFolder + "/" + name + " Genes UP Reactome";
                client.analyze("Functional classification", 
                        params
                        .add("bioHub", "Reactome pathways (63)")
                        .add("outputTable", output), 
                        false, true, true);
                js.add("Funclass Reactome UP", output);
                output = funClassFolder + "/" + name + " Genes DOWN GO";
                client.analyze("Functional classification", 
                        params
                        .add("sourcePath", path + " Genes DOWN")
                        .add("bioHub", "Full gene ontology classification")
                        .add("outputTable", output), 
                        false, true, true);
                js.add("Funclass GO DOWN", output);
                output = funClassFolder + "/" + name + " Genes DOWN Reactome";
                client.analyze("Functional classification", 
                        params
                        .add("bioHub", "Reactome pathways (63)")
                        .add("outputTable", output), 
                        false, true, true);
                js.add("Funclass Reactome DOWN", output);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        });
    }
    
    private Map<String, String> runLimma(JsonObject createdResults) {
        limmaFolder = studyFolder + "/" + LIMMA_FOLDER_NAME;
        JsonArray controlArrays = metaTable.get(controlGroup).asObject().get("cel").asArray();
        if (controlArrays.size() < 2) {
            throw new IllegalArgumentException(controlGroup + " has < 2 arrays. At least 2 samples per group/condition required.");
        }
        Map<String, String> limmaResults = new HashMap<>();
        String filterUp   = config.getString(Parameter.filterUp.name(), Parameter.filterUp.def.asString());
        String filterDown = config.getString(Parameter.filterDown.name(), Parameter.filterDown.def.asString());
        String filterUnch = config.getString(Parameter.filterUnch.name(), Parameter.filterUnch.def.asString());
        
        metaTable.forEach(g -> {
            if (!g.getName().equals(controlGroup)) {
                JsonArray arrays = g.getValue().asObject().get("cel").asArray();
                if (arrays.size() < 2)
                    throw new IllegalArgumentException(g.getName() + " has < 2 arrays. At least 2 samples per group/condition required.");
                JsonObject params = new JsonObject()
                        .add("inputTablePath", normalizedData)
                        .add("inputLogarithmBase", "log2")
                        .add("firstName", g.getName())
                        .add("firstColumns", arrays)
                        .add("secondName", controlGroup)
                        .add("secondColumns", controlArrays)
                        .add("outputFolderPath", limmaFolder);
                String resultName  = g.getName() + " vs. " + controlGroup;
                String geneTable   = limmaFolder + "/" + resultName + " Genes";
                String entrezTable = limmaFolder + "/" + resultName + " Entrez genes";
                JsonObject convertParams = new JsonObject()
                        .add("sourceTable", limmaFolder + "/" + resultName)
                        .add("species", species)
                        .add("sourceType", "Probes: Affymetrix")
                        .add("targetType", "Genes: Ensembl")
                        .add("outputTable", geneTable);
                JsonObject filterParams = new JsonObject()
                        .add("inputPath", geneTable)
                        .add("filteringMode", "Rows for which expression is true");
                if (createdResults.get(resultName) == null) {
                    createdResults.add(resultName, new JsonObject());
                }
                JsonObject js = createdResults.get(resultName).asObject();
                try {
                    logger.info("Analyzing " + g.getName() + " vs. " + controlGroup);
                    client.analyze("Limma", params, false, true, true);
                    limmaResults.put(resultName, limmaFolder + "/" + resultName);
                    js.add("Limma", limmaResults.get(resultName));
                    client.analyze("Convert table", convertParams, false, true, true);
                    client.analyze("Filter table", 
                            filterParams
                              .add("filterExpression", filterUp)
                              .add("outputPath", geneTable + " UP"), 
                            false, true, true);
                    client.analyze("Filter table", 
                            filterParams
                              .add("filterExpression", filterDown)
                              .add("outputPath", geneTable + " DOWN"), 
                            false, true, true);
                    client.analyze("Filter table", 
                            filterParams
                              .add("filterExpression", filterUnch)
                              .add("outputPath", geneTable + " UNCH"), 
                            false, true, true);
                    client.analyze("Convert table",
                            convertParams
                            .add("targetType", "Genes: Entrez")
                            .add("outputTable", entrezTable),
                            false, true, true);
                    client.analyze("Filter table", 
                            filterParams
                              .add("inputPath", entrezTable)
                              .add("filterExpression", filterUp)
                              .add("outputPath", entrezTable + " UP"), 
                            false, true, true);
                    client.analyze("Filter table", 
                            filterParams
                              .add("filterExpression", filterDown)
                              .add("outputPath", entrezTable + " DOWN"),
                            false, true, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            }
        });
        return limmaResults;
    }
    
    private void runPCA() throws Exception {
        JsonObject params = new JsonObject()
                .add("inputTablePath", normalizedData)
                .add("outputFolderPath", studyFolder + "/pca");
        String[] prefs = {"first", "second", "third", "fourth", "fifth"};
        List<String> groups = metaTable.names();
        for (int g = 0; g < Math.min(groups.size(), prefs.length); ++g) {
            params.add(prefs[g] + "Name", groups.get(g))
                  .add(prefs[g] + "Columns", metaTable.get(groups.get(g)).asObject().get("cel").asArray());
        }
        client.analyze("PCA", params, false, false, false);
    }
    
    private void makeNormalizationQualityPlots() throws Exception {
        client.analyze("Normalization quality plots",
                new JsonObject().add("inputTablePath", normalizedData)
                .add("inputLogarithmBase", "log2")
                .add("outputFolderPath", studyFolder + "/" + "normalization_plots"),
                false, false, false);
    }
    
    private void normalizeCelFiles() throws Exception {
        JsonArray params = new JsonArray();
        JsonArray cfiles = new JsonArray();
        metaTable.forEach(g -> {
            g.getValue()
                .asObject()
                .get("cel")
                .asArray()
                .forEach(c -> 
                    cfiles.add(celFolder + "/" + c.asString())
                );
        });
        params.add(new JsonObject().add("name", "celFiles").add("value", cfiles))
              .add(new JsonObject().add("name", "method").add("value", "RMA"))
              .add(new JsonObject().add("name", "bgCorrection").add("value", "RMA"))
              .add(new JsonObject().add("name", "normMethod").add("value", "quantiles"))
              .add(new JsonObject().add("name", "pmCorrection").add("value", "pmonly"))
              .add(new JsonObject().add("name", "summarization").add("value", "medianpolish"))
              .add(new JsonObject().add("name", "cdf").add("value", ""))
              .add(new JsonObject().add("name", "outputLogarithmBase").add("value", "log2"))
              .add(new JsonObject().add("name", "outputPath").add("value", normalizedData));
        JsonObject res = client.analyze("Affymetrix normalization", params, false, true, true);
        waitForResult(studyFolderName, NORMALIZED_DATA);
        logger.info(res.toString());
    }
    
    private void uploadZipArchive() throws Exception {
        logger.info("Importing ZIP archive into study folder");
        String zipArchive = config.getString(Parameter.celZipArchive.name(), Parameter.celZipArchive.def.asString());
        celFolderName = FilenameUtils.getBaseName(zipArchive);
        celFolder = studyFolder + "/" + celFolderName;
        JsonObject res = client.imPort(zipArchive, 
                studyFolder,
                ZipImportExample.ZIP_IMPORTER_NAME,
                Json.parse(ZipImportExample.CEL_IMPORT_PARAMS));
        waitForResult(studyFolderName, celFolderName);
        logger.info(res.toString());
    }
    
    /**
     * Waits a few seconds for result to be ready.
     * 
     * @param folder - Plattform folder of result item
     * @param name - Name of result item
     * @throws Exception
     */
    private void waitForResult(String folder, String name) throws Exception {
        int waitTime = 0;
        do {
            Thread.sleep(3000L);
            waitTime += 3;
        } while (client.existsElement(folder, name).getBoolean("exists", false) && waitTime < 10);
    }
    
    private JsonObject getMetaTable() throws Exception {
        String mfile = config.getString(Parameter.metadataTable.name(), Parameter.metadataTable.def.asString());
        if (mfile.isEmpty())
            throw new IllegalArgumentException("Missing metadata table");
        JsonObject meta = new JsonObject();
        try (BufferedReader br = new BufferedReader(new FileReader(mfile))) {
            String line;
            String[] L;
            int ln = 0;
            JsonObject group;
            while ((line = br.readLine()) != null) {
                ln++;
                L = line.split("\t");
                if (ln == 1)
                    continue;
                if (meta.get(L[1]) == null)
                    meta.add(L[1], new JsonObject()
                            .add("isControl", Integer.parseInt(L[2]))
                            .add("cel", new JsonArray()));
                group = meta.get(L[1]).asObject();
                if (controlGroup == null && group.getInt("isControl", -1) == 1) {
                    controlGroup = L[1];
                    logger.info("Control group " + controlGroup);
                }
                group.get("cel").asArray().add(L[0]);
            }
        }
        return meta;
    }
    
    @Override
    void run(String[] args) throws Exception {
        if (args.length > 0) {
            setConfig(new FileReader(args[0]));
        }
        run();
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
    
    public LimmaWorkflowExample setConfig(Reader reader) throws Exception {
        return setConfig(Json.parse(reader).asObject());
    }
    
    public LimmaWorkflowExample setConfig(JsonObject config) throws Exception {
        this.config = config;
        return this;
    }
}
