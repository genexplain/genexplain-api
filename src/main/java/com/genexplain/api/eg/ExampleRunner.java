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

import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genexplain.api.app.ApplicationCommand;
import com.genexplain.api.app.Command;
import com.genexplain.util.GxUtil;


/**
 * @author pst
 *
 */
@Command(name="example", description="Runs examples")
public class ExampleRunner implements ApplicationCommand {
    
    private Logger logger;
    
    private Predicate<String> filter = cls -> {
        try {
            Class<?> cl = Class.forName(cls);
            if (cl.getAnnotation(GxAPIExample.class) == null || (cl.getSuperclass() != AbstractAPIExample.class))
                return false;
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };
    
    public ExampleRunner() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    /* (non-Javadoc)
     * @see com.genexplain.api.app.ApplicationCommand#run(java.lang.String[])
     */
    @Override
    public void run(String[] args) throws Exception {
        if (args.length < 1)
            args = new String[] {"list"};
        if (args[0].equals("list")) {
            listExamples();
        } else {
            executeExample(args[0]);
        }
    }
    
    private void listExamples() {
        List<String> examples = GxUtil.scanPackage(new String[] {this.getClass().getPackage().getName()}, filter);
        
        examples.forEach(cls -> {
            try {
                Class<?> cl = Class.forName(cls);
                GxAPIExample annotation = cl.getAnnotation(GxAPIExample.class);
                logger.info(annotation.name() + " - " + annotation.description() + 
                        " (" + cl.getCanonicalName() + ")");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    private void executeExample(String name) throws Exception {
        List<String> examples = GxUtil.scanPackage(new String[] {this.getClass().getPackage().getName()}, filter);
        for (String cls : examples) {
            Class<?> cl = Class.forName(cls);
            GxAPIExample annotation = cl.getAnnotation(GxAPIExample.class);
            if (annotation.name().equals(name)) {
                Object obj = cl.newInstance();
                ((AbstractAPIExample) obj).run();
                return;
            }
        }
    }
}
