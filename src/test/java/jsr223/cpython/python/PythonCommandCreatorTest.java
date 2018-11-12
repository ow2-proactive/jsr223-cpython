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

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;


/**
 * @author ActiveEon Team
 * @since 17/10/2017
 */
public class PythonCommandCreatorTest {
    private final PythonCommandCreator pythonCommandCreator = new PythonCommandCreator();

    @Test
    public void createPythonExecutionCommand() throws Exception {
        File file = new File("test.py");
        String pythonVersion = "python";
        String[] command = pythonCommandCreator.createPythonExecutionCommand(file, pythonVersion);

        if (OperatingSystem.getOperatingSystem() == OperatingSystem.windows) {
            assertEquals(command[2], pythonVersion);
            assertEquals(command[3], file.getPath());
        } else {
            assertEquals(command[0], pythonVersion);
            assertEquals(command[2], file.getPath());
        }

    }

}
