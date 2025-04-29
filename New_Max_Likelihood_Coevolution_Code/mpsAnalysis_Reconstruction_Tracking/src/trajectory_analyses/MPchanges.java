/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory_analyses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import objects.TreeObject.Node;
import objects.TreeObject.TrieNode;


/**
 * Class to track the changes in the tree based on a maximum parsimony approach.
 * It calls methods in the MaximumParsimony class.
 * @author davidtalavera
 */
public class MPchanges {

    private final Node myNode;
    private final TrieNode myTrieNode;
    // private Node backtrackNode;

    /**
     * Constructor.
     * @param myNode
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MPchanges(Node myNode) {
        this.myOneColumnAncestorMap = new HashMap();
        this.oneColumnPathway = new ArrayList();
        this.oneColumnBinaryTrajectory_Nodes = new ArrayList();
        this.oneColumnBinaryTrajectory_Branches = new ArrayList();
        this.myNode = myNode;
        this.myTrieNode = null;
    }

    public MPchanges(TrieNode myTrieNode) {
        this.myOneColumnAncestorMap = new HashMap();
        this.oneColumnPathway = new ArrayList();
        this.oneColumnBinaryTrajectory_Nodes = new ArrayList();
        this.oneColumnBinaryTrajectory_Branches = new ArrayList();
        this.myTrieNode = myTrieNode;
        this.myNode = null;
    }

    
    ArrayList<Integer> oneColumnBinaryTrajectory_Nodes;
    ArrayList<Integer> oneColumnBinaryTrajectory_Branches;
    @SuppressWarnings("rawtypes")
    ArrayList<ArrayList> oneColumnPathway;
    HashMap<String, Integer> myOneColumnAncestorMap;
    @SuppressWarnings({ "unchecked", "rawtypes" })
    HashMap<String, ArrayList> myOneColumnNodesMap = new HashMap();
    @SuppressWarnings({ "unchecked", "rawtypes" })
    HashMap<TrieNode, String> listOfOneColumnPostVisitedNodes = new HashMap();

    private int bitForChild(int idx) {
        return 1 << idx;
    }

    /*Public methods*/
    
    /**
     * Method to track changes given a single map (MSA column).
     * @param myLeafsMap
     * @throws java.lang.Exception
     */
    // public void trackParsimonyChanges(HashMap<String, String> myLeafsMap) throws Exception {
    //     oneColumnPathway.clear();
    //     myOneColumnNodesMap.clear();
    //     oneColumnBinaryTrajectory_Nodes.clear();
    //     oneColumnBinaryTrajectory_Branches.clear();
    //     listOfOneColumnPostVisitedNodes.clear();
    //     intialOneColumnParsimony(this.myNode, myLeafsMap);

    //     listOfOneColumnPostVisitedNodes.clear();
    //     parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.parent);
    //     parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.left);
    //     parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.right);
        
