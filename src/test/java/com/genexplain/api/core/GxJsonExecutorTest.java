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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.genexplain.api.core.GxJsonExecutor;
import com.genexplain.api.core.GxJsonExecutorException;
import com.genexplain.api.core.GxJsonExecutorParameters;
import com.genexplain.util.GxUtil;

public class GxJsonExecutorTest {
    
    private GxHttpClientStub     client;
    private GxHttpConnectionStub connection;
    private GxJsonExecutor       executor;
    private JsonObject           conf;
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Before
    public void create() throws Exception {
        client     = new GxHttpClientStub();
        connection = new GxHttpConnectionStub();
        connection.login();
        client.setConnection(connection);
        GxJsonExecutorParameters params = new GxJsonExecutorParameters();
        params.setHttpConnection(connection);
        params.setHttpClient(client);
        executor   = new GxJsonExecutor();
        executor.setParameters(params);
        conf = new JsonObject();
        
    }
    
    @Test
    public void canAnalyzeTool() throws Exception {
        String method = "test analysis method";
        conf.add("method", method);
        JsonObject params = new JsonObject().add("param1", "value1").add("param2", "value2");
        conf.add("parameters", params);
        conf.add("workflow", false);
        conf.add("wait", false);
        conf.add("progress", true);
        JsonObject lastJson = executor.analyze(conf).getLastJsonObject();
        assertNotNull(lastJson);
        assertFalse(lastJson.isEmpty());
        assertEquals(lastJson.getString("app",""), method);
        assertFalse(lastJson.getBoolean("wait", true));
        assertTrue(lastJson.getBoolean("progress", false));
        assertEquals(lastJson.getString("called",""), "analyze");
    }
    
    @Test
    public void getTablePrintsStdout() throws Exception {
        conf.add("table", "test print table")
            .add("toStdout", true);
        
        PrintStream           stdOut  = System.out;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(ostream));
        
