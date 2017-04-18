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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.genexplain.api.app.ApplicationCommand;
import com.genexplain.api.app.Command;
import com.genexplain.util.GxUtil;


/**
 * A JSON interface for platform operations
 * implemented by a {@link com.genexplain.api.core.GxHttpClient platform client}.
 * <p>
 * The class has a defined set of executor types which map to methods in the platform
 * client. Tasks are defined as JsonObjects and specified to {@link #execute(JsonObject)}
 * individually or as a JsonArray of task objects to {@link #execute(JsonArray)}.
 * </p>
 * <p>
 * A task object must contain a property <b>do</b> whose value is an element of
 * {@link ExecutorType}. Other properties may be required by individual executors.
 * </p>
 * 
 * @author pst
 *
 */
@Command(name="exec",description="Executes tasks configured in provided JSON file")
public class GxJsonExecutor implements ApplicationCommand {
    
    public static final String NO_INPUT_MESSAGE = "Please specify a config file in JSON format";
    
    /**
     * Available executor types.
     * 
     * @author pst
     */
    public enum ExecutorType {
        list("Lists contents of specified folder"),
        get("Gets specified table"),
        put("Puts table into specified folder"),
        analyze("Calls the analysis method with specified parameters"),
        imPort("Import a data file using a dedicated importer"),
        export("Export data using a dedicated exporter"),
        createFolder("Creates a folder"),
        itemParameters("Lists parameters for specified application, importer, or exporter"),
        listItems("Lists available application, importer, or exporter items"),
        jobStatus("Gets the status for a job id");
        
        private String desc = "";
        
        private ExecutorType(String d) {
            desc = d;
        }
        
        public String get() { return desc; }
    }
    
    /**
     * Implemented by executor methods or classes.
     * 
     * @author pst
     *
     * @param <T>
     *           The input type
     * @param <R>
     *           The output type
     */
    @FunctionalInterface
    public interface Executor<T,R> {
        public R apply(T t) throws Exception;
    }
    
    private Logger                   logger = LoggerFactory.getLogger(this.getClass());
    private GxJsonExecutorParameters params;
    
    private Map<ExecutorType,Executor<JsonObject, GxJsonExecutor>> executors;
    
    private JsonObject lastJsonObject;
    
    public GxJsonExecutor() throws Exception {
        init();
    }
    
    private void init() throws Exception {
        
        params = new GxJsonExecutorParameters();
        
        executors = new HashMap<>();
        
        executors.put(ExecutorType.list, this::list);
        executors.put(ExecutorType.get, this::getTable);
        executors.put(ExecutorType.put, new PutTableExecutor().setExecutor(this));
        executors.put(ExecutorType.analyze, this::analyze);
        executors.put(ExecutorType.imPort, new ImportExecutor().setExecutor(this));
        executors.put(ExecutorType.export, new ExportExecutor().setExecutor(this));
        executors.put(ExecutorType.createFolder, this::createFolder);
        executors.put(ExecutorType.jobStatus, this::getJobStatus);
        executors.put(ExecutorType.itemParameters, this::getItemParameters);
        executors.put(ExecutorType.listItems, this::listItems);
    }
    
    
    /**
     * Extracts parameters from a property of the input object named <b>parameters</b>,
     * which is itself an object that specifies parameters as property names and values.
     * <p>
     * Non-string parameter values are converted to a string prefixed with the word
     * <b>$DECODE$</b>, so that they can be recognized, e.g. by 
     * {@link com.genexplain.api.core.GxHttpClientImpl#analyze(String, Map, boolean, boolean, boolean) 
     * GxHttpClientImpl#analyze} and properly converted to JsonValues before submission
     * to the platform.
     * </p>
     * <p>
     * Other implementations of {@link com.genexplain.api.core.GxHttpClient GxHttpClient} may need to
     * be aware of this if used behind this executor.
     * </p>
     * 
     * @param conf
     *           A JSON object that contains a property object named <code>parameters</b>
     *           
     * @return The map corresponding to the <code>parameter</code> object
     */
    public static Map<String,String> getJsonParameters(JsonObject conf) {
        Map<String,String> params = new HashMap<>();
        if (conf.get("parameters") != null) {
           conf.get("parameters").asObject().forEach(param -> {
               JsonValue val  = param.getValue();
               if (val.isString()) {
                   params.put(param.getName(), val.asString());
               } else {
                   params.put(param.getName(), "$DECODE$" + val.toString());
               }
           });
        }
        return params;
    }
    
