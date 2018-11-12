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
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import javax.script.ScriptEngine;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


/**
 * @author ActiveEon Team
 * @since 12/11/2018
 */
public class TestOutputStreaming {

    @Test(timeout = 60000)
    public void testOutputStreaming() throws Exception {
        String pythonScript = IOUtils.toString(TestOutputStreaming.class.getResource("/scripts/outputstream.py")
                                                                        .toURI(),
                                               Charsets.UTF_8);
        try (PipedInputStream pis = new PipedInputStream();
                InputStreamReader isr = new InputStreamReader(pis);
                BufferedReader br = new BufferedReader(isr);

                PipedOutputStream pos = new PipedOutputStream(pis);
                final PrintStream ps = new PrintStream(pos)) {
            SimpleScript ss = new SimpleScript(pythonScript,
                                               PythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
            final TaskScript taskScript = new TaskScript(ss);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    taskScript.execute(new HashMap(), ps, ps);
                }
            });
            thread.start();
            long starttime = System.currentTimeMillis();
            String line;
            while (!(line = br.readLine()).contains("Hello World!") && line != null) {
                // go on
            }
            Assert.assertFalse("Message should be read before reaching end of stream", line == null);
            Assert.assertTrue("Message should be read before the sleep expiration of 30 seconds",
                              System.currentTimeMillis() - starttime < 30000);
        }

    }
}