        JsonObject lastJson = executor.getTable(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called",""), "getTable");
        
        System.setOut(stdOut);
        
        JsonObject pr = Json.parse(ostream.toString()).asObject();
        assertEquals(lastJson.size(), pr.size());
        assertEquals(lastJson.get("called").asString(), pr.get("called").asString());
        assertEquals(lastJson.get("table").asString(), pr.get("table").asString());
    }
    
    
    /**
     * Integration test
     * 
     * @throws Exception
     */
    @Test
    public void getTablePrintsFile() throws Exception {
        String file = "src/test/resources/test_file.txt";
        conf.add("table",  "test print table")
            .add("toFile", file);
        
        JsonObject lastJson = executor.getTable(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called",""), "getTable");
        
        JsonObject pr = Json.parse(new FileReader(file)).asObject();
        
        assertEquals(lastJson.size(), pr.size());
        assertEquals(lastJson.get("called").asString(), pr.get("called").asString());
        assertEquals(lastJson.get("table").asString(), pr.get("table").asString());
        
        if (!(new File(file).delete())) {
            throw new IOException("Could not delete file " + file);
        }
    }
    
    
    @Test
    public void canAnalyzeWorkflow() throws Exception {
        String method = "test workflow";
        conf.add("method", method);
        JsonObject params = new JsonObject().add("param1",GxJsonExecutor.ExecutorType.analyze.get())
                                            .add("param2", "value2");
        conf.add("parameters", params);
        conf.add("workflow", true);
        conf.add("wait", false);
        conf.add("progress", true);
        JsonObject lastJson = executor.analyze(conf).getLastJsonObject();
        assertNotNull(lastJson);
        assertFalse(lastJson.isEmpty());
        assertEquals(lastJson.getString("app",""), method);
        assertFalse(lastJson.getBoolean("wait", true));
        assertTrue(lastJson.getBoolean("progress", false));
        assertEquals(lastJson.getString("called",""), "analyze");
    }
    
    @Test
    public void analyzeExceptionForMissingMethod() throws Exception {
        JsonObject params = new JsonObject().add("param1", GxJsonExecutor.ExecutorType.analyze.get())
                                            .add("param2", "value2");
        conf.add("parameters", params);
        conf.add("workflow", true);
        conf.add("wait", false);
        conf.add("progress", true);
        exception.expect(IllegalArgumentException.class);
        executor.analyze(conf).getLastJsonObject();
    }
    
    @Test
    public void executesListOfTasks() throws Exception {
        JsonArray tasks = new JsonArray();
        tasks.add(new JsonObject()
                .add("do", GxJsonExecutor.ExecutorType.listItems.toString())
                .add("type", "applications"))
             .add(new JsonObject()
                .add("do", GxJsonExecutor.ExecutorType.listItems.toString())
                .add("type", "importers"))
             .add(new JsonObject()
                .add("do", GxJsonExecutor.ExecutorType.listItems.toString())
                .add("type", "exporters"))
             .add(new JsonObject()
                .add("do", GxJsonExecutor.ExecutorType.list.toString())
                .add("folder", "test folder"))
             .add(new JsonObject()
                .add("do", GxJsonExecutor.ExecutorType.jobStatus.toString())
                .add("jobId", "abcd1234"));
        JsonObject lastJson = executor.execute(tasks).getLastJsonObject();
        assertEquals(client.getCallNum(), tasks.size());
        List<String> called = client.getCalled();
        assertEquals(called.get(0), "listApplications");
        assertEquals(called.get(1), "listImporters");
        assertEquals(called.get(2), "listExporters");
        assertEquals(called.get(3), "list");
        assertEquals(called.get(4), "getJobStatus");
        assertEquals(lastJson.getString("jobId",""),"abcd1234");
    }
    
    @Test
    public void executeThrowsException() throws Exception {
        JsonArray tasks = new JsonArray();
        tasks.add(new JsonObject()
                .add("do", "listItems")
                .add("type", "applications"))
             .add(new JsonObject()
                .add("do",GxJsonExecutor.ExecutorType.jobStatus.toString()));
        exception.expect(GxJsonExecutorException.class);
        executor.execute(tasks).getLastJsonObject();
        assertEquals(client.getCallNum(), tasks.size());
    }
    
    @Test
    public void canCreateFolder() throws Exception {
        conf.add("path", "test path").add("name", "test name");
        JsonObject lastJson = executor.createFolder(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "createFolder");
        assertEquals(client.getCalled().get(0), "createFolder");
        assertEquals(lastJson.getString("path", ""), "test path");
        assertEquals(lastJson.getString("name", ""), "test name");
    }
    
    @Test
    public void canExecuteCreateFolder() throws Exception {
        conf.add("path", "test path").add("name", "test name");
        conf.add("do", GxJsonExecutor.ExecutorType.createFolder.toString());
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "createFolder");
        assertEquals(client.getCalled().get(0), "createFolder");
        assertEquals(lastJson.getString("path", ""), "test path");
        assertEquals(lastJson.getString("name", ""), "test name");
    }
    
    /**
     * Integration test
     * 
     * @throws Exception
     */
    @Test 
    public void canExport() throws Exception {
        JsonObject params = new JsonObject().add("param1", GxJsonExecutor.ExecutorType.export.get())
                                            .add("param2", "value2");
        String testFile = "src/test/resources/test_export_file";
        conf.add("file", testFile)
            .add("path", "test export path")
            .add("exporter", "test exporter")
            .add("do", "export")
            .add("parameters", params);        
        executor.execute(conf);
        
        JsonObject lastJson = ((GxHttpClientStub)executor.getParameters().getHttpClient()).getForVoid();
        
        if (!new File(testFile).delete()) {
            throw new IOException("Could not delete file: " + testFile);
        }

        assertEquals(lastJson.getString("called", ""), "export");
        assertEquals(lastJson.getString("path", ""), "test export path");
    }
    
    @Test 
    public void canExportExceptionForEmptyPath() throws Exception {
        JsonObject params = new JsonObject().add("param1", GxJsonExecutor.ExecutorType.export.get())
                                            .add("param2", "value2");
        String testFile = "src/test/resources/test_export_file";
        conf.add("file", testFile)
            .add("path", "")
            .add("exporter", "test exporter")
            .add("do", "export")
            .add("parameters", params);
        exception.expect(IllegalArgumentException.class);
        executor.execute(conf);
    }
    