    //     listOfOneColumnPostVisitedNodes.clear();
    //     subsequentOneColumnParsimony(this.myNode);
    //     if (myOneColumnAncestorMap.isEmpty()) {
    //         ancestorCounter(this.myNode);
    //     }
    // }
    /**
     * Counts the total number of nodes in the tree starting from the given node.
     * @param node The starting node
     * @return The total count of nodes in the subtree
     */
    private int countNodes(TrieNode node) {
        if (node == null) return 0;
        // create queue to do BFS
        Queue<TrieNode> queue = new LinkedList<>();
        queue.add(node);
        int total_nodes = 0;
        while (!queue.isEmpty()) {
            TrieNode current = queue.poll();
            total_nodes++;
            for (TrieNode child : current.children) {
                queue.add(child);
            }
        }
        return total_nodes;
    }
    // Add to MPchanges.java
    public void useProvidedAncestors(HashMap<String, String> myLeafsMap) throws Exception {
        // Clear previous data
        oneColumnPathway.clear();
        myOneColumnNodesMap.clear();
        oneColumnBinaryTrajectory_Nodes.clear();
        oneColumnBinaryTrajectory_Branches.clear();
        myOneColumnAncestorMap.clear();
        listOfOneColumnPostVisitedNodes.clear();

        System.out.println("Tree has " + countNodes(this.myTrieNode) + " nodes");

        // Create mapping between node names
        // HashMap<String, String> nodeNameMapping = createNodeMapping();

        // Map each node to its state
        for (String nodeName : myLeafsMap.keySet()) {
            ArrayList<String> stateList = new ArrayList<>();
            String state = myLeafsMap.get(nodeName);
            stateList.add(state);

            // Use the correct node name based on mapping
            // String treeNodeName = nodeName;
//            if (nodeName.startsWith("N") && !nodeName.startsWith("Node")) {
//                // Try to find a tree node that corresponds to this FASTA node
//                String mappedName = nodeNameMapping.get(nodeName);
//                if (mappedName != null) {
//                    System.out.println("Using mapped name: " + nodeName + " -> " + mappedName);
//                    treeNodeName = mappedName;
//                }
//            }

            // Store the state
            myOneColumnNodesMap.put(nodeName, stateList);

            // Record ancestral states
            if (nodeName.startsWith("N")) {
                if (myOneColumnAncestorMap.containsKey(stateList.toString())) {
                    int count = myOneColumnAncestorMap.get(stateList.toString());
                    myOneColumnAncestorMap.put(stateList.toString(), count + 1);
                } else {
                    myOneColumnAncestorMap.put(stateList.toString(), 1);
                }
            }
        }

        // Process the tree to generate binary trajectories
        processNodeBinaryData(this.myTrieNode);

        // Debug output
        System.out.println("After processing: Nodes binary size = " +
                oneColumnBinaryTrajectory_Nodes.size() + ", Branches binary size = " +
                oneColumnBinaryTrajectory_Branches.size());

        // Add states to pathway
        oneColumnPathway.addAll(myOneColumnNodesMap.values());
    }

// New method to create proper node name mapping
//     private HashMap<String, String> createNodeMapping() {
//         HashMap<String, String> mapping = new HashMap<>();

//         // Get all tree nodes
//         List<Node> treeNodes = getAllTreeNodes(this.myNode);

//         // Create mapping based on node numbers
//         for (Node node : treeNodes) {
//             if (node.value.startsWith("Node")) {
//                 try {
//                     // Extract number from Node name (e.g., "Node1" -> 1)
//                     int nodeNum = Integer.valueOf(node.value);


//                     // Create mappings for different possible FASTA node formats
//                     mapping.put("N" + nodeNum, node.value);

//                     // Handle 3-digit node numbers in FASTA file
//                     int n900 = 900 + nodeNum;
//                     mapping.put("N" + n900, node.value);

//                     // Other potential mappings
//                     int n100 = 100 + nodeNum;
//                     mapping.put("N" + n100, node.value);

//                     // Map more precisely based on tree structure
//                     int childCount = countChildNodes(node);

// //                    // Try different prefixes based on subtree size
// //                    for (int i = 1; i <= 9; i++) {
// //                        int nPrefix = i * 100 + nodeNum;
// //                        mapping.put("N" + nPrefix, node.value);
// //                    }

//                 } catch (NumberFormatException e) {
//                     // Skip if node name doesn't contain a number
//                 }
//             }
//         }

//         return mapping;
//     }

// Recursively get all nodes in the tree
    // private List<Node> getAllTreeNodes(Node node) {
    //     List<Node> nodes = new ArrayList<>();
    //     if (node == null) return nodes;

    //     nodes.add(node);
    //     if (node.left != null) nodes.addAll(getAllTreeNodes(node.left));
    //     if (node.right != null) nodes.addAll(getAllTreeNodes(node.right));

    //     return nodes;
    // }

// Count child nodes (for more sophisticated mapping)
    // private int countChildNodes(Node node) {
    //     if (node == null) return 0;

    //     int count = 1; // Count self
    //     if (node.left != null) count += countChildNodes(node.left);
    //     if (node.right != null) count += countChildNodes(node.right);

