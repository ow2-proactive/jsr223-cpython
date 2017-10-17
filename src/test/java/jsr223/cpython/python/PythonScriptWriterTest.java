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