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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

/**
 * A class for small utility functions that aren't better put elsewhere.
 * 
 * @author pst
 *
 */
public class GxUtil {
    
    /**
     * Scans packages for classes that pass the provided filter and returns a list
     * of class names.
     * 
     * @param packages - the packages to scan
     * @param filter   - a filter predicate to return the desired classes
     * @return a list of class names
     */
    public static List<String> scanPackage(String[] packages, Predicate<String> filter) {
        FastClasspathScanner scanner = new FastClasspathScanner(packages);
        scanner.scan();
        List<String> classes = new ArrayList<>();
        
        scanner.getNamesOfAllClasses().stream().filter(filter).forEach(cls -> {
            classes.add(cls);
        });
        
        return classes;
    }
    
    /**
     * Available log levels.
     */
	public enum LogLevel {
		INFO,
		TRACE,
		WARN,
		ERROR,
		DEBUG
	}
	
	/**
	 * If <b>verbose</b> the <b>message</b> is logged using the <b>logger</b> on
	 * the given <b>log level</b>.
	 * 
	 * @param verbose
	 *           whether or not to log the message
	 *           
	 * @param message
	 *           message to be shown
	 *           
	 * @param logger
	 *           logger to be used for logging
	 *            
	 * @param level
	 *           log level as enumerated in {@link LogLevel}
	 */
	public static void showMessage(boolean verbose, String message, Logger logger, LogLevel level) {
		if (verbose) {
			switch (level) {
			case TRACE:   logger.trace(message); break;
			case WARN:    logger.warn(message); break;
			case ERROR:   logger.error(message); break;
			case DEBUG:   logger.debug(message); break;
			default: logger.info(message);
			}
		}
	}
	
	/**
	 * Writes <b>output</b> to specified <b>file</b>.
	 * 
	 * @param file
	 *           file path
	 *           
	 * @param output
	 *           output to write
	 *           
	 * @throws Exception
	 *           May throw an exception caused by forwarding specified parameters
	 *           to other methods
	 */
    public static void writeToFile(String file, String output) throws Exception {
        Path path = Paths.get(file);
        Files.write(path, output.getBytes());
    }
}
