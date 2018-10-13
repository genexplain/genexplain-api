package com.genexplain.api.pipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.genexplain.api.core.GxJsonExecutor.BranchSelector;
import com.genexplain.api.pipe.BindingSiteAnalysisSelector.JsonProperty;
import com.genexplain.api.core.GxJsonExecutorParameters;

public class TaskGivenTableSize implements BranchSelector {
    
    /**
     * Names of JSON properties that can specify parameters
     * to the selector
     * 
     * @author pst
     *
     */
    public enum JsonProperty {
        TEST_TABLE("testTable"),
        MIN_SIZE("minSize"),
        DECIDE_YES("decideYes"),
        DECIDE_NO("decideNo");
        
        private String name;
        
        private JsonProperty(String name) {
            this.name = name;
        }
        
        public String get() { return name; }
    }
    
    private Logger logger;
    
    public TaskGivenTableSize() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    @Override
    public JsonValue apply(JsonObject task, GxJsonExecutorParameters params) throws Exception {
       if (PipelineUtils.hasEnoughGenes(task.getString(JsonProperty.TEST_TABLE.get(), ""),
               task.getInt(JsonProperty.MIN_SIZE.get(), 0), params)) {
           return task.get(JsonProperty.DECIDE_YES.get());
       } else {
           return task.get(JsonProperty.DECIDE_NO.get());
       }
    }
    
}
