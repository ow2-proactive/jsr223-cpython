package jsr223.cpython;
/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.log4j.Logger;

import jsr223.cpython.entrypoint.EntryPoint;
import jsr223.cpython.processbuilder.SingletonPythonProcessBuilderFactory;
import jsr223.cpython.processbuilder.Utils.PythonProcessBuilderUtilities;
import py4j.GatewayServer;
import jsr223.cpython.python.PythonCommandCreator;
import jsr223.cpython.python.PythonScriptWriter;


/**
 * @author ActiveEon Team
 * @since 04/10/2017
 */
public class PythonScriptEngine extends AbstractScriptEngine {

    private static final Logger log = Logger.getLogger(PythonScriptEngine.class);

    private PythonScriptWriter pythonScriptWriter = new PythonScriptWriter();

    private PythonCommandCreator pythonCommandCreator = new PythonCommandCreator();

    public PythonScriptEngine() {

    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        File pythonFile = null;

        try {
            pythonFile = pythonScriptWriter.writeFileToDisk(script);
        } catch (IOException e) {
            log.warn("Failed to write content to python file: ", e);
        }

        //Create Python Command
        //TODO change here to automatically choose python version
        String[] pythonCommand = pythonCommandCreator.createPythonExecutionCommand(pythonFile, "python3");

        //Create the EntryPoint and start the gateway server
        EntryPoint entryPoint = EntryPoint.getInstance();
        GatewayServer gatewayServer = new GatewayServer(entryPoint, 25335);
        gatewayServer.start();

        //Populate the bindings in the gateway server
        Bindings bindings = entryPoint.getBindings();
        bindings.putAll(context.getBindings(ScriptContext.ENGINE_SCOPE));

        //Create a process builder
        ProcessBuilder processBuilder = SingletonPythonProcessBuilderFactory.getInstance()
                                                                            .getProcessBuilder(pythonCommand);

        Process process = null;

        try {

            //Start process
            process = processBuilder.start();

            //Wait for the process to exit
            int exitValue = process.waitFor();

            if (exitValue != 0) {
                throw new ScriptException("Python process execution has failed with exit code " + exitValue);
            }
            return exitValue;

        } catch (IOException e) {
            throw new ScriptException("Check if Python is installed properly. Failed to execute Python with exception: " +
                                      e);
        } catch (InterruptedException e1) {
            log.info("Python script execution interrupted. " + e1.getMessage());
            if (process != null) {
                process.destroy();
            }
        } finally {
            if (process != null) {
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    log.info("Python execution was not finished correctly after the interruption. " + e.getMessage());
                }
            }

            //Delete pythonFile
            if (pythonFile != null) {
                boolean deleted = pythonFile.delete();
                if (!deleted) {
                    log.warn("File: " + pythonFile.getAbsolutePath() + " was not deleted.");
                }
            }
            //Stop the gateway server
            gatewayServer.shutdown();
        }
        return null;
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {

        StringWriter stringWriter = new StringWriter();

        try {
            PythonProcessBuilderUtilities.pipe(reader, stringWriter);
        } catch (IOException e) {
            log.warn("Filed to convert Reader into StringWriter. Not possible to execute Python script.");
            log.debug("Filed to convert Reader into StringWriter. Not possible to execute Python script.", e);
        }

        return eval(stringWriter.toString(), context);
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return new PythonScriptEngineFactory();
    }

}
