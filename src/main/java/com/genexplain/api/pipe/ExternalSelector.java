package com.genexplain.api.pipe;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.genexplain.api.core.GxJsonExecutor.BranchSelector;
import com.genexplain.api.pipe.BindingSiteAnalysisSelector.JsonProperty;
import com.genexplain.util.GxUtil;
import com.genexplain.api.core.GxJsonExecutorParameters;

public class ExternalSelector implements BranchSelector {
    
    /**
     * Names of JSON properties that can specify parameters
     * to the selector
     * 
     * @author pst
     *
     */
    public enum JsonProperty {
        TOOL("bin"),
        TOOL_PARAMS("params"),
        SHOW_OUTPUT("showOutput"),
        OUTPUT_KEY("outputKey"),
        DEFAULT_TASK("defaultTask");
        
        private String name;
        
        private JsonProperty(String name) {
            this.name = name;
        }
        
        public String get() { return name; }
    }
    
    private Logger logger;
    
    public ExternalSelector() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    @Override
    public JsonValue apply(JsonObject task, GxJsonExecutorParameters params) throws Exception {
        if (task.get(JsonProperty.TOOL.get()) == null) {
            throw new IllegalArgumentException("No binary specified");
        }
        List<String> args = new ArrayList<>();
        args.add(task.get(JsonProperty.TOOL.get()).asString());
        if (task.get("params") != null) {
            JsonValue jv = task.get("params");
            if (jv.isString()) {
                args.add(jv.asString());
            } else if (jv.isArray()) {
                jv.asArray().forEach(p -> {
                    args.add(p.asString());
                });
            }
        }
        ProcessBuilder builder = new ProcessBuilder();
        GxUtil.showMessage(params.isVerbose(), "Starting " + args, logger, GxUtil.LogLevel.INFO);
        final Process prc = builder.command(args).start();
        boolean showOutput = task.getBoolean(JsonProperty.SHOW_OUTPUT.get(), false);
        InputStream is = prc.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        String key = task.getString(JsonProperty.OUTPUT_KEY.get(),"NEXT_TASK");
        String nextTask = task.getString(JsonProperty.DEFAULT_TASK.get(), "");
        while ((line = br.readLine()) != null) {
            if (showOutput) {
                logger.info("External output: " + line);
            }
            if (line.startsWith(key)) {
                nextTask = line.substring((key + "=").length());
            }
        }
        br.close();
        is.close();
        if (prc.exitValue() != 0) {
            throw new RuntimeException("An error occurred when trying to execute: " + args);
        } else if (prc.isAlive()) {
            prc.destroy();
        }
        GxUtil.showMessage(params.isVerbose(), "Selected task: " + nextTask, logger, GxUtil.LogLevel.INFO);
        return task.get(nextTask);
    }
    
}