    @Test 
    public void canExportExceptionForMissingPath() throws Exception {
        JsonObject params = new JsonObject().add("param1", GxJsonExecutor.ExecutorType.export.get())
                                            .add("param2", "value2");
        String testFile = "src/test/resources/test_export_file";
        conf.add("file", testFile)
            .add("exporter", "test exporter")
            .add("do", "export")
            .add("parameters", params);
        exception.expect(IllegalArgumentException.class);
        executor.execute(conf);
    }
    
    @Test
    public void canImport() throws Exception {
        JsonObject params = new JsonObject().add("param1", GxJsonExecutor.ExecutorType.imPort.get())
                                            .add("param2", "value2");
        String testFile = "test_file";
        conf.add("file", testFile)
            .add("path", "test import path")
            .add("importer", "test importer")
            .add("do", "imPort")
            .add("parameters", params);        
        
        
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        
        assertEquals(lastJson.getString("called", ""), "imPort");
        assertEquals(lastJson.getString("path", ""), "test import path");
        assertEquals(lastJson.getString("importer", ""), "test importer");
    }
    
    @Test
    public void canGetTable() throws Exception {
        conf.add("table", "test table");
        JsonObject lastJson = executor.getTable(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "getTable");
        assertEquals(client.getCalled().get(0), "getTable");
        assertEquals(lastJson.getString("table", ""), "test table");
    }
    
    @Test
    public void canPutTable() throws Exception {
        JsonArray cols = new JsonArray();
        cols.add(new JsonArray().add("ID").add("Text"))
            .add(new JsonArray().add("Val").add("Float"));
        JsonArray table = new JsonArray();
        table.add(new JsonArray().add("id1").add("id2"))
             .add(new JsonArray().add("1.2").add("3.4"));
        conf.add("path", "test put table path")
            .add("columns", cols)
            .add("table", table)
            .add("do", "put");
        
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        
        assertEquals(lastJson.getString("called", ""), "putTable");
        assertEquals(lastJson.getString("path", ""), "test put table path");
    }
    
    @Test
    public void putTableExceptionForEmptyPath() throws Exception {
        JsonArray cols = new JsonArray();
        cols.add(new JsonArray().add("ID").add("Text"))
            .add(new JsonArray().add("Val").add("Float"));
        JsonArray table = new JsonArray();
        table.add(new JsonArray().add("id1").add("id2"))
             .add(new JsonArray().add("1.2").add("3.4"));
        conf.add("path", "")
            .add("columns", cols)
            .add("table", table)
            .add("do", "put");
        exception.expect(IllegalArgumentException.class);
        executor.execute(conf).getLastJsonObject();
    }
    
    @Test
    public void putTableExceptionForEmptyColumns() throws Exception {
        JsonArray cols = new JsonArray();
        JsonArray table = new JsonArray();
        table.add(new JsonArray().add("id1").add("id2"))
             .add(new JsonArray().add("1.2").add("3.4"));
        conf.add("path", "test put table path")
            .add("columns", cols)
            .add("table", table)
            .add("do", "put");
        exception.expect(IllegalArgumentException.class);
        executor.execute(conf).getLastJsonObject();
    }
    
    @Test
    public void putTableExceptionForEmptyData() throws Exception {
        JsonArray cols = new JsonArray();
        cols.add(new JsonArray().add("ID").add("Text"))
            .add(new JsonArray().add("Val").add("Float"));
        JsonArray table = new JsonArray();
        conf.add("path", "test put table path")
            .add("columns", cols)
            .add("table", table)
            .add("do", "put");
        exception.expect(IllegalArgumentException.class);
        executor.execute(conf).getLastJsonObject();
    }
    
    @Test
    public void putTableExceptionForUnequalColNums() throws Exception {
        JsonArray cols = new JsonArray();
        cols.add(new JsonArray().add("ID").add("Text"))
            .add(new JsonArray().add("Val").add("Float"))
            .add(new JsonArray().add("Val2").add("Float"));
        JsonArray table = new JsonArray();
        table.add(new JsonArray().add("id1").add("id2"))
             .add(new JsonArray().add("1.2").add("3.4"));
        conf.add("path", "test put table path")
            .add("columns", cols)
            .add("table", table)
            .add("do", "put");
        exception.expect(IllegalArgumentException.class);
        executor.execute(conf).getLastJsonObject();
    }
    