    //     return count;
    // }

// New method to process nodes and generate binary data
    private void processNodeBinaryData(TrieNode node) {
        if (node == null || !myOneColumnNodesMap.containsKey(node.value)) return;

        var parentState = myOneColumnNodesMap.get(node.value);

        int idx = 0;
        for (TrieNode child : node.children) {
            if (!myOneColumnNodesMap.containsKey(child.value)) { idx++; continue; }

            var childState = myOneColumnNodesMap.get(child.value);
            boolean changed = !parentState.equals(childState);

            oneColumnBinaryTrajectory_Nodes.add(changed ? 1 : 0);
            oneColumnBinaryTrajectory_Branches.add(changed ? bitForChild(idx) : 0);

            processNodeBinaryData(child);
            idx++;
        }
        // if (node == null || !myOneColumnNodesMap.containsKey(node.value)) {
        //     return;
        // }

        // // Process left child
        // if (node.left != null && myOneColumnNodesMap.containsKey(node.left.value)) {
        //     ArrayList<String> nodeState = myOneColumnNodesMap.get(node.value);
        //     ArrayList<String> leftState = myOneColumnNodesMap.get(node.left.value);

        //     // Add binary data for state change
        //     boolean hasChange = !nodeState.equals(leftState);
        //     oneColumnBinaryTrajectory_Nodes.add(hasChange ? 1 : 0);
        //     oneColumnBinaryTrajectory_Branches.add(hasChange ? 1 : 0);

        //     // Continue processing
        //     processNodeBinaryData(node.left);
        // }

        // // Process right child
        // if (node.right != null && myOneColumnNodesMap.containsKey(node.right.value)) {
        //     ArrayList<String> nodeState = myOneColumnNodesMap.get(node.value);
        //     ArrayList<String> rightState = myOneColumnNodesMap.get(node.right.value);

        //     // Add binary data for state change
        //     boolean hasChange = !nodeState.equals(rightState);
        //     oneColumnBinaryTrajectory_Nodes.add(hasChange ? 1 : 0);
        //     oneColumnBinaryTrajectory_Branches.add(hasChange ? 2 : 0);

        //     // Continue processing
        //     processNodeBinaryData(node.right);
        // }
    }

    // Traverse tree to record changes
    private void traverseTreeForTrajectory(TrieNode node) {
        if (node == null || listOfOneColumnPostVisitedNodes.containsKey(node)) {
            return;
        }
        listOfOneColumnPostVisitedNodes.put(node, null);

        /* ---- add this node’s state to the pathway if known ---- */
        if (myOneColumnNodesMap.containsKey(node.value)) {
            oneColumnPathway.add(myOneColumnNodesMap.get(node.value));
        }

        /* ---- iterate over every child, not just left/right ---- */
        ArrayList<String> parentState = myOneColumnNodesMap.get(node.value);

        int idx = 0;                         // child index for bit mask
        for (TrieNode child : node.children) {

            if (myOneColumnNodesMap.containsKey(child.value) && parentState != null) {

                ArrayList<String> childState  = myOneColumnNodesMap.get(child.value);
                boolean changed = !parentState.equals(childState);

                /* Nodes: 0/1 flag  */
                oneColumnBinaryTrajectory_Nodes.add(changed ? 1 : 0);

                /* Branches: one bit per child (1,2,4,…)           */
                oneColumnBinaryTrajectory_Branches.add(changed ? bitForChild(idx) : 0);
            }
            traverseTreeForTrajectory(child);
            idx++;
        }

        /* ---- now handle the parent edge (same rule as before) ---- */
        if (node.parent != null &&
            !listOfOneColumnPostVisitedNodes.containsKey(node.parent) &&
            myOneColumnNodesMap.containsKey(node.parent.value) &&
            parentState != null) {

            ArrayList<String> pState = myOneColumnNodesMap.get(node.parent.value);
            boolean changed = !parentState.equals(pState);

            oneColumnBinaryTrajectory_Nodes.add(changed ? 1 : 0);
            oneColumnBinaryTrajectory_Branches.add(changed ? 3 : 0);   // keep “3” for the upward edge
            traverseTreeForTrajectory(node.parent);
        }
        
        // if (node == null || listOfOneColumnPostVisitedNodes.containsKey(node)) {
        //     return;
        // }
        
        // // Mark visited
        // listOfOneColumnPostVisitedNodes.put(node, null);
        
        // // Add to pathway
        // if (myOneColumnNodesMap.containsKey(node.value)) {
        //     oneColumnPathway.add(myOneColumnNodesMap.get(node.value));
        // }
        
        // // Process left child
        // if (node.left != null && myOneColumnNodesMap.containsKey(node.left.value) && 
        //     myOneColumnNodesMap.containsKey(node.value)) {
            
        //     @SuppressWarnings("unchecked")
        //     ArrayList<String> parentState = myOneColumnNodesMap.get(node.value);
        //     @SuppressWarnings("unchecked")
        //     ArrayList<String> childState = myOneColumnNodesMap.get(node.left.value);
        //     boolean hasChange = !parentState.equals(childState);
            
        //     oneColumnBinaryTrajectory_Nodes.add(hasChange ? 1 : 0);
        //     oneColumnBinaryTrajectory_Branches.add(hasChange ? 1 : 0);
        //     traverseTreeForTrajectory(node.left);
        // }
        
        // // Process right child
        // if (node.right != null && myOneColumnNodesMap.containsKey(node.right.value) && 
        //     myOneColumnNodesMap.containsKey(node.value)) {
            
        //     @SuppressWarnings("unchecked")
        //     ArrayList<String> parentState = myOneColumnNodesMap.get(node.value);
        //     @SuppressWarnings("unchecked")
        //     ArrayList<String> childState = myOneColumnNodesMap.get(node.right.value);
        //     boolean hasChange = !parentState.equals(childState);
            
        //     oneColumnBinaryTrajectory_Nodes.add(hasChange ? 1 : 0);
        //     oneColumnBinaryTrajectory_Branches.add(hasChange ? 2 : 0);
        //     traverseTreeForTrajectory(node.right);
        // }
        
        // // Process parent (for non-root nodes)
        // if (node.parent != null && myOneColumnNodesMap.containsKey(node.parent.value) && 
        //     myOneColumnNodesMap.containsKey(node.value) && 
        //     !listOfOneColumnPostVisitedNodes.containsKey(node.parent)) {
            
        //     @SuppressWarnings("unchecked")
        //     ArrayList<String> nodeState = myOneColumnNodesMap.get(node.value);
        //     @SuppressWarnings("unchecked")
        //     ArrayList<String> parentState = myOneColumnNodesMap.get(node.parent.value);
        //     boolean hasChange = !nodeState.equals(parentState);
            
        //     oneColumnBinaryTrajectory_Nodes.add(hasChange ? 1 : 0);
        //     oneColumnBinaryTrajectory_Branches.add(hasChange ? 3 : 0);
        //     traverseTreeForTrajectory(node.parent);
        // }
    }

