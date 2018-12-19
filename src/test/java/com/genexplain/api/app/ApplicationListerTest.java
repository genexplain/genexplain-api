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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.eclipsesource.json.JsonObject;

/**
 * @author pst
 *
 */
public class ApplicationListerTest {
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    public void canSetParameterInputStream() throws Exception {
        JsonObject params = new JsonObject().add("server", "test-server")
                                    .add("user", "test-user")
                                    .add("password",  "test-password");
        ByteArrayInputStream is = new ByteArrayInputStream(params.toString().getBytes());
        ApplicationLister lister = new ApplicationLister();
        lister.setParameters(is);
        JsonObject lparams = lister.getParameters();
        assertEquals(params.getString("server","a"),lparams.getString("server","b"));
        assertEquals(params.getString("user","c"),lparams.getString("user","d"));
        assertEquals(params.getString("password","e"),lparams.getString("password","f"));
    }
    
    @Test
    public void canSetParameterString() throws Exception {
        JsonObject params = new JsonObject().add("server", "test-server")
                                    .add("user", "test-user")
                                    .add("password",  "test-password");
        ApplicationLister lister = new ApplicationLister();
        lister.setParameters(params.toString());
        JsonObject lparams = lister.getParameters();
        assertEquals(params.getString("server","a"),lparams.getString("server","b"));
        assertEquals(params.getString("user","c"),lparams.getString("user","d"));
        assertEquals(params.getString("password","e"),lparams.getString("password","f"));
    }
    
    @Test
    public void canLogIn() throws Exception {
        String client = Class.forName("com.genexplain.api.app.GxHttpClientStub").getName();
        String con    = Class.forName("com.genexplain.api.app.GxHttpConnectionStub").getName();
        JsonObject params = new JsonObject().add("server", "test-server")
                .add("user", "test-user")
                .add("password",  "test-password")
                .add("connection", con)
                .add("client", client);
        ApplicationLister lister = new ApplicationLister();
        lister.setParameters(params.toString());
        lister.login();
        assertEquals(client, lister.getClient().getClass().getName());
        assertEquals(con, lister.getConnection().getClass().getName());
        assertTrue(lister.getConnection().hasLoggedIn());
    }
    
