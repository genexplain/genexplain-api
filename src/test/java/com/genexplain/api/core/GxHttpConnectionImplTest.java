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

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.eclipsesource.json.JsonObject;
import com.genexplain.api.core.GxHttpConfigurationException;
import com.genexplain.api.core.GxHttpConnection;
import com.genexplain.api.core.GxHttpConnectionImpl;
import com.genexplain.api.core.GxHttpException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;




public class GxHttpConnectionImplTest {
    
    private final int    mockPort = 4715;
    private final String server   = "http://localhost:" + mockPort;
    private final String basePath = "/biouml";
    private final String user     = "tester";
    private final String password = "testing";
    
    private GxHttpConnection con;
    
    @Rule
    public WireMockRule mockRule = new WireMockRule(WireMockConfiguration.options().port(mockPort)); //may be included for debugging: .notifier(new Slf4jNotifier(true)));
    
    @Rule
	public final ExpectedException exception = ExpectedException.none();
	
	@Before
	public void makeConnection() throws Exception {
	    con = new GxHttpConnectionImpl()
	            .setLoginParameters(server, user, password)
	            .setVerbose(true)
	            .setBasePath(basePath);
	}
    
	@Test
	public void queryJsonExceptionForMissingType() throws Exception {
        String testUrl  = basePath + "/missing-type";
        
        stubFor(post(urlPathEqualTo(testUrl)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"no-type-field\":true}")));
        
        exception.expect(GxHttpException.class);
        con.queryJSON(testUrl, new HashMap<>());
        