    // Public method for column analysis with provided ancestors
    public void trackParsimonyChangesWithProvidedAncestors(HashMap<String, String> myLeafsMap) throws Exception {
        useProvidedAncestors(myLeafsMap);
    }

    // Method for pair analysis with provided ancestors
    public void trackParsimonyChangesWithProvidedAncestors(HashMap<String, String> myLeafsMap1, 
                                                        HashMap<String, String> myLeafsMap2) throws Exception {
        oneColumnPathway.clear();
        myOneColumnNodesMap.clear();
        oneColumnBinaryTrajectory_Nodes.clear();
        oneColumnBinaryTrajectory_Branches.clear();
        myOneColumnAncestorMap.clear();
        listOfOneColumnPostVisitedNodes.clear();
        
        // Create pairs of states
        for (String nodeName : myLeafsMap1.keySet()) {
            if (myLeafsMap2.containsKey(nodeName)) {
                ArrayList<String> pairs = new ArrayList<>();
                String pair = myLeafsMap1.get(nodeName).concat(myLeafsMap2.get(nodeName));
                pairs.add(pair);
                myOneColumnNodesMap.put(nodeName, pairs);
                
                // Count ancestor states
                if (nodeName.startsWith("N")) {
                    if (myOneColumnAncestorMap.containsKey(pairs.toString())) {
                        int count = myOneColumnAncestorMap.get(pairs.toString());
                        myOneColumnAncestorMap.put(pairs.toString(), count + 1);
                    } else {
                        myOneColumnAncestorMap.put(pairs.toString(), 1);
                    }
                }
            }
        }
        
        // Build trajectory
        traverseTreeForTrajectory(this.myTrieNode);
    }


    /**
     * Method to track changes given two maps (MSA columns).
     * @param myLeafsMap1
     * @param myLeafsMap2
     * @throws java.lang.Exception
     */
    //@SuppressWarnings({ "unchecked", "rawtypes" })
    // public void trackParsimonyChanges(HashMap<String, String> myLeafsMap1, HashMap<String, String> myLeafsMap2) throws Exception {
        
    //     oneColumnPathway.clear();
    //     myOneColumnNodesMap.clear();
    //     oneColumnBinaryTrajectory_Nodes.clear();
    //     oneColumnBinaryTrajectory_Branches.clear();
    //     listOfOneColumnPostVisitedNodes.clear();
    //     intialOneColumnParsimony(this.myNode, myLeafsMap1);

