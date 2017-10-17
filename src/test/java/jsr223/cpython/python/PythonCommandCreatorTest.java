package jsr223.cpython.python;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

        assertEquals(command[0], pythonVersion);
        assertEquals(command[1], file.getPath());

    }

}