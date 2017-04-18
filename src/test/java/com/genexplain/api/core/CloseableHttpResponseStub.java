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
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.params.HttpParams;

public class CloseableHttpResponseStub implements CloseableHttpResponse {
    
    private HttpEntity entity;
    
    @Override
    public StatusLine getStatusLine() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setStatusLine(StatusLine statusline) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStatusCode(int code) throws IllegalStateException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {
        // TODO Auto-generated method stub

    }

    @Override
    public HttpEntity getEntity() {
        return entity;
    }

    @Override
    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    @Override
    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLocale(Locale loc) {
        // TODO Auto-generated method stub

    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean containsHeader(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Header[] getHeaders(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Header getFirstHeader(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Header getLastHeader(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Header[] getAllHeaders() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addHeader(Header header) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addHeader(String name, String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHeader(Header header) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHeader(String name, String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHeaders(Header[] headers) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeHeader(Header header) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeHeaders(String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public HeaderIterator headerIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HeaderIterator headerIterator(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpParams getParams() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setParams(HttpParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() throws IOException {  }

}