    /**
     * Executes tasks listed in specified JsonArray.
     * 
     * @param tasks
     *           An array of tasks that can be executed by {@link #execute(JsonObject)}
     *           
     * @return This executor to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    public GxJsonExecutor execute(JsonArray tasks) throws Exception {
        tasks.forEach(task -> {
            try {
                execute(task.asObject());
            } catch (Exception e) {
                throw new GxJsonExecutorException(e);
            }
        });
        return this;
    }
    
    /**
     * Executes a single task configured by the specified JsonObject.
     * 
     * @param task
     *           The task object
     *           
     * @return This executor to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    public GxJsonExecutor execute(JsonObject task) throws Exception {
        ExecutorType et = ExecutorType.valueOf(task.getString("do",""));
        return executors.get(et).apply(task);
    }
    
    /**
     * Creates a new folder. 
     * 
     * The server response is stored and can be retrieved by
     * calling {@link #getLastJsonObject()}.
     *  
     * @param conf
     *           Object containing properties <b>path</b> and <b>name</b> required
     *           for {@link com.genexplain.api.core.GxHttpClient#createFolder(String, String) the client method}
     *           
     * @return This executor to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     * 
     * @see com.genexplain.api.core.GxHttpClient#createFolder(String, String)
     */
    public GxJsonExecutor createFolder(JsonObject conf) throws Exception {
        GxUtil.showMessage(params.isVerbose(), "Creating folder", logger, GxUtil.LogLevel.INFO);
        String path = conf.get("path").asString();
        String name = conf.get("name").asString();
        lastJsonObject = params.getHttpClient().createFolder(path, name);
        return this;
    }
    
    /**
     * Gets the listing for specified folder and stores it in the object so
     * that it can be retrieved by {@link #getLastJsonObject() getLastJsonObject}.
     * 
     * @param conf
     *           Object containing a property <b>folder</b> to be provided
     *           to {@link com.genexplain.api.core.GxHttpClient#list(String) the client method}.
     *          
     * @return This executor to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     *           
     * @see com.genexplain.api.core.GxHttpClient#list(String)
     */
    public GxJsonExecutor list(JsonObject conf) throws Exception {
        GxUtil.showMessage(params.isVerbose(), "Getting folder listing", logger, GxUtil.LogLevel.INFO);
        String folder = conf.get("folder").asString();
        lastJsonObject = params.getHttpClient().list(folder);
        printLastJson(conf);
        return this;
    }
    