    @Test
    public void putTableExceptionForMissingData() throws Exception {
        JsonArray cols = new JsonArray();
        cols.add(new JsonArray().add("ID").add("Text"))
            .add(new JsonArray().add("Val").add("Float"));
        JsonArray table = new JsonArray();
        table.add(new JsonArray().add("id1").add("id2"))
             .add(new JsonArray().add("1.2").add("3.4"));
        conf.add("path", "test put table path")
            .add("columns", cols)
            .add("do", "put");
        exception.expect(IllegalArgumentException.class);
        executor.execute(conf).getLastJsonObject();
    }
    
    /**
     * Integration test 
     * 
     * @throws Exception
     */
    @Test
    public void canPutTableFromFile() throws Exception {
        JsonArray cols = new JsonArray();
        cols.add(new JsonArray().add("ID").add("Text"))
            .add(new JsonArray().add("Val").add("Float"));
        
        String file = "src/test/resources/test_put_table.txt";
        GxUtil.writeToFile(file,"#\n#\nid1\t2.3\nid2\t4.5\n");
        conf.add("path", "test put table path")
            .add("columns", cols)
            .add("file", file)
            .add("skip", 2)
            .add("do", "put");
        
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        
        if (!new File(file).delete()) {
            throw new IOException("Could not delete file: " + file);
        }
        
        assertEquals(lastJson.getString("called", ""), "putTable");
        assertEquals(lastJson.getString("path", ""), "test put table path");
    }
    
