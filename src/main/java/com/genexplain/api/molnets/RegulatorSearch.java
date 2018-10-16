/**
 * Copyright (C) 2018 geneXplain GmbH, Wolfenbuettel, Germany
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

package com.genexplain.api.molnets;

import java.io.FileInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.genexplain.api.app.ApplicationCommand;
import com.genexplain.api.app.Command;
import com.genexplain.api.core.GxJsonExecutor;
import com.genexplain.api.core.GxJsonExecutorParameters;
import com.genexplain.base.JsonConfigurable;

@Command(name="regulator-search",description="JSON and Java interface to carry out a regulator search")
public class RegulatorSearch implements ApplicationCommand, JsonConfigurable {
    
    public static final String REGULATOR_SEARCH_TOOL = "Regulator search";
    /**
     * Names of JSON properties that are used in the config
     * 
     * @author Philip Stegmaier
     *
     */
    public enum JsonProperty {
        SOURCE_PATH("sourcePath"),
        WEIGHT_COLUMN("weightColumn"),
        LIMIT_INPUT("isInputSizeLimited"),
        INPUT_SIZE_LIMIT("inputSizeLimit"),        
        MAX_RADIUS("maxRadius"),
        SCORE_CUTOFF("scoreCutoff"),
        BIOHUB("bioHub"),
        SPECIES("species"),
        WITH_FDR("calculatingFDR"),
        FDR_CUTOFF("FDRcutoff"),
        ZSCORE_CUTOFF("ZScoreCutoff"),
        PENALTY("penalty"),
        CONTEXTS("contextDecorators"),
        CONTEXT_TABLE("tableName"),
        CONTEXT_COLUMN("tableColumn"),
        CONTEXT_DECAY("decayFactor"),
        REMOVE_NODES("removeNodeDecorators"),
        REMOVE_NODES_TABLE("inputTable"),
        WITH_ISOFORMS("isoformFactor"),
        OUTPUT("outputTable"),
        WAIT("wait"),
        PROGRESS("progress");
        
        private String name;
        
        private JsonProperty(String name) {
            this.name = name;
        }
        
        public String get() { return name; }
    }
    
    public enum ValueType {
        STRING, BOOLEAN, DOUBLE, INT
    }
    
    private JsonObject config;
    private Logger     logger;
    private GxJsonExecutor executor;
    
    public RegulatorSearch() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    @Override
    public void run(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println(GxJsonExecutor.NO_INPUT_MESSAGE);
            return;
        }
        setConfig(new FileInputStream(args[0]));
        execute();
    }
    
    public GxJsonExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(GxJsonExecutor executor) {
        this.executor = executor;
    }

    public RegulatorSearch execute() throws Exception {
        return execute(config);
    }
    
    public RegulatorSearch execute(JsonObject config) throws Exception {
        JsonObject execParams = makeExecutable(config);
        if (config.getBoolean("justPrint", false)) {
            System.out.println(execParams.toString());
            return this;
        }
        if (executor == null)
            executor = new GxJsonExecutor();
        executor.run((GxJsonExecutorParameters)new GxJsonExecutorParameters().setConfig(execParams));
        return this;
    }
    
    public JsonObject makeExecutable(JsonObject config) throws Exception {
        JsonArray params = getSearchParameters(config);
        JsonObject execParams = new JsonObject();
        JsonObject regTask = new JsonObject().add("do", "analyze")
                .add("method", REGULATOR_SEARCH_TOOL)
                .add("workflow", false);
        regTask.add("parameters", params);
        if (config.get(JsonProperty.WAIT.get()) != null) {
            regTask.add(JsonProperty.WAIT.get(), config.get(JsonProperty.WAIT.get()).asBoolean());
        }
        if (config.get(JsonProperty.PROGRESS.get()) != null) {
            regTask.add(JsonProperty.PROGRESS.get(), config.get(JsonProperty.PROGRESS.get()).asBoolean());
        }
        addGlobalExecutorParams(config, execParams);
        execParams.add("tasks", new JsonArray().add(regTask));
        return execParams;
    }
    
    private void addGlobalExecutorParams(JsonObject config, JsonObject execParams) {
        if (config.get(GxJsonExecutorParameters.JsonProperty.USER.get()) != null) {
            execParams.add(GxJsonExecutorParameters.JsonProperty.USER.get(), 
                    config.get(GxJsonExecutorParameters.JsonProperty.USER.get()).asString());
        }
        if (config.get(GxJsonExecutorParameters.JsonProperty.PASSWORD.get()) != null) {
            execParams.add(GxJsonExecutorParameters.JsonProperty.PASSWORD.get(), 
                    config.get(GxJsonExecutorParameters.JsonProperty.PASSWORD.get()).asString());
        }
        if (config.get(GxJsonExecutorParameters.JsonProperty.SERVER.get()) != null) {
            execParams.add(GxJsonExecutorParameters.JsonProperty.SERVER.get(), 
                    config.get(GxJsonExecutorParameters.JsonProperty.SERVER.get()).asString());
        }
        if (config.get(GxJsonExecutorParameters.JsonProperty.VERBOSE.get()) != null) {
            execParams.add(GxJsonExecutorParameters.JsonProperty.VERBOSE.get(), 
                    config.get(GxJsonExecutorParameters.JsonProperty.VERBOSE.get()).asBoolean());
        }
        if (config.get(GxJsonExecutorParameters.JsonProperty.RECONNECT.get()) != null) {
            execParams.add(GxJsonExecutorParameters.JsonProperty.RECONNECT.get(), 
                    config.get(GxJsonExecutorParameters.JsonProperty.RECONNECT.get()).asBoolean());
        }
        if (config.get(GxJsonExecutorParameters.JsonProperty.CONNECTION.get()) != null) {
            execParams.add(GxJsonExecutorParameters.JsonProperty.CONNECTION.get(), 
                    config.get(GxJsonExecutorParameters.JsonProperty.CONNECTION.get()).asString());
        }
        if (config.get(GxJsonExecutorParameters.JsonProperty.HTTP_CLIENT.get()) != null) {
            execParams.add(GxJsonExecutorParameters.JsonProperty.HTTP_CLIENT.get(), 
                    config.get(GxJsonExecutorParameters.JsonProperty.HTTP_CLIENT.get()).asString());
        }
        if (config.get(GxJsonExecutorParameters.JsonProperty.REPLACE_STRINGS.get()) != null) {
            execParams.add(GxJsonExecutorParameters.JsonProperty.REPLACE_STRINGS.get(), 
                    config.get(GxJsonExecutorParameters.JsonProperty.REPLACE_STRINGS.get()).asArray());
        }
    }
    
    public JsonArray getSearchParameters(JsonObject config) throws Exception {
        JsonArray params = new JsonArray();
        setValue(JsonProperty.SOURCE_PATH, config, params, ValueType.STRING);
        setValue(JsonProperty.WEIGHT_COLUMN, config, params, ValueType.STRING);
        setValue(JsonProperty.LIMIT_INPUT, config, params, ValueType.BOOLEAN);
        setValue(JsonProperty.INPUT_SIZE_LIMIT, config, params, ValueType.INT);
        setValue(JsonProperty.MAX_RADIUS, config, params, ValueType.INT);
        setValue(JsonProperty.SCORE_CUTOFF, config, params, ValueType.DOUBLE);
        setValue(JsonProperty.BIOHUB, config, params, ValueType.STRING);
        setValue(JsonProperty.SPECIES, config, params, ValueType.STRING);
        setValue(JsonProperty.WITH_FDR, config, params, ValueType.BOOLEAN);
        setValue(JsonProperty.FDR_CUTOFF, config, params, ValueType.DOUBLE);
        setValue(JsonProperty.ZSCORE_CUTOFF, config, params, ValueType.DOUBLE);
        setValue(JsonProperty.PENALTY, config, params, ValueType.DOUBLE);
        setDecorators(config, params);
        setValue(JsonProperty.WITH_ISOFORMS, config, params, ValueType.BOOLEAN);
        setValue(JsonProperty.OUTPUT, config, params, ValueType.STRING);
        return params;
    }
    
    private void setDecorators(JsonObject config, JsonArray params) {
        JsonArray decorators = new JsonArray();
        if (config.get(JsonProperty.CONTEXTS.get()) != null) {
            JsonArray ctx = config.get(JsonProperty.CONTEXTS.get()).asArray();
            ctx.forEach(t -> {
                JsonArray ja = new JsonArray();
                ja.add(new JsonObject().add("name", "decoratorName").add("value", "Apply Context"));
                JsonArray pa = new JsonArray();
                JsonObject tx = t.asObject();
                if (tx.get(JsonProperty.CONTEXT_TABLE.get()) != null) {
                    pa.add(new JsonObject().add("name", JsonProperty.CONTEXT_TABLE.get())
                            .add("value", tx.get(JsonProperty.CONTEXT_TABLE.get()).asString()));
                } else {
                    throw new RuntimeException("Missing context decorator table, unspecified.");
                }
                if (tx.get(JsonProperty.CONTEXT_COLUMN.get()) != null) {
                    pa.add(new JsonObject().add("name", JsonProperty.CONTEXT_COLUMN.get())
                            .add("value", tx.get(JsonProperty.CONTEXT_COLUMN.get()).asString()));
                } else {
                    throw new RuntimeException("Missing context decorator column, unspecified.");
                }
                double dec = 0.1;
                if (tx.get(JsonProperty.CONTEXT_DECAY.get()) != null) {
                    dec = tx.get(JsonProperty.CONTEXT_DECAY.get()).asDouble();
                }
                pa.add(new JsonObject().add("name", JsonProperty.CONTEXT_DECAY.get())
                            .add("value", dec));
                ja.add(new JsonObject().add("name", "parameters").add("value", pa));
                decorators.add(ja);
            });
        }
        if (config.get(JsonProperty.REMOVE_NODES.get()) != null) {
            JsonArray rns = config.get(JsonProperty.REMOVE_NODES.get()).asArray();
            rns.forEach(t -> {
                JsonArray ja = new JsonArray();
                ja.add(new JsonObject().add("name", "decoratorName").add("value", "Remove nodes"));
                JsonArray pa = new JsonArray();
                JsonObject tx = t.asObject();
                if (tx.get(JsonProperty.REMOVE_NODES_TABLE.get()) != null) {
                    pa.add(new JsonObject().add("name", JsonProperty.REMOVE_NODES_TABLE.get())
                            .add("value", tx.get(JsonProperty.REMOVE_NODES_TABLE.get()).asString()));
                } else {
                    throw new RuntimeException("Missing table with nodes to remove, unspecified.");
                }
                ja.add(new JsonObject().add("name", "parameters").add("value", pa));
                decorators.add(ja);
            });
        }
        params.add(new JsonObject().add("name","decorators").add("value", decorators));
    }
    
    private void setValue(JsonProperty prop, JsonObject config, JsonArray params, ValueType vtype) {
        if (config.get(prop.get()) != null) {
            switch (vtype) {
            case STRING:
                params.add(new JsonObject().add("name",prop.get()).add("value", config.getString(prop.get(), "")));
                break;
            case BOOLEAN:
                params.add(new JsonObject().add("name",prop.get()).add("value", config.getBoolean(prop.get(), false)));
                break;
            case DOUBLE:
                params.add(new JsonObject().add("name",prop.get()).add("value", config.get(prop.get()).asDouble()));
                break;
            case INT:
                params.add(new JsonObject().add("name",prop.get()).add("value", config.get(prop.get()).asInt()));
                break;
            default:
                break;
            }
        }
    }
    
    /**
     * Returns the current configuration object.
     */
    public JsonObject getConfig() { return config; }
    
    /**
     * Sets the configuration object.
     * 
     * @see com.genexplain.base.JsonConfigurable#setConfig(JsonObject)
     */
    @Override
    public JsonConfigurable setConfig(JsonObject config) throws Exception {
        this.config = config;
        return this;
    }
}
