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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.Writer;

import org.junit.Assume;
import org.junit.Test;
import org.mockito.Matchers;

import jsr223.cpython.processbuilder.PythonProcessBuilderFactory;
import jsr223.cpython.processbuilder.Utils.PythonProcessBuilderUtilities;


/**
 * @author ActiveEon Team
 * @since 17/10/2017
 */
public class PythonVersionGetterTest {
    private static final String pythonVersoin = "python";

    @Test
    public void getPythonVersionWithCommandBuilder() throws Exception {
        PythonVersionGetter pythonVersionGetter = new PythonVersionGetter();

        String result = pythonVersionGetter.getPythonVersion(pythonVersoin);

        assertThat(result, is(notNullValue()));
    }

    @Test
    public void getPythonVersionInvalidCommand() throws Exception {
        PythonProcessBuilderFactory pythonProcessBuilderFactory = mock(PythonProcessBuilderFactory.class);
        PythonProcessBuilderUtilities pythonProcessBuilderUtilities = spy(PythonProcessBuilderUtilities.class);

        when(pythonProcessBuilderFactory.getProcessBuilder(Matchers.<String[]> anyVararg())).thenReturn(new ProcessBuilder("...."));

        PythonVersionGetter pythonVersionGetter = new PythonVersionGetter(pythonProcessBuilderFactory,
                                                                          pythonProcessBuilderUtilities);

        assertThat(pythonVersionGetter.getPythonVersion(pythonVersoin),
                   is(PythonVersionGetter.PYTHON_VERSION_IF_NOT_INSTALLED));
    }

    @Test
    public void getPythonVersionNullFactory() throws Exception {
        PythonVersionGetter pythonVersionGetter = new PythonVersionGetter(null, new PythonProcessBuilderUtilities());

        assertThat(pythonVersionGetter.getPythonVersion(pythonVersoin),
                   is(PythonVersionGetter.PYTHON_VERSION_IF_NOT_INSTALLED));
    }

    @Test
    public void getPythonVersionWithProcessBuilderFactory() throws Exception {
        PythonProcessBuilderFactory pythonProcessBuilderFactory = mock(PythonProcessBuilderFactory.class);
        PythonProcessBuilderUtilities pythonProcessBuilderUtilities = spy(PythonProcessBuilderUtilities.class);

        when(pythonProcessBuilderFactory.getProcessBuilder(Matchers.<String[]> anyVararg())).thenReturn(new ProcessBuilder(""));

        PythonVersionGetter pythonVersionGetter = new PythonVersionGetter(pythonProcessBuilderFactory,
                                                                          pythonProcessBuilderUtilities);

        pythonVersionGetter.getPythonVersion(pythonVersoin);

        verify(pythonProcessBuilderFactory).getProcessBuilder(Matchers.<String[]> anyVararg());

    }

    @Test
    public void getPythonVersionWithProcessBuilderFactoryOnWindows() throws Exception {
        Assume.assumeTrue(System.getProperty("os.name").toLowerCase().startsWith("win"));

        PythonProcessBuilderFactory pythonProcessBuilderFactory = mock(PythonProcessBuilderFactory.class);
        PythonProcessBuilderUtilities pythonProcessBuilderUtilities = spy(PythonProcessBuilderUtilities.class);

        when(pythonProcessBuilderFactory.getProcessBuilder(Matchers.<String[]> anyVararg())).thenReturn(new ProcessBuilder("cmd",
                                                                                                                           "/C",
                                                                                                                           "dir"));

        PythonVersionGetter pythonVersionGetter = new PythonVersionGetter(pythonProcessBuilderFactory,
                                                                          pythonProcessBuilderUtilities);

        pythonVersionGetter.getPythonVersion(pythonVersoin);

        verify(pythonProcessBuilderUtilities).attachStreamsToProcess(any(Process.class),
                                                                     any(Writer.class),
                                                                     any(Writer.class),
                                                                     any(Reader.class));

    }

    @Test
    public void getPythonVersionWithProcessBuilderFactoryOnLinux() throws Exception {
        Assume.assumeTrue(System.getProperty("os.name").toLowerCase().startsWith("lin"));

        PythonProcessBuilderFactory pythonProcessBuilderFactory = mock(PythonProcessBuilderFactory.class);
        PythonProcessBuilderUtilities pythonProcessBuilderUtilities = spy(PythonProcessBuilderUtilities.class);

        when(pythonProcessBuilderFactory.getProcessBuilder(Matchers.<String[]> anyVararg())).thenReturn(new ProcessBuilder("ls"));

        PythonVersionGetter pythonVersionGetter = new PythonVersionGetter(pythonProcessBuilderFactory,
                                                                          pythonProcessBuilderUtilities);

        pythonVersionGetter.getPythonVersion(pythonVersoin);

        verify(pythonProcessBuilderUtilities).attachStreamsToProcess(any(Process.class),
                                                                     any(Writer.class),
                                                                     any(Writer.class),
                                                                     any(Reader.class));

    }

    @Test
    public void getPythonVersionWithProcessBuilderFactoryOnMac() throws Exception {
        Assume.assumeTrue(System.getProperty("os.name").toLowerCase().startsWith("mac"));

        PythonProcessBuilderFactory pythonProcessBuilderFactory = mock(PythonProcessBuilderFactory.class);
        PythonProcessBuilderUtilities pythonProcessBuilderUtilities = spy(PythonProcessBuilderUtilities.class);

        when(pythonProcessBuilderFactory.getProcessBuilder(Matchers.<String[]> anyVararg())).thenReturn(new ProcessBuilder("ls"));

        PythonVersionGetter pythonVersionGetter = new PythonVersionGetter(pythonProcessBuilderFactory,
                                                                          pythonProcessBuilderUtilities);

        pythonVersionGetter.getPythonVersion(pythonVersoin);

        verify(pythonProcessBuilderUtilities).attachStreamsToProcess(any(Process.class),
                                                                     any(Writer.class),
                                                                     any(Writer.class),
                                                                     any(Reader.class));

    }

}
