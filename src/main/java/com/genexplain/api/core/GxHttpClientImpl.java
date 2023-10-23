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

package com.genexplain.api.core;

import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.genexplain.util.GxUtil;

/**
 * Implementation of {@link com.genexplain.api.core.GxHttpClient}
 * 
 * @see com.genexplain.api.core.GxHttpClient
 * 
 * @author pst
 *
 */
public class GxHttpClientImpl implements GxHttpClient {    
    
    public static final String  PROJECT_NAME_REGEX      = "[a-zA-Z0-9]{3,}[a-zA-Z0-9,\\.\\s\\(\\)\\[\\]\\_-]*";
    public static final Pattern PROJECT_NAME_PATTERN    = Pattern.compile(PROJECT_NAME_REGEX);
    public static final int     NON_JSON_RESPONSE_LIMIT = 10;
    public static final String  JOB_PREF = "JOBID";
	
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private SimpleDateFormat timeFormat = new SimpleDateFormat("mmssSSS");
    
    private int jobNo = 0;
    
    private boolean verbose = false;
    
	private GxHttpConnection con = null;
	
	private String blockingProcessError = "";
    
	
	/**
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#importTable(String, String, String, boolean, String, int, int, String, String, boolean, String, String)
	 */
	@Override
	public JsonObject importTable(String file, String folder, String tableName, boolean processQuotes,
            GxHttpClient.ColumnDelimiter delim, int headerRow, int dataRow, String commentString, String columnForID,
            boolean addSuffix, String tableType, String species) throws Exception {
	    GxUtil.showMessage(verbose, "Importing: " + file + " into " + folder, logger, GxUtil.LogLevel.INFO);
        String fileId = nextJobId();
        String jobId  = nextJobId();
        
        HttpPost   httpPost  = new HttpPost(con.getServer() + con.getBasePath() + Path.UPLOAD.getPath());
        FileBody   fileBody  = new FileBody(new File(file));
        StringBody idPart    = new StringBody(fileId, ContentType.TEXT_PLAIN);
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("fileID",idPart)
                .addPart("file", fileBody)
                .build();
        httpPost.setEntity(reqEntity);
        CloseableHttpResponse response = con.getHttpClient().execute(httpPost);
        try {
            logger.info(EntityUtils.toString(response.getEntity()));
        } finally {
            response.close();
        }
        String pq = "true";
        if (!processQuotes)
            pq = "false";
        String sf = "true";
        if (!addSuffix)
            sf = "false";
        Map<String,String> iparams = new HashMap<>();
        String params = "[\n {\"name\": \"tableName\",\n\"value\": \"" + tableName + "\"},\n" +
                            "{\"name\": \"delimiterType\",\n\"value\": \"" + delim.getValue() + "\"},\n" + 
                            "{\"name\": \"processQuotes\",\n \"value\": " + pq + "},\n" +
                            "{\"name\": \"headerRow\",\n \"value\": \"" + headerRow + "\"},\n" +
                            "{\"name\": \"dataRow\",\n \"value\": \"" + dataRow + "\"},\n" +
                            "{\"name\": \"commentString\",\"value\": \"" + commentString + "\"},\n" +
                            "{\"name\": \"columnForID\",\"value\": \"" + columnForID + "\"},\n" + 
                            "{\"name\": \"addSuffix\",\"value\": " + sf + "},\n" +
                            "{\"name\": \"tableType\",\"value\": \"" + tableType + "\"},\n" +
                            "{\"name\": \"species\",\"value\": \"" + species + "\"}\n]";
        iparams.put("type", "import");
        iparams.put("de", folder);
        iparams.put("fileID", fileId);
        iparams.put("jobID", jobId);
        iparams.put("format", "Tabular (*.txt, *.xls, *.tab, etc.)");
        iparams.put("json", params);
        return con.queryJSON(con.getBasePath() + Path.IMPORT.getPath(), iparams);
	}
	