    //     listOfOneColumnPostVisitedNodes.clear();
    //     parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.parent);
    //     parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.left);
    //     parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.right);
        
    //     @SuppressWarnings({ "unchecked", "rawtypes" })
    //     HashMap<String, ArrayList> OneColumnNodesMap1 = copyMap(myOneColumnNodesMap);
        
    //     oneColumnPathway.clear();
    //     myOneColumnNodesMap.clear();
    //     oneColumnBinaryTrajectory_Nodes.clear();
    //     oneColumnBinaryTrajectory_Branches.clear();
    //     listOfOneColumnPostVisitedNodes.clear();
    //     intialOneColumnParsimony(this.myNode, myLeafsMap2);

    //     listOfOneColumnPostVisitedNodes.clear();
    //     parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.parent);
    //     parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.left);
    //     parsimonyRoot2LeavesFunction(backtrackNode, backtrackNode.right);
        
    //     @SuppressWarnings({ "unchecked", "rawtypes" })
    //     HashMap<String, ArrayList> OneColumnNodesMap2 = copyMap(myOneColumnNodesMap);
        
    //     oneColumnPathway.clear();
    //     myOneColumnNodesMap.clear();
    //     for (String nodes : OneColumnNodesMap1.keySet()) {
    //         ArrayList <String> pairs;
    //         pairs = new ArrayList();
            
    //         @SuppressWarnings("unchecked")
    //         ArrayList <String> p1Array = OneColumnNodesMap1.get(nodes);
    //         @SuppressWarnings("unchecked")
    //         ArrayList <String> p2Array = OneColumnNodesMap2.get(nodes);
            
    //         for (String r1 : p1Array) {
    //             for (String r2 : p2Array) {
    //                 String pair = r1.concat(r2);
    //                 pairs.add(pair);
    //             }
    //         }
            
    //         myOneColumnNodesMap.put(nodes, pairs);
    //         oneColumnPathway.add(pairs);
    //     }
        
    //     listOfOneColumnPostVisitedNodes.clear();
    //     subsequentOneColumnParsimony(this.myNode);
    //     if (myOneColumnAncestorMap.isEmpty()) {
    //         ancestorCounter(this.myNode);
    //     }
    // }
    
    /**
     * Method to return the trajectory in single or pairs of amino acids.
     * @return
     * @throws Exception 
     */
    @SuppressWarnings("rawtypes")
    public ArrayList getOneColumnPathway() throws Exception {
        return oneColumnPathway;
    }
    
    /**
     * Method to return the nodes trajectory in bits
     * @return
     * @throws Exception 
     */
    @SuppressWarnings("rawtypes")
    public ArrayList getOneColumnBinaryTrajectory_Nodes() throws Exception {
        return oneColumnBinaryTrajectory_Nodes;
    }
    
