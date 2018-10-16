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

public class BindingSiteAnalysisSelectorTest {
    private GxHttpClientStub     client;
    private GxHttpConnectionStub connection;
    private GxJsonExecutor       executor;
    private JsonObject           conf;
    
    private JsonObject both = new JsonObject().add("do","setParameters")
            .add("before", new JsonArray().add(new JsonArray().add("$DECIDE_BOTH$").add("executed")));
    private JsonObject up = new JsonObject().add("do","setParameters")
            .add("before", new JsonArray().add(new JsonArray().add("$DECIDE_UP$").add("executed")));
    private JsonObject down = new JsonObject().add("do","setParameters")
            .add("before", new JsonArray().add(new JsonArray().add("$DECIDE_DOWN$").add("executed")));
    private JsonObject none = new JsonObject().add("do","setParameters")
            .add("before", new JsonArray().add(new JsonArray().add("$DECIDE_NONE$").add("executed")));
    
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
        client.clearTestTables();
        client.setTestTable("up_table", new JsonArray().add(new JsonArray().add("A").add("B").add("C").add("D")));
        client.setTestTable("down_table", new JsonArray().add(new JsonArray().add("E").add("F").add("G").add("H")));
        client.setTestTable("nc_table", new JsonArray().add(new JsonArray().add("I").add("J").add("K").add("L")));
    }
    
    @Test
    public void canSelectAnalysis() throws Exception {
        conf = new JsonObject().add("do", "branch")
                .add("branchSelector", "com.genexplain.api.pipe.BindingSiteAnalysisSelector")
                .add("genesUp", "up_table")
                .add("genesDown", "down_table")
                .add("genesUnchanged", "nc_table")
                .add("minYesSize",3)
                .add("minNoSize",3)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_UP.get(), up)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_DOWN.get(), down)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_BOTH.get(), both)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_NONE.get(), none);
        GxJsonExecutorParameters params = executor.getParameters();
        params.setReplaceStrings(new JsonArray());
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "setTaskParameters");
        assertEquals(params.getReplaceStrings().get(0).asArray().get(0).asString(), "$DECIDE_BOTH$");
        assertEquals(params.getReplaceStrings().size(), 1);
    }
    
    @Test
    public void canDecideUp() throws Exception {
        conf = new JsonObject().add("do", "branch")
                .add("branchSelector", "com.genexplain.api.pipe.BindingSiteAnalysisSelector")
                .add("genesUp", "up_table")
                .add("genesDown", "down_table")
                .add("genesUnchanged", "nc_table")
                .add("minYesSize",3)
                .add("minNoSize",3)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_UP.get(), up)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_DOWN.get(), down)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_BOTH.get(), both)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_NONE.get(), none);
        client.setTestTable("down_table", new JsonArray().add(new JsonArray().add("E").add("F")));
        GxJsonExecutorParameters params = executor.getParameters();
        params.setReplaceStrings(new JsonArray());
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "setTaskParameters");
        assertEquals(params.getReplaceStrings().get(0).asArray().get(0).asString(), "$DECIDE_UP$");
        assertEquals(params.getReplaceStrings().size(), 1);
    }
    
    @Test
    public void canDecideDown() throws Exception {
        conf = new JsonObject().add("do", "branch")
                .add("branchSelector", "com.genexplain.api.pipe.BindingSiteAnalysisSelector")
                .add("genesUp", "up_table")
                .add("genesDown", "down_table")
                .add("genesUnchanged", "nc_table")
                .add("minYesSize",3)
                .add("minNoSize",3)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_UP.get(), up)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_DOWN.get(), down)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_BOTH.get(), both)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_NONE.get(), none);
        client.setTestTable("up_table", new JsonArray().add(new JsonArray().add("A").add("B")));
        GxJsonExecutorParameters params = executor.getParameters();
        params.setReplaceStrings(new JsonArray());
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "setTaskParameters");
        assertEquals(params.getReplaceStrings().get(0).asArray().get(0).asString(), "$DECIDE_DOWN$");
        assertEquals(params.getReplaceStrings().size(), 1);
    }
    
    @Test
    public void canDecideNoneNC() throws Exception {
        conf = new JsonObject().add("do", "branch")
                .add("branchSelector", "com.genexplain.api.pipe.BindingSiteAnalysisSelector")
                .add("genesUp", "up_table")
                .add("genesDown", "down_table")
                .add("genesUnchanged", "nc_table")
                .add("minYesSize",3)
                .add("minNoSize",3)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_UP.get(), up)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_DOWN.get(), down)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_BOTH.get(), both)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_NONE.get(), none);
        client.setTestTable("nc_table", new JsonArray().add(new JsonArray().add("I").add("J")));
        GxJsonExecutorParameters params = executor.getParameters();
        params.setReplaceStrings(new JsonArray());
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "setTaskParameters");
        assertEquals(params.getReplaceStrings().get(0).asArray().get(0).asString(), "$DECIDE_NONE$");
        assertEquals(params.getReplaceStrings().size(), 1);
    }
    
    @Test
    public void canDecideNoneYes() throws Exception {
        conf = new JsonObject().add("do", "branch")
                .add("branchSelector", "com.genexplain.api.pipe.BindingSiteAnalysisSelector")
                .add("genesUp", "up_table")
                .add("genesDown", "down_table")
                .add("genesUnchanged", "nc_table")
                .add("minYesSize",3)
                .add("minNoSize",3)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_UP.get(), up)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_DOWN.get(), down)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_BOTH.get(), both)
                .add(BindingSiteAnalysisSelector.JsonProperty.DECIDE_NONE.get(), none);
        client.setTestTable("up_table", new JsonArray().add(new JsonArray().add("A").add("B")));
        client.setTestTable("down_table", new JsonArray().add(new JsonArray().add("E").add("F")));
        GxJsonExecutorParameters params = executor.getParameters();
        params.setReplaceStrings(new JsonArray());
        JsonObject lastJson = executor.execute(conf).getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "setTaskParameters");
        assertEquals(params.getReplaceStrings().get(0).asArray().get(0).asString(), "$DECIDE_NONE$");
        assertEquals(params.getReplaceStrings().size(), 1);
    }
}
