package com.genexplain.api.pipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.genexplain.api.core.GxJsonExecutor.BranchSelector;
import com.genexplain.util.GxUtil;
import com.genexplain.api.core.GxJsonExecutorParameters;

public class BindingSiteAnalysisSelector implements BranchSelector {
    
    /**
     * Names of JSON properties that can specify parameters
     * to the selector
     * 
     * @author pst
     *
     */
    public enum JsonProperty {
        GENES_UP("genesUp"),
        GENES_DOWN("genesDown"),
        GENES_NC("genesUnchanged"),
        MIN_YES_SIZE("minYesSize"),
        MIN_NO_SIZE("minNoSize"),
        DECIDE_BOTH("decideBoth"),
        DECIDE_UP("decideUpOnly"),
        DECIDE_DOWN("decideDownOnly"),
        DECIDE_NONE("decideNone");
        
        private String name;
        
        private JsonProperty(String name) {
            this.name = name;
        }
        
        public String get() { return name; }
    }

    public static final int DEFAULT_MIN_YES_SIZE = 10;
    public static final int DEFAULT_MIN_NO_SIZE  = 1000;
    
    private Logger logger;
    
    public BindingSiteAnalysisSelector() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    @Override
    public JsonValue apply(JsonObject task, GxJsonExecutorParameters params) throws Exception {
        boolean up = PipelineUtils.hasEnoughGenes(task.getString(JsonProperty.GENES_UP.get(), ""), 
                task.getInt(JsonProperty.MIN_YES_SIZE.get(), DEFAULT_MIN_YES_SIZE),
                params);
        boolean down = PipelineUtils.hasEnoughGenes(task.getString(JsonProperty.GENES_DOWN.get(), ""), 
                task.getInt(JsonProperty.MIN_YES_SIZE.get(), DEFAULT_MIN_YES_SIZE),
                params);
        boolean nc = PipelineUtils.hasEnoughGenes(task.getString(JsonProperty.GENES_NC.get(), ""), 
                task.getInt(JsonProperty.MIN_NO_SIZE.get(), DEFAULT_MIN_NO_SIZE),
                params);
        if (nc) {
            if (up) {
                if (down) {
                    GxUtil.showMessage(params.isVerbose(), "Selected " + JsonProperty.DECIDE_BOTH, logger, GxUtil.LogLevel.INFO);
                    return task.get(JsonProperty.DECIDE_BOTH.get());
                } else {
                    GxUtil.showMessage(params.isVerbose(), "Selected " + JsonProperty.DECIDE_UP, logger, GxUtil.LogLevel.INFO);
                    return task.get(JsonProperty.DECIDE_UP.get());
                }
            } else if (down) {
                GxUtil.showMessage(params.isVerbose(), "Selected " + JsonProperty.DECIDE_DOWN, logger, GxUtil.LogLevel.INFO);
                return task.get(JsonProperty.DECIDE_DOWN.get());
            } else {
                GxUtil.showMessage(params.isVerbose(), "Selected " + JsonProperty.DECIDE_NONE, logger, GxUtil.LogLevel.INFO);
                return task.get(JsonProperty.DECIDE_NONE.get());
            }
        } else {
            GxUtil.showMessage(params.isVerbose(), "Selected " + JsonProperty.DECIDE_NONE, logger, GxUtil.LogLevel.INFO);
            return task.get(JsonProperty.DECIDE_NONE.get());
        }
    }
}
