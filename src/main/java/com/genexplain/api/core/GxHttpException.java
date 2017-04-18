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

/**
 * An exception that may be thrown by classes in the <i>core</i> package
 * like {@link com.genexplain.api.core.GxHttpConnectionImpl} or 
 * {@link com.genexplain.api.core.GxHttpClientImpl}
 * 
 * @author pst
 */
public class GxHttpException extends Exception {
    private static final long serialVersionUID = 1L;
    private String msg;
    
    public GxHttpException(String string) {
        msg = string;
    }
    
    @Override
    public String getMessage() {
        return msg;
    }
}
