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

import java.io.FileOutputStream;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.eclipsesource.json.JsonObject;
import com.genexplain.util.GxUtil;

/**
 * Implements an executor to export data elements. An instance of this class is
 * reachable in {@link com.genexplain.api.core.GxJsonExecutor GxJsonExecutor} 
 * as {@link com.genexplain.api.core.GxJsonExecutor.ExecutorType#export export}
 * 
 * @see com.genexplain.api.core.GxHttpClient#export(String, String, java.io.OutputStream, Map)
 * 
 * @author pst
 *
 */
public class ExportExecutor extends AbstractGxExecutor {
    
    public ExportExecutor() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    /**
     * Exports a data element. This normally should not be executed by directly,
     * but via {@link com.genexplain.api.core.GxJsonExecutor the JSON executor}
     * 
     * <p>Parameters specified in the input object are:
     * <ul>
     * <li><b>file</b>: local file into which to export</li>
     * <li><b>path</b>: data element path to export</li>
     * <li><b>exporter</b>: exporter to apply</li>
     * <li><b>parameters</b>: JsonObject with parameters to be provided to exporter</li>
     * </ul>
     * </p>
     * <p>
     * The parameters object may be empty, depending on the exporter. All other parameters are required.
     * </p>
     */
    @Override
    public GxJsonExecutor apply(JsonObject conf) throws Exception {
        GxUtil.showMessage(executor.getParameters().isVerbose(), "Exporting file", logger, GxUtil.LogLevel.INFO);
        
        String file     = getString("file",conf,true,false);
        String path     = getString("path",conf,true,false);
        String exporter = getString("exporter",conf,true,false);
        
        Map<String,String> params = GxJsonExecutor.getJsonParameters(conf);
        
        executor.getParameters().getHttpClient().export(path, exporter, new FileOutputStream(file), params);
        
        return executor;
    }
}
