/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Class to read the different input files and to return content as an array of lines.
 * @author davidtalavera
 */
public class ReaderForFile {

    private final String file;
    private final ArrayList<String> lines;

    /**
     * Constructor.
     * @param file
     */
    public ReaderForFile(String file) {
        this.file = file;
        lines = new ArrayList();
    }

    /*Public methods*/
    
    /**
     * Method to read the content of the file.
     * @throws Exception 
     */
    public void readFileContents() throws Exception {
        try (BufferedReader inputFile = new BufferedReader(new FileReader(this.file))) {
            boolean exit = false;
            do {
                String myLine = inputFile.readLine();
                if (myLine != null) {
                    lines.add(myLine);
                } else {
                    exit = true;
                }
            } while (exit == false);
        }
    }

    /**
     * Method to return the read lines.
     * @return 
     */
    public ArrayList<String> getLines() {
        return lines;
    }
}
