package utilities;

import java.io.*;
import java.util.*;


/* copied code from https://stackoverflow.com/questions/49756410/read-a-fasta-file-into-string-java */

public class ReadFASTAFile {

    public TreeMap<String, String> map;
    private final String filepath;

    public ReadFASTAFile(String filepath) {
        this.filepath = filepath;
        map = new TreeMap<>();
    }
    
    public void parseAncestralSequences() throws FileNotFoundException {

        boolean first = true;
        String prevID = "";
        String ID;
        String sequence = "";
    
        try (Scanner sc = new Scanner(new File(this.filepath))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.charAt(0) == '>') {
                    ID = line.substring(1);
                    if (first) {
                        first = false;
                    } else {
                        this.map.put(prevID, sequence);
                    }
                    prevID = ID;
                    sequence = "";
                } else {
                    sequence += line;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
        if (sequence.length() > 0) {
            this.map.put(prevID, sequence);
        }

    }
}