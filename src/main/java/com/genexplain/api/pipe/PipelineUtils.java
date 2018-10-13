package com.genexplain.api.pipe;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;
import com.genexplain.api.core.GxJsonExecutorParameters;

public class PipelineUtils {
    public static boolean hasEnoughGenes(String setPath, int minSize, GxJsonExecutorParameters params) throws Exception {
        if (setPath.isEmpty())
            return false;
        JsonValue val = params.getHttpClient().getTable(setPath);
        if (val.isObject() && val.asObject().get("data") != null) {
            JsonArray ar = val.asObject().get("data").asArray();
            if (ar.size() > 0 && ar.get(0).asArray().size() >= minSize) {
                return true;
            } else {
                return false;
            }
            
        } else {
            return false;
        }
    }
}
