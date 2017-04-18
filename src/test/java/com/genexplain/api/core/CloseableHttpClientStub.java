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

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.eclipsesource.json.JsonObject;

public class CloseableHttpClientStub extends CloseableHttpClient {

    @Override
    public HttpParams getParams() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloseableHttpResponse execute(HttpUriRequest request) {
        CloseableHttpResponse response = new CloseableHttpResponseStub();
        HttpEntityStub entity = new HttpEntityStub();
        JsonObject js = new JsonObject();
        js.add("uri", request.getURI().toString());
        js.add("data", request.toString());
        entity.setContent(js);
        response.setEntity(entity);
        return response;
    }

    @Override
    public CloseableHttpResponse execute(HttpUriRequest request, HttpContext context)
            throws IOException, ClientProtocolException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloseableHttpResponse execute(HttpHost target, HttpRequest request)
            throws IOException, ClientProtocolException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloseableHttpResponse execute(HttpHost target, HttpRequest request,
            HttpContext context) throws IOException, ClientProtocolException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T execute(HttpUriRequest request,
            ResponseHandler<? extends T> responseHandler)
            throws IOException, ClientProtocolException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T execute(HttpUriRequest request,
            ResponseHandler<? extends T> responseHandler, HttpContext context)
            throws IOException, ClientProtocolException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request,
            ResponseHandler<? extends T> responseHandler)
            throws IOException, ClientProtocolException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request,
            ResponseHandler<? extends T> responseHandler, HttpContext context)
            throws IOException, ClientProtocolException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target,
            HttpRequest request, HttpContext context)
            throws IOException, ClientProtocolException {
        // TODO Auto-generated method stub
        return null;
    }

}
