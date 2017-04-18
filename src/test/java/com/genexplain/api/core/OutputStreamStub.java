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
import java.io.OutputStream;
import java.util.Arrays;

public class OutputStreamStub extends OutputStream {
    
    StringBuilder sb = new StringBuilder();
    
    @Override
    public void write(int arg0) throws IOException {
        sb.append((char)arg0);
    }
    
    @Override
    public void write(byte[] bytes) throws IOException {
        for (byte bt:bytes) { sb.append((char)bt); };
    }
    
    public String getData() { return sb.toString(); }
}
