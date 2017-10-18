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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import javax.script.ScriptEngine;

import org.hamcrest.Matchers;
import org.junit.Test;


/**
 * @author ActiveEon Team
 * @since 18/10/2017
 */
public class PythonScriptEngineFactoryTest {
    @Test
    public void getExtensions() throws Exception {
        assertThat(pythonScriptEngineFactory.getExtensions(), hasItem(containsString("py")));
    }

    @Test
    public void getMimeTypes() throws Exception {
        assertThat(pythonScriptEngineFactory.getMimeTypes(), hasItem(containsString("python")));
    }

    @Test
    public void getNames() throws Exception {
        assertThat(pythonScriptEngineFactory.getNames(), hasItem(containsString("python")));
        assertThat(pythonScriptEngineFactory.getNames(), hasItem(containsString("Python")));
    }

    @Test
    public void getParameter() throws Exception {

        assertThat(pythonScriptEngineFactory.getParameter(ScriptEngine.NAME),
                   Matchers.<Object> is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME)));

        assertThat(pythonScriptEngineFactory.getParameter(ScriptEngine.ENGINE),
                   Matchers.<Object> is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.ENGINE)));

        assertThat(pythonScriptEngineFactory.getParameter(ScriptEngine.ENGINE_VERSION),
                   Matchers.<Object> is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.ENGINE_VERSION)));

        assertThat(pythonScriptEngineFactory.getParameter(ScriptEngine.LANGUAGE),
                   Matchers.<Object> is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.LANGUAGE)));

        assertThat(pythonScriptEngineFactory.getParameter(ScriptEngine.LANGUAGE_VERSION),
                   Matchers.<Object> is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.LANGUAGE_VERSION)));
    }

    private PythonScriptEngineFactory pythonScriptEngineFactory = new PythonScriptEngineFactory();

    @Test
    public void getEngineName() throws Exception {
        assertThat(pythonScriptEngineFactory.getEngineName(),
                   is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME)));
    }

    @Test
    public void getEngineVersion() throws Exception {
        assertThat(pythonScriptEngineFactory.getEngineVersion(),
                   is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.ENGINE_VERSION)));
    }

    @Test
    public void getLanguageName() throws Exception {
        assertThat(pythonScriptEngineFactory.getLanguageName(),
                   is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.LANGUAGE)));
    }

    @Test
    public void getLanguageVersion() throws Exception {
        assertThat(pythonScriptEngineFactory.getLanguageVersion(),
                   is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.LANGUAGE_VERSION)));
    }

    @Test
    public void getScriptEngine() throws Exception {
        assertThat(pythonScriptEngineFactory.getScriptEngine() instanceof PythonScriptEngine, is(true));
    }

}