	/**
	 * Creates a new project in the workspace of the user whose
	 * username and password are provided parameters.
	 * <p>
	 * Required parameters are:
	 * <ul>
	 * <li>user     - username for authorization
	 * <li>password - password for authorization
	 * <li>project  - a (new) name for the project that matches {@link #PROJECT_NAME_REGEX}
	 * </ul>
	 * </p>
	 * A project description parameter named 'description' is optional.
	 * 
	 * @param params
	 *           Parameters needed for project creation (see description)
	 *           
	 * @return The response from the platform
	 * 
	 * @throws IllegalArgumentException
	 *           If a required parameter is missing or the project name is invalid
	 *           
	 * @throws Exception
	 *           Other exceptions may be caused by method calls
	 *           
	 * @see com.genexplain.api.core.GxHttpClient#createProject(Map)
	 */
	@Override
	public JsonObject createProject(Map<String,String> params) throws IllegalArgumentException, Exception {
	    final StringBuilder sb = new StringBuilder();
        String[] required = new String[]{"user","pass","project"};
        Arrays.asList(required).forEach(par -> {
            if (!params.containsKey(par)) {
                sb.append("Missing parameter '" + par + "'.");
            }
        });
        if (sb.length() > 0) {
            throw new IllegalArgumentException(sb.toString());
        }
        validateProjectName(params.get("project"));
        GxUtil.showMessage(verbose, "Creating project: " + params.get("project"), logger, GxUtil.LogLevel.INFO);
	    if (!params.containsKey("description") || params.get("description").isEmpty()) {
            params.put("description", params.get("project"));
        }
	    return con.queryJSON(con.getBasePath() + Path.CREATE_PROJECT.getPath(), params);
    }
	
	private void validateProjectName(String name) throws IllegalArgumentException {
        if (name.isEmpty() || !PROJECT_NAME_PATTERN.matcher(name).matches())
            throw new IllegalArgumentException("Invalid project name (may be empty).");
    }
    
	/*
	 * (non-Javadoc)
	 * @see com.genexplain.api.core.GxHttpClient#createFolder(java.lang.String, java.lang.String)
	 */
    @Override
    public JsonObject createFolder(String path, String name) throws Exception {
        GxUtil.showMessage(verbose, "Creating folder: " + name + " in " + path, logger, GxUtil.LogLevel.INFO);
        Map<String,String> params = new HashMap<>();
        params.put("service", "access.service");
        params.put("command", "25");
        params.put("dc", path);
        params.put("de", name);
        return con.queryJSON(con.getBasePath() + Path.CREATE_FOLDER.getPath(), params);
    }
	
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#deleteElement(java.lang.String, java.lang.String)
     */
	@Override
    public JsonObject deleteElement(String folder, String name) throws Exception {
	    GxUtil.showMessage(verbose, "Deleting element: " + name + " in " + folder, logger, GxUtil.LogLevel.INFO);
        if (!folder.startsWith("data")) {
            throw new IllegalArgumentException("Element must be located in the data branch.");
        }
        folder = folder.replaceAll("[\\s+\\/]+$", "");
        String[] F = folder.split("/");
        if (F.length == 3 && folder.startsWith("data/Projects") &&
            (name.contentEquals("Data") || name.contentEquals("Journal"))) {
            throw new IllegalArgumentException("Cannot delete element " + name + " in " + folder);
        }
        Map<String,String> params = new HashMap<>();
        params.put("service", "access.service");
        params.put("command", "26");
        params.put("dc", folder);
        params.put("de", name);
        return con.queryJSON(con.getBasePath() + Path.DELETE_ELEMENT.getPath(), params);
    }
    
