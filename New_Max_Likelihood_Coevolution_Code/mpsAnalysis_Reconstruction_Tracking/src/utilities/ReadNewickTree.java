/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.util.ArrayList;
import java.util.HashMap;
import objects.TreeObject;
import objects.TreeObject.TrieNode;

/**
 *
 * @author davidtalavera
 * Class to read the phylogenetic tree in Newick format.
 */
public class ReadNewickTree {

    private final String myFile;
    private ArrayList<String> lines;
    private final TreeObject tree;
    private final HashMap<String, TrieNode> nodesInTree;
    private NewickParser parser;

    /*
     * Constructor.
     */
    public ReadNewickTree(String myFile) {
        this.myFile = myFile;
        lines = new ArrayList();
        tree = new TreeObject();
        nodesInTree = new HashMap();
    }

    /*Public methods*/

    /**
     * Method to read the tree file line by line.
     * @throws Exception
     */
    public void readFileContents() throws Exception {
        ReaderForFile reader = new ReaderForFile(this.myFile);
        reader.readFileContents();
        lines = reader.getLines();

        // Initialize the parser
        parser = new NewickParser(myFile);
    }

    /**
     * Method to solve the tree.
     * @throws Exception
     */
    public void solveTree() throws Exception {
        try {
            // If parser not initialized, do it now
            if (parser == null) {
                parser = new NewickParser(myFile);
            }

            // Parse the tree
            parser.parseTree();

            // Get all nodes from the parser
            nodesInTree.putAll(parser.getNodes());

            System.out.println("Tree solved with " + nodesInTree.size() + " nodes");

            // Debug: print all node relationships
            System.out.println("NODE RELATIONSHIPS:");
            for (String nodeName : nodesInTree.keySet()) {
                TrieNode node = nodesInTree.get(nodeName);
                System.out.println("Node:" + nodeName + " - Children: " + node.children);

            }

            // Note: the tree is already unrooted by the parser if needed
        } catch (Exception e) {
            System.err.println("Failed to solve tree: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Method to return the map of nodes.
     * @return
     */
    public HashMap<String, TrieNode> getNodes() {
        return nodesInTree;
    }
}
