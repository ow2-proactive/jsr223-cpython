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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

import javax.script.ScriptEngine;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


/**
 * @author ActiveEon Team
 * @since 18/10/2017
 */
public class TestError {

    @Test
    public void test() throws Exception {
        String messageAfter = "Must not";

        String pythonScript = "while True print 'Hello world'\n" + "print(\"Must not\")\n";

        SimpleScript ss = new SimpleScript(pythonScript, PythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        TaskScript taskScript = new TaskScript(ss);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        ScriptResult<Serializable> res = taskScript.execute(null, new PrintStream(output), new PrintStream(output));

        System.out.println("Script output :");
        System.out.println(output);

        Assert.assertNotNull("The script exception must not be null", res.getException());
        Assert.assertTrue("The script exception must contain the error statement",
                          res.getException().getMessage().contains("failed"));
        Assert.assertTrue("The script output must contain the error statement",
                          output.toString().contains("SyntaxError"));
        Assert.assertFalse("The script output must not contain the message after the error",
                           output.toString().contains(messageAfter));

    }

}
