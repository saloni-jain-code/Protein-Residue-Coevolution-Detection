/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import objects.TreeObject;
import objects.TreeObject.Node;

/**
 *
 * @author davidtalavera
 * Class to read the phylogenetic tree in Newick format.
 */
public class ReadNewickTree {

    private final String myFile;
    private ArrayList<String> lines;
    private final TreeObject tree;
    private final HashMap<String, Node> nodesInTree;

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
    }

    /**
     * Method to solve the tree.
     * @throws Exception 
     */
    public void solveTree() throws Exception {
        String treeLine = lines.get(0);
        
        Pattern patternInsideParentheses = Pattern.compile("\\([\\w[,][\\.][\\:][\\#][\\-]]+\\)");

        int nodeNumber = 0;
        do {
            
            Matcher matcherInsideParentheses = patternInsideParentheses.matcher(treeLine);
            
            while (matcherInsideParentheses.find()) {
                
                String stringToReplace = matcherInsideParentheses.group();
                HashMap<String, ArrayList> dataInsideParentheses = readInsideParentheses(matcherInsideParentheses.group());
                ArrayList<String> sequencesInNode = new ArrayList();
                if (dataInsideParentheses.get("node_names").isEmpty() == false) {
                    sequencesInNode = dataInsideParentheses.get("node_names");
                }

                nodeNumber++;
                String nodeMsg = "Node" + nodeNumber;
                
                Pattern patternExtendedOutsideParentheses = Pattern.compile("\\(" + matcherInsideParentheses.group() + "\\)[\\d[\\.]]+");
                Matcher matcherExtendedOutsideParentheses = patternExtendedOutsideParentheses.matcher(treeLine);
                while (matcherExtendedOutsideParentheses.find()) {
                    String valueOfBootstrap = bootstrapExtension(matcherExtendedOutsideParentheses.group());
                    treeLine = treeLine.replaceAll("\\(" + stringToReplace + "\\)" + valueOfBootstrap, nodeMsg);
                }
                treeLine = treeLine.replaceAll("\\(" + stringToReplace + "\\)", nodeMsg);

                if (sequencesInNode.isEmpty()) {
                } else if (sequencesInNode.size() == 1) {
                    setTree(nodeMsg, sequencesInNode.get(0), null);

                } else if (sequencesInNode.size() == 2) {
                    setTree(nodeMsg, sequencesInNode.get(0), sequencesInNode.get(1));

                } else if (sequencesInNode.size() > 2) {
                    setTree(nodeMsg, sequencesInNode.get(0), sequencesInNode.get(1));

                    String virtualNode = nodeMsg;
                    for (int i = 2; i < sequencesInNode.size(); i++) {
                        nodeNumber++;
                        nodeMsg = "Node" + nodeNumber;
                        setTree(nodeMsg, virtualNode, sequencesInNode.get(i));

                        virtualNode = nodeMsg;
                    }
                }
            }
        } while (treeLine.contains(","));

        String root = "Node" + nodeNumber;
        unrootTree(root);

    }

    /**
     * Method to return the map of nodes.
     * @return 
     */
    public HashMap<String, Node> getNodes() {
        return nodesInTree;
    }

    /*Private methods*/
    
    /**
     * Method to read inside the parentheses.
     * @param insideParenthesesMsg
     * @return 
     */
    private HashMap<String, ArrayList> readInsideParentheses(String insideParenthesesMsg) {

        ArrayList<String> arrayOfNodeNames = new ArrayList();
        ArrayList<Integer> arrayOfNodeOrders = new ArrayList();
        HashMap<String, ArrayList> insideParenthesesMap = new HashMap();

        insideParenthesesMsg = insideParenthesesMsg.replace("(", "");
        insideParenthesesMsg = insideParenthesesMsg.replace(")", "");

        String[] arrayOfNodesInsideParentheses = insideParenthesesMsg.split(",");

        for (String arrayOfNodesInsideParenthese : arrayOfNodesInsideParentheses) {
            String nodeName = null;
            Integer nodeOrder = null;
            if (arrayOfNodesInsideParenthese.contains(":")) {
                String[] array1 = arrayOfNodesInsideParenthese.split(":");
                nodeName = array1[0];
                if (array1[1].contains("#")) {
                    String[] array2 = array1[1].split("#");
//                    branchLength = Double.valueOf(array2[0]);
                    nodeOrder = Integer.valueOf(array2[1]);
                } else {
//                    branchLength = Double.valueOf(array1[1]);
                }
            } else if (arrayOfNodesInsideParenthese.contains("#")) {
                String[] array1 = arrayOfNodesInsideParenthese.split("#");
                nodeName = array1[0];
                nodeOrder = Integer.valueOf(array1[1]);
            } else {
                nodeName = arrayOfNodesInsideParenthese;
            }
            if (nodeName != null) {
                arrayOfNodeNames.add(nodeName);
            }
            if (nodeOrder != null) {
                arrayOfNodeOrders.add(nodeOrder);
            }
        }

        if (arrayOfNodeNames.isEmpty() == false) {
            insideParenthesesMap.put("node_names", arrayOfNodeNames);
        }

        if (arrayOfNodeOrders.isEmpty() == false) {
            insideParenthesesMap.put("node_order", arrayOfNodeOrders);
        }

        return insideParenthesesMap;
    }

    /**
     * Method to read the values outside the parentheses.
     * @param extensionOutsideParenthesesMsg
     * @return 
     */
    private String bootstrapExtension(String extensionOutsideParenthesesMsg) {
        String[] array = extensionOutsideParenthesesMsg.split("\\)");
        return array[1];
    }

    /**
     * Method to set the tree.
     * @param node
     * @param node1
     * @param node2 
     */
    private void setTree(String node, String node1, String node2) {
        Node iNode = null;
        Node left = null;
        Node right = null;

        if (node != null) {
            if (nodesInTree.containsKey(node)) {
                iNode = nodesInTree.get(node);
            } else {
                iNode = new Node(node);
                nodesInTree.put(node, iNode);
            }
        }
        if (node1 != null) {
            if (nodesInTree.containsKey(node1)) {
                left = nodesInTree.get(node1);
            } else {
                left = new Node(node1);
                nodesInTree.put(node1, left);
            }
        }
        if (node2 != null) {
            if (nodesInTree.containsKey(node2)) {
                right = nodesInTree.get(node2);
            } else {
                right = new Node(node2);
                nodesInTree.put(node2, right);
            }
        }
        tree.insertNode(iNode, left, right);

    }

    /**
     * Method to unroot the tree.
     * @param value
     * @throws IOException 
     */
    private void unrootTree(String value) throws IOException {
        Node root = nodesInTree.get(value);

        tree.unrootTree(root);
        
    }
}