    /**
     * Method to return the branches trajectory in bits
     * @return oneColumnBinaryTrajectory_Branches
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public ArrayList getOneColumnBinaryTrajectory_Branches() throws Exception {
        return oneColumnBinaryTrajectory_Branches;
    }
    
    /**
     * Method to return the ancestral counts per amino acid/pair of amino acids
     * @return
     * @throws Exception 
     */
    @SuppressWarnings("rawtypes")
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
    // private void intialOneColumnParsimony(Node node, HashMap<String, String> myLeafsMap) throws Exception {
    //     if (node != null) {
    //         backtrackNode = node;
    //         if (listOfOneColumnPostVisitedNodes.containsKey(node) == false) {
    //             listOfOneColumnPostVisitedNodes.put(node, null);
    //             intialOneColumnParsimony(node.left, myLeafsMap);
    //             intialOneColumnParsimony(node.right, myLeafsMap);
    //             intialOneColumnParsimony(node.parent, myLeafsMap);
    //             if (node.left != null && node.right != null && node.parent != null) {
    //                 if (myOneColumnNodesMap.containsKey(node.left.value) == true && myOneColumnNodesMap.containsKey(node.right.value) == true && myOneColumnNodesMap.containsKey(node.parent.value) == true) {
    //                     parsimonyLeaves2RootFunction(node, node.left, node.right);
    //                     parsimonyLeaves2RootFunction(node, node, node.parent);
    //                 } else if (myOneColumnNodesMap.containsKey(node.right.value) == true && myOneColumnNodesMap.containsKey(node.parent.value) == true) {
    //                     parsimonyLeaves2RootFunction(node, node.right, node.parent);
    //                 } else if (myOneColumnNodesMap.containsKey(node.left.value) == true && myOneColumnNodesMap.containsKey(node.parent.value) == true) {
    //                     parsimonyLeaves2RootFunction(node, node.left, node.parent);
    //                 } else if (myOneColumnNodesMap.containsKey(node.left.value) && myOneColumnNodesMap.containsKey(node.right.value)) {
    //                     parsimonyLeaves2RootFunction(node, node.left, node.right);
    //                 } else if (myOneColumnNodesMap.containsKey(node.left.value) == false) {
    //                     myOneColumnNodesMap.put(node.value, myOneColumnNodesMap.get(node.right.value));
    //                 } else if (myOneColumnNodesMap.containsKey(node.right.value) == false) {
    //                     myOneColumnNodesMap.put(node.value, myOneColumnNodesMap.get(node.left.value));
    //                 }
    //             } else if (node.left != null && node.right != null) {
    //                 if (myOneColumnNodesMap.containsKey(node.left.value) && myOneColumnNodesMap.containsKey(node.right.value)) {
    //                     parsimonyLeaves2RootFunction(node, node.left, node.right);
    //                 } else if (myOneColumnNodesMap.containsKey(node.left.value) == false) {
    //                     myOneColumnNodesMap.put(node.value, myOneColumnNodesMap.get(node.right.value));
    //                 } else if (myOneColumnNodesMap.containsKey(node.right.value) == false) {
    //                     myOneColumnNodesMap.put(node.value, myOneColumnNodesMap.get(node.left.value));
    //                 }
    //             } else {
    //                 @SuppressWarnings({ "unchecked", "rawtypes" })
    //                 ArrayList<String> myArray = new ArrayList();
    //                 String myState = (String) myLeafsMap.get(node.value);
    //                 myArray.add(myState);
    //                 Collections.sort(myArray);
    //                 myOneColumnNodesMap.put(node.value, myArray);
    //             }
    //         }
    //     }
    // }

    /**
     * Method to finish the reconstruction and to solve the branches
     * @param node
     * @throws Exception 
     */
    // private void subsequentOneColumnParsimony(Node node) throws Exception {
    //     if (node != null) {
    //         if (listOfOneColumnPostVisitedNodes.containsKey(node) == false) {
    //             listOfOneColumnPostVisitedNodes.put(node, null);
    //             subsequentOneColumnParsimony(node.left);
    //             subsequentOneColumnParsimony(node.right);
                
    //             if (node.left != null && node.right != null) {
    //                 parsimonyRecentCommonAncestorFunction(node, node.left, node.right);
    //             }
                
    //             if (node.equals(node.parent.parent) && listOfOneColumnPostVisitedNodes.containsKey(node.parent) == false) {
    //                 parsimonyRecentCommonAncestorFunction(node, node.parent);
    //             }
    
    //             subsequentOneColumnParsimony(node.parent);
    //         }
    //     }
    // }
    
    /**
     * Method to parsimoniously move from the leaves to the root while getting the nodes trajectory in bits.
     */
    // @SuppressWarnings("unchecked")
    // private void parsimonyLeaves2RootFunction(Node node, Node node1, Node node2) throws Exception {
    //     @SuppressWarnings({ "unchecked", "rawtypes" })
    //     ArrayList<String> myArray = new ArrayList();
    //     myArray.addAll(myOneColumnNodesMap.get(node1.value));
    //     myArray.retainAll(myOneColumnNodesMap.get(node2.value));
        
    //     if (myArray.isEmpty()) {
    //         myArray.addAll(myOneColumnNodesMap.get(node1.value));
    //         myArray.addAll(myOneColumnNodesMap.get(node2.value));
    //         oneColumnBinaryTrajectory_Nodes.add(1);
    //     }
    //     else {
    //         oneColumnBinaryTrajectory_Nodes.add(0);
    //     }
        
    //     Collections.sort(myArray);
    //     myOneColumnNodesMap.put(node.value, myArray);
    // }
    
    /**
     * Method to parsimoniously move from the root to the leaves.
     */
    // @SuppressWarnings("unchecked")
    // private void parsimonyRoot2LeavesFunction(Node node1, Node node2) throws Exception {
        
    //     ArrayList<String> myArray1;
    //     myArray1 = new ArrayList<> (myOneColumnNodesMap.get(node1.value));
    //     ArrayList<String> myArray2;
    //     myArray2 = new ArrayList<> (myOneColumnNodesMap.get(node2.value));
        
