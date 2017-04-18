/**
 * Copyright (C) 2017 geneXplain GmbH, Wolfenbuettel, Germany
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

package com.genexplain.api.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.genexplain.util.GxUtil;

/**
 * Implements an executor to put a data table into the platform. An instance of this class is
 * reachable in {@link com.genexplain.api.core.GxJsonExecutor GxJsonExecutor} 
 * as {@link com.genexplain.api.core.GxJsonExecutor.ExecutorType#put put}.
 * 
 * @see com.genexplain.api.core.GxHttpClient#putTable(String, JsonArray, List)
 * 
 * @author pst
 *
 */
public class PutTableExecutor extends AbstractGxExecutor {
    
    public PutTableExecutor() { 
        logger = LoggerFactory.getLogger(this.getClass()); 
    }
    
    /**
     * Puts specified table into the platform. This normally should not be executed by directly,
     * but via {@link com.genexplain.api.core.GxJsonExecutor the JSON executor}
     * 
     * <p>Parameters specified in the input object are:
     * <ul>
     * <li><b>path</b>: the designated path of the table in the platform</li>
     * <li><b>columns</b>: definition of columns consisting of an array of two element-arrays containing
     *                     name and {@link GxColumnDef.ColumnType column type}
     * </li>
     * <li><b>table</b> or <b>file</b>: if table, then a JsonArray of column arrays is expected. Otherwise
     *                                  a data matrix can be parsed from the specified file.</li>
     * <li><b>skip</b>: used only for file upload, the number of rows to skip in the beginning of the file.
     *                  The default is 0.
     * <li><b>delimiter</b>: used only for file upload, the column delimiter. The default is '\t'.
     * </ul>
     */
    @Override
    public GxJsonExecutor apply(JsonObject conf) throws Exception {
        GxUtil.showMessage(executor.getParameters().isVerbose(), "Putting table", logger, GxUtil.LogLevel.INFO);
        
        String            path = getPath(conf);
        List<GxColumnDef> cols = getColumns(conf);
        JsonArray         data = getData(conf, cols);
        return executor.setLastJsonObject(executor.getParameters().getHttpClient().putTable(path, data, cols));
    }

    private JsonArray getData(JsonObject conf, List<GxColumnDef> cols) throws IllegalArgumentException, Exception {
        JsonArray data = null;
        if (conf.get("table") != null) {
            data = conf.get("table").asArray();
            if (data.isEmpty()) {
                throw new IllegalArgumentException("Specified data matrix is empty");
            } else if (data.size() != cols.size()) {
                throw new IllegalArgumentException("Unequal numbers of data matrix columns and specified columns");
            }
        } else if (conf.get("file") != null) {
            data = getFile(conf, cols.size());
        } else {
            throw new IllegalArgumentException("Missing data matrix (table or file");
        }
        return data;
    }
    
    private JsonArray getFile(JsonObject conf, int colNum) throws Exception {
        JsonArray data = new JsonArray();
        
        for (int c = 0;c < colNum; ++c)
            data.add(new JsonArray());
        
        String file = conf.get("file").asString();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String delim = conf.getString("delimiter", "\\t");
        int skipNum = conf.getInt("skip", 0);
        String line;
        String[] L;
        int ln = 0;
        while ((line = br.readLine()) != null) {
            ln++;
            if (ln <= skipNum)
                continue;
            L = line.split(delim);
            for (int c = 0;c < colNum;++c) {
                data.get(c).asArray().add(L[c]);
            }
        }
        return data;
    }
    
    private String getPath(JsonObject conf) throws Exception {
        if (conf.get("path") == null || conf.get("path").asString().isEmpty()) {
            throw new IllegalArgumentException("Mising target path to put table");
        }
        return conf.get("path").asString();
    }
    
    
    private List<GxColumnDef> getColumns(JsonObject conf) throws Exception {
        if (conf.get("columns") == null || conf.get("columns").asArray().isEmpty()) {
            throw new IllegalArgumentException("Missing column definition");
        }
        JsonArray         cols = conf.get("columns").asArray();
        List<GxColumnDef> defs = new ArrayList<>();
        cols.forEach(col -> {
            GxColumnDef def = new GxColumnDef();
            def.setName(col.asArray().get(0).asString());
            def.setType(GxColumnDef.ColumnType.valueOf(col.asArray().get(1).asString()));
            defs.add(def);
        });
        return defs;
    }
}
