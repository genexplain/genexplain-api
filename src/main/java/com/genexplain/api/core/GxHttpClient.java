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

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;


/**
 * A GxHttpClient provides an interface for higher-level access
 * to the geneXplain platform, usually on top of a {@link com.genexplain.api.core.GxHttpConnection}.
 * <p>
 * The functionality covers methods to put and get table data, import, export and analyze
 * types of data, invoke workflows, or querying job status of submitted tasks.
 * </p>
 * 
 * @author pst
 */
public interface GxHttpClient {
    
    /**
     * Request paths used by a {@link com.genexplain.api.core.GxHttpClient}
     * 
     * @author pst
     */
    public enum Path {
        CREATE_PROJECT("/support/createProjectWithPermission"),
        CREATE_FOLDER("/web/data"),
        DELETE_ELEMENT("/web/data"),
        TABLE_COLUMNS("/web/table/columns"),
        TABLE_DATA("/web/table/rawdata"),
        PUT_TABLE("/web/table/createTable"),
        ANALYZE("/web/analysis"),
        WORKFLOW("/web/research"),
        JOB_CONTROL("/web/jobcontrol"),
        ANALYSIS_PARAMS("/web/bean/get"),
        ANALYSIS_LIST("/web/analysis/list"),
        UPLOAD("/web/upload"),
        IMPORT("/web/import"),
        IMPORT_LIST("/web/import/list"),
        EXPORT("/web/export"),
        EXPORT_LIST("/web/export/list"),
        LIST("/web/data");
        
        private String path = "";
        
        private Path(String path) {
            this.path = path;
        }
        
        public String getPath() { return path; } 
    }
    
    /**
     * Job status codes.
     *  
     * @author pst
     */
    public enum JobStatus {
        CREATED(1),
        RUNNING(2),
        PAUSED(3),
        COMPLETED(4),
        TERMINATED_BY_REQUEST(5),
        TERMINATED_BY_ERROR(6);
        
        private int value;
        
        private JobStatus(int value) {
            this.value = value;
        }
        
        public int getValue() { return value; }
        
        public static JobStatus get(int v) {
            for (JobStatus js : JobStatus.values()) {
                if (js.getValue() == v) {
                    return js;
                }
            }
            return null;
        }
    }
    
    public enum ColumnDelimiter {
        Tab(0), Spaces(1), Commas(2), Pipes(3);
        
        private int index;
        
        private ColumnDelimiter(int index) {
            this.index = index;
        }
        
        public int getValue() { return index; }
        
        public static ColumnDelimiter get(int i) {
            for (ColumnDelimiter cd : ColumnDelimiter.values()) {
                if (cd.getValue() == i) {
                    return cd;
                }
            }
            return null;
        }
    }
    
    /**
     * Imports a table from a text file into the platform
     * 
     * @param file
     *           path to file to import
     * @param folder
     *           folder into which the table is imported
     * @param tableName
     *           name for the table within the platform
     * @param processQuotes
     *           true to process quotation marks
     * @param delim
     *           column delimiter, one of (Tab, Spaces, Commas, Pipes)
     * @param headerRow
     *           row index of the table header
     * @param dataRow
     *           row index of the first data row
     * @param commentString
     *           string to recognize comment lines
     * @param columnForID
     *           name of column with ids
     * @param addSuffix
     *           true to add suffix to ensure unique ids
     * @param tableType
     *           type of platform table, e.g. Genes: Ensembl
     * @param species
     *           Latin name of species
     */
    public JsonObject importTable(String file, String folder, String tableName, boolean processQuotes,
            ColumnDelimiter delim, int headerRow, int dataRow, String commentString, String columnForID,
            boolean addSuffix, String tableType, String species) throws Exception;
    
