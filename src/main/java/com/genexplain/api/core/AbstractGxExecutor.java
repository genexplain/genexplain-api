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

import org.slf4j.Logger;

import com.eclipsesource.json.JsonObject;

/**
 * Abstract class for JSON executors.
 * 
 * @author pst
 *
 */
public abstract class AbstractGxExecutor implements GxJsonExecutor.Executor<JsonObject, GxJsonExecutor> {
    
    protected Logger         logger;
    protected GxJsonExecutor executor;

    public AbstractGxExecutor setExecutor(GxJsonExecutor executor) {
        this.executor = executor;
        return this;
    }
    
    /**
     * Extracts a string parameter from the conf object.
     *  
     * @param name
     *           Name of the property to get
     *           
     * @param conf
     *           JsonObject that contains the parameter
     *           
     * @param required
     *           <code>true</code> if the parameter is expected to be present
     *           
     * @param canBeEmpty
     *           <code>true</code> if the parameter can be empty
     *           
     * @return The extracted parameter string
     * 
     * @throws IllegalArgumentException
     *           If property with <b>name</b> does not exist in <b>conf</b> and
     *           it is <b>required</b> or must not be empty, or if the empty value is
     *           invalid.
     */
    protected String getString(String name, JsonObject conf, boolean required, boolean canBeEmpty) throws IllegalArgumentException {
        String value = "";
        if (conf.get(name) == null) {
            if (required || !canBeEmpty) {
                throw new IllegalArgumentException("Missing or empty property " + name);
            }
        } else {
            value = conf.getString(name, "");
        }
        if (value.isEmpty() && !canBeEmpty) {
            throw new IllegalArgumentException("Property " + name + " must not be empty");
        }
        return value;
    }
}
