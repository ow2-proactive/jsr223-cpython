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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;


/**
 * @author ActiveEon Team
 * @since 17/10/2017
 */
public class PythonScriptWriterTest {

    private final String fileContent = "This is a test.";

    @Test
    public void writeFileToDisk() throws Exception {
        PythonScriptWriter pythonScriptWriter = new PythonScriptWriter();

        File file = pythonScriptWriter.writeFileToDisk(fileContent);
        file.deleteOnExit();

        assertTrue(new File(file.getAbsolutePath()).exists());
        assertTrue(Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8).contains(fileContent));

    }
}