    /**
     * Creates a project in the user's workspace
     * 
     * @param params
     *           Parameters required for project creation. See implementation
     *           for details.
     *           
     * @return The server repsonse
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject createProject(Map<String,String> params) throws Exception;
    
    /**
     * Creates a folder with given name in the specified path.
     * 
     * @param path
     *           Path of the parent folder
     * 
     * @param name
     *           The folder name
     * 
     * @return The server response
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject createFolder(String path, String name) throws Exception;
    
    /**
     * Deletes a data element.
     * <p>
     * <b>Use this method at your own risk and responsibility! Data may be
     * irrecoverably lost.</b>
     * </p>
     *  
     * @param folder
     *           The folder that contains the element
     *     
     * @param name
     *           The name of the element to delete
     *     
     * @return The server response
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject deleteElement(String folder, String name) throws Exception;
    
    /**
     * Returns a boolean indicating whether an element with specified name exists in the folder
     *  
     * @param element
     *           The name of the element
     * @param folder
     *           The folder that might contain the element
     *     
     * @return An object that contains the result of the request or an alternative response in case of failure 
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject existsElement(String element, String folder) throws Exception;
    
    /**
     * Returns a JSON object with the elements contained in specified folder or an alternative response from
     * the platform.
     *  
     * @param folder
     *           The folder whose contents are to be listed
     *     
     * @return An object that contains the folder elements or an alternative response in case of failure 
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject list(String folder) throws Exception;
    
    /**
     * Fetches a data table specified by path and returns a JSON object that either contains
     * the table data or another response from the platform.
     * 
     * @param tablePath
     *           The path to the table
     *     
     * @return a JSON object with the table (see description), or a failure response from the platform
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject getTable(String tablePath) throws Exception;
    
    /**
     * Uploads a table into the platform.
     * 
     * @param path
     *           Path of the table in the platform
     *           
     * @param data
     *           The table data, see implementation for details
     *     
     * @param columns
     *           Column definitions as instances of {@link com.genexplain.api.core.GxColumnDef}
     *  
     * @return The server response
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject putTable(String path, JsonArray data, List<GxColumnDef> columns) throws Exception;
    
    /**
     * Carries out the analysis using specified parameters. To invoke a workflow, specify
     * its path within the platform workspace and set isWorkflow to <code>true</code>. The method
     * can wait for the analysis to finish, and log progress to standard output, 
     * or return immediately. 
     * 
     * @param appName
     *           Name of the analysis application or workflow
     *           
     * @param params
     *           Parameters for the analysis
     *           
     * @param isWorkflow
     *          Set <code>true</code> if the specified app name is a workflow path, false
     *          otherwise 
     *           
     * @param wait
     *           Whether this method shall wait for completion or return immediately
     *           
     * @param progress
     *           Whether to show progress while waiting for the analysis to complete
     *           
     * @return The server response
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject analyze(String appName, JsonValue params, boolean isWorkflow, boolean wait, boolean progress) throws Exception;
    
    /**
     * Fetches the status of specified job.
     * 
     * @param jobId
     *           The id of the job to query
     *           
     * @return An object containing the status information or another server response
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject getJobStatus(String jobId) throws Exception;
    
    /**
     * Lists the available analysis applications.
     * 
     * @return An object containing the application descriptions or another server response
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject listApplications() throws Exception;
    
    /**
     * Lists the available exporters.
     * 
     * @return An object containing the importers or another server response
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject listExporters() throws Exception;
    
    /**
     * Lists the available importers.
     * 
     * @return An object containing the importers or another server response
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject listImporters() throws Exception;
    
    /**
     * Returns the definition of exporter parameters as received from the platform for specified path
     * and exporter. The exporters can be listed with {@link #listExporters() listExporters}.
     * 
     * @param path
     *           The element to export
     * 
     * @param exporter
     *           Exporter to show parameters for
     * 
     * @return An object containing the exporter parameters or another server response
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject getExporterParameters(String path, String exporter) throws Exception;
    
    /**
     * Returns the definition of importer parameters as received from the platform for specified path
     * and importer. The importers can be listed with {@link #listImporters() listImporters}.
     * 
     * @param path
     *           The path to import to
     *           
     * @param importer
     *           Importer to show parameters for
     *           
     * @return An object containing the parameters or another server response
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject getImporterParameters(String path, String importer) throws Exception;
    
    /**
     * Returns the definition of parameters available for the specified application.
     * Only the application name should be specified, not the full "platform path" of the tool.
     * 
     * @param appName
     *           Name of the analysis tool as it appears in the tool listing.
     *           
     * @return An object containing the parameters or another server response
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject getAnalysisParameters(String appName) throws Exception;
    
    /**
     * Exports the specified element using the selected exporter into <b>outfile</b>.
     * 
     * @param path
     *           The data element to export
     *           
     * @param exporter
     *           Exporter to apply to this download
     *  
     * @param fs
     *           {@link java.io.OutputStream OutputStream} to write export
     *           
     * @param params
     *           Any parameters to be provided to the exporter
     *           
     * @throws Exception
     *           May throw or cause an exception
     */
    public void export(String path, String exporter, OutputStream fs, JsonValue params) throws Exception;
    
    /**
     * Imports a file into the platform.
     * 
     * @param file
     *           The file to import
     *           
     * @param parentPath
     *           Parent folder into which to import
     *           
     * @param importer
     *           Importer to apply to this upload
     *            
     * @param params
     *           Any parameters to be provided to the importer
     *           
     * @return The server response
     * 
     * @throws Exception
     *           May throw or cause an exception
     */
    public JsonObject imPort(String file, String parentPath, String importer, JsonValue params) throws Exception;
    
    /**
     * Returns the {@link com.genexplain.api.core.GxHttpConnection connection} that is 
     * currently used to access the platform.
     * 
     * @return The current {@link com.genexplain.api.core.GxHttpConnection connection}
     */
	public GxHttpConnection getConnection();
	
	/**
     * Sets the connection that will be used to access the platform.
     * 
     * @param con
     *           The {@link com.genexplain.api.core.GxHttpConnection connection} to be used
     *           
     * @return The client to enable fluent calls
     */
	public GxHttpClient setConnection(GxHttpConnection con) throws Exception;
	
	/**
	 * Whether this client is in verbose mode.
	 * 
	 * @return <code>true</code> if verbose
	 */
	public boolean getVerbose();
	
	/**
	 * Sets whether client shall be more verbose or not.
	 * 
	 * @param verbose
	 *           <code>true</code> if client shall produce more log output,
	 *           <code>false</code> for less.
	 *           
	 * @return The client to enable fluent calls
	 */
	public GxHttpClient setVerbose(boolean verbose);
}