    /**
     * Fetches listings of available applications, importers or exporters.
     * <p>
     * The specified object should contain a property <b>type</b> whose value is
     * one of <b>applications, importers, exporters</b>, or if not <b>applications</b>
     * is used as default. The specified type causes the corresponding listing
     * to be fetched using respectively
     * <ul>
     * <li>{@link com.genexplain.api.core.GxHttpClient#listApplications() listApplications}
     * <li>{@link com.genexplain.api.core.GxHttpClient#listImporters() listImporters}
     * <li>{@link com.genexplain.api.core.GxHttpClient#listExporters() listExporters}
     * </ul>
     * </p>
     * <p>
     * The listing can be retrieved by {@link #getLastJsonObject() getLastJsonObject}.
     * </p>
     * <p>
     * The conf object may further contain properties <b>toFile</b> (with String value) and/or <b>toStdout</b>
     * (with boolean value) that can be used to log the listing to file and/or standard output.
     * </p>
     * 
     * @param conf
     *           An object, may be empty.
     *           
     * @return This executor to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    public GxJsonExecutor listItems(JsonObject conf) throws Exception {
        if (conf == null)
            conf = new JsonObject();
        String type = conf.getString("type", "applications");
        GxUtil.showMessage(params.isVerbose(), "Getting list of " + type, logger, GxUtil.LogLevel.INFO);
        switch (type) {
            case "importers": lastJsonObject = params.getHttpClient().listImporters(); break;
            case "exporters": lastJsonObject = params.getHttpClient().listExporters(); break;
            default: lastJsonObject = params.getHttpClient().listApplications(); 
        }
        printLastJson(conf);
        return this;
    }
    
    /**
     * Fetches parameter definitions for an application, exporter, or importer 
     * and stores the result so that it is available from {@link #getLastJsonObject() getLastJsonObject}.
     * <p>
     * The conf object may contain a property <b>type</b> whose value is one of
     * <b>application, importer, exporter</b>. The default type is <b>application</b>.
     * </p>
     * <p>
     * Required is a property <b>name</b> whose value is the name of the application, exporter,
     * or importer. And if type is exporter or importer, a property <b>path</b> with the
     * platform path is required.
     * </p>
     * <p>
     * The conf object may further contain properties <b>toFile</b> (with String value) and/or <b>toStdout</b>
     * (with boolean value) that can be used to log the listing to file and/or standard output.
     * </p>
     * 
     * @param conf
     *           An object with required properties
     *           
     * @return This executor to enable fluent calls
     * 
     * @throws IllegalArgumentException
     *           If the item name is not specified or empty, or
     *           if type is <b>importer or exporter</b> and property
     *           <b>path</b> is missing or empty
     *           
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     *           
     * @see com.genexplain.api.core.GxHttpClient#getAnalysisParameters(String)
     * @see com.genexplain.api.core.GxHttpClient#getImporterParameters(String, String)
     * @see com.genexplain.api.core.GxHttpClient#getExporterParameters(String, String)
     */
    public GxJsonExecutor getItemParameters(JsonObject conf) throws IllegalArgumentException, Exception {
        String type = conf.getString("type", "application");
        String name = conf.getString("name", "");
        if (name.isEmpty())
            throw new IllegalArgumentException("Missing item name");
        GxUtil.showMessage(params.isVerbose(), "Getting parameters for " + type + " " + name, logger, GxUtil.LogLevel.INFO);
        if (type.endsWith("porter")) {
            String path = conf.getString("path", "");
            if (path.isEmpty())
                throw new IllegalArgumentException("Missing path for ex/import parameter listing");
            if (type.equals("importer")) {
                lastJsonObject = params.getHttpClient().getImporterParameters(path, name);
            } else if (type.equals("exporter")) {
                lastJsonObject = params.getHttpClient().getExporterParameters(path, name);
            } else {
                throw new IllegalArgumentException("Unknown item type: " + type);
            }
        } else if (type.equals("application")) {
            lastJsonObject = params.getHttpClient().getAnalysisParameters(name);
        } else {
            throw new IllegalArgumentException("Unknown item type: " + type);
        }
        printLastJson(conf);
        return this;
    }
    
    /**
     * Fetches the status of a job specified within the conf object by
     * property <b>jobId</b>. The server response can be retrieved from
     * {@link #getLastJsonObject() getLastJsonObject}. 
     * <p>
     * The conf object may further contain properties <b>toFile</b> (with String value) and/or <b>toStdout</b>
     * (with boolean value) that can be used to log the listing to file and/or standard output.
     * </p>
     * 
     * @param conf
     *           An object containing the job id
     * 
     * @return This executor to enable fluent calls
     * 
     * @throws IllegalArgumentException
     *           If jobId property is missing or empty
     *           
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     *           
     * @see com.genexplain.api.core.GxHttpClient#getJobStatus(String)
     */
    public GxJsonExecutor getJobStatus(JsonObject conf) throws IllegalArgumentException, Exception {
        GxUtil.showMessage(params.isVerbose(), "Getting job status", logger, GxUtil.LogLevel.INFO);
        String jobId = conf.getString("jobId", "");
        if (jobId.isEmpty()) {
            throw new IllegalArgumentException("Missing job id");
        }
        lastJsonObject = params.getHttpClient().getJobStatus(jobId);
        printLastJson(conf);
        return this;
    }
    
    
    /**
     * Fetches a table whose path is specified by property <b>table</b>.
     * The table or other server response can be retrieved from 
     * {@link #getLastJsonObject() getLastJsonObject}. 
     * <p>
     * The conf object may further contain properties <b>toFile</b> (with String value) and/or <b>toStdout</b>
     * (with boolean value) that can be used to log the listing to file and/or standard output.
     * </p>
     * 
     * @param conf
     *           An object containing the table path
     * 
     * @return This executor to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     *           
     * @see com.genexplain.api.core.GxHttpClient#getTable(String)
     */
    public GxJsonExecutor getTable(JsonObject conf) throws Exception {
        GxUtil.showMessage(params.isVerbose(), "Getting table", logger, GxUtil.LogLevel.INFO);
        String table = conf.get("table").asString();
        lastJsonObject = params.getHttpClient().getTable(table);
        printLastJson(conf);
        return this;
    }
    
