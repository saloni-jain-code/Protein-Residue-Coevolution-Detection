/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Class to write the output to a file.
 * @author davidtalavera
 */
public class WriterForFile {

    private final String myFile;
    private FileWriter wFile;

    /**
     * Constructor.
     * @param myFile
     */
    public WriterForFile(String myFile) {
        this.myFile = myFile;
    }

    /*Public methods*/
    
    /**
     * Method to set up the connection to the output file.
     * @throws IOException 
     */
    public void setFile() throws IOException {
        wFile = new FileWriter(this.myFile);
    }
    
    /**
     * Method to write the content to the output file.
     * @param myContents 
     */
    public void writeFileContents(String myContents) {
        try {
            PrintWriter outFile = new PrintWriter(wFile, false);
            outFile.println(myContents);
        } catch (Exception e) {
        }
    }
    
    /**
     * Method to close the connection to the output file.
     * @throws IOException 
     */
    public void closeFile() throws IOException {
        wFile.close();
    }
}
