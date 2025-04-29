/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to read the MSA in Phylip format.
 * @author davidtalavera
 */
public class ReadPhylipAlignment {
    
    private final String myFile;
    private int numberOfSequences;
    private int numberOfAlignedColumns;
    private ArrayList<String> lines;
    private final HashMap<Integer, String> columnsPositions;
    private final HashMap<ArrayList, HashMap> columnsMap;
    private final HashMap<String, Integer> amountOfStates;
    public TreeMap<String, String> sequences;
    
    /*
     * Constructor.
     */
    public ReadPhylipAlignment(String myFile) {
        this.myFile = myFile;
        columnsPositions = new HashMap();
        columnsMap = new HashMap();
        amountOfStates = new HashMap();
        sequences = new TreeMap();
    }
    
    /*Public methods*/
    
    /**
     * Method to redd the alignment file line by line.
     * @throws Exception 
     */
    public void readFileContents() throws Exception {
        ReaderForFile reader = new ReaderForFile(this.myFile);
        reader.readFileContents();
        lines = reader.getLines();
    }

    // added by Amanda
    public void incorporateAncestralSequences(String fastaFile) throws Exception {
        ReadFASTAFile fastaReader = new ReadFASTAFile(fastaFile);
        fastaReader.parseAncestralSequences();
        
        // Add ancestral sequences to our existing sequences
        for (Map.Entry<String, String> entry : fastaReader.map.entrySet()) {
            // Only add nodes that start with 'N' (ancestral nodes)
            if (entry.getKey().startsWith("N")) {
                sequences.put(entry.getKey(), entry.getValue());
            }
        }
        
        // Update sequence count
        numberOfSequences = sequences.size();
    }

    

    
    /**
     * Method to read the first line of the file.
     * @throws java.lang.Exception
     */
    public void readFirstLineAlignment() throws Exception {
        String firstLine = lines.get(0);
        String array[] = firstLine.split("\\s+");
        switch (array.length) {
            case 2 -> {
                numberOfSequences = Integer.parseInt(array[0]);
                numberOfAlignedColumns = Integer.parseInt(array[1]);
            }
            case 3 -> {
                numberOfSequences = Integer.parseInt(array[1]);
                numberOfAlignedColumns = Integer.parseInt(array[2]);
            }
            default -> {
                numberOfSequences = 0; // Exception
                numberOfAlignedColumns = 0; // Exception
                throw new Exception("First line in the alinment file seems to not be in PHYLIP format.");
            }
        }
    }
    