	/*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#existsElement(java.lang.String,java.lang.String)
     */
    @Override
    public JsonObject existsElement(String element, String folder) throws Exception {
        JsonObject js = list(folder);
        if (js.get("names") != null) {
            JsonArray names = js.get("names").asArray();
            for (JsonValue value : names) {
                if (value.asObject().get("name") != null && 
                        value.asObject().get("name").asString().equals(element))
                    return new JsonObject().add("exists", true);
            };
        } else {
            return js;
        }
        return new JsonObject().add("exists", false);
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#list(java.lang.String)
     */
    @Override
    public JsonObject list(String folder) throws Exception {
        Map<String,String> params = new HashMap<>();
        params.put("service", "access.service");
        params.put("command", "29");
        params.put("dc", folder);
        JsonObject js = con.queryJSON(con.getBasePath() + Path.LIST.getPath(), params);
        if (js.get("values") != null) {
            return Json.parse(js.get("values").asString()).asObject();
        } else {
            return js;
        }
    }
    
    /**
     * @return If the JSON property <b>type == 0</b>, the object contains a 
     *         definition of the columns in an array-of-objects field named <b>columns</b> 
     *         and the data in an array-of-arrays field named <b>data</b>. If <b>type</b> has
     *         another value the server response is returned as is.
     * 
     * @see com.genexplain.api.core.GxHttpClient#getTable(java.lang.String)
     */
    @Override
    public JsonObject getTable(String tablePath) throws Exception {
        GxUtil.showMessage(verbose, "Getting table: " + tablePath, logger, GxUtil.LogLevel.INFO);
        Map<String,String> params = new HashMap<>();
        params.put("de", tablePath);
        JsonObject js = con.queryJSON(con.getBasePath() + Path.TABLE_COLUMNS.getPath(), params);
        JsonObject table = new JsonObject();
        if (js.getInt("type",-1) != 0) {
            return js;
        } else {
            table.add("columns", js.get("values").asArray());
        }
        js = con.queryJSON(con.getBasePath() + Path.TABLE_DATA.getPath(), params);
        if (js.getInt("type",-1) != 0) {
            return js;
        } else {
            table.add("data", js.get("values").asArray());
        }
        return table;
    }
	
    /**
     * @param path
     *           Path of table in the platform workspace
     *           
     * @param data
     *           The table data as an array of columns
     *           
     * @param columns
     *           Columns specified as {@link com.genexplain.com.api.core.GxColumnDef} objects. The
     *           column order in this list must correspond to <b>data</b>
     *           
     * @see com.genexplain.api.core.GxHttpClient#putTable(java.lang.String, com.eclipsesource.json.JsonArray, java.util.List)
     */
    @Override
    public JsonObject putTable(String path, JsonArray data, List<GxColumnDef> columns) throws Exception {
        GxUtil.showMessage(verbose, "Putting data into " + path, logger, GxUtil.LogLevel.INFO);
        if (columns.size() == 0) {
            throw new IllegalArgumentException("Empty column list");
        } else if (data.size() == 0) {
            throw new IllegalArgumentException("Empty data matrix");
        } else if (columns.size() != data.size()) {
            throw new IllegalArgumentException("Unequal number of elements in column list and data columns");
        }
        JsonArray colDefs = getColumnDefs(columns);
        Map<String,String> params = new HashMap<>();
        params.put("de", path);
        params.put("columns", colDefs.toString());
        params.put("data", data.toString());
        return con.queryJSON(con.getBasePath() + Path.PUT_TABLE.getPath(), params);
    }
    
    /**
     * Converts list of {@link com.genexplain.com.api.core.GxColumnDef} into a JsonArray that
     * can be submitted to the platform.
     * 
     * @param defs
     *           The column definitions
     *           
     * @return The corresponding JsonArray as processed by the platform
     * 
     * @throws IllegalArgumentException
     *           If a column name is empty, occurs multiple times, the ID column is not of type
     *           {@link GxColumnDef.ColumnType#Text}, or if an ID column is missing
     */
    private JsonArray getColumnDefs(List<GxColumnDef> defs) throws IllegalArgumentException {
        JsonArray ja = new JsonArray();
        Set<String> names = new HashSet<>();
        defs.forEach(def -> {
            if (def.getName().isEmpty())
                throw new IllegalArgumentException("Column name must not be empty");
            if (names.contains(def.getName()))
                throw new IllegalArgumentException("Column name " + def.getName() + " occurs multiple times");
            if (def.getName().equals("ID") && def.getType() != GxColumnDef.ColumnType.Text)
                throw new IllegalArgumentException("The ID column must be of type " + GxColumnDef.ColumnType.Text);
            ja.add(new JsonObject().add("name", def.getName()).add("type",def.getType().toString()));
            names.add(def.getName());
        });
        if (!names.contains("ID"))
            throw new IllegalArgumentException("There needs to be one column named ID");
        return ja;
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#analyze(java.lang.String, java.util.Map, boolean, boolean, boolean)
     */
    @Override
    public JsonObject analyze(String appName, JsonValue params, boolean isWorkflow, boolean wait, boolean progress) throws Exception {
        GxUtil.showMessage(verbose, "Starting analysis " + appName, logger, GxUtil.LogLevel.INFO);
        String jobId = nextJobId();
        JsonArray ja = getParameterArray(params);
        Map<String,String> sendParams = new HashMap<>();
        sendParams.put("jobID", jobId);
        sendParams.put("de", appName);
        sendParams.put("useJsonOrder", "no");
        String analysisPath = con.getBasePath() + Path.ANALYZE.getPath();
        if (isWorkflow) {
            sendParams.put("action", "start_workflow");
            analysisPath = con.getBasePath() + Path.WORKFLOW.getPath();
        }
        sendParams.put("json", ja.toString());
        JsonObject js = con.queryJSON(analysisPath, sendParams);
        js.add("jobId", jobId);
        if (wait) {
            JobStatus stat = waitForProcess(jobId, progress);
            js.add("jobStatus", stat.toString());
            if (stat == JobStatus.TERMINATED_BY_ERROR) {
                js = new JsonObject().add("type", 1)
                        .add("message", JobStatus.TERMINATED_BY_ERROR.toString() + 
                                ": " + blockingProcessError);
            }            
        }
        return js;
    }
    
    /**
     * Converts parameters to a JsonArray that can be sent to
     * the platform.
     * 
     * @param params
     *           Parameters to be sent to platform
     * 
     * @return The JsonArray for parameters
     */
    private JsonArray getParameterArray(JsonValue params) {
        if (params.isArray()) {
            return params.asArray();
        } else {
            JsonArray ja = new JsonArray();
            params.asObject().forEach(mem -> {
                JsonValue jv = mem.getValue();
                if (jv.isObject()) {
                    ja.add(new JsonObject().add("name", mem.getName()).add("value", getParameterArray(jv.asObject())));
                } else {
                    ja.add(new JsonObject().add("name", mem.getName()).add("value", mem.getValue()));
                }
            });
            return ja;
        }
    }
    
    /**
     * Creates a job id in a similar way as the R API. The id consists of a short letter code (e.g. APIJ)
     * and a long number which may be composed of current seconds and fractional seconds as well as
     * a job counter.
     * 
     * @return The job id
     */
    private String nextJobId() {
        jobNo++;
        return JOB_PREF + timeFormat.format(new Date()) + String.format("%03d",jobNo);
    }
    
    /**
     * Waits for the specified process to complete and optionally prints out progress info if available.
     * 
     * @param jobId
     *           The job id to query status
     *           
     * @param progress
     *           Whether to log progress
     *           
     * @throws Exception
     *           An exception may be caused by internal method calls
     */
    private JobStatus waitForProcess(String jobId, boolean progress) throws Exception {
        JobStatus stat;
        int messageLen = 0;
        int nonJsonCounter = 0;
        do {
            JsonObject js = getJobStatus(jobId);
            System.out.println(js.toString());
            if (js.getInt("type", 0) == -1) {
                nonJsonCounter++;
                if (nonJsonCounter >= NON_JSON_RESPONSE_LIMIT) {
                    throw new IllegalStateException("Received too many non-JSON responses from server");
                } else {
                    stat = JobStatus.RUNNING;
                }
            } else {
                nonJsonCounter = 0;
                stat = JobStatus.get(js.get("status").asInt() + 1);
                if (progress) {
                    String percent = "";
                    if (js.get("percent") != null) {
                        percent = js.getInt("percent",0) + "%";
                    } else {
                        percent = "0%";
                    }
                    if (js.get("values") != null) {
                        JsonValue values = js.get("values");
                        String msg, val;
                        if (values.isArray()) {
                            val = values.asArray().get(0).asString();
                        } else {
                            val = values.asString();
                        }
                        msg = val.substring(messageLen);
                        if (msg.length() > 0) {
                            logger.info(msg.replaceAll("[\\n]?INFO", "\n " + percent + ": INFO"));
                        }
                        messageLen = val.length();
                    }
                }
                if (stat == JobStatus.TERMINATED_BY_ERROR) {
                    blockingProcessError = js.toString();
                    if (verbose)
                        logger.error("An error occurred: " + js.toString());
                    break;
                }
            }
            Thread.sleep(5000L);
        } while (!(stat == null || stat == JobStatus.COMPLETED || stat == JobStatus.TERMINATED_BY_ERROR || stat == JobStatus.TERMINATED_BY_REQUEST));
        return stat;
    }
    
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#getAnalysisParameters(java.lang.String)
     */
    @Override
    public JsonObject getAnalysisParameters(String appName) throws Exception {
        GxUtil.showMessage(verbose, "Getting parameters for " + appName, logger, GxUtil.LogLevel.INFO);
        Map<String,String> params = new HashMap<>();
        params.put("de", "properties/method/parameters/" + appName);
        params.put("showMode", "1");
        JsonObject js = con.queryJSON(con.getBasePath() + Path.ANALYSIS_PARAMS.getPath(),params);
        if (js.getInt("type",-1) != 0) {
            return js;
        } else {
            return js.add("parameters", js.get("values").asArray());
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#imPort(java.lang.String, java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public JsonObject imPort(String file, String parentPath, String importer, JsonValue params) throws Exception {
        GxUtil.showMessage(verbose, "Importing: " + file + " into " + parentPath + " using " + importer, logger, GxUtil.LogLevel.INFO);
        String fileId = nextJobId().substring(JOB_PREF.length());
        String jobId  = nextJobId();
        URIBuilder builder   = new URIBuilder(
                con.getServer() + 
                con.getBasePath() + 
                Path.UPLOAD.getPath());
        builder.addParameter("fileID", fileId)
               .addParameter("name", "\"upload" + 
                                     fileId + "\" id=\"upload" + 
                                     fileId + "\" enctype=\"multipart/form-data\"");
        HttpPost   httpPost  = new HttpPost(builder.build());
        FileBody   fileBody  = new FileBody(new File(file));
        
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .build();
        httpPost.setEntity(reqEntity);
        CloseableHttpResponse response = con.getHttpClient().execute(httpPost);
        try {
            logger.info(EntityUtils.toString(response.getEntity()));
        } finally {
            response.close();
        }
        params = getParameterArray(params);
        Map<String,String> iparams = new HashMap<>();
        iparams.put("type", "import");
        iparams.put("de", parentPath);
        iparams.put("fileID", fileId);
        iparams.put("jobID", jobId);
        iparams.put("format", importer);
        iparams.put("json", params.toString());
        return con.queryJSON(con.getBasePath() + Path.IMPORT.getPath(), iparams);
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#export(java.lang.String, java.lang.String, java.io.OutputStream, java.util.Map)
     */
    @Override
    public void export(String path, String exporter, OutputStream fs, JsonValue params) throws Exception {
        GxUtil.showMessage(verbose, "Exporting " + path + " using " + exporter, logger, GxUtil.LogLevel.INFO);
        JsonArray ja = getParameterArray(params);
        Map<String, String> eparams = new HashMap<>();
        eparams.put("exporter", exporter);
        eparams.put("type", "de");
        eparams.put("detype", "Element");
        eparams.put("de", path);
        eparams.put("parameters",ja.toString());
        CloseableHttpResponse resp = con.queryBioUML(con.getBasePath() + Path.EXPORT.getPath(), eparams);
        try {
            fs.write(EntityUtils.toByteArray(resp.getEntity()));
        } finally {
            resp.close();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#listApplications()
     */
    @Override
    public JsonObject listApplications() throws Exception {
        GxUtil.showMessage(verbose, "Getting application list", logger, GxUtil.LogLevel.INFO);
        return con.queryJSON(con.getBasePath() + Path.ANALYSIS_LIST.getPath(), new HashMap<String,String>());
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#listExporters()
     */
    @Override
    public JsonObject listExporters() throws Exception {
        GxUtil.showMessage(verbose, "Getting exporter list", logger, GxUtil.LogLevel.INFO);
        return con.queryJSON(con.getBasePath() + Path.EXPORT_LIST.getPath(), new HashMap<String,String>());
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#listImporters()
     */
    @Override
    public JsonObject listImporters() throws Exception {
        GxUtil.showMessage(verbose, "Getting importer list", logger, GxUtil.LogLevel.INFO);
        return con.queryJSON(con.getBasePath() + Path.IMPORT_LIST.getPath(), new HashMap<String,String>());
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#getImporterParameters(java.lang.String, java.lang.String)
     */
    @Override
    public JsonObject getImporterParameters(String path, String importer) throws Exception {
        GxUtil.showMessage(verbose, "Getting parameters for importer " + importer + " on path " + path, logger, GxUtil.LogLevel.INFO);
        Map<String,String> params = new HashMap<>();
        params.put("de", path);
        params.put("detype", "Element");
        params.put("type", "properties");
        params.put("format", importer);
        params.put("jobID", nextJobId());
        return con.queryJSON(con.getBasePath() + Path.IMPORT.getPath(), params);
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#getExporterParameters(java.lang.String, java.lang.String)
     */
    @Override
    public JsonObject getExporterParameters(String path, String exporter) throws Exception {
        GxUtil.showMessage(verbose, "Getting parameters for exporter " + exporter + " from path " + path, logger, GxUtil.LogLevel.INFO);
        Map<String,String> params = new HashMap<>();
        params.put("de", path);
        params.put("detype", "Element");
        params.put("type", "deParams");
        params.put("exporter", exporter);
        return con.queryJSON(con.getBasePath() + Path.EXPORT.getPath(), params);
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#getJobStatus(java.lang.String)
     */
    @Override
    public JsonObject getJobStatus(String jobId) throws Exception {
        //GxUtil.showMessage(verbose, "Getting status for job " + jobId, logger, GxUtil.LogLevel.INFO);
        Map<String,String> params = new HashMap<>();
        params.put("jobID", jobId);
        return con.queryJSON(con.getBasePath() + Path.JOB_CONTROL.getPath(), params);
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#getConnection()
     */
	@Override
	public GxHttpConnection getConnection() {
		return con;
	}
    
	/**
	 * Sets the {@link com.genexplain.api.core.GxHttpConnection connection} to be used
	 * in subsequent platform requests.
	 * <p>
	 * The connection should be fully configured. If not logged in, yet, the method will invoke
     * {@link com.genexplain.api.core.GxHttpConnection#login() login}.
     * </p>
	 * 
	 * @see com.genexplain.api.core.GxHttpClient#setConnection(com.genexplain.api.core.GxHttpConnection)
	 */
	@Override
	public GxHttpClient setConnection(GxHttpConnection con) throws Exception {
	    if (!con.hasLoggedIn()) {
	        con.login();
	    }
		this.con = con;
		return this;
	}
    
	/*
	 * (non-Javadoc)
	 * @see com.genexplain.api.core.GxHttpClient#getVerbose()
	 */
    @Override
    public boolean getVerbose() {
        return verbose;
    }
    
    /*
     * (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#setVerbose(boolean)
     */
    @Override
    public GxHttpClient setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }
}
