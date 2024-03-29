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

import java.io.FileReader;
import java.io.StringWriter;

import org.slf4j.LoggerFactory;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.PrettyPrint;
import com.genexplain.api.core.GxHttpClientImpl;
import com.genexplain.api.core.GxHttpConnectionImpl;

/**
 * @author pst
 */
@GxAPIExample(name="getParameters", description="Fetches parameter descriptions for a specified analysis tool")
public class GetParametersExample extends AbstractAPIExample {
	
    public GetParametersExample() {
        logger = LoggerFactory.getLogger(this.getClass());
        config = null;
    }
    
    /* (non-Javadoc)
     * @see com.genexplain.api.eg.AbstractAPIExample#run()
     */
    @Override
    public void run() throws Exception {
    	if (config == null) {
            System.out.println("Please provide configuration parameters");
            return;
        }
        connect();
        
        JsonObject result = client.getAnalysisParameters("Search for enriched TFBSs (genes)");
        StringWriter writer = new StringWriter();
        result.get("parameters").writeTo(writer, PrettyPrint.indentWithSpaces(4));
        System.out.println(writer.toString());
        writer.close();
    }

    @Override
    void run(String[] args) throws Exception {
    	if (args.length > 0) {
            setConfig(new FileReader(args[0]));
        }
        run();
    }
}
