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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

/**
 * @author pst
 *
 */
public class APIRunner {
    
    public static final String   API_PACKAGE_PROPERTY = "gxapi.packages";
    
    public static final String[] DEFAULT_API_PACKAGES = {"com.genexplain.api"};
    
    public static final String USAGE = "java -jar (...).jar <command> <args> ...";
    
    public static final List<String> helpFlags = Arrays.asList(new String[]{"-h","--help","-help","--h","help"});
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String,AppCommand> commands;
    
    private class AppCommand {
        String   name;
        String   description;
        Class<?> cmd;
    }
    
    public APIRunner() throws Exception {
        findCommands();
    }

    @SuppressWarnings("unchecked")
    private void findCommands() throws Exception {
        String[] apiPackages;
        if (System.getProperty(API_PACKAGE_PROPERTY) == null || 
                System.getProperty(API_PACKAGE_PROPERTY).isEmpty()) {
            apiPackages = DEFAULT_API_PACKAGES;
        } else {
            apiPackages = System.getProperty(API_PACKAGE_PROPERTY).split(":");
        }
        FastClasspathScanner scanner = new FastClasspathScanner(apiPackages);
        scanner.scan();
        Command    annotation;
        boolean    isCommand;
        AppCommand cmd;
        commands = new HashMap<>();
        for (String cl : scanner.getNamesOfAllClasses()) {
            Class c = Class.forName(cl);
            if (c.getAnnotation(Command.class) != null) {
                isCommand = false;
                for (Class ic : Arrays.asList(c.getInterfaces())) {
                    if (ic.getName().equals(Class.forName("com.genexplain.api.app.ApplicationCommand").getName())) {
                        isCommand = true;
                    }
                }
                if (!isCommand) continue;
                annotation = (Command)c.getAnnotation(Command.class);
                if (commands.containsKey(annotation.name())) {
                    throw new UnsupportedOperationException("Command name " +
                              annotation.name() +
                              " already exists");
                }
                cmd = new AppCommand();
                cmd.name        = annotation.name();
                cmd.description = annotation.description();
                cmd.cmd         = c;
                commands.put(cmd.name, cmd);
                
            }
        }
    }

    public void printHelp() {
        System.out.println("\nUsage: " + USAGE + "\n\nAvailable commands:\n");
        
        for (String cmd : commands.keySet()) {
            System.out.println(cmd + "  -  " + commands.get(cmd).description);
        }
        
        System.out.println("\nJava packages to be scanned for ApplicationCommand implemenations\n" +
                           "can be specified as system property using the java -D option\n");
        System.out.println("\nFor more info about each command try (java -D" + 
                              API_PACKAGE_PROPERTY + "=... -jar ...) COMMAND -h\n");
        System.out.println("\n---------------------------\n\n");
    }
    
    public static void main(String[] args) throws Exception {
        APIRunner app = new APIRunner();
        if (args.length < 1 || helpFlags.contains(args[0])) {
            app.printHelp();
            return;
        }
        
        String cmd = args[0];
        
        String[] cargs = (args.length > 1) ? Arrays.copyOfRange(args, 1, args.length) : new String[] {};
        AppCommand com = app.commands.get(cmd);
        
        if (com == null) {
            app.printHelp();
            throw new IllegalArgumentException("Unknown command " + cmd);
        }
        
        app.logger.info("Running command " + cmd);
        ((ApplicationCommand) com.cmd.newInstance()).run(cargs);
    }
}
