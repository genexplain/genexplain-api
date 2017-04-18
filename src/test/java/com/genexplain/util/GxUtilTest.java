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

package com.genexplain.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;


public class GxUtilTest {
    
    String testMessage = "test message";
    Logger logger      = LoggerFactory.getLogger(this.getClass());
    
    @Before
    public void configureLogger() throws Exception {
        String config = "<configuration>\n" + 
                "    <appender name=\"STDOUT\" class=\"ch.qos.logback.core.ConsoleAppender\">\n" + 
                "        <encoder>\n" + 
                "            <pattern>%d{yyyy-dd-mm HH:mm:ss} [%thread] %-5level %logger{36} | %msg%n</pattern>\n" + 
                "        </encoder>\n" + 
                "    </appender>\n" + 
                "    <root level=\"trace\">\n" + 
                "        <appender-ref ref=\"STDOUT\" />\n" + 
                "    </root>\n" + 
                "</configuration>\n";
        LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        configurator.doConfigure(new ByteArrayInputStream(config.getBytes()));
    }
    
    @Test
    public void logsToTrace() {
        PrintStream           stdOut  = System.out;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(ostream));
        
        logger = LoggerFactory.getLogger(this.getClass());
        
        GxUtil.showMessage(true, testMessage, logger, GxUtil.LogLevel.TRACE);
        
        System.setOut(stdOut);
        
        System.out.println(ostream.toString());
        
        assertTrue(ostream.toString().contains(GxUtil.LogLevel.TRACE.name()) && ostream.toString().contains(testMessage));
    }
    
    @Test
    public void logsToError() {
        PrintStream           stdOut  = System.out;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(ostream));
        
        logger = LoggerFactory.getLogger(this.getClass());
        
        GxUtil.showMessage(true, testMessage, logger, GxUtil.LogLevel.ERROR);
        
        System.setOut(stdOut);
        
        System.out.println(ostream.toString());
        
        assertTrue(ostream.toString().contains(GxUtil.LogLevel.ERROR.name()) && ostream.toString().contains(testMessage));
    }
    
    @Test
    public void logsToWarn() {
        PrintStream           stdOut  = System.out;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(ostream));
        
        logger = LoggerFactory.getLogger(this.getClass());
        
        GxUtil.showMessage(true, testMessage, logger, GxUtil.LogLevel.WARN);
        
        System.setOut(stdOut);
        
        System.out.println(ostream.toString());
        
        assertTrue(ostream.toString().contains(GxUtil.LogLevel.WARN.name()) && ostream.toString().contains(testMessage));
    }
    
    @Test
    public void logsToDebug() {
        PrintStream           stdOut  = System.out;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(ostream));
        
        logger = LoggerFactory.getLogger(this.getClass());
        
        GxUtil.showMessage(true, testMessage, logger, GxUtil.LogLevel.DEBUG);
        
        System.setOut(stdOut);
        
        System.out.println(ostream.toString());
        
        assertTrue(ostream.toString().contains(GxUtil.LogLevel.DEBUG.name()) && ostream.toString().contains(testMessage));
    }
    
    @Test
    public void logsToInfo() {
        PrintStream           stdOut  = System.out;
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(ostream));
        
        logger = LoggerFactory.getLogger(this.getClass());
        
        GxUtil.showMessage(true, testMessage, logger, GxUtil.LogLevel.INFO);
        
        System.setOut(stdOut);
        
        System.out.println(ostream.toString());
        
        assertTrue(ostream.toString().contains(GxUtil.LogLevel.INFO.name()) && ostream.toString().contains(testMessage));
    }
}
