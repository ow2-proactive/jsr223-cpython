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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * @author ActiveEon Team
 * @since 04/10/2017
 */
@NoArgsConstructor
@AllArgsConstructor
public class PythonVersionGetter {

    public static final String PYTHON_VERSION_IF_NOT_INSTALLED = "Could not determine version";

    private String PYTHON_VERSION_COMMAND = " --version"; // this command is needed to retrieve only specific string with python version

    /**
     * Retrieves the Python version
     *
     * @return The currently installed version return by the python command or an string indicates that the version could not be determined.
     */
    public String getPythonVersion(String python2or3) {

        String result = PYTHON_VERSION_IF_NOT_INSTALLED; //Default error string for result if version recovery fails

        if (python2or3.toLowerCase().equals("python3")) {
            PYTHON_VERSION_COMMAND = "python3" + PYTHON_VERSION_COMMAND;
        } else {
            PYTHON_VERSION_COMMAND = "python" + PYTHON_VERSION_COMMAND;
        }

        try {
            Process process = Runtime.getRuntime().exec(PYTHON_VERSION_COMMAND);

            //TODO change here, I don't like this try
            //To show the result
            InputStream fis = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                //TODO rewrite the restul
                result = line;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
