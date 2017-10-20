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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import jsr223.cpython.processbuilder.SingletonPythonProcessBuilderFactory;
import jsr223.cpython.python.PythonCommandCreator;


/**
 * @author ActiveEon Team
 * @since 18/10/2017
 */
public class TestSpecificPythonVersionRequired {

    @Test
    public void testBadPythonVersionRequiredInGI() throws Exception {
        String pythonVersionWantToUse = "Hello";

        ScriptResult<Serializable> res = testHelper(pythonVersionWantToUse);

        System.out.println("Script Exception :");
        System.out.println(res.getException());

        Assert.assertTrue("An error occurred with a bad python command requirement", res.errorOccured());
        Assert.assertTrue("The python script must be executed by the python version required in Generic Info",
                          res.getException().getMessage().contains("Check if Python is installed properly"));

    }

    @Test
    public void testPython3() throws Exception {
        PythonCommandCreator pythonCommandCreator = new PythonCommandCreator();
        String[] pythonCommand = pythonCommandCreator.createPythonCommandWithParameter(null, "python3", "-h");

        ProcessBuilder pb = new ProcessBuilder(pythonCommand);
        try {
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            Assume.assumeTrue("Python3 should be installed in order to pass this test", e == null);
        }

        String pythonVersionWantToUse = "python3";

        ScriptResult<Serializable> res = testHelper(pythonVersionWantToUse);

        System.out.println("Script Output :");
        System.out.println(res.getOutput());

        Assert.assertTrue("The python script must be executed by the python version required in Generic Info",
                          res.getOutput().toString().contains("Hello world!"));
    }

    public ScriptResult<Serializable> testHelper(String pythonVersionRequiredInGI) throws InvalidScriptException {
        HashMap<String, Serializable> giMap = new HashMap(1);
        giMap.put("PYTHON_COMMAND", pythonVersionRequiredInGI);

        Map<String, Object> aBindings = Collections.singletonMap(SchedulerConstants.GENERIC_INFO_BINDING_NAME,
                                                                 (Object) giMap);

        String pythonScript = "print('Hello world!')";

        SimpleScript ss = new SimpleScript(pythonScript, PythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        TaskScript taskScript = new TaskScript(ss);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        ScriptResult<Serializable> res = taskScript.execute(aBindings,
                                                            new PrintStream(output),
                                                            new PrintStream(output));

        return res;
    }
}