    @Test
    public void canExecuteGetTable() throws Exception {
        conf.add("table", "test table").add("do", GxJsonExecutor.ExecutorType.get.toString());
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "getTable");
        assertEquals(client.getCalled().get(0), "getTable");
        assertEquals(lastJson.getString("table", ""), "test table");
    }
    
    @Test
    public void canListApplicationParamters() throws Exception {
        conf.add("type", "application")
            .add("name", "test analysis");
        JsonObject lastJson = executor.getItemParameters(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "getAnalysisParameters");
        assertEquals(client.getCalled().get(0), "getAnalysisParameters");
        assertEquals(lastJson.getString("name", ""), "test analysis");
    }
    
    @Test
    public void canExecuteListApplicationParamters() throws Exception {
        conf.add("type", "application")
            .add("name", "test analysis")
            .add("do", GxJsonExecutor.ExecutorType.itemParameters.toString());
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "getAnalysisParameters");
        assertEquals(client.getCalled().get(0), "getAnalysisParameters");
        assertEquals(lastJson.getString("name", ""), "test analysis");
    }
    
    @Test
    public void listItemParametersExceptionUnknownType() throws Exception {
        conf.add("type", "unknown")
            .add("name", "test importer")
            .add("path", "test path");
        exception.expect(IllegalArgumentException.class);
        executor.getItemParameters(conf).getLastJsonObject();
    }
    
    @Test
    public void listItemParametersExceptionWrongType() throws Exception {
        conf.add("type", "porter")
            .add("name", "test importer")
            .add("path", "test path");
        exception.expect(IllegalArgumentException.class);
        executor.getItemParameters(conf).getLastJsonObject();
    }
    
    @Test
    public void listItemParametersExceptionMissingName() throws Exception {
        conf.add("type", "application");
        exception.expect(IllegalArgumentException.class);
        executor.getItemParameters(conf).getLastJsonObject();
    }
    
    @Test
    public void listItemParametersExceptionMissingPath() throws Exception {
        conf.add("type", "importer")
            .add("name", "test importer");
        exception.expect(IllegalArgumentException.class);
        executor.getItemParameters(conf).getLastJsonObject();
    }
    
    @Test
    public void canListImporterParamters() throws Exception {
        conf.add("type", "importer")
            .add("name", "test importer")
            .add("path", "test path");
        JsonObject lastJson = executor.getItemParameters(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "getImporterParameters");
        assertEquals(client.getCalled().get(0), "getImporterParameters");
        assertEquals(lastJson.getString("importer", ""), "test importer");
    }
    
    @Test
    public void canExecuteListImporterParamters() throws Exception {
        conf.add("type", "importer")
            .add("name", "test importer")
            .add("path", "test path")
            .add("do", GxJsonExecutor.ExecutorType.itemParameters.toString());
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "getImporterParameters");
        assertEquals(client.getCalled().get(0), "getImporterParameters");
        assertEquals(lastJson.getString("importer", ""), "test importer");
    }
    
    @Test
    public void canListExporterParamters() throws Exception {
        conf.add("type", "exporter")
            .add("name", "test exporter")
            .add("path", "test path");
        JsonObject lastJson = executor.getItemParameters(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "getExporterParameters");
        assertEquals(client.getCalled().get(0), "getExporterParameters");
        assertEquals(lastJson.getString("exporter", ""), "test exporter");
    }
    
    @Test
    public void canExecuteListExporterParamters() throws Exception {
        conf.add("type", "exporter")
            .add("name", "test exporter")
            .add("path", "test path")
            .add("do", GxJsonExecutor.ExecutorType.itemParameters.toString());
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "getExporterParameters");
        assertEquals(client.getCalled().get(0), "getExporterParameters");
        assertEquals(lastJson.getString("exporter", ""), "test exporter");
    }
    
    @Test
    public void canRunFromInputStream() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n")
          .append("  \"connection-class\": \"com.genexplain.api.core.GxHttpConnectionStub\",\n")
          .append("  \"client-class\": \"com.genexplain.api.core.GxHttpClientStub\",\n")
          .append("  \"server\": \"http://test.server\",\n")
          .append("  \"user\": \"test.user\",\n")
          .append("  \"password\": \"test.password\",\n")
          .append("  \"tasks\": [\n")
          .append("    {\n")
          .append("      \"do\": \"get\",\n")
          .append("      \"table\": \"test table\"\n")
          .append("    }\n")
          .append("  ]\n")
          .append("}\n");
        InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
        executor.run(is);
        JsonObject lastJson = executor.getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "getTable");
        assertEquals(lastJson.getString("table", ""), "test table");
        assertEquals(executor.getParameters().getServer(), "http://test.server");
        assertEquals(executor.getParameters().getUser(), "test.user");
        assertEquals(executor.getParameters().getPassword(), "test.password");
    }
    
    /**
     * Integration test
     * 
     * @throws Exception
     */
    @Test
    public void canRunFromMain() throws Exception {
        StringBuilder sb = new StringBuilder();
        String outfile = "src/test/resources/test-executor.out";
        sb.append("{\n")
          .append("  \"connection-class\": \"com.genexplain.api.core.GxHttpConnectionStub\",\n")
          .append("  \"client-class\": \"com.genexplain.api.core.GxHttpClientStub\",\n")
          .append("  \"server\": \"http://test.server\",\n")
          .append("  \"user\": \"test.user\",\n")
          .append("  \"password\": \"test.password\",\n")
          .append("  \"tasks\": [\n")
          .append("    {\n")
          .append("      \"do\": \"get\",\n")
          .append("      \"table\": \"test table\",\n")
          .append("      \"toFile\": \"" + outfile + "\"\n")
          .append("    }\n")
          .append("  ]\n")
          .append("}\n");
        String file = "src/test/resources/test-executor-config.json";
        FileWriter fw = new FileWriter(file);
        fw.write(sb.toString());
        fw.close();
        
        new GxJsonExecutor().run(new String[]{file});
        
        if (!(new File(file).delete())) {
            throw new IOException("Could not delete file " + file);
        }
        
        JsonObject pr = Json.parse(new FileReader(outfile)).asObject();
        
        if (!(new File(outfile).delete())) {
            throw new IOException("Could not delete file " + outfile);
        }
        
        assertEquals(pr.get("called").asString(), "getTable");
        assertEquals(pr.get("table").asString(), "test table");
    }
    
    @Test
    public void noInputFileCausesMainMessage() throws Exception {
        PrintStream           stdOut  = System.out;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(ostream));
        
        new GxJsonExecutor().run(new String[0]);
        
        System.setOut(stdOut);
        
        assertEquals(ostream.toString().replaceAll("\\n$", ""), GxJsonExecutor.NO_INPUT_MESSAGE);
    }
}
