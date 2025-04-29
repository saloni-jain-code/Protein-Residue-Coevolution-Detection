/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory_analyses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import objects.AlignmentObject.Alignment;
import objects.TreeObject.TrieNode;

/**
 * Class for the patterns used in the first step analysis.
 * @author davidtalavera
 */
public class Pattern {

    @SuppressWarnings("rawtypes")
    ArrayList<Integer> arrayOfPositions = new ArrayList();
    String pattern;
    ArrayList<Integer> nodesBinaryTrajectoryArray;
    ArrayList<Integer> branchesBinaryTrajectoryArray;
    @SuppressWarnings("rawtypes")
    ArrayList<ArrayList> reconstructedTrajectoryArray;
    HashMap<String, Integer> ancestorNodesMap;
    public String nodesBinaryTrajectoryString;
    public String branchesBinaryTrajectoryString;
    public String reconstructedTrajectoryString;
    public String ancestorNodesString;
    
    /*
     * Constructor.
     */
    /**
     * Constructor.
     */
    public Pattern(String s) {
        pattern = s;
        // Initialize all other fields to prevent NullPointerExceptions
        arrayOfPositions = new ArrayList<>();
        nodesBinaryTrajectoryArray = new ArrayList<>();
        branchesBinaryTrajectoryArray = new ArrayList<>();
        reconstructedTrajectoryArray = new ArrayList<>();
        ancestorNodesMap = new HashMap<>();
        nodesBinaryTrajectoryString = "";
        branchesBinaryTrajectoryString = "";
        reconstructedTrajectoryString = "";
        ancestorNodesString = "";
    }
    
    /*Public methods*/
    
    /**
     * Method to launch the ancestral reconstruction calculations and get the results.
     * @param myAlignment
     * @param queryNode 
     */
    // public void calculatePathway(Alignment myAlignment, Node queryNode) {
    //     try {
    //         String[] tmp = pattern.split("");
    //         @SuppressWarnings("rawtypes")
    //         ArrayList<String> myKey = new ArrayList();
    //         myKey.addAll(Arrays.asList(tmp));
    //         if (myKey.get(0).isEmpty()) {
    //             myKey.remove(0);
    //         }
            
    //         MPchanges myParsimony = new MPchanges(queryNode);
    //         myParsimony.trackParsimonyChanges(myAlignment.columnsMap.get(myKey));
            
    //         nodesBinaryTrajectoryArray = myParsimony.getOneColumnBinaryTrajectory_Nodes();
    //         nodesBinaryTrajectoryString = formatBinaryArrayToString(nodesBinaryTrajectoryArray);
    //         branchesBinaryTrajectoryArray = myParsimony.getOneColumnBinaryTrajectory_Branches();
    //         branchesBinaryTrajectoryString = formatBinaryArrayToString(branchesBinaryTrajectoryArray);
    //         reconstructedTrajectoryArray = myParsimony.getOneColumnPathway();
    //         reconstructedTrajectoryString = formatStateArrayToString(reconstructedTrajectoryArray);
            
    //         // needs to be changed to get the ancestor nodes from FASTA file 
    //         ancestorNodesMap = myParsimony.getOneColumnAncestorMaps();
    //         ancestorNodesString = formatAncesteroMapToString(ancestorNodesMap);
            
    //     } catch (Exception ex) {
    //         Logger.getLogger(Pattern.class.getName()).log(Level.SEVERE, null, ex);
    //     }
            
    // }

