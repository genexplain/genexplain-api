package com.genexplain.base;

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import prophecy.common.JSONMinify;

public interface JsonConfigurable {
    /**
     * Sets configuration from an input stream. The input stream is extracted
     * to a string which is parsed by a JSON parser. The JSON is minified before
     * parsing. The minifier removes C/C++-style comments.
     * 
     * @param is
     *           Inputstream from which a JsonObject can be read
     *           
     * @return This interface object to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    default JsonConfigurable setConfig(InputStream is) throws Exception {
        StringWriter sw = new StringWriter();
        IOUtils.copy(is, sw, "UTF-8");
        return setConfig(JSONMinify.minify(sw.toString()));
    }
    
    /**
     * Sets configuration from a string which is parsed by a JSON parser.
     * The JSON is minified before parsing. The minifier removes C/C++-style comments.
     * 
     * @param config
     *           String that can be parsed into a JsonObject
     *           
     * @return This interface object to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    default JsonConfigurable setConfig(String config) throws Exception {
        return setConfig(Json.parse(config).asObject());
    }
    
    /**
     * Sets configuration object.
     * 
     * @param config
     *           JsonObject containing configuration parameters
     *           
     * @return This interface object to enable fluent calls
     * 
     * @throws Exception
     *           An exception may be thrown or caused by internal
     *           method calls
     */
    JsonConfigurable setConfig(JsonObject config) throws Exception;
}
