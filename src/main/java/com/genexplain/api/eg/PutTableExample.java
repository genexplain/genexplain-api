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
package com.genexplain.api.eg;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.genexplain.api.core.GxColumnDef;

/**
 * @author pst
 */
@GxAPIExample(name="putTable", description="Stores a table under the specified path")
public class PutTableExample extends AbstractAPIExample {

    public PutTableExample() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    /* (non-Javadoc)
     * @see com.genexplain.api.eg.AbstractAPIExample#run()
     */
    @Override
    public void run() throws Exception {
        connect();
        
        JsonArray data = new JsonArray();
        data.add(new JsonArray().add("TP53").add("ETS1").add("IRF1"))
        // Data values need to be provided as text even if the column type is numeric
            .add(new JsonArray().add("1").add("2").add("3"));
        logger.info(data.toString());
        
        List<GxColumnDef> columns = new ArrayList<>();
        columns.add(new GxColumnDef("ID", GxColumnDef.ColumnType.Text));
        columns.add(new GxColumnDef("Data", GxColumnDef.ColumnType.Integer));
        
        JsonObject result = client.putTable("data/Projects/Demo project/Data/a_test_table", data, columns);
        logger.info(result.toString());
    }

    @Override
    void run(String[] args) throws Exception {
        run();
    }
}
