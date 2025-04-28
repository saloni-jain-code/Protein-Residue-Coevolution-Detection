/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory_analyses;

import objects.AlignmentObject.Alignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import objects.TreeObject.Node;

/**
 * Class for the patterns used in the first step analysis.
 * @author davidtalavera
 */
public class Pattern {

    ArrayList<Integer> arrayOfPositions = new ArrayList();
    String pattern;
    ArrayList<Integer> nodesBinaryTrajectoryArray;
    ArrayList<Integer> branchesBinaryTrajectoryArray;
    ArrayList<ArrayList> reconstructedTrajectoryArray;
    HashMap<String, Integer> ancestorNodesMap;
    public String nodesBinaryTrajectoryString;
    public String branchesBinaryTrajectoryString;
    public String reconstructedTrajectoryString;
    public String ancestorNodesString;
    
    /*
     * Constructor.
     */
    public Pattern(String s) {
        pattern = s;
    }
    
    /*Public methods*/
    
    /**
     * Method to launch the ancestral reconstruction calculations and get the results.
     * @param myAlignment
     * @param queryNode 
     */
    public void calculatePathway(Alignment myAlignment, Node queryNode) {
        try {
            String[] tmp = pattern.split("");
            ArrayList<String> myKey = new ArrayList();
            myKey.addAll(Arrays.asList(tmp));
            if (myKey.get(0).isEmpty()) {
                myKey.remove(0);
            }
            
            MPchanges myParsimony = new MPchanges(queryNode);
            myParsimony.trackParsimonyChanges(myAlignment.columnsMap.get(myKey));
            
            nodesBinaryTrajectoryArray = myParsimony.getOneColumnBinaryTrajectory_Nodes();
            nodesBinaryTrajectoryString = formatBinaryArrayToString(nodesBinaryTrajectoryArray);
            branchesBinaryTrajectoryArray = myParsimony.getOneColumnBinaryTrajectory_Branches();
            branchesBinaryTrajectoryString = formatBinaryArrayToString(branchesBinaryTrajectoryArray);
            reconstructedTrajectoryArray = myParsimony.getOneColumnPathway();
            reconstructedTrajectoryString = formatStateArrayToString(reconstructedTrajectoryArray);
            ancestorNodesMap = myParsimony.getOneColumnAncestorMaps();
            ancestorNodesString = formatAncesteroMapToString(ancestorNodesMap);
            
        } catch (Exception ex) {
            Logger.getLogger(Pattern.class.getName()).log(Level.SEVERE, null, ex);
        }
            
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
    private String formatBinaryArrayToString(ArrayList array) {
        String s = array.toString();
        s = s.replace(", ", " ");
        s = s.replace("[", "");
        s = s.replace("]", "");
        
        return s;
    }
    
    /**
     * Method to format the value into a string suitable for printing.
     * @param array
     * @return 
     */
    private String formatStateArrayToString(ArrayList array) {
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
    private String formatAncesteroMapToString(HashMap hmap) {
        String s = hmap.toString();
        s = s.replace(", ", "\t");
        s = s.replace("[", "");
        s = s.replace("]", "");
        s = s.replace("{", "");
        s = s.replace("}", "");
        
        return s;
    }
}
