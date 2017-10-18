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

        assertThat(pythonScriptEngineFactory.getParameter(ScriptEngine.NAME), Matchers.<Object>is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME)));

        assertThat(pythonScriptEngineFactory.getParameter(ScriptEngine.ENGINE), Matchers.<Object>is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.ENGINE)));

        assertThat(pythonScriptEngineFactory.getParameter(ScriptEngine.ENGINE_VERSION), Matchers.<Object>is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.ENGINE_VERSION)));

        assertThat(pythonScriptEngineFactory.getParameter(ScriptEngine.LANGUAGE), Matchers.<Object>is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.LANGUAGE)));

        assertThat(pythonScriptEngineFactory.getParameter(ScriptEngine.LANGUAGE_VERSION), Matchers.<Object>is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.LANGUAGE_VERSION)));
    }

    private PythonScriptEngineFactory pythonScriptEngineFactory = new PythonScriptEngineFactory();

    @Test
    public void getEngineName() throws Exception {
        assertThat(pythonScriptEngineFactory.getEngineName(), is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.NAME)));
    }

    @Test
    public void getEngineVersion() throws Exception {
        assertThat(pythonScriptEngineFactory.getEngineVersion(), is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.ENGINE_VERSION)));
    }

    @Test
    public void getLanguageName() throws Exception {
        assertThat(pythonScriptEngineFactory.getLanguageName(), is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.LANGUAGE)));
    }

    @Test
    public void getLanguageVersion() throws Exception {
        assertThat(pythonScriptEngineFactory.getLanguageVersion(), is(pythonScriptEngineFactory.PARAMETERS.get(ScriptEngine.LANGUAGE_VERSION)));
    }

    @Test
    public void getScriptEngine() throws Exception {
        assertThat(pythonScriptEngineFactory.getScriptEngine() instanceof PythonScriptEngine, is(true));
    }

}