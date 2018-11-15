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
package jsr223.cpython.utils;

import java.io.StringWriter;

import org.apache.log4j.Logger;

import jsr223.cpython.processbuilder.PythonProcessBuilderFactory;
import jsr223.cpython.processbuilder.SingletonPythonProcessBuilderFactory;
import jsr223.cpython.processbuilder.Utils.PythonProcessBuilderUtilities;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * @author ActiveEon Team
 * @since 04/10/2017
 */
@NoArgsConstructor
@AllArgsConstructor
public class PythonVersionGetter {

    private static final Logger log = Logger.getLogger(PythonVersionGetter.class);

    public static final String PYTHON_VERSION_IF_NOT_INSTALLED = "Could not determine version";

    private final String PYTHON_VERSION_COMMAND = "--version"; // this command is needed to retrieve only specific string with python version

    private PythonProcessBuilderFactory factory = SingletonPythonProcessBuilderFactory.getInstance();

    private PythonProcessBuilderUtilities processBuilderUtilities = new PythonProcessBuilderUtilities();

    /**
     * Retrieves the Python version
     *
     * @return The currently installed version return by the python command or an string indicates that the version could not be determined.
     */
    public String getPythonVersion(String pythonVersion) {

        String result = PYTHON_VERSION_IF_NOT_INSTALLED; //Default error string for result if version recovery fails

        Process process = null;

        try {
            String[] pythonCommand = new String[] { pythonVersion, PYTHON_VERSION_COMMAND };

            ProcessBuilder processBuilder = factory.getProcessBuilder(pythonCommand);
            process = processBuilder.start();

            StringWriter commandOutput = new StringWriter();
            StringWriter processError = new StringWriter();

            processBuilderUtilities.attachStreamsToProcess(process, commandOutput, processError, null);

            //Wait for process to exit
            process.waitFor();

            // Extract output
            result = commandOutput.toString().equals("") ? processError.toString() : commandOutput.toString();

        } catch (Exception e) {
            log.debug("Could not determine python version: " + e.getMessage());
            if (process != null) {
                process.destroy();
            }
        }

        return result;
    }

}
