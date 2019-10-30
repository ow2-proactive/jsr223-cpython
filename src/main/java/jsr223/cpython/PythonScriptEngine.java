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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.utils.CookieBasedProcessTreeKiller;

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

    private static synchronized GatewayServer startGatewayServer(EntryPoint entryPoint, String authToken) {
        GatewayServer gatewayServer = new GatewayServer.GatewayServerBuilder().entryPoint(entryPoint)
                                                                              .authToken(authToken)
                                                                              .javaPort(0)
                                                                              .build();
        gatewayServer.start();
        return gatewayServer;
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {

        EntryPoint entryPoint = new EntryPoint();

        String authToken = UUID.randomUUID().toString();

        //Create the EntryPoint and start the gateway server
        GatewayServer gatewayServer = startGatewayServer(entryPoint, authToken);

        //Retrieve the port used by the gateway server
        int port = gatewayServer.getListeningPort();
        log.info("Python gateway server started using port : " + port);

        // Create Python Command.
        // If we find a specific python which is required in generic info, we need to use this specific version of python.
        String pythonVersion = "python";

        // Use IPython Parallel Engine if specified
        // https://ipyparallel.readthedocs.io/en/latest/index.html
        boolean useIPyParallel = false;

        // Use a pre-defined IPython engine if specified
        /*
         * By default uses engine index = 0,
         * if engine index == -1, use `load_balanced_view`
         * The LoadBalancedView is the class for load-balanced execution via the task scheduler.
         * These views always run tasks on exactly one engine, but let the scheduler determine where
         * that should be, allowing load-balancing of tasks.
         */
        int paramIPyParallelEngine = 0;

        // Use a pre-defined IPython connector if specified
        /*
         * This form assumes that the default connection information (stored in
         * ipcontroller-client.json,
         * found in IPYTHONDIR/profile_default/security) is accurate. If the controller was started
         * on a remote machine, you must copy that connection file to the client machine,
         * or enter its contents as arguments to the Client constructor.
         */
        String paramIPyParallelConnector = null;

        // Check generic information
        Map<String, String> genericInfo = (Map<String, String>) context.getBindings(ScriptContext.ENGINE_SCOPE)
                                                                       .get(SchedulerConstants.GENERIC_INFO_BINDING_NAME);

        if (genericInfo != null) {
            String version = genericInfo.get("PYTHON_COMMAND");
            if (version != null && !version.trim().isEmpty()) {
                pythonVersion = version;
            }

            if ("true".equalsIgnoreCase(genericInfo.get("IPYPARALLEL_ENABLED"))) {
                useIPyParallel = true;
            }

            String engine = genericInfo.get("IPYPARALLEL_ENGINE");
            try {
                if (engine != null && !engine.trim().isEmpty()) {
                    paramIPyParallelEngine = Integer.parseInt(engine);
                    if (paramIPyParallelEngine < 0) {
                        paramIPyParallelEngine = -1;
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("Failed to parse IPython Parallel engine index: ", e);
            }

            String connector = genericInfo.get("IPYPARALLEL_CONNECTOR");
            if (connector != null && !connector.trim().isEmpty()) {
                paramIPyParallelConnector = connector;
            }
        }

        // Write script to file
        File pythonFile = null;
        File refPythonFile = null;
        if (useIPyParallel) {
            try {
                refPythonFile = pythonScriptWriter.writeFileToDisk(script, port, authToken);
                pythonFile = pythonScriptWriter.writeIPyParallelFileToDisk(refPythonFile,
                                                                           paramIPyParallelEngine,
                                                                           paramIPyParallelConnector);
            } catch (IOException e) {
                log.warn("Failed to write content to python file: ", e);
            }
        } else {
            try {
                pythonFile = pythonScriptWriter.writeFileToDisk(script, port);
            } catch (IOException e) {
                log.warn("Failed to write content to python file: ", e);
            }
        }

        String[] pythonCommand = pythonCommandCreator.createPythonExecutionCommand(pythonFile, pythonVersion);

        //Populate the bindings in the gateway server
        Bindings bindingsShared = entryPoint.getBindings();
        bindingsShared.putAll(context.getBindings(ScriptContext.ENGINE_SCOPE));
        if (bindingsShared == null) {
            throw new ScriptException("No bindings specified in the script context");
        }

        //Create a process builder
        ProcessBuilder processBuilder = SingletonPythonProcessBuilderFactory.getInstance()
                                                                            .getProcessBuilder(pythonCommand);

        Map<String, String> env = processBuilder.environment();
        env.put("CPYTHON_TOKEN", authToken);

        Process process = null;
        Thread shutdownHook = null;
        CookieBasedProcessTreeKiller processTreeKiller = null;

        try {
            processTreeKiller = createProcessTreeKiller(context, processBuilder.environment());

            //Start process
            process = processBuilder.start();

            final Process shutdownHookProcessReference = process;
            final CookieBasedProcessTreeKiller shutdownHookPTKReference = processTreeKiller;
            shutdownHook = new Thread() {
                @Override
                public void run() {
                    destroyProcessAndWaitForItToBeDestroyed(shutdownHookProcessReference);
                    if (shutdownHookPTKReference != null) {
                        shutdownHookPTKReference.kill();
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownHook);

            //Attach streams
            processBuilderUtilities.attachStreamsToProcess(process,
                                                           context.getWriter(),
                                                           context.getErrorWriter(),
                                                           context.getReader());

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

            if (useIPyParallel) {
                if (refPythonFile != null) {
                    boolean deleted = refPythonFile.delete();
                    if (!deleted) {
                        log.warn("File: " + refPythonFile.getAbsolutePath() + " was not deleted.");
                    }
                }
            }

            //Stop the gateway server
            gatewayServer.shutdown();

            if (shutdownHook != null) {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }

            if (processTreeKiller != null) {
                processTreeKiller.kill();
            }
        }
        return null;
    }

    private static void destroyProcessAndWaitForItToBeDestroyed(Process process) {
        try {
            process.destroy();
            process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CookieBasedProcessTreeKiller createProcessTreeKiller(ScriptContext context,
            Map<String, String> environment) {
        CookieBasedProcessTreeKiller processTreeKiller = null;
        Map<String, String> genericInfo = (Map<String, String>) context.getBindings(ScriptContext.ENGINE_SCOPE)
                                                                       .get(SchedulerConstants.GENERIC_INFO_BINDING_NAME);
        Map<String, String> variables = (Map<String, String>) context.getBindings(ScriptContext.ENGINE_SCOPE)
                                                                     .get(SchedulerConstants.VARIABLES_BINDING_NAME);

        if (genericInfo != null && variables != null &&
            !"true".equalsIgnoreCase(genericInfo.get(SchedulerConstants.DISABLE_PROCESS_TREE_KILLER_GENERIC_INFO))) {
            String cookieSuffix = "CPython_Job" + variables.get(SchedulerVars.PA_JOB_ID) + "Task" +
                                  variables.get(SchedulerVars.PA_TASK_ID);
            processTreeKiller = CookieBasedProcessTreeKiller.createProcessChildrenKiller(cookieSuffix, environment);
        }
        return processTreeKiller;
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {

        StringWriter stringWriter = new StringWriter();

        try {
            PythonProcessBuilderUtilities.pipe(new BufferedReader(reader), new BufferedWriter(stringWriter));
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