    /**
     * Method to read the MSA depending on the format.
     * @throws java.lang.Exception
     */
    public void solveAlignment() throws Exception {
        String format = "sequential";
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isEmpty()) {
                format = "interleaved";
                break;
            }
        }
        
        if (format.equals("sequential")) {
            
            solveSequential();
            
        } else if (format.equals("interleaved")) {
            
            solveInterleaved();
            
        }
    }
    
    /**
     * Method to compress the alignment info.
     * 
     * columns ArrayList is all of the columns in alignment (a colmn is all characters in every sequence at some position)
     * 
     * @throws java.lang.Exception
     */
    public void compressAlignmentSingle() throws Exception {
                
        for (int i = 0; i < numberOfAlignedColumns; i++) {
            ArrayList<String> columns = new ArrayList();
            HashMap<String, String> columnsPerSpecies;
            columnsPerSpecies = new HashMap();
            Iterator iterator;
            iterator = sequences.keySet().iterator();
            while (iterator.hasNext()) {
                String sequenceKey = iterator.next().toString();
                columns.add(sequences.get(sequenceKey).substring(i, i + 1));
                columnsPerSpecies.put(sequenceKey, sequences.get(sequenceKey).substring(i, i + 1));
                int number = amountOfStates.containsKey(sequences.get(sequenceKey).substring(i, i + 1)) ? amountOfStates.get(sequences.get(sequenceKey).substring(i, i + 1)) : 0;
                amountOfStates.put(sequences.get(sequenceKey).substring(i, i + 1), number + 1);
                
            }
            
            columnsMap.put(columns, columnsPerSpecies);
            String column2string = concatenateArrayList(columns, "");
            columnsPositions.put(i+1, column2string);
            
        }
    }
    
    /**
     * Method to return the map of columns.
     * @return 
     * @throws java.lang.Exception 
     */
    public HashMap<ArrayList, HashMap> getColumnsMap() throws Exception {
        return columnsMap;
    }
    
    /**
     * Method to return the map of positions of the columns.
     * @return 
     */
    public HashMap<Integer, String> getColumnsPosition() {
        return columnsPositions;
    }
    
    /*Private methods*/
    
    /**
     * Method to solve the first line of the sequence independent of the format.
     */
    private String[] solveFirstLineSequence(String firstLine) {
        while (firstLine.endsWith(" ")) {
            firstLine = firstLine.substring(0, firstLine.length() - 1);
        }
        
        int charPosition = firstLine.length();
        for (int i = 0; i < firstLine.length(); i++) {
            if (firstLine.charAt(i) == ' ') {
                charPosition = i;
                break;
            }
        }
        String sequenceName = firstLine.substring(0, charPosition);
        sequenceName = sequenceName.replace(" ", "");
        String onlySequence = firstLine.replace(sequenceName, "");
        onlySequence = onlySequence.replace(" ", "");
        String[] arrayToReturn = {sequenceName, onlySequence};
        
        return arrayToReturn;
    }
    
    /**
     * Method to solve successive lines of the sequence independent of the format.
     */
    private String solveSuccessiveLineSequence(String sequenceName, String successiveLine) {
        if (successiveLine.contains(sequenceName)) {
            successiveLine = successiveLine.replace(sequenceName, "");
        }
        successiveLine = successiveLine.replace(" ", "");
        return successiveLine;
    }
    
    /**
     * Method to solve the MSA in sequential format.
     */
    private void solveSequential() throws Exception {
        
        int i = 0; // counter for the number of sequences
        int j = 1; // counter for the number of lines. First line contains general info
        while (i < numberOfSequences) {
            String sequenceName = null;
            int lengthOfSequence = 0;
            while (lengthOfSequence < numberOfAlignedColumns) {
                if (sequenceName == null) {
                    String[] firstLineArray = solveFirstLineSequence(lines.get(j));
                    sequenceName = firstLineArray[0];
                    sequences.put(sequenceName, firstLineArray[1]);
                } else {
                    String preexistingFragment = sequences.get(sequenceName);
                    String newFragment = solveSuccessiveLineSequence(sequenceName, lines.get(j));
                    sequences.put(sequenceName, preexistingFragment + newFragment);
                    
                }
                lengthOfSequence = sequences.get(sequenceName).toCharArray().length;
                j++;
            }
            if (lengthOfSequence != numberOfAlignedColumns) {
                throw new Exception("Length of sequence " + sequenceName + " does not agree with header: " + lengthOfSequence + " != " + numberOfAlignedColumns);
            }
            i++;
        }
    }
    
    /**
     * Method to solve the MSA in interleaved format.
     */
    private void solveInterleaved() throws Exception {
        String[] arrayOfNames = new String[numberOfSequences];
        for (int i = 1; i <= numberOfSequences; i++) {
            String[] firstLineArray = solveFirstLineSequence(lines.get(i));
            String sequenceName = firstLineArray[0];
            arrayOfNames[i - 1] = sequenceName;
            sequences.put(sequenceName, firstLineArray[1]);
        }
        
        Pattern numericalPattern = Pattern.compile("\\d+\\s*");
        int j = 0;
        for (int i = 1 + numberOfSequences; i < lines.size(); i++) {
            Matcher numericalMatcher = numericalPattern.matcher(lines.get(i));
            if (lines.get(i).length() != 0 && numericalMatcher.matches() == false) {
                String preexistingFragment = sequences.get(arrayOfNames[j]);
                String newFragment = solveSuccessiveLineSequence(arrayOfNames[j], lines.get(i));
                sequences.put(arrayOfNames[j], preexistingFragment + newFragment);
                if (j < numberOfSequences - 1) {
                    j++;
                } else {
                    j = 0;
                }
                
            }
        }
        
        for (String sequenceName : sequences.keySet()) {
            int lengthOfSequence = sequences.get(sequenceName).toCharArray().length;
            if (lengthOfSequence != numberOfAlignedColumns) {
                throw new Exception("Length of sequence " + sequenceName + " does not agree with header: " + lengthOfSequence + " != " + numberOfAlignedColumns);
            }
        }
    }    

    /**
     * Method to concatenate an ArrayList into a string with each element separated with a separator.
     * @param list
     * @param separator
     * @return
     */
    private String concatenateArrayList(ArrayList<String> list, String separator) {
        StringBuilder b = new StringBuilder();
        b.append(list.get(0));
        for (int i = 1; i < list.size(); i++) {
            b.append(separator).append(list.get(i));
        }
        String s = b.toString();
        return s;
    }
}
