/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis_launchers;

import trajectory_analyses.Pattern;
import variables.ProgramVariables;
import utilities.WriterForFile;
import objects.AlignmentObject.Alignment;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import objects.TreeObject.Node;

/**
 * Class to perform the first analysis.
 * @author davidtalavera
 */
public class FirstStepAnalysis {
    
    Alignment msa;
    HashMap<String, Pattern> patternsMap;
    
    /**
     * Constructor.
     * @param msa
     * @param patternsMap
     */
    public FirstStepAnalysis(Alignment msa, HashMap<String, Pattern> patternsMap) {
        this.msa = msa;
        this.patternsMap = patternsMap;
    }
    
    /*Public methods*/
    
    /**
     * Method for calculating the maximum parsimony evolutionary trajectory.
     * @param initialNode
     */
    public void performAnalysis(Node initialNode) {
        
        for (String key : patternsMap.keySet()) {
            Pattern pattern = patternsMap.get(key);
            pattern.calculatePathway(msa, initialNode);
        }
    }
    /**
     * Method for printing the maximum parsimony evolutionary trajectory
     * into 4 different files.
     * @param N
     */
    public void writeResults(int N) {
        
        WriterForFile resultsFile1 = null;
        WriterForFile resultsFile2 = null;
        WriterForFile resultsFile3 = null;
        WriterForFile resultsFile4 = null;
        if (ProgramVariables.InternalVariables.outputFile != null) {
            resultsFile1 = new WriterForFile(ProgramVariables.InternalVariables.outputFile.concat(".binary_nodes.tab"));
            resultsFile2 = new WriterForFile(ProgramVariables.InternalVariables.outputFile.concat(".binary_branches.tab"));
            resultsFile3 = new WriterForFile(ProgramVariables.InternalVariables.outputFile.concat(".all_nodes_states.tab"));
            resultsFile4 = new WriterForFile(ProgramVariables.InternalVariables.outputFile.concat(".ancestor_nodes_states.tab"));
            try {
                resultsFile1.setFile();
                resultsFile2.setFile();
                resultsFile3.setFile();
                resultsFile4.setFile();
            } catch (IOException ex) {
                Logger.getLogger(mpsCoevolutionAnalyser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        for (int i = 1; i <= N; i++) {
            Pattern pattern;
            pattern = patternsMap.get(msa.columnsPositions.get(i));
            String nodesTrajectory = pattern.nodesBinaryTrajectoryString;
            String branchesTrajectory = pattern.branchesBinaryTrajectoryString;
            String statesTrajectory = pattern.reconstructedTrajectoryString;
            String ancestorStates = pattern.ancestorNodesString;
      
            if (ProgramVariables.InternalVariables.outputFile != null) {
                if (resultsFile1 != null) {
                    resultsFile1.writeFileContents(nodesTrajectory);
                }
                if (resultsFile2 != null) {
                    resultsFile2.writeFileContents(branchesTrajectory);
                }
                if (resultsFile3 != null) {
                    resultsFile3.writeFileContents(statesTrajectory);
                }
                if (resultsFile4 != null) {
                    resultsFile4.writeFileContents(ancestorStates);
                }
            }
            else { 
                System.out.println("#1 " + nodesTrajectory);
                System.out.println("#2 " + branchesTrajectory);
                System.out.println("#3 " + statesTrajectory);
                System.out.println("#4 " + ancestorStates);
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
                if (resultsFile3 != null) {
                    resultsFile3.closeFile();
                }
                if (resultsFile4 != null) {
                    resultsFile4.closeFile();
                }

            } catch (IOException ex) {
                Logger.getLogger(mpsCoevolutionAnalyser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
   
    }
}