        verify(postRequestedFor(urlPathEqualTo(testUrl)));
	}
	
	@Test
    public void queryJsonConvertsOkTypeToZero() throws Exception {
        String testUrl  = basePath + "/convert-ok";
        
        stubFor(post(urlPathEqualTo(testUrl)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"type\":\"ok\"}")));
        
        JsonObject js = con.queryJSON(testUrl, new HashMap<>());
        
        assertEquals(js.getInt("type",-1),0);
    }
	
	@Test
    public void queryJsonConvertsErrorTypeToOne() throws Exception {
        String testUrl  = basePath + "/convert-error";
        
        stubFor(post(urlPathEqualTo(testUrl)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"type\":\"error\"}")));
        
        JsonObject js = con.queryJSON(testUrl, new HashMap<>());
        
        assertEquals(js.getInt("type",-1),1);
    }
	
	@Test
    public void queryJsonExceptionForUnknownTypeString() throws Exception {
        String testUrl  = basePath + "/unknown-type";
        
        stubFor(post(urlPathEqualTo(testUrl)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"type\":\"unknown\"}")));
        
        exception.expect(GxHttpException.class);
        con.queryJSON(testUrl, new HashMap<>());
    }
	
	@Test
    public void queryJsonExceptionForUnknownTypeType() throws Exception {
        String testUrl  = basePath + "/unknown-type-type";
        
        stubFor(post(urlPathEqualTo(testUrl)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"type\": {\"type\": false}}")));
        
        exception.expect(GxHttpException.class);
        con.queryJSON(testUrl, new HashMap<>());
    }
	
	@Test
    public void queryJsonTriesReconnect() throws Exception {
        String testUrl  = basePath + "/reconnect";
        String loginUrl = basePath + GxHttpConnection.Path.LOGIN.getPath();
        
        con.setReconnect(true);
        
        stubFor(post(urlPathEqualTo(testUrl)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"type\": 3}")));
        stubFor(post(urlPathEqualTo(loginUrl)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"type\": 0}"))
                );
        
        Map<String,String> params = new HashMap<>();
        params.put("one","A");
        params.put("two","B");
        con.queryJSON(testUrl, params);
        
        verify(1,postRequestedFor(urlPathEqualTo(loginUrl)));
        verify(2,postRequestedFor(urlPathEqualTo(testUrl)));
    }
	
	@Test
	public void canLogin() throws Exception {
	    String testUrl  = basePath + GxHttpConnection.Path.LOGIN.getPath();
	    
	    stubFor(post(urlPathEqualTo(testUrl)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"type\":0}")));
	    
	    con.login();
	    
	    verify(postRequestedFor(urlPathEqualTo(testUrl))
	            .withRequestBody(equalTo("username=" + user + "&password=" + password)));
	}
	
	@Test
    public void throwsExceptionForEmptyResponse() throws Exception {
        String testUrl  = basePath + GxHttpConnection.Path.LOGIN.getPath();
        
        stubFor(post(urlPathEqualTo(testUrl)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("")));
        
        exception.expect(GxHttpException.class);
        con.login();
    }
	
	@Test
    public void throwsExceptionForEmptyJsonResponse() throws Exception {
        String testUrl  = basePath + GxHttpConnection.Path.LOGIN.getPath();
        
        stubFor(post(urlPathEqualTo(testUrl)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")));
        
        exception.expect(GxHttpException.class);
        con.login();
    }
	
	@Test
    public void throwsExceptionForMissingTypeField() throws Exception {
        String testUrl  = basePath + GxHttpConnection.Path.LOGIN.getPath();
        
        stubFor(post(urlPathEqualTo(testUrl)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":0}")));
        
        exception.expect(GxHttpException.class);
        con.login();
    }
	
	@Test
    public void throwsExceptionForNonzeroType() throws Exception {
        String testUrl  = basePath + GxHttpConnection.Path.LOGIN.getPath();
        
        stubFor(post(urlPathEqualTo(testUrl)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"type\":1}")));
        
        exception.expect(GxHttpException.class);
        con.login();
    }
	
	@Test
    public void canLogout() throws Exception {
        String testUrl  = basePath + GxHttpConnection.Path.LOGOUT.getPath();
        
        stubFor(post(urlPathEqualTo(testUrl)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"type\":0}")));
        
        con.logout();
        
        verify(postRequestedFor(urlPathEqualTo(testUrl)));
    }
	
	@Test
    public void canPing() throws Exception {
        String testUrl  = basePath + GxHttpConnection.Path.PING.getPath();
        
        stubFor(post(urlPathEqualTo(testUrl)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"type\":0}")));
        
        con.ping();
        
        verify(postRequestedFor(urlPathEqualTo(testUrl)));
    }
	
	@Test
    public void loginRequiresValidPath() throws Exception {
	    GxHttpConnection con = new GxHttpConnectionImpl();
	    exception.expect(GxHttpConfigurationException.class);
	    con.login();
    }
	
	@Test
	public void canSetUsername() throws Exception {
		GxHttpConnection con = new GxHttpConnectionImpl();
		String testName = "test";
		con.setUsername(testName);
		assertEquals(con.getUsername(), testName);
	}
	
	@Test
	public void usernameCannotBeNullOrEmpty() throws Exception {
		GxHttpConnection con = new GxHttpConnectionImpl();
		exception.expect(IllegalArgumentException.class);
		con.setUsername("");
		
        exception.expect(IllegalArgumentException.class);
        con.setUsername(null);
	}
	
	@Test
    public void canSetPassword() throws Exception {
        GxHttpConnection con = new GxHttpConnectionImpl();
        String pwd = "test";
        con.setPassword(pwd);
        assertEquals(con.getPassword(), pwd);
    }
	
	@Test
    public void nullPasswordIsEmptyString() throws Exception {
        GxHttpConnection con = new GxHttpConnectionImpl();
        con.setPassword(null);
        assertEquals(con.getPassword(), "");
    }
	
	@Test
    public void serverCannotBeNullOrEmpty() throws Exception {
        GxHttpConnection con = new GxHttpConnectionImpl();
        exception.expect(IllegalArgumentException.class);
        con.setServer(null);
        
        exception.expect(IllegalArgumentException.class);
        con.setServer("");
    }
	
	@Test
    public void serverMustBeValidURL() throws Exception {
        GxHttpConnection con = new GxHttpConnectionImpl();
        
        exception.expect(IllegalArgumentException.class);
        con.setServer("test.genexplain.com");
    }
	
	@Test
    public void canSetServer() throws Exception {
        GxHttpConnection con = new GxHttpConnectionImpl();
        String server = "http://test.genexplain.com";
        con.setServer(server);
        assertEquals(con.getServer(), server);
    }
	
	
	@Test
	public void canConstructGxHttpConnectionImpl() {
		GxHttpConnection con = new GxHttpConnectionImpl();
		assertEquals(con.getClass().getName(), GxHttpConnectionImpl.class.getName());
	}
	
	@Test
	public void canSetBasePath() throws Exception {
		GxHttpConnection con = new GxHttpConnectionImpl();
		String path = "/biouml";
		con.setBasePath(path);
		assertEquals(con.getBasePath(),path);
	}
	
	@Test
	public void canGetLoggedState() {
	    GxHttpConnection con = new GxHttpConnectionImpl();
	    assertEquals(con.hasLoggedIn(),false);
	}
	
	@Test
	public void basePathGetsSlashPrefix() throws Exception {
		GxHttpConnection con = new GxHttpConnectionImpl();
		String path = "biouml";
		con.setBasePath(path);
		assertEquals(con.getBasePath(),"/"+path);
	}
	
	@Test
	public void basePathSlashSuffixesAreRemoved() throws Exception {
		GxHttpConnection con = new GxHttpConnectionImpl();
		String path = "/biouml//";
		String cor  = "/biouml";
		con.setBasePath(path);
		assertEquals(con.getBasePath(),cor);
	}
	
	@Test
	public void emptyOrNullBasepathIsValid() throws Exception {
		GxHttpConnection con = new GxHttpConnectionImpl();
		con.setBasePath("");
		con.setBasePath(null);
		assertEquals(con.getBasePath(), "");
	}
	
	@Test
	public void setBasePathDetectsMalformedPath() throws Exception {
		GxHttpConnection con = new GxHttpConnectionImpl();
		exception.expect(IllegalArgumentException.class);
		con.setBasePath("/%%(&%)  %yeah_-[//");
		
	}
	
	@Test 
	public void canSetReconnect() {
	    GxHttpConnection con = new GxHttpConnectionImpl();
	    if (con.getReconnect()) {
	        con.setReconnect(false);
	        assertEquals(con.getReconnect(), false);
	    } else {
	        con.setReconnect(true);
            assertEquals(con.getReconnect(), true);
	    }
	}
	
	@Test 
    public void canSetVerbose() {
        GxHttpConnection con = new GxHttpConnectionImpl();
        if (con.isVerbose()) {
            con.setVerbose(false);
            assertEquals(con.isVerbose(), false);
        } else {
            con.setVerbose(true);
            assertEquals(con.isVerbose(), true);
        }
    }
}
