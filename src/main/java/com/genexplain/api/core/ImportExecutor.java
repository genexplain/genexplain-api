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

import java.util.Map;

import org.slf4j.LoggerFactory;

import com.eclipsesource.json.JsonObject;
import com.genexplain.util.GxUtil;

/**
 * Implements an executor to import data into the platform. An instance of this class is
 * reachable in {@link com.genexplain.api.core.GxJsonExecutor GxJsonExecutor}
 * as {@link com.genexplain.api.core.GxJsonExecutor.ExecutorType#import import}.
 * 
 * @see com.genexplain.api.core.GxHttpClient#imPort(String, String, String, Map)
 * 
 * @author pst
 *
 */
public class ImportExecutor extends AbstractGxExecutor {
    
    public ImportExecutor() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    /**
     * Imports a data element. This normally should not be executed by directly,
     * but via {@link com.genexplain.api.core.GxJsonExecutor the JSON executor}
     * 
     * <p>Parameters specified in the input object are:
     * <ul>
     * <li><b>file</b>: local file to import</li>
     * <li><b>path</b>: the designated import location in the platform</li>
     * <li><b>importer</b>: importer to apply</li>
     * <li><b>parameters</b>: JsonObject with parameters to be provided to importer</li>
     * </ul>
     * </p>
     * <p>
     * The parameters object may be empty, depending on the importer. All other parameters are required.
     * </p>
     */
    @Override
    public GxJsonExecutor apply(JsonObject conf) throws Exception {
        GxUtil.showMessage(executor.getParameters().isVerbose(), "Importing file", logger, GxUtil.LogLevel.INFO);
        
        String file     = getString("file",conf,true,false);
        String path     = getString("path",conf,true,false);
        String importer = getString("importer",conf,true,false);
        
        Map<String,String> params = GxJsonExecutor.getJsonParameters(conf);
        
        return executor.setLastJsonObject(executor.getParameters().getHttpClient().imPort(file, path, importer, params));
    }
}