    //     if(myArray2.equals(myArray1) == false) {
    //         ArrayList<String> myArray_bckup = new ArrayList<> (myArray2);
    //         myArray2.retainAll(myArray1);
    //         if (myArray2.isEmpty()) {
    //             myArray2 = myArray_bckup;
    //         }
    //         myOneColumnNodesMap.put(node2.value, myArray2);
    //     }

    //     oneColumnPathway.add(myArray2);
        
    //     if (node2.left != null) {
    //         parsimonyRoot2LeavesFunction(node2, node2.left);
            
    //     }
    //     if (node2.right != null) {
    //         parsimonyRoot2LeavesFunction(node2, node2.right);
    //     }    
    // }
    
    /**
     * Method to solve branches given that node and two connected nodes.
     * @param node
     * @param node_l
     * @param node_r
     * @throws Exception 
     */
    // @SuppressWarnings("unchecked")
    // private void parsimonyRecentCommonAncestorFunction(Node node, Node node_l, Node node_r) throws Exception {
        
    //     ArrayList<String> myArray;
    //     myArray = new ArrayList<> (myOneColumnNodesMap.get(node.value));
        
    //     int branchID = 0;
         
    //     if (node_l != null) {
    //         ArrayList<String> myArray1;
    //         myArray1 = new ArrayList<> (myOneColumnNodesMap.get(node_l.value));
    //         if (myArray1.equals(myArray) == false) {
    //             branchID = branchID + 1;
    //             ancestorCounter(node_l);
    //         }
    //     }
        
    //     if (node_r != null) {
    //         ArrayList<String> myArray2;
    //         myArray2 = new ArrayList<> (myOneColumnNodesMap.get(node_r.value));
    //         if (myArray2.equals(myArray) == false) {
    //             branchID = branchID + 2;
    //             ancestorCounter(node_r);
    //         }
    //     }
        
    //     oneColumnBinaryTrajectory_Branches.add(branchID);
    // }
    
   /**
    * Method to solve branches given that node and another connected node.
    * @param node
    * @param node_p
    * @throws Exception 
    */
    // @SuppressWarnings("unchecked")
    // private void parsimonyRecentCommonAncestorFunction(Node node, Node node_p) throws Exception {
        
    //     ArrayList<String> myArray;
    //     myArray = new ArrayList<> (myOneColumnNodesMap.get(node.value));
        
    //     int branchID = 0;
        
    //     if (node_p != null) {
    //         ArrayList<String> myArray1;
    //         myArray1 = new ArrayList<> (myOneColumnNodesMap.get(node_p.value));
    //         if (myArray1.equals(myArray) == false) {
    //             branchID = branchID + 3;
    //             ancestorCounter(node_p);
    //         }
    //     }
        
    //     oneColumnBinaryTrajectory_Branches.add(branchID);
    // }
    
    /**
     * Method to count the ancestor nodes.
     * @param node
     * @throws Exception 
     */
    // @SuppressWarnings("unchecked")
    // private void ancestorCounter(Node node) throws Exception {
        
    //     ArrayList<String> myArray;
    //     myArray = new ArrayList<> (myOneColumnNodesMap.get(node.value));
        
    //     if (myArray.size() == 1) {
    //         if(myOneColumnAncestorMap.containsKey(myArray.toString())) { 
    //             int i = myOneColumnAncestorMap.get(myArray.toString());
    //             i ++;
    //             myOneColumnAncestorMap.put(myArray.toString(), i);
                
    //         } else {
    //             myOneColumnAncestorMap.put(myArray.toString(), 1);
    //         }
            
    //     }
    // }
    
    /**
     * Method to make a copy of a map.
     * @param oldMap
     * @return 
     */
    // @SuppressWarnings({ "unchecked", "rawtypes" })
    // private HashMap copyMap(@SuppressWarnings("rawtypes") HashMap oldMap) {
    //     @SuppressWarnings("rawtypes")
    //     HashMap newMap;
    //     newMap = new HashMap();
        
    //     @SuppressWarnings("rawtypes")
    //     Iterator iterator = oldMap.keySet().iterator();
    //     while (iterator.hasNext()) {
    //         Object key = iterator.next();
    //         Object value = oldMap.get(key);
    //         newMap.put(key, value);
    //     }
    //     return newMap;
    // }
 
}
