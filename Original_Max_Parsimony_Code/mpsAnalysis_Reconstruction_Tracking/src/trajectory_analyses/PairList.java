/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory_analyses;

import utilities.ReaderForFile;
import objects.AlignmentObject.Alignment;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import objects.TreeObject.Node;
import variables.UserToProgramInterface;

/**
 * Class for the pairs of positions used in the second step analysis
 * @author davidtalavera
 */
public class PairList {
    
    String reconstructedPairsString;
    String ancestorPairsString;
    HashMap<Integer,String[]> pairsOfPositionsMap = new HashMap();
    HashMap<Integer,String[]> pairsOfPatternsMap = new HashMap();
    ArrayList<ArrayList> reconstructedPairsArray;
    HashMap<String, Integer> ancestorPairsMap;
    HashMap<Integer, String> reconstructionResulstMap = new HashMap();
    HashMap<Integer, String> ancestorResulstMap = new HashMap();
    
    /**
     * Constructor.
     */
    public PairList() {
    }

    /*Public methods*/
    
    /**
     * Method to read the pairs from the file.
     * @param file
     * @throws FileNotFoundException
     * @throws Exception 
     */
    public void readPairs(String file) throws FileNotFoundException, Exception {

        ArrayList<String> lines = new ArrayList();
        try {
            ReaderForFile reader = new ReaderForFile(file);
            reader.readFileContents();
            lines = reader.getLines();

        } catch (IOException ex) {
            Logger.getLogger(UserToProgramInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for (int i = 0; i < lines.size(); i++) {
            String[] positions = lines.get(i).split("\\t");
            pairsOfPositionsMap.put(i, positions);
        }
    }
    
    /**
     * Method to set the pairs of positions into a map.
     * @param msAlignment
     * @param patternsMap
     */
    public void setPairs(Alignment msAlignment, HashMap<String, Pattern> patternsMap) {
        for (Integer key : pairsOfPositionsMap.keySet()) {
            int p1 = Integer.parseInt(pairsOfPositionsMap.get(key)[0]);
            int p2 = Integer.parseInt(pairsOfPositionsMap.get(key)[1]);
            
            String pattern1 = msAlignment.columnsPositions.get(p1);
            String pattern2 = msAlignment.columnsPositions.get(p2);
            
            String[] pair;
            pair = new String[2];
            
            pair[0] = pattern1;
            pair[1] = pattern2;
            
            pairsOfPatternsMap.put(key, pair);
        }
    }
    
    /**
     * Method to track the pairs.
     * @param myAlignment
     * @param queryNode
     * @throws Exception 
     */
    public void trackPairs(Alignment myAlignment, Node queryNode) throws Exception {
        Iterator iterator = pairsOfPatternsMap.keySet().iterator();
        while (iterator.hasNext()) {
            Integer key = Integer.valueOf(iterator.next().toString());
            String[] pair = pairsOfPatternsMap.get(key);
            
            String[] tmp;
            
            tmp = pair[0].split("");
            ArrayList<String> pattern1 = new ArrayList();
            pattern1.addAll(Arrays.asList(tmp));
            if (pattern1.get(0).isEmpty()) {
                pattern1.remove(0);
            }
            
            tmp = pair[1].split("");
            ArrayList<String> pattern2 = new ArrayList();
            pattern2.addAll(Arrays.asList(tmp));
            if (pattern2.get(0).isEmpty()) {
                pattern2.remove(0);
            }
            
            MPchanges myParsimony = new MPchanges(queryNode);
            myParsimony.trackParsimonyChanges(myAlignment.columnsMap.get(pattern1),myAlignment.columnsMap.get(pattern2));
            reconstructedPairsArray = myParsimony.getOneColumnPathway();
            reconstructedPairsString = formatStateArrayToString(reconstructedPairsArray);
            ancestorPairsMap = myParsimony.getOneColumnAncestorMaps();
            ancestorPairsString = formatAncesteroMapToString(ancestorPairsMap);
            
            reconstructionResulstMap.put(key, reconstructedPairsString);
            ancestorResulstMap.put(key, ancestorPairsString);      
            
        }
    }
    
    /**
     * Method to return the map of pairs.
     * @return 
     */
    public HashMap getPairsMap() {
        return pairsOfPositionsMap;
    }
    
    /**
     * Method to return the trajectory in pairs of amino acids.
     * @return 
     */
    public HashMap getReconstructionMap() {
        return reconstructionResulstMap;
    }
    
    /**
     * Method to return the map of counts of ancestor combinations.
     * @return 
     */
    public HashMap getAncestorMap() {
        return ancestorResulstMap;
    }
    
    /*Private methods*/
    
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
