/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory_analyses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import objects.TreeObject.Node;

/**
 * Class to track the changes in the tree based on a maximum parsimony approach.
 * It calls methods in the MaximumParsimony class.
 * @author davidtalavera
 */
public class MPchanges {

    private final Node myNode;
    private Node backtrackNode;

    /**
     * Constructor.
     * @param myNode
     */
    public MPchanges(Node myNode) {
        this.myOneColumnAncestorMap = new HashMap();
        this.oneColumnPathway = new ArrayList();
        this.oneColumnBinaryTrajectory_Nodes = new ArrayList();
        this.oneColumnBinaryTrajectory_Branches = new ArrayList();
        this.myNode = myNode;
    }
    
    ArrayList<Integer> oneColumnBinaryTrajectory_Nodes;
    ArrayList<Integer> oneColumnBinaryTrajectory_Branches;
    ArrayList<ArrayList> oneColumnPathway;
    HashMap<String, Integer> myOneColumnAncestorMap;
    HashMap<String, ArrayList> myOneColumnNodesMap = new HashMap();
    HashMap<Node, String> listOfOneColumnPostVisitedNodes = new HashMap();

    /*Public methods*/
    
    /**
     * Method to track changes given a single map (MSA column).
     * @param myLeafsMap
     * @throws java.lang.Exception
     */
    public void trackParsimonyChanges(HashMap<String, String> myLeafsMap) throws Exception {
        oneColumnPathway.clear();
        myOneColumnNodesMap.clear();
        oneColumnBinaryTrajectory_Nodes.clear();
        oneColumnBinaryTrajectory_Branches.clear();
        listOfOneColumnPostVisitedNodes.clear();
        intialOneColumnParsimony(this.myNode, myLeafsMap);

        listOfOneColumnPostVisitedNodes.clear();
        parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.parent);
        parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.left);
        parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.right);
        
        listOfOneColumnPostVisitedNodes.clear();
        subsequentOneColumnParsimony(this.myNode);
        if (myOneColumnAncestorMap.isEmpty()) {
            ancestorCounter(this.myNode);
        }
    }
    
    /**
     * Method to track changes given two maps (MSA columns).
     * @param myLeafsMap1
     * @param myLeafsMap2
     * @throws java.lang.Exception
     */
    public void trackParsimonyChanges(HashMap<String, String> myLeafsMap1, HashMap<String, String> myLeafsMap2) throws Exception {
        
        oneColumnPathway.clear();
        myOneColumnNodesMap.clear();
        oneColumnBinaryTrajectory_Nodes.clear();
        oneColumnBinaryTrajectory_Branches.clear();
        listOfOneColumnPostVisitedNodes.clear();
        intialOneColumnParsimony(this.myNode, myLeafsMap1);

        listOfOneColumnPostVisitedNodes.clear();
        parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.parent);
        parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.left);
        parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.right);
        
        HashMap<String, ArrayList> OneColumnNodesMap1 = copyMap(myOneColumnNodesMap);
        
        oneColumnPathway.clear();
        myOneColumnNodesMap.clear();
        oneColumnBinaryTrajectory_Nodes.clear();
        oneColumnBinaryTrajectory_Branches.clear();
        listOfOneColumnPostVisitedNodes.clear();
        intialOneColumnParsimony(this.myNode, myLeafsMap2);

        listOfOneColumnPostVisitedNodes.clear();
        parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.parent);
        parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.left);
        parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.right);
        
        HashMap<String, ArrayList> OneColumnNodesMap2 = copyMap(myOneColumnNodesMap);
        
        oneColumnPathway.clear();
        myOneColumnNodesMap.clear();
        for (String nodes : OneColumnNodesMap1.keySet()) {
            ArrayList <String> pairs;
            pairs = new ArrayList();
            
            ArrayList <String> p1Array = OneColumnNodesMap1.get(nodes);
            ArrayList <String> p2Array = OneColumnNodesMap2.get(nodes);
            
            for (String r1 : p1Array) {
                for (String r2 : p2Array) {
                    String pair = r1.concat(r2);
                    pairs.add(pair);
                }
            }
            
            myOneColumnNodesMap.put(nodes, pairs);
            oneColumnPathway.add(pairs);
        }
        
        listOfOneColumnPostVisitedNodes.clear();
        subsequentOneColumnParsimony(this.myNode);
        if (myOneColumnAncestorMap.isEmpty()) {
            ancestorCounter(this.myNode);
        }
    }
    
    /**
     * Method to return the trajectory in single or pairs of amino acids.
     * @return
     * @throws Exception 
     */
    public ArrayList getOneColumnPathway() throws Exception {
        return oneColumnPathway;
    }
    
    /**
     * Method to return the nodes trajectory in bits
     * @return
     * @throws Exception 
     */
    public ArrayList getOneColumnBinaryTrajectory_Nodes() throws Exception {
        return oneColumnBinaryTrajectory_Nodes;
    }
    
    /**
     * Method to return the branches trajectory in bits
     * @return oneColumnBinaryTrajectory_Branches
     * @throws Exception
     */
    public ArrayList getOneColumnBinaryTrajectory_Branches() throws Exception {
        return oneColumnBinaryTrajectory_Branches;
    }
    
    /**
     * Method to return the ancestral counts per amino acid/pair of amino acids
     * @return
     * @throws Exception 
     */
    public HashMap getOneColumnAncestorMaps() throws Exception {
        return myOneColumnAncestorMap;
    }
    
    /*Private methods*/
    
    /**
     * Method to start the parsimonious ancestral reconstruction from the leaves to the root
     * @param node
     * @param myLeafsMap
     * @throws Exception 
     */
    private void intialOneColumnParsimony(Node node, HashMap<String, String> myLeafsMap) throws Exception {
        if (node != null) {
            backtrackNode = node;
            if (listOfOneColumnPostVisitedNodes.containsKey(node) == false) {
                listOfOneColumnPostVisitedNodes.put(node, null);
                intialOneColumnParsimony(node.left, myLeafsMap);
                intialOneColumnParsimony(node.right, myLeafsMap);
                intialOneColumnParsimony(node.parent, myLeafsMap);
                if (node.left != null && node.right != null && node.parent != null) {
                    if (myOneColumnNodesMap.containsKey(node.left.value) == true && myOneColumnNodesMap.containsKey(node.right.value) == true && myOneColumnNodesMap.containsKey(node.parent.value) == true) {
                        parsimonyLeaves2RootFunction(node, node.left, node.right);
                        parsimonyLeaves2RootFunction(node, node, node.parent);
                    } else if (myOneColumnNodesMap.containsKey(node.right.value) == true && myOneColumnNodesMap.containsKey(node.parent.value) == true) {
                        parsimonyLeaves2RootFunction(node, node.right, node.parent);
                    } else if (myOneColumnNodesMap.containsKey(node.left.value) == true && myOneColumnNodesMap.containsKey(node.parent.value) == true) {
                        parsimonyLeaves2RootFunction(node, node.left, node.parent);
                    } else if (myOneColumnNodesMap.containsKey(node.left.value) && myOneColumnNodesMap.containsKey(node.right.value)) {
                        parsimonyLeaves2RootFunction(node, node.left, node.right);
                    } else if (myOneColumnNodesMap.containsKey(node.left.value) == false) {
                        myOneColumnNodesMap.put(node.value, myOneColumnNodesMap.get(node.right.value));
                    } else if (myOneColumnNodesMap.containsKey(node.right.value) == false) {
                        myOneColumnNodesMap.put(node.value, myOneColumnNodesMap.get(node.left.value));
                    }
                } else if (node.left != null && node.right != null) {
                    if (myOneColumnNodesMap.containsKey(node.left.value) && myOneColumnNodesMap.containsKey(node.right.value)) {
                        parsimonyLeaves2RootFunction(node, node.left, node.right);
                    } else if (myOneColumnNodesMap.containsKey(node.left.value) == false) {
                        myOneColumnNodesMap.put(node.value, myOneColumnNodesMap.get(node.right.value));
                    } else if (myOneColumnNodesMap.containsKey(node.right.value) == false) {
                        myOneColumnNodesMap.put(node.value, myOneColumnNodesMap.get(node.left.value));
                    }
                } else {
                    ArrayList<String> myArray = new ArrayList();
                    String myState = (String) myLeafsMap.get(node.value);
                    myArray.add(myState);
                    Collections.sort(myArray);
                    myOneColumnNodesMap.put(node.value, myArray);
                }
            }
        }
    }

    /**
     * Method to finish the reconstruction and to solve the branches
     * @param node
     * @throws Exception 
     */
    private void subsequentOneColumnParsimony(Node node) throws Exception {
        if (node != null) {
            if (listOfOneColumnPostVisitedNodes.containsKey(node) == false) {
                listOfOneColumnPostVisitedNodes.put(node, null);
                subsequentOneColumnParsimony(node.left);
                subsequentOneColumnParsimony(node.right);
                
                if (node.left != null && node.right != null) {
                    parsimonyRecentCommonAncestorFunction(node, node.left, node.right);
                }
                
                if (node.equals(node.parent.parent) && listOfOneColumnPostVisitedNodes.containsKey(node.parent) == false) {
                    parsimonyRecentCommonAncestorFunction(node, node.parent);
                }
    
                subsequentOneColumnParsimony(node.parent);
            }
        }
    }
    
    /**
     * Method to parsimoniously move from the leaves to the root while getting the nodes trajectory in bits.
     */
    private void parsimonyLeaves2RootFunction(Node node, Node node1, Node node2) throws Exception {
        ArrayList<String> myArray = new ArrayList();
        myArray.addAll(myOneColumnNodesMap.get(node1.value));
        myArray.retainAll(myOneColumnNodesMap.get(node2.value));
        
        if (myArray.isEmpty()) {
            myArray.addAll(myOneColumnNodesMap.get(node1.value));
            myArray.addAll(myOneColumnNodesMap.get(node2.value));
            oneColumnBinaryTrajectory_Nodes.add(1);
        }
        else {
            oneColumnBinaryTrajectory_Nodes.add(0);
        }
        
        Collections.sort(myArray);
        myOneColumnNodesMap.put(node.value, myArray);
    }
    
    /**
     * Method to parsimoniously move from the root to the leaves.
     */
    private void parsimonyRoot2LeavesFunction(Node node1, Node node2) throws Exception {
        
        ArrayList<String> myArray1;
        myArray1 = new ArrayList<> (myOneColumnNodesMap.get(node1.value));
        ArrayList<String> myArray2;
        myArray2 = new ArrayList<> (myOneColumnNodesMap.get(node2.value));
        
        if(myArray2.equals(myArray1) == false) {
            ArrayList<String> myArray_bckup = new ArrayList<> (myArray2);
            myArray2.retainAll(myArray1);
            if (myArray2.isEmpty()) {
                myArray2 = myArray_bckup;
            }
            myOneColumnNodesMap.put(node2.value, myArray2);
        }

        oneColumnPathway.add(myArray2);
        
        if (node2.left != null) {
            parsimonyRoot2LeavesFunction(node2, node2.left);
            
        }
        if (node2.right != null) {
            parsimonyRoot2LeavesFunction(node2, node2.right);
        }    
    }
    
    /**
     * Method to solve branches given that node and two connected nodes.
     * @param node
     * @param node_l
     * @param node_r
     * @throws Exception 
     */
    private void parsimonyRecentCommonAncestorFunction(Node node, Node node_l, Node node_r) throws Exception {
        
        ArrayList<String> myArray;
        myArray = new ArrayList<> (myOneColumnNodesMap.get(node.value));
        
        int branchID = 0;
         
        if (node_l != null) {
            ArrayList<String> myArray1;
            myArray1 = new ArrayList<> (myOneColumnNodesMap.get(node_l.value));
            if (myArray1.equals(myArray) == false) {
                branchID = branchID + 1;
                ancestorCounter(node_l);
            }
        }
        
        if (node_r != null) {
            ArrayList<String> myArray2;
            myArray2 = new ArrayList<> (myOneColumnNodesMap.get(node_r.value));
            if (myArray2.equals(myArray) == false) {
                branchID = branchID + 2;
                ancestorCounter(node_r);
            }
        }
        
        oneColumnBinaryTrajectory_Branches.add(branchID);
    }
    
   /**
    * Method to solve branches given that node and another connected node.
    * @param node
    * @param node_p
    * @throws Exception 
    */
    private void parsimonyRecentCommonAncestorFunction(Node node, Node node_p) throws Exception {
        
        ArrayList<String> myArray;
        myArray = new ArrayList<> (myOneColumnNodesMap.get(node.value));
        
        int branchID = 0;
        
        if (node_p != null) {
            ArrayList<String> myArray1;
            myArray1 = new ArrayList<> (myOneColumnNodesMap.get(node_p.value));
            if (myArray1.equals(myArray) == false) {
                branchID = branchID + 3;
                ancestorCounter(node_p);
            }
        }
        
        oneColumnBinaryTrajectory_Branches.add(branchID);
    }
    
    /**
     * Method to count the ancestor nodes.
     * @param node
     * @throws Exception 
     */
    private void ancestorCounter(Node node) throws Exception {
        
        ArrayList<String> myArray;
        myArray = new ArrayList<> (myOneColumnNodesMap.get(node.value));
        
        if (myArray.size() == 1) {
            if(myOneColumnAncestorMap.containsKey(myArray.toString())) { 
                int i = myOneColumnAncestorMap.get(myArray.toString());
                i ++;
                myOneColumnAncestorMap.put(myArray.toString(), i);
                
            } else {
                myOneColumnAncestorMap.put(myArray.toString(), 1);
            }
            
        }
    }
    
    /**
     * Method to make a copy of a map.
     * @param oldMap
     * @return 
     */
    private HashMap copyMap(HashMap oldMap) {
        HashMap newMap;
        newMap = new HashMap();
        
        Iterator iterator = oldMap.keySet().iterator();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            Object value = oldMap.get(key);
            newMap.put(key, value);
        }
        return newMap;
    }
 
}
