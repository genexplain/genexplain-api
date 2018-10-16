package com.genexplain.api.pipe;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.genexplain.api.core.GxHttpClientStub;
import com.genexplain.api.core.GxHttpConnectionStub;
import com.genexplain.api.core.GxJsonExecutor;
import com.genexplain.api.core.GxJsonExecutorParameters;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class TaskGivenTableSizeTest {
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
    public void canExecuteGivenTableSize() throws Exception {
        client.setTestTable("test_table", new JsonArray().add(new JsonArray().add("A").add("B").add("C").add("D")));
        JsonObject yes = new JsonObject().add("do","setParameters")
                .add("before", new JsonArray().add(new JsonArray().add("$DECIDE_YES$").add("executed")));
        JsonObject no = new JsonObject().add("do","setParameters")
                .add("before", new JsonArray().add(new JsonArray().add("$DECIDE_NO$").add("executed")));
        
        conf = new JsonObject().add("do", "branch")
                .add("branchSelector", "com.genexplain.api.pipe.TaskGivenTableSize")
                .add("testTable", "test_table")
                .add("minSize",3)
                .add("decideYes", yes)
                .add("decideNo", no);
        GxJsonExecutorParameters params = executor.getParameters();
        params.setReplaceStrings(new JsonArray());
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "setTaskParameters");
        assertEquals(params.getReplaceStrings().get(0).asArray().get(0).asString(), "$DECIDE_YES$");
        assertEquals(params.getReplaceStrings().size(), 1);
    }
    
    @Test
    public void canDecideNo() throws Exception {
        client.setTestTable("test_table", new JsonArray().add(new JsonArray().add("A").add("B").add("C").add("D")));
        JsonObject yes = new JsonObject().add("do","setParameters")
                .add("before", new JsonArray().add(new JsonArray().add("$DECIDE_YES$").add("executed")));
        JsonObject no = new JsonObject().add("do","setParameters")
                .add("before", new JsonArray().add(new JsonArray().add("$DECIDE_NO$").add("executed")));
        
        conf = new JsonObject().add("do", "branch")
                .add("branchSelector", "com.genexplain.api.pipe.TaskGivenTableSize")
                .add("testTable", "test_table")
                .add("minSize",5)
                .add("decideYes", yes)
                .add("decideNo", no);
        GxJsonExecutorParameters params = executor.getParameters();
        params.setReplaceStrings(new JsonArray());
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        client.removeTestTable("test_table");
        assertEquals(lastJson.getString("called", ""), "setTaskParameters");
        assertEquals(params.getReplaceStrings().get(0).asArray().get(0).asString(), "$DECIDE_NO$");
        assertEquals(params.getReplaceStrings().size(), 1);
    }
}
