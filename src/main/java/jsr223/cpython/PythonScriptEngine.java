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
package jsr223.cpython;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.SchedulerConstants;

import jsr223.cpython.entrypoint.EntryPoint;
import jsr223.cpython.processbuilder.SingletonPythonProcessBuilderFactory;
import jsr223.cpython.processbuilder.Utils.PythonProcessBuilderUtilities;
import jsr223.cpython.python.PythonCommandCreator;
import jsr223.cpython.python.PythonScriptWriter;
import py4j.GatewayServer;


/**
 * @author ActiveEon Team
 * @since 04/10/2017
 */
public class PythonScriptEngine extends AbstractScriptEngine {

    private static final Logger log = Logger.getLogger(PythonScriptEngine.class);

    private PythonScriptWriter pythonScriptWriter = new PythonScriptWriter();

    private PythonCommandCreator pythonCommandCreator = new PythonCommandCreator();

    private PythonProcessBuilderUtilities processBuilderUtilities = new PythonProcessBuilderUtilities();

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
        String pythonVersion = "python";
        Map<String, String> genericInfo = (HashMap<String, String>) context.getBindings(ScriptContext.ENGINE_SCOPE)
                                                                           .get(SchedulerConstants.GENERIC_INFO_BINDING_NAME);
        if (genericInfo != null && genericInfo.containsKey("PYTHON_COMMAND")) {
            pythonVersion = genericInfo.get("PYTHON_COMMAND");
        }
        String[] pythonCommand = pythonCommandCreator.createPythonExecutionCommand(pythonFile, pythonVersion);

        //Create the EntryPoint and start the gateway server
        EntryPoint entryPoint = EntryPoint.getInstance();
        GatewayServer gatewayServer = new GatewayServer(entryPoint, 25335);
        gatewayServer.start();

        //Populate the bindings in the gateway server
        Bindings bindingsShared = entryPoint.getBindings();
        bindingsShared.putAll(context.getBindings(ScriptContext.ENGINE_SCOPE));
        if (bindingsShared == null) {
            throw new ScriptException("No bindings specified in the script context");
        }

        //Create a process builder
        ProcessBuilder processBuilder = SingletonPythonProcessBuilderFactory.getInstance()
                                                                            .getProcessBuilder(pythonCommand);

        Process process = null;

        try {

            //Start process
            process = processBuilder.start();

            //Attach streams
            processBuilderUtilities.attachStreamsToProcess(process,
                                                           context.getWriter(),
                                                           context.getErrorWriter(),
                                                           null);

            //Wait for the process to exit
            int exitValue = process.waitFor();

            if (exitValue != 0) {
                throw new ScriptException("Python process execution has failed with exit code " + exitValue);
            }

            context.getBindings(ScriptContext.ENGINE_SCOPE).putAll(bindingsShared);

            Object resultValue = true;

            return resultValue;

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
            log.warn("Failed to convert Reader into StringWriter. Not possible to execute Python script.");
            log.debug("Failed to convert Reader into StringWriter. Not possible to execute Python script.", e);
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
