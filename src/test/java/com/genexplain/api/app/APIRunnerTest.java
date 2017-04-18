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

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.genexplain.api.app.test.TestApplicationCommand2;


/**
 * @author pst
 *
 */
public class APIRunnerTest {
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    public void printsHelpForNoArgs() throws Exception {
        PrintStream           stdOut  = System.out;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(ostream));
        
        try {
            APIRunner.main(new String[]{});
            String out = ostream.toString();
            assertTrue(out.length() > 0);
            assertTrue(out.contains(APIRunner.API_PACKAGE_PROPERTY));
        } finally {
            System.setOut(stdOut);
        }
    }
    
    @Test
    public void usesDefaultApiPackages() throws Exception {
        System.setProperty(APIRunner.API_PACKAGE_PROPERTY, "");
        APIRunner.main(new String[]{"test-command"});
        assertEquals(TestApplicationCommand.getInstances(),1);
        System.setProperty(APIRunner.API_PACKAGE_PROPERTY, "");
    }
    
    @Test
    public void canSpecifyApiPackages() throws Exception {
        System.setProperty(APIRunner.API_PACKAGE_PROPERTY, "com.genexplain.api.app.test");
        APIRunner.main(new String[]{"test-command-2"});
        assertEquals(TestApplicationCommand2.getInstances(),1);
        System.setProperty(APIRunner.API_PACKAGE_PROPERTY, "");
    }
    
    @Test
    public void exceptionForDuplicateCommands() throws Exception {
        try {
            System.setProperty(APIRunner.API_PACKAGE_PROPERTY, "com.genexplain.api:com.genexplain.fail");
            exception.expect(UnsupportedOperationException.class);
            APIRunner.main(new String[]{"test-command"});
        } finally {
            System.setProperty(APIRunner.API_PACKAGE_PROPERTY, "");
        }
    }
    
    
    @Test
    public void stdoutAndExceptionForUnknownCommand() throws Exception {
        System.setProperty(APIRunner.API_PACKAGE_PROPERTY, "");
        PrintStream           stdOut  = System.out;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(ostream));
        
        try {
            exception.expect(IllegalArgumentException.class);
            APIRunner.main(new String[]{"unknown-test-command"});
            String out = ostream.toString();
            assertTrue(out.length() > 0);
            assertTrue(out.contains(APIRunner.API_PACKAGE_PROPERTY));
        } finally {
            System.setOut(stdOut);
        }
    }
    
}
