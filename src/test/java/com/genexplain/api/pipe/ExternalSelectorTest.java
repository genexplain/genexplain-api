package com.genexplain.api.pipe;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.genexplain.api.core.GxHttpClientStub;
import com.genexplain.api.core.GxHttpConnectionStub;
import com.genexplain.api.core.GxJsonExecutor;
import com.genexplain.api.core.GxJsonExecutorParameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ExternalSelectorTest {
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
    public void canSelectByExternalTool() throws Exception {
        if (System.getProperty("os.name").contains("inux") || System.getProperty("os.name").contains("nix")) {
            conf = new JsonObject().add("do", "branch")
                    .add("branchSelector", "com.genexplain.api.pipe.ExternalSelector")
                    .add("bin", "sh")
                    .add("params", "temp.sh")
                    .add("outputKey", "SELECT")
                    .add("selectTask", new JsonObject().add("do","setParameters")
                            .add("before", new JsonArray().add(new JsonArray().add("$FROM_FILE$").add("file_param"))));
            FileWriter fw = new FileWriter("temp.sh");
            fw.write("#!/bin/sh\necho \"SELECT=selectTask\"\n");
            fw.close();
            GxJsonExecutorParameters params = executor.getParameters();
            params.setReplaceStrings(new JsonArray());
            JsonObject lastJson = executor.execute(conf).getLastJsonObject();
            new File("temp.sh").delete();
            assertEquals(lastJson.getString("called", ""), "setTaskParameters");
            assertEquals(params.getReplaceStrings().get(0).asArray().get(0).asString(), "$FROM_FILE$");
            assertEquals(params.getReplaceStrings().size(), 1); 
        }
    }
    
    @Test
    public void interceptsMissingExternalTool() throws Exception {
        if (System.getProperty("os.name").contains("inux") || System.getProperty("os.name").contains("nix")) {
            conf = new JsonObject().add("do", "branch")
                    .add("branchSelector", "com.genexplain.api.pipe.ExternalSelector")
                    .add("params", "temp.sh")
                    .add("outputKey", "SELECT")
                    .add("selectTask", new JsonObject().add("do","setParameters")
                            .add("before", new JsonArray().add(new JsonArray().add("$FROM_FILE$").add("file_param"))));
            exception.expect(IllegalArgumentException.class);
            executor.execute(conf).getLastJsonObject(); 
        }
    }
    
    @Test
    public void checksExternalToolValue() throws Exception {
        if (System.getProperty("os.name").contains("inux") || System.getProperty("os.name").contains("nix")) {
            conf = new JsonObject().add("do", "branch")
                    .add("branchSelector", "com.genexplain.api.pipe.ExternalSelector")
                    .add("bin", "sh")
                    .add("params", new JsonArray().add("temp.sh").add("input_string"))
                    .add("outputKey", "SELECT")
                    .add("selectTask", new JsonObject().add("do","setParameters")
                            .add("before", new JsonArray().add(new JsonArray().add("$FROM_FILE$").add("file_param"))));
            FileWriter fw = new FileWriter("temp.sh");
            fw.write("#!/bin/sh\necho \"Exiting with error\"\nexit 1\n");
            fw.close();
            exception.expect(RuntimeException.class);
            executor.execute(conf);
            new File("temp.sh").delete(); 
        }
    }
}
