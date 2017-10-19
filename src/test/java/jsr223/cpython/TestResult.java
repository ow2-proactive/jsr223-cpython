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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


/**
 * @author ActiveEon Team
 * @since 18/10/2017
 */
public class TestResult {

    @Test
    public void testTaskResult() throws Exception {
        String pythonScript = "result = 123";

        SimpleScript ss = new SimpleScript(pythonScript, PythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute();

        System.out.println("Script output:");
        System.out.println(res.getResult());

        Assert.assertEquals("The result is returned correctly", 123, res.getResult());

    }

    @Test
    public void testSelectionScriptResult() throws Exception {
        Map<String, Object> aBindings = new HashMap<>();
        String pythonScript = "selected = False";

        SelectionScript selectionScript = new SelectionScript(pythonScript,
                                                              PythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME),
                                                              true);

        ScriptResult<Boolean> res = selectionScript.execute(aBindings, System.out, System.err);
        System.out.println(SelectionScript.RESULT_VARIABLE);
        System.out.println("Script output:");
        System.out.println(res.getResult());

        Assert.assertEquals("The result should be false", false, res.getResult());
    }

    @Test
    public void testLoop() throws Exception {

        String loopPythonScript = "loop='* * * * *'";

        Map<String, Object> aBindings = new HashMap<>();

        FlowScript loopScript = FlowScript.createLoopFlowScript(loopPythonScript,
                                                                PythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME),
                                                                "aTarget");

        ScriptResult<FlowAction> res = loopScript.execute(aBindings, System.out, System.err);

        System.out.println("Script output:");
        System.out.println(res.getOutput());

        Assert.assertEquals("The result should contain the loop decision",
                            FlowActionType.LOOP,
                            res.getResult().getType());

        org.junit.Assert.assertEquals("The result should contain the cron expression",
                                      "* * * * *",
                                      res.getResult().getCronExpr());

        loopPythonScript = "loop = True";

        loopScript = FlowScript.createLoopFlowScript(loopPythonScript,
                                                     PythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME),
                                                     "aTarget");
        res = loopScript.execute(aBindings, System.out, System.err);

        System.out.println("Script output:");
        System.out.println(res.getOutput());

        org.junit.Assert.assertEquals("The result should contain the loop decision",
                                      FlowActionType.LOOP,
                                      res.getResult().getType());

        org.junit.Assert.assertEquals("The result should contain the loop decision",
                                      "aTarget",
                                      res.getResult().getTarget());
    }

    @Test
    public void testReplicate() throws Exception {
        String replicatePythonScript = "runs = 2";

        Map<String, Object> aBindings = new HashMap<>();

        FlowScript replicateScript = FlowScript.createReplicateFlowScript(replicatePythonScript,
                                                                          PythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME));

        ScriptResult<FlowAction> res = replicateScript.execute(aBindings, System.out, System.err);

        System.out.println("Script output:");
        System.out.println(res.getOutput());

        org.junit.Assert.assertEquals("The result should contain the replicate runs",
                                      2,
                                      res.getResult().getDupNumber());
    }

    @Test
    public void testBranch() throws Exception {
        String branchPythonScript = "branch = 'if';";

        Map<String, Object> aBindings = new HashMap<>();

        FlowScript loopScript = FlowScript.createIfFlowScript(branchPythonScript,
                                                              PythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME),
                                                              "ifTarget",
                                                              "elseTarget",
                                                              "continuationTarget");
        ScriptResult<FlowAction> res = loopScript.execute(aBindings, System.out, System.err);

        System.out.println("Script output:");
        System.out.println(res.getOutput());

        org.junit.Assert.assertEquals("The result should contain the branch decision",
                                      FlowActionType.IF,
                                      res.getResult().getType());

        org.junit.Assert.assertEquals("The result should contain the if target",
                                      "ifTarget",
                                      res.getResult().getTarget());
    }
}
