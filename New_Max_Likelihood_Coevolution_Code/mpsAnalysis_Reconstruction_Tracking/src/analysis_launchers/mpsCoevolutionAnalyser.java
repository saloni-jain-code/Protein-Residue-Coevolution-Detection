/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis_launchers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import objects.AlignmentObject;
import objects.AlignmentObject.Alignment;
import objects.TreeObject.Node;
import objects.TreeObject.TrieNode;
import trajectory_analyses.Pattern;
import utilities.ReadFASTAFile;
import utilities.ReadNewickTree;
import utilities.ReadPhylipAlignment;
import variables.ProgramVariables.InternalVariables;
import variables.UserToProgramInterface;

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
            msa_Reader.readFileContents();
            msa_Reader.readFirstLineAlignment();
            msa_Reader.solveAlignment();

            // Read ancestral sequences if provided
            if (InternalVariables.ancestralFile != null) {
                ReadFASTAFile fastaReader = new ReadFASTAFile(InternalVariables.ancestralFile);
                fastaReader.parseAncestralSequences();
            
                // Add ancestral sequences to the alignment
                for (String node : fastaReader.map.keySet()) {
                    if (node.startsWith("N")) {
                        msa_Reader.sequences.put(node, fastaReader.map.get(node));
                    }
                }
                InternalVariables.usingProvidedAncestors = true;
            }
            msa_Reader.compressAlignmentSingle();
            msAlignmentConstructor.setAlignment(msAlignment, msa_Reader);
            // print msa_Reader.sequences
            System.out.println("msa_Reader.sequences: " + msa_Reader.sequences);

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

        HashMap<String, TrieNode> nodesInNewickTree = myTreeFile2.getNodes();
        TrieNode queryNode = nodesInNewickTree.get("N1");
        
        /*
         * Block for running one of the two possible analyses.
         * The first analysis generates the binary matrix of changes.
         * The second analysis is used to identify residue combinations.
         */
        if (InternalVariables.pairsFile == null) {
            FirstStepAnalysis firstAnalysis = new FirstStepAnalysis(msAlignment,patternsInAlignment);
            if (InternalVariables.usingProvidedAncestors) {
                // need to implement this new method
                firstAnalysis.performAnalysisWithProvidedAncestors(queryNode);
            }
            // } else {
            //     firstAnalysis.performAnalysis(queryNode);
            // }
            firstAnalysis.writeResults(numberOfSites);
        }
        else {
            SecondStepAnalysis secondAnalysis = new SecondStepAnalysis(InternalVariables.pairsFile,msAlignment,patternsInAlignment);
            if (InternalVariables.usingProvidedAncestors) {
                // need to implement this new method
                secondAnalysis.performAnalysisWithProvidedAncestors(queryNode);
            }
            // } else {
            //     secondAnalysis.performAnalysis(queryNode);
            // }
            secondAnalysis.writeResults();
        }
    }
}
