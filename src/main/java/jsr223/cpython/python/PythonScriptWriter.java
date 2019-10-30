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
package jsr223.cpython.python;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.TaskScript;


/**
 * @author ActiveEon Team
 * @since 05/10/2017
 */
public class PythonScriptWriter {
    //Extension
    public static final String PYTHON_FILE_EXTENSION = ".cpy";

    public File writeFileToDisk(String fileContent) throws IOException {
        return writeFileToDisk(fileContent, 0, null);
    }

    public File writeFileToDisk(String fileContent, int port) throws IOException {
        return writeFileToDisk(fileContent, port, null);
    }

    public File writeFileToDisk(String fileContent, int port, String authToken) throws IOException {
        File pythonTempFile = null;
        try {
            pythonTempFile = File.createTempFile("jsr223-cpython-", PYTHON_FILE_EXTENSION);
        } catch (IOException e) {
            throw new IOException("Unable to create python temp file. " + e);
        }

        try (FileWriter pythonScriptFileWriter = new FileWriter(pythonTempFile);
                BufferedWriter pythonScriptBufferedWriter = new BufferedWriter(pythonScriptFileWriter)) {
            if (authToken == null) {
                writeLine("import os, sys", pythonScriptBufferedWriter);
            }
            writeLine("from py4j.java_gateway import JavaGateway, GatewayParameters", pythonScriptBufferedWriter);
            if (authToken == null) {
                writeLine("params = GatewayParameters(auth_token=os.environ['CPYTHON_TOKEN'], port=" + port +
                          ", auto_convert=True)", pythonScriptBufferedWriter);
            } else {
                writeLine("params = GatewayParameters(auth_token='" + authToken + "', port=" + port +
                          ", auto_convert=True)", pythonScriptBufferedWriter);
            }
            writeLine("gateway = JavaGateway(gateway_parameters=params)", pythonScriptBufferedWriter);
            //Add the bindings to locals() variable in Python
            writeLine("bindings = gateway.entry_point.getBindings()", pythonScriptBufferedWriter);
            writeLine("locals().update(bindings)", pythonScriptBufferedWriter);
            writeLine(fileContent, pythonScriptBufferedWriter);
            writeLine("if '" + TaskScript.RESULT_VARIABLE + "' in locals():", pythonScriptBufferedWriter);
            writeLine("    bindings['" + TaskScript.RESULT_VARIABLE + "'] = " + TaskScript.RESULT_VARIABLE + "",
                      pythonScriptBufferedWriter);
            writeLine("if '" + SelectionScript.RESULT_VARIABLE + "' in locals():", pythonScriptBufferedWriter);
            writeLine("    bindings['" + SelectionScript.RESULT_VARIABLE + "'] = " + SelectionScript.RESULT_VARIABLE +
                      "", pythonScriptBufferedWriter);
            writeLine("if '" + FlowScript.branchSelectionVariable + "' in locals():", pythonScriptBufferedWriter);
            writeLine("    bindings['" + FlowScript.branchSelectionVariable + "'] = " +
                      FlowScript.branchSelectionVariable + "", pythonScriptBufferedWriter);
            writeLine("if '" + FlowScript.replicateRunsVariable + "' in locals():", pythonScriptBufferedWriter);
            writeLine("    bindings['" + FlowScript.replicateRunsVariable + "'] = " + FlowScript.replicateRunsVariable +
                      "", pythonScriptBufferedWriter);
            writeLine("if '" + FlowScript.loopVariable + "' in locals():", pythonScriptBufferedWriter);
            writeLine("    bindings['" + FlowScript.loopVariable + "'] = " + FlowScript.loopVariable + "",
                      pythonScriptBufferedWriter);
            writeLine("if '" + SchedulerConstants.RESULT_METADATA_VARIABLE + "' in locals():",
                      pythonScriptBufferedWriter);
            writeLine("    bindings['" + SchedulerConstants.RESULT_METADATA_VARIABLE + "'] = " +
                      SchedulerConstants.RESULT_METADATA_VARIABLE + "", pythonScriptBufferedWriter);
            if (authToken == null) {
                writeLine("sys.exit()", pythonScriptBufferedWriter);
            }
        } catch (IOException e) {
            throw new IOException("Unable to write the python scripts to a file. ", e);
        }
        return pythonTempFile;
    }

    public File writeIPyParallelFileToDisk(File refPythonFile, int engine, String connector) throws IOException {
        File pythonTempFile = null;
        try {
            pythonTempFile = File.createTempFile("jsr223-cpython-", PYTHON_FILE_EXTENSION);
        } catch (IOException e) {
            throw new IOException("Unable to create python temp file. " + e);
        }

        try (FileWriter pythonScriptFileWriter = new FileWriter(pythonTempFile);
                BufferedWriter pythonScriptBufferedWriter = new BufferedWriter(pythonScriptFileWriter)) {
            writeLine("import ipyparallel as ipp", pythonScriptBufferedWriter);
            if (connector == null) {
                writeLine("rc = ipp.Client()", pythonScriptBufferedWriter);
            } else {
                writeLine("rc = ipp.Client('" + connector + "')", pythonScriptBufferedWriter);
            }
            if (engine >= 0) {
                writeLine("view = rc[" + engine + "]", pythonScriptBufferedWriter);
            } else {
                writeLine("view = rc.load_balanced_view()", pythonScriptBufferedWriter);
            }
            writeLine("view.block = True", pythonScriptBufferedWriter);
            writeLine("view.track = True", pythonScriptBufferedWriter);
            writeLine("filename = '" + refPythonFile.getAbsolutePath() + "'", pythonScriptBufferedWriter);
            writeLine("cmd = \"__file__='{0}'\".format(filename)", pythonScriptBufferedWriter);
            writeLine("ar = view.execute(cmd)", pythonScriptBufferedWriter);
            writeLine("ar = view.run(filename)", pythonScriptBufferedWriter);
            writeLine("print(ar.stdout)", pythonScriptBufferedWriter);
        } catch (IOException e) {
            throw new IOException("Unable to write the python scripts to a file. ", e);
        }
        return pythonTempFile;
    }

    public void writeLine(String lineContent, BufferedWriter bw) throws IOException {
        bw.write(lineContent);
        bw.newLine();
    }
}
