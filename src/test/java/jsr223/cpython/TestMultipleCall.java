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

import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.script.ScriptEngine;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

/**
 * @author ActiveEon Team
 * @since 27/10/2017
 */
public class TestMultipleCall {
    ExecutorService executors = Executors.newFixedThreadPool(5);

    @Test
    public void testMultipleCall() throws InterruptedException, ExecutionException {
        List<Callable<ScriptResult<Serializable>>> tasks = new ArrayList<>();

        for(int i = 0; i<20; i++) {
            tasks.add(new Callable<ScriptResult<Serializable>>() {
                @Override
                public ScriptResult<Serializable> call() throws Exception {
                    String pythonScript = "result = 123";

                    SimpleScript ss = new SimpleScript(pythonScript, PythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
                    TaskScript taskScript = new TaskScript(ss);
                    return taskScript.execute();
                }
            });
        }

        List<Future<ScriptResult<Serializable>>> res = executors.invokeAll(tasks);

        for(Future<ScriptResult<Serializable>> result : res) {

            System.out.println("Script output:");
            System.out.println(result.get().getResult());

            if (result.get().getResult() == null) {
                fail("The result is null, the Script Engine is not executed correctly!");
            }

            Assert.assertEquals("The result is returned correctly", 123, result.get().getResult());
        }
    }
}
