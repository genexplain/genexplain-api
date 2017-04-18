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
package com.genexplain.api.app;

import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

import com.eclipsesource.json.JsonObject;
import com.genexplain.api.core.GxHttpConnection;

/**
 * @author pst
 *
 */
public class GxHttpConnectionStub implements GxHttpConnection {
    
    private String server;
    private String user;
    private String password;
    
    private boolean hasLoggedIn = false;
    
    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#setVerbose(boolean)
     */
    @Override
    public GxHttpConnection setVerbose(boolean verbose) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#isVerbose()
     */
    @Override
    public boolean isVerbose() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#hasLoggedIn()
     */
    @Override
    public boolean hasLoggedIn() {
       return hasLoggedIn;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#setReconnect(boolean)
     */
    @Override
    public GxHttpConnection setReconnect(boolean rec) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#getReconnect()
     */
    @Override
    public boolean getReconnect() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#getBasePath()
     */
    @Override
    public String getBasePath() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#setBasePath(java.lang.String)
     */
    @Override
    public GxHttpConnection setBasePath(String basePath) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#getUsername()
     */
    @Override
    public String getUsername() {
        return user;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#setUsername(java.lang.String)
     */
    @Override
    public GxHttpConnection setUsername(String username) throws Exception {
        user = username;
        return this;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#getPassword()
     */
    @Override
    public String getPassword() {
        return password;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#setPassword(java.lang.String)
     */
    @Override
    public GxHttpConnection setPassword(String password) throws Exception {
        this.password = password;
        return this;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#getServer()
     */
    @Override
    public String getServer() {
        return server;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#setServer(java.lang.String)
     */
    @Override
    public GxHttpConnection setServer(String server) throws Exception {
        this.server = server;
        return this;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#queryBioUML(java.lang.String, java.util.Map)
     */
    @Override
    public CloseableHttpResponse queryBioUML(String path,
            Map<String, String> params) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#queryJSON(java.lang.String, java.util.Map)
     */
    @Override
    public JsonObject queryJSON(String path, Map<String, String> params)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#getHttpClient()
     */
    @Override
    public CloseableHttpClient getHttpClient() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#ping()
     */
    @Override
    public JsonObject ping() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#login()
     */
    @Override
    public JsonObject login() throws Exception {
        hasLoggedIn = true;
        return new JsonObject().add("type", 0);
    }

    /* (non-Javadoc)
     * @see com.genexplain.api.core.GxHttpConnection#logout()
     */
    @Override
    public CloseableHttpResponse logout() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
