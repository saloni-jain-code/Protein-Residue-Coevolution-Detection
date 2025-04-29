/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis_launchers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import objects.AlignmentObject.Alignment;
import objects.TreeObject.TrieNode;
import trajectory_analyses.PairList;
import trajectory_analyses.Pattern;
import utilities.WriterForFile;
import variables.ProgramVariables;

/**
 * Class to perform the second analysis.
 * @author davidtalavera
 */
public class SecondStepAnalysis {
    
    String file;
    Alignment msa;
    HashMap<String, Pattern> patternsMap;
    PairList listOfPairs;
    
    /**
     * Constructor.
     * @param file
     * @param msa
     * @param patternsMap
     */
    public SecondStepAnalysis(String file, Alignment msa, HashMap<String, Pattern> patternsMap) {
        this.file = file;
        this.msa = msa;
        this.patternsMap = patternsMap;
    }

    // Added by Amanda 
    public void performAnalysisWithProvidedAncestors(TrieNode initialNode) throws Exception {
        listOfPairs = new PairList();
        listOfPairs.readPairs(file);
        listOfPairs.setPairs(msa, patternsMap);
        listOfPairs.trackPairsWithProvidedAncestors(msa, initialNode);
    }
    
    /*Public methods*/
    
    /**
     * Method for calculating the maximum parsimony evolutionary trajectory.
     * @param initialNode
     * @throws java.lang.Exception
     */
    // public void performAnalysis(Node initialNode) throws Exception {

    //     listOfPairs = new PairList();
    //     listOfPairs.readPairs(file);
    //     listOfPairs.setPairs(msa,patternsMap);
    //     listOfPairs.trackPairs(msa,initialNode);
    // }
    
    /**
     * Method for printing the results into 2 different files.
     */
    public void writeResults() {
        
        HashMap <Integer, String[]> pairsMap = listOfPairs.getPairsMap();
        HashMap <Integer, String> resultsMap1 = listOfPairs.getReconstructionMap();
        HashMap <Integer, String> resultsMap2 = listOfPairs.getAncestorMap();
        
        WriterForFile resultsFile1 = null;
        WriterForFile resultsFile2 = null;
        if (ProgramVariables.InternalVariables.outputFile != null) {
            resultsFile1 = new WriterForFile(ProgramVariables.InternalVariables.outputFile.concat(".pairs_all_nodes_states.tab"));
            resultsFile2 = new WriterForFile(ProgramVariables.InternalVariables.outputFile.concat(".pairs_ancestor_nodes_states.tab"));
            try {
                resultsFile1.setFile();
                resultsFile2.setFile();
            } catch (IOException ex) {
                Logger.getLogger(mpsCoevolutionAnalyser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        Iterator iterator = pairsMap.keySet().iterator();
        while (iterator.hasNext()) {
            
            Integer key = Integer.valueOf(iterator.next().toString());
            String statesTrajectory = resultsMap1.get(key);
            String ancestorStates = resultsMap2.get(key);
      
            if (ProgramVariables.InternalVariables.outputFile != null) {
                if (resultsFile1 != null) {
                    resultsFile1.writeFileContents(statesTrajectory);
                }
                if (resultsFile2 != null) {
                    resultsFile2.writeFileContents(ancestorStates);
                }
            }
            else { 
                System.out.println("#1 " + statesTrajectory);
                System.out.println("#2 " + ancestorStates);
            }
        }
        
        if (ProgramVariables.InternalVariables.outputFile != null) {
            try {
                if (resultsFile1 != null) {
                    resultsFile1.closeFile();
                }
                if (resultsFile2 != null) {
                    resultsFile2.closeFile();
                }

            } catch (IOException ex) {
                Logger.getLogger(mpsCoevolutionAnalyser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
}
