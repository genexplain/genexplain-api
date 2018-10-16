package com.genexplain.api.molnets;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.genexplain.api.core.GxHttpClientStub;
import com.genexplain.api.core.GxHttpConnectionStub;
import com.genexplain.api.core.GxJsonExecutor;
import com.genexplain.api.core.GxJsonExecutorParameters;
import com.genexplain.api.molnets.RegulatorSearch.JsonProperty;
import com.genexplain.api.molnets.RegulatorSearch.ValueType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RegulatorSearchTest {
    private JsonObject conf;
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Before
    public void create() throws Exception {
        /*client     = new GxHttpClientStub();
        connection = new GxHttpConnectionStub();
        connection.login();
        client.setConnection(connection);
        GxJsonExecutorParameters params = new GxJsonExecutorParameters();
        params.setHttpConnection(connection);
        params.setHttpClient(client);
        executor   = new GxJsonExecutor();
        executor.setParameters(params);
        client.clearTestTables();*/
        conf = new JsonObject();
        
        conf.add(GxJsonExecutorParameters.JsonProperty.USER.get(), "test_user");
        conf.add(GxJsonExecutorParameters.JsonProperty.PASSWORD.get(), "test_password");
        conf.add(GxJsonExecutorParameters.JsonProperty.SERVER.get(), "test_server");
        conf.add(GxJsonExecutorParameters.JsonProperty.VERBOSE.get(), true);
        conf.add(GxJsonExecutorParameters.JsonProperty.RECONNECT.get(), true);
        conf.add(GxJsonExecutorParameters.JsonProperty.CONNECTION.get(), "com.genexplain.api.core.GxHttpConnectionStub");
        conf.add(GxJsonExecutorParameters.JsonProperty.HTTP_CLIENT.get(), "com.genexplain.api.core.GxHttpClientStub");
        conf.add(GxJsonExecutorParameters.JsonProperty.REPLACE_STRINGS.get(), 
                    new JsonArray().add(new JsonArray().add("$REG_SEARCH$").add("replacement")));
        conf.add(RegulatorSearch.JsonProperty.WAIT.get(), true);
        conf.add(RegulatorSearch.JsonProperty.PROGRESS.get(), true);
        
        conf.add(RegulatorSearch.JsonProperty.SOURCE_PATH.get(), "test_source_path");
        conf.add(RegulatorSearch.JsonProperty.WEIGHT_COLUMN.get(), "test_weight_column");
        conf.add(RegulatorSearch.JsonProperty.LIMIT_INPUT.get(), false);
        conf.add(RegulatorSearch.JsonProperty.INPUT_SIZE_LIMIT.get(), 1000000);
        conf.add(RegulatorSearch.JsonProperty.MAX_RADIUS.get(), 10);
        conf.add(RegulatorSearch.JsonProperty.SCORE_CUTOFF.get(), 0.0);
        conf.add(RegulatorSearch.JsonProperty.BIOHUB.get(), "test_biohub");
        conf.add(RegulatorSearch.JsonProperty.SPECIES.get(), "test_species");
        conf.add(RegulatorSearch.JsonProperty.WITH_FDR.get(), true);
        conf.add(RegulatorSearch.JsonProperty.FDR_CUTOFF.get(), 0.05);
        conf.add(RegulatorSearch.JsonProperty.ZSCORE_CUTOFF.get(), 1.0);
        conf.add(RegulatorSearch.JsonProperty.PENALTY.get(), 0.5);
        conf.add(RegulatorSearch.JsonProperty.CONTEXTS.get(), new JsonArray()
                .add(new JsonObject()
                        .add(RegulatorSearch.JsonProperty.CONTEXT_TABLE.get(), "test_context_table")
                        .add(RegulatorSearch.JsonProperty.CONTEXT_COLUMN.get(), "test_context_column")
                        .add(RegulatorSearch.JsonProperty.CONTEXT_DECAY.get(), 1.0)
                     )
            );
        conf.add(RegulatorSearch.JsonProperty.REMOVE_NODES.get(), new JsonArray()
                .add(new JsonObject()
                        .add(RegulatorSearch.JsonProperty.REMOVE_NODES_TABLE.get(), "test_remove_nodes_table")
                     )
            );
        conf.add(RegulatorSearch.JsonProperty.WITH_ISOFORMS.get(), true);
        conf.add(RegulatorSearch.JsonProperty.OUTPUT.get(), "test_output");
    }
    
    @Test
    public void canSetConfigAndJustPrint() throws Exception {
        RegulatorSearch rs = new RegulatorSearch();
        conf.add("justPrint", true);
        rs.setConfig(conf);
        GxJsonExecutor executor = new GxJsonExecutor();
        rs.setExecutor(executor);
        rs.execute();
        JsonObject lastJson = executor.getLastJsonObject();
        assertNull(lastJson);
    }
    
    @Test
    public void canSetConfigAndExecute() throws Exception {
        RegulatorSearch rs = new RegulatorSearch();
        rs.setConfig(conf);
        GxJsonExecutor executor = new GxJsonExecutor();
        rs.setExecutor(executor);
        rs.execute();
        JsonObject lastJson = executor.getLastJsonObject();
        assertEquals(lastJson.getString("called", ""), "analyze");
        assertEquals(lastJson.getString("app", ""), RegulatorSearch.REGULATOR_SEARCH_TOOL);
    }
    
    @Test
    public void canSetConfig() throws Exception {
        RegulatorSearch rs = (RegulatorSearch)new RegulatorSearch().setConfig(new JsonObject()
                .add(RegulatorSearch.JsonProperty.SPECIES.get(), "test_species"));
        assertEquals(rs.getConfig().getString(RegulatorSearch.JsonProperty.SPECIES.get(), ""), "test_species");
    }
}