    // Find the calculatePathwayWithProvidedAncestors method and modify it:
    public void calculatePathwayWithProvidedAncestors(Alignment myAlignment, TrieNode queryNode) {
        try {
            String[] tmp = pattern.split("");
            ArrayList<String> myKey = new ArrayList<>();
            myKey.addAll(Arrays.asList(tmp));
            if (myKey.get(0).isEmpty()) {
                myKey.remove(0);
            }

            // Get the column map
            HashMap<String, String> columnMap = new HashMap<>();

            // CRITICAL FIX: Convert from complex map to simple String->String map
            HashMap<String, HashMap> originalMap = myAlignment.columnsMap.get(myKey);
            if (originalMap != null) {
                for (String key : originalMap.keySet()) {
                    // Extract the actual character state for each node
                    Object value = originalMap.get(key);
                    if (value instanceof String) {
                        columnMap.put(key, (String)value);
                    } else if (value instanceof HashMap) {
                        // If it's a nested map, get the first value
                        HashMap valueMap = (HashMap)value;
                        if (!valueMap.isEmpty()) {
                            Object firstValue = valueMap.values().iterator().next();
                            columnMap.put(key, firstValue.toString());
                        }
                    }
                }
            }

            System.out.println("Column map contains " + columnMap.size() + " entries");

            // Create the parsimony object with the properly formatted map
            MPchanges myParsimony = new MPchanges(queryNode);
            myParsimony.trackParsimonyChangesWithProvidedAncestors(columnMap);

            // Get results
            nodesBinaryTrajectoryArray = myParsimony.getOneColumnBinaryTrajectory_Nodes();
            branchesBinaryTrajectoryArray = myParsimony.getOneColumnBinaryTrajectory_Branches();
            reconstructedTrajectoryArray = myParsimony.getOneColumnPathway();
            ancestorNodesMap = myParsimony.getOneColumnAncestorMaps();

            // Format results to strings
            nodesBinaryTrajectoryString = formatBinaryArrayToString(nodesBinaryTrajectoryArray);
            branchesBinaryTrajectoryString = formatBinaryArrayToString(branchesBinaryTrajectoryArray);
            reconstructedTrajectoryString = formatStateArrayToString(reconstructedTrajectoryArray);
            ancestorNodesString = formatAncesteroMapToString(ancestorNodesMap);

            // DEBUG PRINT STATEMENTS:
            System.out.println("Column map contains " + myAlignment.columnsMap.get(myKey).size() + " entries");
            System.out.println("Reconstructed trajectory: " + reconstructedTrajectoryString);
            System.out.println("Ancestor nodes map: " + ancestorNodesString);
            System.out.println("Nodes binary trajectory: " + nodesBinaryTrajectoryString);
            System.out.println("Branches binary trajectory: " + branchesBinaryTrajectoryString);
            // print the numbr of rows and columns in the binary matrix
            System.out.println("Nodes Binary Array size: " + nodesBinaryTrajectoryArray.size());
            System.out.println("Branches Binary Array size: " + branchesBinaryTrajectoryArray.size());            
            

        } catch (Exception ex) {
            System.err.println("Error in calculatePathwayWithProvidedAncestors: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Replace the existing formatBinaryArrayToString method with this improved version:
    private String formatBinaryArrayToString(ArrayList<?> array) {
        if (array == null || array.isEmpty()) {
            return "0"; // Default value to prevent empty files
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(array.get(i));
        }
        return sb.toString();
    }

    public String getPattern() {
        return pattern;
    }
    /**
     * Method to set the position to reconstruct.
     * @param p 
     */
    public void setPositions(int p) {
        arrayOfPositions.add(p);
    }

    /*Private methods*/
    
    /**
     * Method to format the value into a string suitable for printing.
     * @param array
     * @return 
     */

    
    /**
     * Method to format the value into a string suitable for printing.
     * @param array
     * @return 
     */
    private String formatStateArrayToString(@SuppressWarnings("rawtypes") ArrayList array) {
        String s = array.toString();
        s = s.replace("], [", "\t");
        s = s.replace(", ", "|");
        s = s.replace("[", "");
        s = s.replace("]", "");
        
        return s;
    }
    
    /**
     * Method to format the value into a string suitable for printing.
     * @param hmap
     * @return 
     */
    private String formatAncesteroMapToString(@SuppressWarnings("rawtypes") HashMap hmap) {
        String s = hmap.toString();
        s = s.replace(", ", "\t");
        s = s.replace("[", "");
        s = s.replace("]", "");
        s = s.replace("{", "");
        s = s.replace("}", "");
        
        return s;
    }
}
