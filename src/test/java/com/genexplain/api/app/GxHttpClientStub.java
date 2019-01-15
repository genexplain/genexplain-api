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
package com.genexplain.api.app;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.genexplain.api.core.GxColumnDef;
import com.genexplain.api.core.GxHttpClient;
import com.genexplain.api.core.GxHttpConnection;

/**
 * @author pst
 *
 */
public class GxHttpClientStub implements GxHttpClient {
    
    private GxHttpConnection con;
    
    private int listAppStatus      = 0;
    private int getAppParamsStatus = 0;
    
    private boolean calledListApps  = false;
    private boolean appParamsExcept = false;
    
    private Set<String> gotAnalysisParams = new HashSet<>();
    
    public boolean calledListApps() { return calledListApps; }
    
    public Set<String> gotAnalysisParams() { return gotAnalysisParams; }
    
    public void setAppParamsExcept(boolean e) {
        appParamsExcept = e;
    }
    
    public void setListAppStatus(int s) { 
        listAppStatus = s;
    }
    
    public void setGetAppParamsStatus(int s) { 
        getAppParamsStatus = s;
    }
    
    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#createProject(java.util.Map)
     */
    @Override
    public JsonObject createProject(Map<String, String> params)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#createFolder(java.lang.String, java.lang.String)
     */
    @Override
    public JsonObject createFolder(String path, String name) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#deleteElement(java.lang.String, java.lang.String)
     */
    @Override
    public JsonObject deleteElement(String folder, String name)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#list(java.lang.String)
     */
    @Override
    public JsonObject list(String folder) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#getTable(java.lang.String)
     */
    @Override
    public JsonObject getTable(String tablePath) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#putTable(java.lang.String, com.eclipsesource.json.JsonArray, java.util.List)
     */
    @Override
    public JsonObject putTable(String path, JsonArray data,
            List<GxColumnDef> columns) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#analyze(java.lang.String, java.util.Map, boolean, boolean, boolean)
     */
    @Override
    public JsonObject analyze(String appName, JsonValue params,
            boolean isWorkflow, boolean wait, boolean progress)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#getJobStatus(java.lang.String)
     */
    @Override
    public JsonObject getJobStatus(String jobId) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#listApplications()
     */
    @Override
    public JsonObject listApplications() throws Exception {
        calledListApps = true;
        return new JsonObject().add("type", listAppStatus)
                .add("values",new JsonArray().add("app1").add("app2").add("app3"));
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#listExporters()
     */
    @Override
    public JsonObject listExporters() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#listImporters()
     */
    @Override
    public JsonObject listImporters() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#getExporterParameters(java.lang.String, java.lang.String)
     */
    @Override
    public JsonObject getExporterParameters(String path, String exporter)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#getImporterParameters(java.lang.String, java.lang.String)
     */
    @Override
    public JsonObject getImporterParameters(String path, String importer)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#getAnalysisParameters(java.lang.String)
     */
    @Override
    public JsonObject getAnalysisParameters(String appName) throws Exception {
        if (appParamsExcept) {
            throw new RuntimeException("Test exception");
        }
        gotAnalysisParams.add(appName);
        JsonObject params = new JsonObject()
                                .add("type",getAppParamsStatus)
                                .add("values", new JsonArray()
                                        .add(new JsonObject()
                                                .add("name", appName + "-param")
                                                .add("displayName", appName + "-displayName")
                                                .add("description", appName + "-description")
                                         )
                                 );
                                
        return params;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#export(java.lang.String, java.lang.String, java.io.OutputStream, java.util.Map)
     */
    @Override
    public void export(String path, String exporter, OutputStream fs,
            JsonValue params) throws Exception {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#imPort(java.lang.String, java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public JsonObject imPort(String file, String parentPath, String importer,
            JsonValue params) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#getConnection()
     */
    @Override
    public GxHttpConnection getConnection() {
        return con;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#setConnection(com.genexplain.api.core.GxHttpConnection)
     */
    @Override
    public GxHttpClient setConnection(GxHttpConnection con) throws Exception {
        this.con = con;
        return this;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#getVerbose()
     */
    @Override
    public boolean getVerbose() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#setVerbose(boolean)
     */
    @Override
    public GxHttpClient setVerbose(boolean verbose) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpClient#existsElement(java.lang.String, java.lang.String)
     */
    @Override
    public JsonObject existsElement(String element, String folder)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonObject importTable(String file, String folder, String tableName, boolean processQuotes,
            ColumnDelimiter delim, int headerRow, int dataRow, String commentString, String columnForID,
            boolean addSuffix, String tableType, String species) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}
