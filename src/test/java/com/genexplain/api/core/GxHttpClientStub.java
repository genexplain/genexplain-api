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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.genexplain.api.core.GxColumnDef;
import com.genexplain.api.core.GxHttpClient;
import com.genexplain.api.core.GxHttpConnection;

public class GxHttpClientStub implements GxHttpClient {
    
    private GxHttpConnection con;
    
    private int callNum = 0;
    
    private List<String> called = new ArrayList<>();
    
    private JsonObject forVoid;
    
    public int getCallNum() { return callNum; }
    
    public List<String> getCalled() { return called; }
    
    public JsonObject getForVoid() { return forVoid; }
    
    @Override
    public JsonObject createProject(Map<String, String> params)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonObject createFolder(String path, String name) throws Exception {
        callNum++;
        called.add("createFolder");
        return new JsonObject().add("called", "createFolder")
                               .add("path", path)
                               .add("name", name);
    }

    @Override
    public JsonObject deleteElement(String folder, String name)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonObject list(String folder) throws Exception {
        callNum++;
        called.add("list");
        return new JsonObject().add("called", "list").add("folder", folder);
    }

    @Override
    public JsonObject getTable(String tablePath) throws Exception {
        callNum++;
        called.add("getTable");
        return new JsonObject().add("called", "getTable")
                               .add("table", tablePath);
    }

    @Override
    public JsonObject putTable(String path, JsonArray data,
                               List<GxColumnDef> columns) throws Exception {
        callNum++;
        called.add("putTable");
        JsonObject cols = new JsonObject();
        columns.forEach(col ->  {
            cols.add(col.getName(), col.getType().toString());
        });
        return new JsonObject().add("called", "putTable")
                               .add("path", path)
                               .add("data", data)
                               .add("columns", cols);
    }

    @Override
    public JsonObject analyze(String appName, JsonObject params, boolean isWorkflow,
                              boolean wait, boolean progress) throws Exception {
        callNum++;
        JsonObject js = new JsonObject()
                .add("type", 0)
                .add("app", appName)
                .add("params", params)
                .add("workflow", isWorkflow)
                .add("wait", wait)
                .add("progress", progress)
                .add("called", "analyze");
        called.add("analyze");
        return js;
    }
    
    @Override
    public JsonObject getJobStatus(String jobId) throws Exception {
        callNum++;
        called.add("getJobStatus");
        return new JsonObject().add("called", "getJobStatus").add("jobId", jobId);
    }

    @Override
    public JsonObject listApplications() throws Exception {
        callNum++;
        called.add("listApplications");
        return new JsonObject().add("called", "listApplications");
    }

    @Override
    public JsonObject listExporters() throws Exception {
        callNum++;
        called.add("listExporters");
        return new JsonObject().add("called", "listExporters");
    }

    @Override
    public JsonObject listImporters() throws Exception {
        callNum++;
        called.add("listImporters");
        return new JsonObject().add("called", "listImporters");
    }

    @Override
    public JsonObject getExporterParameters(String path, String exporter) throws Exception {
        callNum++;
        called.add("getExporterParameters");
        return new JsonObject().add("called", "getExporterParameters")
                               .add("path",  path)
                               .add("exporter", exporter);
    }

    @Override
    public JsonObject getImporterParameters(String path, String importer) throws Exception {
        callNum++;
        called.add("getImporterParameters");
        return new JsonObject().add("called", "getImporterParameters")
                               .add("path",  path)
                               .add("importer", importer);
    }

    @Override
    public JsonObject getAnalysisParameters(String appName) throws Exception {
        callNum++;
        called.add("getAnalysisParameters");
        return new JsonObject().add("called", "getAnalysisParameters")
                               .add("name", appName);
    }

    @Override
    public void export(String path, String exporter, OutputStream fs,
                       JsonObject params) throws Exception {
        try {
            callNum++;
            called.add("export");
            forVoid = new JsonObject().add("called", "export")
                               .add("path", path)
                               .add("exporter", exporter)
                               .add("params", params);
        } finally {
            fs.close();
        }
        
    }

    @Override
    public JsonObject imPort(String file, String parentPath, String importer,
                             JsonObject params) throws Exception {
        callNum++;
        called.add("imPort");
        return new JsonObject().add("called", "imPort")
                               .add("path", parentPath)
                               .add("importer", importer)
                               .add("params", params);
    }

    @Override
    public GxHttpConnection getConnection() {
        return con;
    }

    @Override
    public GxHttpClient setConnection(GxHttpConnection con) throws Exception {
        this.con = con;
        return this;
    }

    @Override
    public boolean getVerbose() {
        // TODO Auto-generated method stub
        return false;
    }

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