    @Test
    public void canGetApplications() throws Exception {
        
        PrintStream           stdOut  = System.out;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(ostream));
        String out;
        try {
            String client = Class.forName("com.genexplain.api.app.GxHttpClientStub").getName();
            String con    = Class.forName("com.genexplain.api.app.GxHttpConnectionStub").getName();
            JsonObject params = new JsonObject().add("server", "test-server")
                    .add("user", "test-user")
                    .add("password",  "test-password")
                    .add("connection", con)
                    .add("client", client);
            ApplicationLister lister = new ApplicationLister();
            lister.setParameters(params.toString());
            lister.login();
            lister.getApplications();
            out = ostream.toString();
            assertTrue(((GxHttpClientStub)lister.getClient()).calledListApps());
            assertTrue(out.contains("app1"));
            assertTrue(out.contains("app2"));
            assertTrue(out.contains("app3"));
        } finally {
            System.setOut(stdOut);
        }
    }
    
    @Test
    public void canGetApplicationParameters() throws Exception {
        
        PrintStream           stdOut  = System.out;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(ostream));
        String out;
        try {
            String client = Class.forName("com.genexplain.api.app.GxHttpClientStub").getName();
            String con    = Class.forName("com.genexplain.api.app.GxHttpConnectionStub").getName();
            JsonObject params = new JsonObject().add("server", "test-server")
                    .add("user", "test-user")
                    .add("password",  "test-password")
                    .add("connection", con)
                    .add("withParameters", true)
                    .add("client", client);
            ApplicationLister lister = new ApplicationLister();
            lister.setParameters(params.toString());
            lister.login();
            lister.getApplications();
            out = ostream.toString();
            assertTrue(((GxHttpClientStub)lister.getClient()).calledListApps());
            Set<String> pcalls = ((GxHttpClientStub)lister.getClient()).gotAnalysisParams();
            assertEquals(pcalls.size(),3);
            assertTrue(pcalls.contains("app1"));
            assertTrue(pcalls.contains("app2"));
            assertTrue(pcalls.contains("app3"));
        } finally {
            System.setOut(stdOut);
        }
    }
    
    @Test
    public void exceptionForErrorGettingApps() throws Exception {
        String client = Class.forName("com.genexplain.api.app.GxHttpClientStub").getName();
        String con    = Class.forName("com.genexplain.api.app.GxHttpConnectionStub").getName();
        JsonObject params = new JsonObject().add("server", "test-server")
                .add("user", "test-user")
                .add("password",  "test-password")
                .add("connection", con)
                .add("client", client);
        ApplicationLister lister = new ApplicationLister();
        lister.setParameters(params.toString());
        lister.login();
        ((GxHttpClientStub)lister.getClient()).setListAppStatus(1);
        exception.expect(ApplicationListerException.class);
        lister.getApplications();
    }
    
    @Test
    public void exceptionForErrorGettingAppParams() throws Exception {
        PrintStream           stdErr  = System.err;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(ostream));
        String err;
        try {
            String client = Class.forName("com.genexplain.api.app.GxHttpClientStub").getName();
            String con    = Class.forName("com.genexplain.api.app.GxHttpConnectionStub").getName();
            JsonObject params = new JsonObject().add("server", "test-server")
                    .add("user", "test-user")
                    .add("password",  "test-password")
                    .add("connection", con)
                    .add("withParameters", true)
                    .add("client", client);
            ApplicationLister lister = new ApplicationLister();
            lister.setParameters(params.toString());
            lister.login();
            ((GxHttpClientStub)lister.getClient()).setGetAppParamsStatus(1);
            lister.getApplications();
            err = ostream.toString();
            assertTrue(err.contains("parameters") && err.contains("app1"));
            assertTrue(err.contains("parameters") && err.contains("app2"));
            assertTrue(err.contains("parameters") && err.contains("app3"));
        } finally {
            System.setErr(stdErr);
        }
    }
    
    @Test
    public void appParamsExceptionIsCaught() throws Exception {
        String client = Class.forName("com.genexplain.api.app.GxHttpClientStub").getName();
        String con    = Class.forName("com.genexplain.api.app.GxHttpConnectionStub").getName();
        JsonObject params = new JsonObject().add("server", "test-server")
                .add("user", "test-user")
                .add("password",  "test-password")
                .add("connection", con)
                .add("withParameters", true)
                .add("client", client);
        ApplicationLister lister = new ApplicationLister();
        lister.setParameters(params.toString());
        lister.login();
        ((GxHttpClientStub)lister.getClient()).setAppParamsExcept(true);
        exception.expect(ApplicationListerException.class);
        lister.getApplications();
    }
    
    /**
     * Integration test
     * 
     * @throws Exception
     */
    @Test
    public void canRunFromFile() throws Exception {
        PrintStream           stdOut  = System.out;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(ostream));
        String out;
        
        try {
            String client = Class.forName("com.genexplain.api.app.GxHttpClientStub").getName();
            String con    = Class.forName("com.genexplain.api.app.GxHttpConnectionStub").getName();
            String infile = "src/test/resources/test-config.json";
            JsonObject params = new JsonObject().add("server", "test-server")
                    .add("user", "test-user")
                    .add("password",  "test-password")
                    .add("connection", con)
                    .add("withParameters", true)
                    .add("client", client);
            FileWriter fw = new FileWriter(infile);
            fw.write(params.toString());
            fw.close();
            ApplicationLister lister = new ApplicationLister();
            lister.run(new String[]{infile});
            FileUtils.forceDelete(new File(infile));
            GxHttpClientStub cli = (GxHttpClientStub) lister.getClient();
            assertTrue(cli.calledListApps());
            assertEquals(cli.gotAnalysisParams().size(),3);
        } finally {
            System.setOut(stdOut);
        }
    }
    
    @Test
    public void canRunWithEmptyArgs() throws Exception {
        PrintStream           stdOut  = System.out;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(ostream));
        String out;
        
        try {
            ApplicationLister lister = new ApplicationLister();
            lister.run(new String[]{});
            out = ostream.toString();
            assertEquals(out, ApplicationLister.NO_ARGS_MESSAGE + "\n");
        } finally {
            System.setOut(stdOut);
        }
    }
}