    /**
     * Prints the object available from {@link #getLastJsonObject()} to file
     * and/or standard out.
     * 
     * @param conf
     *           An object specifying to print to file and/or standard out
     *           
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    private void printLastJson(JsonObject conf) throws Exception {
        if (conf.get("toFile") != null) {
            GxUtil.writeToFile(conf.get("toFile").asString(), new JSONObject(lastJsonObject.toString()).toString(2));
        }
        if (conf.getBoolean("toStdout",false)) {
            System.out.println(new JSONObject(lastJsonObject.toString()).toString(2));
        }
    }
    
    /**
     * Invokes specified analysis method or workflow.
     * <p>
     * The object must contain a property <b>method</b> whose
     * value is the application name or workflow path.
     * </p>
     * <p>
     * Other parameters:
     * <ul>
     * <li><b>workflow</b> - required for workflows, default <code>false</code>, 
     *                       whether <b>method</b> is a workflow
     * <li><b>wait</b> - optional, default <code>false</code>, 
     *                   whether to wait for the analysis to complete
     * <li><b>progress</b> - optional, default <code>false</code>, 
     *                       whether to show progress while waiting
     * </p>
     * 
     * @param conf
     *           The config object for this task
     *           
     * @return This executor to enable fluent calls
     * 
     * @throws IllegalArgumentException
     *           If method is not specified or empty
     *           
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     * 
     * @see {@link com.genexplain.api.core.GxHttpClient#analyze(String, Map, boolean, boolean, boolean)}
     */
    public GxJsonExecutor analyze(JsonObject conf) throws IllegalArgumentException, Exception {
        String method = conf.getString("method", "");
        if (method.isEmpty())
            throw new IllegalArgumentException("Missing method name");
        GxUtil.showMessage(params.isVerbose(), "Starting analysis using " + method, logger, GxUtil.LogLevel.INFO);
        lastJsonObject = params.getHttpClient().analyze(method, 
                    getJsonParameters(conf),
                    conf.getBoolean("workflow", false),
                    conf.getBoolean("wait", true),
                    conf.getBoolean("progress", false));
        return this;
    }
    
    /**
     * Returns the last JsonObject received as a platform response.
     * 
     * @return The last JsonObject
     */
    public JsonObject getLastJsonObject() { return lastJsonObject; }
    
    /**
     * Sets the last JsonObject
     * 
     * @param js
     *           JsonObject to set as last JsonObject
     * @return This executor to enable fluent calls
     */
    protected GxJsonExecutor setLastJsonObject(JsonObject js) {
        lastJsonObject = js;
        return this;
    }
    
    /**
     * Returns this executor's parameter object.
     * 
     * @return The executor parameters
     */
    public GxJsonExecutorParameters getParameters() { return params; }
    
    /**
     * Sets parameters for this executor.
     * <p>
     * The parameters may configure clients or connection types,
     * tasks to be executed, login data.
     * </p>
     * 
     * @param params
     *           Parameter object to configure this executor
     *           
     * @return This executor to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    public GxJsonExecutor setParameters(GxJsonExecutorParameters params) throws Exception {
        this.params = params;
        return this;
    }
    
    /**
     * Batch method to connect and execute tasks as configured
     * in parameters.
     * 
     * @param params
     *           Parameter object to set and execute
     *           
     * @return This executor to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    public GxJsonExecutor run(GxJsonExecutorParameters params) throws Exception {
        setParameters(params);
        params.connect();
        return execute(params.getTasks());
    }
    
    /**
     * Batch method to connect and execute tasks as configured
     * in parameters which are firstly read from specified input stream.
     * 
     * @return This executor to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    public GxJsonExecutor run(InputStream is) throws Exception {
        return run(new GxJsonExecutorParameters().setConfig(is));
    }
    
    /**
     * A single argument is required that specifies a parameter
     * file in JSON format.
     */
    @Override
    public void run(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println(NO_INPUT_MESSAGE);
            return;
        }
        new GxJsonExecutor().run(new FileInputStream(args[0]));
    }
}
