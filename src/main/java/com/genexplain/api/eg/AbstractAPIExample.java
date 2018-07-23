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

import org.slf4j.Logger;

import com.genexplain.api.core.GxHttpClient;
import com.genexplain.api.core.GxHttpClientImpl;
import com.genexplain.api.core.GxHttpConnection;
import com.genexplain.api.core.GxHttpConnectionImpl;

/**
 * @author pst
 *
 */
public abstract class AbstractAPIExample {
    
    public final static String PUBLIC_SERVER = "https://platform.genexplain.com";
    
    protected Logger logger;
    
    protected GxHttpConnection connection;
    protected GxHttpClient     client;
    
    /**
     * This method is called by {@link com.genexplain.api.eg.ExampleRunner} to
     * execute the example code.
     * 
     * @throws Exception
     */
    abstract public void run() throws Exception;
    
    /**
     * Connections to the public server demo account.
     * 
     * @throws Exception
     */
    protected void connect() throws Exception {
        connection = new GxHttpConnectionImpl();
        connection.setServer(PUBLIC_SERVER);
        connection.setUsername("");
        connection.setPassword("");
        connection.setVerbose(true);
        connection.login();
         
        client = new GxHttpClientImpl();
        client.setConnection(connection);
        client.setVerbose(true);
    }
}
