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

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

import com.eclipsesource.json.JsonObject;
import com.genexplain.api.core.GxHttpConnection;

public class GxHttpConnectionStub implements GxHttpConnection {

    private boolean hasLoggedIn = false;
    
    private final String basePath = "/biouml";
    private final String server   = "http://local.testing";
    
    private boolean sendTerminatedByError = false;
    
    public void setSendTerminatedByError(boolean t) {
        sendTerminatedByError = t;
    }
    
    private CloseableHttpClient client;
    
    @Override
    public GxHttpConnection setVerbose(boolean verbose) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isVerbose() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasLoggedIn() {
        return hasLoggedIn;
    }

    @Override
    public GxHttpConnection setReconnect(boolean rec) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getReconnect() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    @Override
    public GxHttpConnection setBasePath(String basePath) throws Exception {
        return this;
    }

    @Override
    public String getUsername() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GxHttpConnection setUsername(String username) throws Exception {
        return this;
    }

    @Override
    public String getPassword() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GxHttpConnection setPassword(String password) throws Exception {
        return this;
    }

    @Override
    public String getServer() {
        return server;
    }

    @Override
    public GxHttpConnection setServer(String server) throws Exception {
        return this;
    }

    @Override
    public CloseableHttpResponse queryBioUML(String path, Map<String, String> params) throws Exception {
        CloseableHttpResponseStub response = new CloseableHttpResponseStub();
        JsonObject js = queryJSON(path, params);
        HttpEntityStub entity = new HttpEntityStub();
        entity.setContent(js);
        response.setEntity(entity);
        return response;
    }

    @Override
    public JsonObject queryJSON(String path, Map<String, String> params) throws Exception {
        JsonObject js = new JsonObject();
        js.add("path", path);
        params.forEach((key,val) -> { js.add(key, val); });
        if (sendTerminatedByError) {
            js.add("type", 1);
            js.add("status", GxHttpClient.JobStatus.TERMINATED_BY_ERROR.getValue()-1);
        } else {
            js.add("type", 0);
        }
        return js;
    }

    @Override
    public JsonObject ping() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonObject login() throws Exception {
        hasLoggedIn = true;
        return new JsonObject();
    }

    @Override
    public CloseableHttpResponse logout() throws Exception {
        hasLoggedIn = false;
        return new CloseableHttpResponseStub();
    }

    /**
     * This method occurs only in the stub.
     * 
     * @param client
     */
    public void setHttpClient(CloseableHttpClient client) {
        this.client = client;
    }
    
    @Override
    public CloseableHttpClient getHttpClient() {
        return client;
    }

}
