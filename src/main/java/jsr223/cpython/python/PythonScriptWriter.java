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


/**
 * @author ActiveEon Team
 * @since 05/10/2017
 */
public class PythonScriptWriter {
    //Extension
    public static final String PYTHON_FILE_EXTENSION = ".py";

    public File writeFileToDisk(String fileContent) throws IOException {
        File pythonTempFile = null;
        try {
            pythonTempFile = File.createTempFile("jsr223-cpython-", PYTHON_FILE_EXTENSION);
        } catch (IOException e) {
            throw new IOException("Unable to create python temp file. " + e);
        }

        // Write python script file to disk
        FileWriter pythonScriptFileWriter = new FileWriter(pythonTempFile);
        BufferedWriter pythonScriptBufferedWriter = new BufferedWriter(pythonScriptFileWriter);
        try {
            writeLine("from py4j.java_gateway import JavaGateway, GatewayParameters", pythonScriptBufferedWriter);
            writeLine("gateway = JavaGateway(gateway_parameters=GatewayParameters(port=25335))",
                      pythonScriptBufferedWriter);
            //Add the bindings to locals() variable in Python
            writeLine("bindings = gateway.entry_point.getBindings()", pythonScriptBufferedWriter);
            writeLine("bindings = locals().update(bindings)", pythonScriptBufferedWriter);
            writeLine(fileContent, pythonScriptBufferedWriter);
        } catch (IOException e) {
            throw new IOException("Unable to write the python scripts to a file. " + e);
        } finally {
            pythonScriptBufferedWriter.close();
            pythonScriptFileWriter.close();
        }
        return pythonTempFile;
    }

    public void writeLine(String lineContent, BufferedWriter bw) throws IOException {
        try {
            bw.write(lineContent);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
