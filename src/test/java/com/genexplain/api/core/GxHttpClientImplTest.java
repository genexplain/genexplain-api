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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.genexplain.api.core.GxColumnDef;
import com.genexplain.api.core.GxHttpClient;
import com.genexplain.api.core.GxHttpClientImpl;
import com.genexplain.api.core.GxHttpConnection;


public class GxHttpClientImplTest {
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    private String user = "tester";
    private String pass = "testing";
    private String project = "test_project";
    private String desc    = "test_project description";
    
    private GxHttpClient client;
    
    @Before
    public void createClient() {
        client = new GxHttpClientImpl();
    }
    
    @Test
    public void canSetConnection() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        
        con.login();
        client.setConnection(con);
        
        assertEquals(client.getConnection(),con);
    }
    
    @Test
    public void setConnectionLogsInIfNotLoggedIn() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        
        boolean before = con.hasLoggedIn();
        client.setConnection(con);
        
        assertEquals(before,false);
        assertEquals(con.hasLoggedIn(),true);
    }
    
    private Map<String,String> getCreateProjectParams() {
        Map<String,String> params = new HashMap<>();
        params.put("user", user);
        params.put("pass", pass);
        params.put("project", project);
        params.put("description", desc);
        return params;
    }
    
    @Test
    public void canCreateProject() throws Exception {
        GxHttpConnection con    = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        Map<String,String> params = getCreateProjectParams();
        JsonObject js = client.createProject(params);
        assertEquals(js.getString("path",""), con.getBasePath() + GxHttpClient.Path.CREATE_PROJECT.getPath());
        assertEquals(js.getString("user", ""), user);
        assertEquals(js.getString("pass", ""), pass);
        assertEquals(js.getString("project", ""), project);
        assertEquals(js.getString("description", ""), desc);
    }
    
    @Test
    public void canCreateFolder() throws Exception {
        GxHttpConnection con    = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        String parent = "data/Projects/test_folder_parent";
        String folder =  "test_folder";
        JsonObject js = client.createFolder(parent, folder);
        assertEquals(js.getString("path",""), con.getBasePath() + GxHttpClient.Path.CREATE_FOLDER.getPath());
        assertEquals(js.getString("service",""), "access.service");
        assertEquals(js.getString("command",""), "25");
        assertEquals(js.getString("dc",""), parent);
        assertEquals(js.getString("de",""), folder);
    }
    
    @Test
    public void createProjectExceptionForMissingUser() throws Exception {
        GxHttpConnection con    = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        Map<String,String> params = getCreateProjectParams();
        params.remove("user");
        exception.expect(IllegalArgumentException.class);
        client.createProject(params);
    }
    
    @Test
    public void createProjectExceptionForMissingPassword() throws Exception {
        GxHttpConnection con    = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        Map<String,String> params = getCreateProjectParams();
        params.remove("pass");
        exception.expect(IllegalArgumentException.class);
        client.createProject(params);
    }
    
    @Test
    public void createProjectExceptionForInvalidProject() throws Exception {
        GxHttpConnection con    = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        Map<String,String> params = getCreateProjectParams();
        params.put("project", "###");
        exception.expect(IllegalArgumentException.class);
        client.createProject(params);
    }
    
    @Test
    public void createProjectReplaceEmptyDescription() throws Exception {
        GxHttpConnection con    = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        Map<String,String> params = getCreateProjectParams();
        params.remove("description");
        JsonObject js = client.createProject(params);
        assertEquals(js.getString("path",""), con.getBasePath() + GxHttpClient.Path.CREATE_PROJECT.getPath());
        assertEquals(js.getString("description", ""), project);
    }
    
    @Test
    public void canSetVerbose() {
        GxHttpClient client = new GxHttpClientImpl();
        if (client.getVerbose() == false) {
            client.setVerbose(true);
            assertEquals(client.getVerbose(), true);
        } else {
            client.setVerbose(false);
            assertEquals(client.getVerbose(), false);
        }
    }
    
    @Test
    public void canDeleteElement() throws Exception {
        GxHttpConnection con    = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        String dc = "data/Projects/my_project";
        String de = "data_element";
        JsonObject js = client.deleteElement(dc, de);
        assertEquals(js.getString("path",""), con.getBasePath() + GxHttpClient.Path.DELETE_ELEMENT.getPath());
        assertEquals(js.getString("service", ""), "access.service");
        assertEquals(js.getString("command", ""), "26");
        assertEquals(js.getString("dc", ""), dc);
        assertEquals(js.getString("de", ""), de);
    }
    
    @Test
    public void deleteElementExceptionForInvalidFolder() throws Exception {
        GxHttpConnection con    = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        String dc = "Projects/my_project";
        String de = "data_element";
        exception.expect(IllegalArgumentException.class);
        client.deleteElement(dc, de);
    }
    
    @Test
    public void canTestExistsElement() throws Exception {
        GxHttpConnection con    = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        String dc = "data/Projects/my_project";
        String de = "data_element";
        JsonObject js = client.existsElement(de, dc);
        assertEquals(js.getString("path",""), con.getBasePath() + GxHttpClient.Path.LIST.getPath());
        assertEquals(js.getString("service", ""), "access.service");
        assertEquals(js.getString("command", ""), "29");
        assertEquals(js.getString("dc", ""), dc);
    }
    
    @Test
    public void canListFolder() throws Exception {
        GxHttpConnection con    = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        String folder = "data/Projects/my_project";
        JsonObject js = client.list(folder);
        assertEquals(js.getString("path",""), con.getBasePath() + GxHttpClient.Path.LIST.getPath());
        assertEquals(js.getString("service", ""), "access.service");
        assertEquals(js.getString("command", ""), "29");
        assertEquals(js.getString("dc", ""), folder);
    }
    
    @Test
    public void listReturnsValuesObject() throws Exception {
        GxHttpConnection con    = new GxHttpConnectionStub() {
            @Override
            public JsonObject queryJSON(String path, Map<String, String> params) throws Exception {
                JsonObject js = new JsonObject();
                JsonObject vals = new JsonObject();
                vals.add("value", "list");
                js.add("values", vals.toString());
                return js;
            }
        };
        con.login();
        client.setConnection(con);
        String folder = "data/Projects/my_project";
        JsonObject js = client.list(folder);
        assertNotNull(js.get("value"));
        assertEquals(js.get("value").asString(),"list");
    }
    
    @Test
    public void canGetTable() throws Exception {
        class ConnectionStub extends GxHttpConnectionStub {
            private int queryCount = 0;
            
            public int getQueryCount() { return queryCount; }
            
            @Override
            public JsonObject queryJSON(String path, Map<String, String> params) throws Exception {
                queryCount++;
                JsonObject js = new JsonObject();
                if (path.endsWith(GxHttpClient.Path.TABLE_COLUMNS.getPath())) {
                    js.add("type", 0);
                    js.add("values", new JsonArray().add("Column 1").add("Column 2"));
                } else if (path.endsWith(GxHttpClient.Path.TABLE_DATA.getPath())) {
                    js.add("type", 0);
                    js.add("values", new JsonArray()
                            .add(new JsonArray().add(1).add(2))
                            .add(new JsonArray().add(3).add(4)));
                } else {
                    js.add("type",1);
                    js.add("error","Received wrong path in test: " + path);
                }
                return js;
            }
        };
        
        ConnectionStub con = new ConnectionStub();
        con.login();
        client.setConnection(con);
        
        String table = "data/Projects/my_project/Data/my_table";
        JsonObject js = client.getTable(table);
        assertEquals(con.getQueryCount(), 2);
        assertNotNull(js.get("data"));
        assertNotNull(js.get("columns"));
    }
    
    @Test
    public void listReturnsFailureResponseColumns() throws Exception {
        class ConnectionStub extends GxHttpConnectionStub {
            private int queryCount = 0;
            
            public int getQueryCount() { return queryCount; }
            
            @Override
            public JsonObject queryJSON(String path, Map<String, String> params) throws Exception {
                queryCount++;
                JsonObject js = new JsonObject();
                if (path.endsWith(GxHttpClient.Path.TABLE_COLUMNS.getPath())) {
                    js.add("type", 1);
                    js.add("info", "column_test");
                } else if (path.endsWith(GxHttpClient.Path.TABLE_DATA.getPath())) {
                    js.add("type", 1);
                    js.add("error", "should_not_be_called");
                } else {
                    js.add("type",1);
                    js.add("error","wrong_path " + path);
                }
                return js;
            }
        };
        
        ConnectionStub con = new ConnectionStub();
        con.login();
        client.setConnection(con);
        
        String folder = "data/Projects/my_project";
        JsonObject js = client.getTable(folder);
        assertEquals(con.getQueryCount(), 1);
        assertNotNull(js.get("type"));
        assertNull(js.get("error"));
        assertNotNull(js.get("info"));
        assertEquals(js.get("info").asString(),"column_test");
        assertEquals(js.get("type").asInt(), 1);
    }
    
    @Test
    public void listReturnsFailureResponseData() throws Exception {
        class ConnectionStub extends GxHttpConnectionStub {
            private int queryCount = 0;
            
            public int getQueryCount() { return queryCount; }
            
            @Override
            public JsonObject queryJSON(String path, Map<String, String> params) throws Exception {
                queryCount++;
                JsonObject js = new JsonObject();
                if (path.endsWith(GxHttpClient.Path.TABLE_COLUMNS.getPath())) {
                    js.add("type", 0);
                    js.add("values", new JsonArray().add("Column 1").add("Column 2"));
                } else if (path.endsWith(GxHttpClient.Path.TABLE_DATA.getPath())) {
                    js.add("type", 1);
                    js.add("info", "data_test");
                } else {
                    js.add("type",1);
                    js.add("error","wrong_path " + path);
                }
                return js;
            }
        };
        
        ConnectionStub con = new ConnectionStub();
        con.login();
        client.setConnection(con);
        
        String folder = "data/Projects/my_project";
        JsonObject js = client.getTable(folder);
        assertEquals(con.getQueryCount(), 2);
        assertNotNull(js.get("type"));
        assertNotNull(js.get("info"));
        assertEquals(js.get("info").asString(),"data_test");
        assertNull(js.get("error"));
        assertEquals(js.get("type").asInt(), 1);
    }
    
    @Test
    public void canPutTable() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        String    path = "data/Projects/my_project/Data/put_table";
        JsonArray data = new JsonArray().add(new JsonArray().add("T1").add("T2")).add(new JsonArray().add(1).add(2)).add(new JsonArray().add("A").add("B"));
        List<GxColumnDef> columns = new ArrayList<>();
        GxColumnDef def = new GxColumnDef();
        def.setName("ID");
        def.setType(GxColumnDef.ColumnType.Text);
        columns.add(def);
        def = new GxColumnDef();
        def.setName("Column 2");
        def.setType(GxColumnDef.ColumnType.Integer);
        columns.add(def);
        def = new GxColumnDef();
        def.setName("Column 3");
        def.setType(GxColumnDef.ColumnType.Text);
        columns.add(def);
        JsonObject js = client.putTable(path, data, columns);
        assertEquals(js.getString("path",""), con.getBasePath() + GxHttpClient.Path.PUT_TABLE.getPath());
        assertEquals(js.getString("de",""), path);
        assertNotNull(js.get("columns"));
        JsonArray T = Json.parse(js.get("columns").asString()).asArray();
        assertEquals(T.get(0).asObject().get("name").asString(),"ID");
        assertEquals(T.get(1).asObject().get("name").asString(),"Column 2");
        assertNotNull(js.get("data"));
        T = Json.parse(js.get("data").asString()).asArray();
        assertEquals(T.asArray().get(0).asArray().get(0).asString(),"T1");
        assertEquals(T.asArray().get(1).asArray().get(0).asInt(),1);
    }
    
    @Test
    public void putTableExceptionForEmptyColumns() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        String path = "data/Projects/my_project/Data/put_table";
        JsonArray data = new JsonArray().add(new JsonArray().add("T1").add("T2")).add(new JsonArray().add(1).add(2)).add(new JsonArray().add("A").add("B"));
        List<GxColumnDef> columns = new ArrayList<>();
        exception.expect(IllegalArgumentException.class);
        client.putTable(path, data, columns);
    }
    
    @Test
    public void putTableExceptionForEmptyData() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        String path = "data/Projects/my_project/Data/put_table";
        JsonArray data = new JsonArray();
        List<GxColumnDef> columns = new ArrayList<>();
        GxColumnDef def = new GxColumnDef();
        def.setName("ID");
        def.setType(GxColumnDef.ColumnType.Text);
        columns.add(def);
        def = new GxColumnDef();
        def.setName("Column 2");
        def.setType(GxColumnDef.ColumnType.Integer);
        columns.add(def);
        exception.expect(IllegalArgumentException.class);
        client.putTable(path, data, columns);
    }
    
    @Test
    public void putTableExceptionForUnequalColumnNums() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        String path = "data/Projects/my_project/Data/put_table";
        JsonArray data = new JsonArray().add(new JsonArray().add("T1").add("T2")).add(new JsonArray().add(1).add(2)).add(new JsonArray().add("A").add("B"));
        List<GxColumnDef> columns = new ArrayList<>();
        GxColumnDef def = new GxColumnDef();
        def.setName("ID");
        def.setType(GxColumnDef.ColumnType.Text);
        columns.add(def);
        def = new GxColumnDef();
        def.setName("Column 2");
        def.setType(GxColumnDef.ColumnType.Integer);
        columns.add(def);
        exception.expect(IllegalArgumentException.class);
        client.putTable(path, data, columns);
    }
    
    @Test
    public void putTableExceptionForEmptyColumnName() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        String path = "data/Projects/my_project/Data/put_table";
        JsonArray data = new JsonArray().add(new JsonArray().add("T1").add("T2")).add(new JsonArray().add(1).add(2));
        List<GxColumnDef> columns = new ArrayList<>();
        GxColumnDef def = new GxColumnDef();
        def.setName("ID");
        def.setType(GxColumnDef.ColumnType.Text);
        columns.add(def);
        def = new GxColumnDef();
        def.setName("");
        def.setType(GxColumnDef.ColumnType.Integer);
        columns.add(def);
        exception.expect(IllegalArgumentException.class);
        client.putTable(path, data, columns);
    }
    
    @Test
    public void putTableExceptionForDoubleColumnName() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        String path = "data/Projects/my_project/Data/put_table";
        JsonArray data = new JsonArray().add(new JsonArray().add("T1").add("T2")).add(new JsonArray().add(1).add(2));
        List<GxColumnDef> columns = new ArrayList<>();
        GxColumnDef def = new GxColumnDef();
        def.setName("ID");
        def.setType(GxColumnDef.ColumnType.Text);
        columns.add(def);
        def = new GxColumnDef();
        def.setName("ID");
        def.setType(GxColumnDef.ColumnType.Integer);
        columns.add(def);
        exception.expect(IllegalArgumentException.class);
        client.putTable(path, data, columns);
    }
    
    @Test
    public void putTableExceptionForIDNotTestColumn() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        String path = "data/Projects/my_project/Data/put_table";
        JsonArray data = new JsonArray().add(new JsonArray().add("T1").add("T2")).add(new JsonArray().add(1).add(2));
        List<GxColumnDef> columns = new ArrayList<>();
        GxColumnDef def = new GxColumnDef();
        def.setName("ID");
        def.setType(GxColumnDef.ColumnType.Integer);
        columns.add(def);
        def = new GxColumnDef();
        def.setName("ID");
        def.setType(GxColumnDef.ColumnType.Integer);
        columns.add(def);
        exception.expect(IllegalArgumentException.class);
        client.putTable(path, data, columns);
    }
    
    @Test
    public void putTableExceptionForMissingIDColumn() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        String path = "data/Projects/my_project/Data/put_table";
        JsonArray data = new JsonArray().add(new JsonArray().add("T1").add("T2")).add(new JsonArray().add(1).add(2));
        List<GxColumnDef> columns = new ArrayList<>();
        GxColumnDef def = new GxColumnDef();
        def.setName("notID");
        def.setType(GxColumnDef.ColumnType.Text);
        columns.add(def);
        def = new GxColumnDef();
        def.setName("alsoNotID");
        def.setType(GxColumnDef.ColumnType.Integer);
        columns.add(def);
        exception.expect(IllegalArgumentException.class);
        client.putTable(path, data, columns);
    }
    
    @Test
    public void canAnalyze() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        JsonObject params = new JsonObject();
        params.add("param1","value1");
        params.add("param2","value2");
        JsonObject js = client.analyze("testApp", params, false, false, false);
        assertEquals(js.get("path").asString(), con.getBasePath() + GxHttpClient.Path.ANALYZE.getPath());
        assertEquals(js.get("de").asString(),"testApp");
        assertNotNull(js.get("jobID"));
        JsonArray jo = Json.parse(js.getString("json","")).asArray();
        assertEquals(jo.size(),2);
        assertEquals(jo.get(0).asObject().get("name").asString(),"param1");
        assertEquals(jo.get(0).asObject().get("value").asString(),"value1");
        assertEquals(jo.get(1).asObject().get("name").asString(),"param2");
        assertEquals(jo.get(1).asObject().get("value").asString(),"value2");
    }
    
    @Test
    public void interceptsTerminatedByError() throws Exception {
        GxHttpConnectionStub con = new GxHttpConnectionStub();
        con.setSendTerminatedByError(true);
        con.login();
        client.setConnection(con);
        JsonObject params = new JsonObject();
        params.add("param1","value1");
        params.add("param2","value2");
        JsonObject js = client.analyze("testApp", params, false, true, false);
        assertTrue(js.getString("message", "").startsWith(GxHttpClient.JobStatus.TERMINATED_BY_ERROR.toString()));
    }
    
    private class AnalysisConnectionStub extends GxHttpConnectionStub {
        private int    queryCount = 0;
        private String jobId      = "";
        private int    equalId    = 0;
        private GxHttpClient.Path analysisPath = GxHttpClient.Path.ANALYZE;
        
        public int getQueryCount() { return queryCount; }
        public int getEqualId() { return equalId; }
        public void setPath(GxHttpClient.Path path) {
            analysisPath = path;
        }
        
        
        @Override
        public JsonObject queryJSON(String path, Map<String, String> params) throws Exception {
            queryCount++;
            JsonObject js = new JsonObject();
            if (path.endsWith(analysisPath.getPath())) {
                js.add("type", 0);
                js.add("test", "is_returned");
                jobId = params.get("jobID");
            } else if (path.endsWith(GxHttpClient.Path.JOB_CONTROL.getPath())) {
                js.add("type", 0);
                if (queryCount < 4) {
                    js.add("status", 2);
                } else {
                    js.add("values", "completed");
                    js.add("status", 4);
                    js.add("percent", 100);

                }
                if (params.get("jobID").equals(jobId)) {
                    equalId++;
                }
            } else {
                js.add("type",1);
                js.add("error","wrong_path " + path);
            }
            return js;
        }
    }
    
    @Test
    public void canAnalyzeAndWait() throws Exception {
        AnalysisConnectionStub con = new AnalysisConnectionStub();
        con.setPath(GxHttpClient.Path.ANALYZE);
        con.login();
        client.setConnection(con);
        JsonObject params = new JsonObject();
        JsonObject js = client.analyze("testApp", params, false, true, true);
        assertEquals(js.get("type").asInt(),0);
        assertEquals(con.getQueryCount(),4);
        assertEquals(con.getEqualId(),3);
        assertEquals(js.get("test").asString(),"is_returned");
    }
    
    @Test
    public void canAnalyzeWorkflow() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        JsonObject params = new JsonObject();
        params.add("param1","value1");
        params.add("param2","value2");
        params.add("param3",new JsonArray().add(1).add(2).add(3).add(4));
        JsonObject js = client.analyze("testWorkflow", params, true, false, false);
        assertEquals(js.get("path").asString(), con.getBasePath() + GxHttpClient.Path.WORKFLOW.getPath());
        assertEquals(js.get("de").asString(),"testWorkflow");
        assertNotNull(js.get("jobID"));
        JsonArray jo = Json.parse(js.getString("json","")).asArray();
        assertEquals(jo.size(),3);
        assertEquals(jo.get(0).asObject().get("name").asString(),"param1");
        assertEquals(jo.get(0).asObject().get("value").asString(),"value1");
        assertEquals(jo.get(1).asObject().get("name").asString(),"param2");
        assertEquals(jo.get(1).asObject().get("value").asString(),"value2");
        assertEquals(jo.get(2).asObject().get("name").asString(),"param3");
        assertEquals(jo.get(2).asObject().get("value").asArray().get(0).asInt(),1);
        assertEquals(js.get("action").asString(),"start_workflow");
    }
    
    
    @Test
    public void canAnalyzeWorkflowAndWait() throws Exception {
        AnalysisConnectionStub con = new AnalysisConnectionStub();
        con.setPath(GxHttpClient.Path.WORKFLOW);
        con.login();
        client.setConnection(con);
        JsonObject params = new JsonObject();
        JsonObject js = client.analyze("testWorkflow", params, true, true, true);
        assertEquals(js.get("type").asInt(),0);
        assertEquals(con.getQueryCount(),4);
        assertEquals(con.getEqualId(),3);
        assertEquals(js.get("test").asString(),"is_returned");
    }
    
    
    @Test
    public void canGetJobStatus() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        
        JsonObject js = client.getJobStatus("test-jobid");
        assertEquals(js.get("type").asInt(),0);
        assertEquals(js.get("path").asString(), con.getBasePath() + GxHttpClient.Path.JOB_CONTROL.getPath());
        assertEquals(js.get("jobID").asString(),"test-jobid");
    }
    
    @Test
    public void canListApplications() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        
        JsonObject js = client.listApplications();
        assertEquals(js.get("type").asInt(),0);
        assertEquals(js.get("path").asString(), con.getBasePath() + GxHttpClient.Path.ANALYSIS_LIST.getPath());
    }
    
    @Test
    public void canListExporters() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        
        JsonObject js = client.listExporters();
        assertEquals(js.get("type").asInt(),0);
        assertEquals(js.get("path").asString(), con.getBasePath() + GxHttpClient.Path.EXPORT_LIST.getPath());
    }
    
    @Test
    public void canListImporters() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        
        JsonObject js = client.listImporters();
        assertEquals(js.get("type").asInt(),0);
        assertEquals(js.get("path").asString(), con.getBasePath() + GxHttpClient.Path.IMPORT_LIST.getPath());
    }
    
    @Test
    public void canGetExporterParameters() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        
        JsonObject js = client.getExporterParameters("data/test", "test_exporter");
        assertEquals(js.get("type").asInt(),0);
        assertEquals(js.get("path").asString(), con.getBasePath() + GxHttpClient.Path.EXPORT.getPath());
        assertEquals(js.get("detype").asString(),"Element");
        assertEquals(js.get("exporter").asString(),"test_exporter");
        assertEquals(js.get("de").asString(),"data/test");
    }
    
    @Test
    public void canGetImporterParameters() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        
        JsonObject js = client.getImporterParameters("data/test", "test_importer");
        assertEquals(js.get("type").asInt(),0);
        assertEquals(js.get("path").asString(), con.getBasePath() + GxHttpClient.Path.IMPORT.getPath());
        assertEquals(js.get("detype").asString(),"Element");
        assertEquals(js.get("format").asString(),"test_importer");
        assertEquals(js.get("de").asString(),"data/test");
    }
    
    @Test
    public void canGetAnalysisParameters() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub() {
            @Override
            public JsonObject queryJSON(String path, Map<String, String> params) throws Exception {
                JsonObject js = new JsonObject();
                js.add("path", path);
                js.add("values", new JsonArray().add("test_param"));
                params.forEach((key,val) -> { js.add(key, val); });
                js.add("type", 0);
                return js;
            }
        };
        con.login();
        client.setConnection(con);
        JsonObject js = client.getAnalysisParameters("test_app");
        assertEquals(js.get("type").asInt(),0);
        assertEquals(js.get("path").asString(), con.getBasePath() + GxHttpClient.Path.ANALYSIS_PARAMS.getPath());
        assertEquals(js.get("de").asString(),"properties/method/parameters/test_app");
        assertEquals(js.get("parameters").asArray().get(0).asString(),"test_param");
    }
    
    @Test
    public void getAnalysisNoParametersOnError() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub() {
            @Override
            public JsonObject queryJSON(String path, Map<String, String> params) throws Exception {
                JsonObject js = new JsonObject();
                js.add("path", path);
                js.add("values", new JsonArray().add("test_param"));
                params.forEach((key,val) -> { js.add(key, val); });
                js.add("type", 1);
                return js;
            }
        };
        con.login();
        client.setConnection(con);
        JsonObject js = client.getAnalysisParameters("test_app");
        assertEquals(js.get("type").asInt(),1);
        assertEquals(js.get("path").asString(), con.getBasePath() + GxHttpClient.Path.ANALYSIS_PARAMS.getPath());
        assertEquals(js.get("de").asString(),"properties/method/parameters/test_app");
        assertNull(js.get("parameters"));
    }
    
    @Test
    public void canExport() throws Exception {
        GxHttpConnection con = new GxHttpConnectionStub();
        con.login();
        client.setConnection(con);
        OutputStreamStub os = new OutputStreamStub();
        JsonObject params = new JsonObject();
        params.add("test_param1", "test_value1");
        params.add("test_param2", "test_value2");
        client.export("data/test", "test_exporter", os, params);
        System.out.println(os.getData());
        assertNotNull(os.getData());
        assertTrue(os.getData().length() > 0);
        JsonObject js = Json.parse(os.getData()).asObject();
        assertEquals(js.get("path").asString(), con.getBasePath() + GxHttpClient.Path.EXPORT.getPath());
        assertEquals(js.get("detype").asString(), "Element");
        assertEquals(js.get("de").asString(), "data/test");
        assertEquals(js.get("exporter").asString(), "test_exporter");
        JsonArray ja = Json.parse(js.get("parameters").asString()).asArray();
        assertTrue(ja.get(0).asObject().get("name").asString().matches("test_param[12]"));
        assertTrue(ja.get(0).asObject().get("value").asString().matches("test_value[12]"));
        assertTrue(ja.get(1).asObject().get("name").asString().matches("test_param[12]"));
        assertTrue(ja.get(1).asObject().get("value").asString().matches("test_value[12]"));
        
    }
    
    @Test
    public void canImport() throws Exception {
        GxHttpConnectionStub con = new GxHttpConnectionStub();
        con.setHttpClient(new CloseableHttpClientStub());
        con.login();
        client.setConnection(con);
        JsonObject params = new JsonObject();
        params.add("test_param1", "test_value1");
        JsonObject js = client.imPort("src/test_resources/test_import.txt","test/data/parent","test_importer",params);
        assertEquals(js.get("type").asInt(),0);
        assertEquals(js.get("path").asString(), con.getBasePath() + GxHttpClient.Path.IMPORT.getPath());
        assertEquals(js.get("de").asString(), "test/data/parent");
        assertEquals(js.get("format").asString(), "test_importer");
        assertNotNull(js.get("fileID"));
        assertNotNull(js.get("jobID"));
        JsonArray ja = Json.parse(js.get("json").asString()).asArray();
        assertTrue(ja.get(0).asObject().get("name").asString().equals("test_param1"));
        assertTrue(ja.get(0).asObject().get("value").asString().equals("test_value1"));
    }
}
