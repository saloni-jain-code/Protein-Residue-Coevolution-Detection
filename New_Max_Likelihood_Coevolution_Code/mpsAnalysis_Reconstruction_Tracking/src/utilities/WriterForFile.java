package utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to write the output to a file.
 * @author davidtalavera
 */
public class WriterForFile {

    private final String myFile;
    private FileWriter wFile;
    private PrintWriter outFile;
    private final List<String> contentBuffer = new ArrayList<>();

    /**
     * Constructor.
     * @param myFile
     */
    public WriterForFile(String myFile) {
        this.myFile = myFile;
        System.out.println("Created writer for file: " + myFile);
    }

    /**
     * Method to set up the connection to the output file.
     * @throws IOException
     */
    public void setFile() throws IOException {
        wFile = new FileWriter(this.myFile);
        System.out.println("File prepared: " + myFile);
    }

    /**
     * Method to add content to the buffer (not writing yet).
     * @param myContents
     */
    public void writeFileContents(String myContents) {
        if (myContents != null) {
            contentBuffer.add(myContents);
            System.out.println("Added content to buffer for file: " + myFile +
                    " (buffer size: " + contentBuffer.size() + ")");
        }
    }

    /**
     * Method to close the connection to the output file.
     * This is where actual writing happens.
     * @throws IOException
     */
    public void closeFile() throws IOException {
        try {
            outFile = new PrintWriter(wFile);

            System.out.println("Writing " + contentBuffer.size() + " lines to " + myFile);

            for (String content : contentBuffer) {
                outFile.println(content);
            }

            outFile.flush();
            outFile.close();
            wFile.close();

            System.out.println("Successfully closed file: " + myFile);
        } catch (Exception ex) {
            System.err.println("Error writing to file: " + ex.getMessage());
            throw new IOException("Failed to write to file: " + ex.getMessage(), ex);
        }
    }
}