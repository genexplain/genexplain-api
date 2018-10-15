package com.genexplain.test;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.genexplain.api.core.GxJsonExecutor.BranchSelector;
import com.genexplain.api.core.GxJsonExecutorParameters;

public class TestBranchSelector implements BranchSelector {
    
    @Override
    public JsonValue apply(JsonObject task, GxJsonExecutorParameters params) throws Exception {
        if (!task.getString("nextTask", "").isEmpty()) {
            params.setNextTaskItem(task.getString("nextTask", ""));
        }
        return task.get("select");
    }
    
}
