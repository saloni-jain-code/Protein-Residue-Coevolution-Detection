/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis_launchers;

import trajectory_analyses.Pattern;
import variables.UserToProgramInterface;
import objects.AlignmentObject;
import objects.AlignmentObject.Alignment;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import variables.ProgramVariables.InternalVariables;
import objects.TreeObject.Node;
import utilities.ReadNewickTree;
import utilities.ReadPhylipAlignment;

/**
 * Main class.
 * @author davidtalavera
 */
public class mpsCoevolutionAnalyser {
    
    /**
     * @param args the command line arguments
     * @throws Exception  
     */
    public static void main(String[] args) throws Exception {   
        // TODO code application logic here


        /*
         * Files or controls
         */

        String controlsFile = args[0];
       
        UserToProgramInterface inputInterface = new UserToProgramInterface();
        try {
            inputInterface.getUDVariablesFile(controlsFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(mpsCoevolutionAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(mpsCoevolutionAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            inputInterface.setIVariables();
        } catch (IOException ex) {
            Logger.getLogger(mpsCoevolutionAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*
         * Block for reading the MSA. It stores the patterns(columns) and their
         * position in the MSA.
         */
        ReadPhylipAlignment msa_Reader = new ReadPhylipAlignment(InternalVariables.msAlignmentFile);
        AlignmentObject msAlignmentConstructor = new AlignmentObject();
        Alignment msAlignment = new Alignment();
        try {
            msAlignmentConstructor.setAlignment(msAlignment, msa_Reader);
        } catch (Exception ex) {
            Logger.getLogger(mpsCoevolutionAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }

       
        HashMap<String, Pattern> patternsInAlignment = new HashMap();
        int numberOfSites = msAlignment.columnsPositions.keySet().size();
        for (int i = 1; i <= numberOfSites; i++) {
            
            Pattern pattern;
            if (patternsInAlignment.containsKey(msAlignment.columnsPositions.get(i))) {
                pattern = patternsInAlignment.get(msAlignment.columnsPositions.get(i));
            } else {
                pattern = new Pattern(msAlignment.columnsPositions.get(i));
                patternsInAlignment.put(msAlignment.columnsPositions.get(i), pattern);
            }
            pattern.setPositions(i);
        }
        
        /*
         * Block for reading the Newick-format tree and create objects
         * containing the info.
         */
        ReadNewickTree myTreeFile2 = new ReadNewickTree(InternalVariables.treeFile);
        myTreeFile2.readFileContents();
        myTreeFile2.solveTree();

        HashMap<String, Node> nodesInNewickTree = myTreeFile2.getNodes();
        Node queryNode = nodesInNewickTree.get("Node1");
        
        /*
         * Block for running one of the two possible analyses.
         * The first analysis generates the binary matrix of changes.
         * The second analysis is used to identify residue combinations.
         */
        if (InternalVariables.pairsFile == null) {
            FirstStepAnalysis firstAnalysis = new FirstStepAnalysis(msAlignment,patternsInAlignment);
            firstAnalysis.performAnalysis(queryNode);
            firstAnalysis.writeResults(numberOfSites);
        }
        else {
            SecondStepAnalysis secondAnalysis = new SecondStepAnalysis(InternalVariables.pairsFile,msAlignment,patternsInAlignment);
            secondAnalysis.performAnalysis(queryNode);
            secondAnalysis.writeResults();
        }
    }
}
