package utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import objects.TreeObject;
import objects.TreeObject.TrieNode;

/**
 * A robust Newick tree parser especially designed to handle trees with N1 at the end
 */
public class NewickParser {

    private final String treeFile;
    private final HashMap<String, TrieNode> nodesInTree;
    private final TreeObject tree;
    private int nodeCounter = 0;
    private TrieNode rootNode = null;
    private String newickString = "";

    /**
     * Constructor
     * @param treeFile Path to the Newick tree file
     */
    public NewickParser(String treeFile) {
        this.treeFile = treeFile;
        this.nodesInTree = new HashMap<>();
        this.tree = new TreeObject();
    }

    public NewickParser() {
        newickString = "(A,B,(C,D)E)F;";
        treeFile = null;
        this.nodesInTree = new HashMap<>();
        this.tree = new TreeObject();
    }

    /**
     * Parse the Newick tree file
     * @throws IOException if file cannot be read
     */
    public void parseTree() throws IOException {
        // Read the Newick string from file
        try (BufferedReader reader = new BufferedReader(new FileReader(treeFile))) {
            newickString = reader.readLine();
            System.out.println("Read Newick string from file: " + (newickString.length() > 100 ?
                    newickString.substring(0, 50) + "..." + newickString.substring(newickString.length() - 50) :
                    newickString));
        }

        // Parse the Newick string
        parseNewickString(newickString);
    }

    /**
     * Extract a specific node name using regex
     * @param name The node name to extract
     * @return true if found
     */
    private boolean extractSpecificNode(String name) {
        // Pattern to match node name (can be at the end of the string)
        Pattern pattern = Pattern.compile("\\)" + name + "[\\:;]|\\)" + name + "$");
        Matcher matcher = pattern.matcher(newickString);

        if (matcher.find()) {
            System.out.println("Found " + name + " at position " + matcher.start());
            return true;
        }

        return false;
    }

    /**
     * Parse a Newick format string directly
     * @param newickString the Newick string to parse
     */
    public void parseNewickString(String newickString) {
        System.out.println("Starting to parse Newick string...");

        // Clean up the string
        newickString = newickString.trim();
        if (newickString.endsWith(";")) {
            newickString = newickString.substring(0, newickString.length() - 1);
        }

        // Pre-check for root N1
        boolean hasN1 = extractSpecificNode("N1");
        System.out.println("Detected N1 in tree: " + hasN1);

        // Stack to track nested nodes
        Stack<TrieNode> nodeStack = new Stack<>();
        // Keep track of the last created node
        TrieNode lastCreatedNode = null;

        // StringBuilder for accumulating node labels
        StringBuilder currentLabel = new StringBuilder();
        boolean inLabel = false;
        boolean skipBranchLength = false;

        // Iterate character by character
        for (int i = 0; i < newickString.length(); i++) {
            char c = newickString.charAt(i);

            // Skip branch length values
            if (skipBranchLength) {
                if (c == ',' || c == ')') {
                    skipBranchLength = false;
                    i--; // Back up so we process this delimiter
                }
                continue;
            }

            switch (c) {
                case '(':
                    // Start of a new subtree
                    nodeCounter++;
                    String nodeId = "Node" + nodeCounter;
                    TrieNode newNode = new TrieNode(nodeId);
                    lastCreatedNode = newNode;

                    // If this is the first node, it's our root for now
                    if (rootNode == null) {
                        rootNode = newNode;
                    }

                    // Add to tree
                    nodesInTree.put(nodeId, newNode);
                    nodeStack.push(newNode);
                    break;

                case ')':
                    // End of a subtree
                    if (inLabel) {
                        // Finish the current label if we're in one
                        inLabel = false;

                        if (currentLabel.length() > 0) {
                            // Create leaf node
                            String leafName = currentLabel.toString();
                            TrieNode leafNode = new TrieNode(leafName);
                            nodesInTree.put(leafName, leafNode);
                            lastCreatedNode = leafNode;

                            // Add as child to current parent
                            if (!nodeStack.isEmpty()) {
                                TrieNode parent = nodeStack.peek();
                                parent.children.add(leafNode);
                                // if (parent.left == null) {
                                //     parent.left = leafNode;
                                // } else if (parent.right == null) {
                                //     parent.right = leafNode;
                                // }
                                leafNode.parent = parent;
                                System.out.println("Connected leaf " + leafName + " to " + parent.value);
                            }
                        }

                        currentLabel = new StringBuilder();
                    }

                    // Pop the completed subtree
                    if (!nodeStack.isEmpty()) {
                        TrieNode finishedNode = nodeStack.pop();

                        // Check for node label after closing parenthesis
                        StringBuilder nodeLabel = new StringBuilder();
                        int j = i + 1;
                        while (j < newickString.length() &&
                                newickString.charAt(j) != ',' &&
                                newickString.charAt(j) != ')' &&
                                newickString.charAt(j) != ':' &&
                                newickString.charAt(j) != ';') {
                            nodeLabel.append(newickString.charAt(j));
                            j++;
                        }

                        // If we found a node label
                        if (nodeLabel.length() > 0) {
                            String label = nodeLabel.toString();
                            System.out.println("Found node label: " + label + " for node " + finishedNode.value);

                            // Update node name in map
                            nodesInTree.remove(finishedNode.value);
                            finishedNode.value = label;
                            nodesInTree.put(label, finishedNode);

                            // Skip ahead to after the label
                            i = j - 1;

                            // If this is N1, mark it as root
                            if (label.equals("N1")) {
                                rootNode = finishedNode;
                            }
                        }

                        // Connect to parent if not root
                        if (!nodeStack.isEmpty()) {
                            TrieNode parent = nodeStack.peek();
                            parent.children.add(finishedNode);
                            // if (parent.left == null) {
                            //     parent.left = finishedNode;
                            // } else if (parent.right == null) {
                            //     parent.right = finishedNode;
                            // }
                            finishedNode.parent = parent;
                            System.out.println("Connected " + finishedNode.value + " to " + parent.value);
                        }
                    }
                    break;

                case ',':
                    // End of current node, sibling follows
                    if (inLabel) {
                        // Finish the current label
                        inLabel = false;

                        if (currentLabel.length() > 0) {
                            // Create leaf node
                            String leafName = currentLabel.toString();
                            TrieNode leafNode = new TrieNode(leafName);
                            nodesInTree.put(leafName, leafNode);
                            lastCreatedNode = leafNode;

                            // Add as child to current parent
                            if (!nodeStack.isEmpty()) {
                                TrieNode parent = nodeStack.peek();
                                parent.children.add(leafNode);
                                // if (parent.left == null) {
                                //     parent.left = leafNode;
                                // } else if (parent.right == null) {
                                //     parent.right = leafNode;
                                // }
                                leafNode.parent = parent;
                                System.out.println("Connected leaf " + leafName + " to " + parent.value);
                            }
                        }

                        currentLabel = new StringBuilder();
                    }
                    break;

                case ':':
                    // Branch length - skip it
                    if (inLabel) {
                        // Finish current label
                        inLabel = false;

                        if (currentLabel.length() > 0) {
                            // Create leaf node
                            String leafName = currentLabel.toString();
                            TrieNode leafNode = new TrieNode(leafName);
                            nodesInTree.put(leafName, leafNode);
                            lastCreatedNode = leafNode;

                            // Connect to parent
                            if (!nodeStack.isEmpty()) {
                                TrieNode parent = nodeStack.peek();
                                parent.children.add(leafNode);
                                // if (parent.left == null) {
                                //     parent.left = leafNode;
                                // } else if (parent.right == null) {
                                //     parent.right = leafNode;
                                // }
                                leafNode.parent = parent;
                                System.out.println("Connected leaf " + leafName + " to " + parent.value);
                            }
                        }

                        currentLabel = new StringBuilder();
                    }

                    // Skip branch length value
                    skipBranchLength = true;
                    break;

                default:
                    // Part of node label
                    inLabel = true;
                    currentLabel.append(c);
                    break;
            }
        }

        // Handle final node label if any
        if (inLabel && currentLabel.length() > 0) {
            String finalLabel = currentLabel.toString();
            System.out.println("Found final label: " + finalLabel);

            // This could be the root (N1)
            if (finalLabel.equals("N1")) {
                System.out.println("Setting N1 as root node");
                // Create N1 node if it doesn't exist
                if (!nodesInTree.containsKey("N1")) {
                    TrieNode n1Node = new TrieNode("N1");

                    // If we have a root, connect it to N1
                    if (rootNode != null && !rootNode.value.equals("N1")) {
                       //n1Node.left = rootNode;
                        n1Node.children.add(rootNode);
                        rootNode.parent = n1Node;
                    }

                    nodesInTree.put("N1", n1Node);
                    rootNode = n1Node;
                }
            } else {
                // Otherwise just add as a regular node
                TrieNode finalNode = new TrieNode(finalLabel);
                nodesInTree.put(finalLabel, finalNode);

                // If we have a last node, try to connect this to it
                if (lastCreatedNode != null) {
                    lastCreatedNode.children.add(finalNode);
                    // if (lastCreatedNode.left == null) {
                    //     lastCreatedNode.left = finalNode;
                    // } else if (lastCreatedNode.right == null) {
                    //     lastCreatedNode.right = finalNode;
                    // }
                    finalNode.parent = lastCreatedNode;
                }
            }
        }

        // Special case: if we have an "N1;" at the end that wasn't captured
        if (hasN1 && !nodesInTree.containsKey("N1")) {
            System.out.println("Creating N1 node from end of tree string");
            TrieNode n1Node = new TrieNode("N1");

            // Find the highest existing node and make it N1's child
            TrieNode highestNode = findHighestNode();
            if (highestNode != null) {
                n1Node.children.add(highestNode);
                // n1Node.left = highestNode;
                highestNode.parent = n1Node;
                System.out.println("Connected highest node " + highestNode.value + " to N1");
            }

            nodesInTree.put("N1", n1Node);
            rootNode = n1Node;
        }

        // Validate tree structure
        validateTree();

        // Print some statistics
        System.out.println("Tree parsing complete. Found " + nodesInTree.size() + " nodes.");
        System.out.println("Root node is: " + (rootNode != null ? rootNode.value : "null"));
    }

    /**
     * Find the node that has no parent (highest in hierarchy)
     * @return The highest node in the tree
     */
    private TrieNode findHighestNode() {
        for (TrieNode node : nodesInTree.values()) {
            if (node.parent == null && !node.value.equals("N1")) {
                return node;
            }
        }
        return null;
    }

    /**
     * Validate and fix tree structure
     */
    private void validateTree() {
        // Ensure we have a root node
        if (rootNode == null && !nodesInTree.isEmpty()) {
            System.out.println("No root found, using the first node");
            rootNode = nodesInTree.values().iterator().next();
        }

        // Check for multiple root nodes (nodes with no parent)
        ArrayList<TrieNode> rootNodes = new ArrayList<>();
        for (TrieNode node : nodesInTree.values()) {
            if (node.parent == null) {
                rootNodes.add(node);
            }
        }

        if (rootNodes.size() > 1) {
            System.out.println("Warning: Multiple root nodes found: " + rootNodes.size());

            // Try to find N1
            TrieNode n1 = nodesInTree.get("N1");
            if (n1 != null) {
                rootNode = n1;

                // Connect other roots to N1
                for (TrieNode node : rootNodes) {
                    if (!node.value.equals("N1")) {
                        n1.children.add(node);
                        // if (n1.left == null) {
                        //     n1.left = node;
                        // } else if (n1.right == null) {
                        //     n1.right = node;
                        // }
                        node.parent = n1;
                        System.out.println("Connected root node " + node.value + " to N1");
                    }
                }
            }
        }

        // Print tree structure
        if (rootNode != null) {
            System.out.println("Tree structure:");
            printNode(rootNode, 0);
        }
    }

    /**
     * Print a node and its descendants
     * @param node The node to print
     * @param depth Current depth for indentation
     */
    private void printNode(TrieNode node, int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        sb.append("- ").append(node.value);
        sb.append(" (Children: ").append(node.children.size()).append(")");
        // sb.append(" (Left: ").append(node.left != null ? node.left.value : "null");
        // sb.append(", Right: ").append(node.right != null ? node.right.value : "null");
        sb.append(", Parent: ").append(node.parent != null ? node.parent.value : "null").append(")");
        System.out.println(sb.toString());

        for (TrieNode child : node.children) {
            printNode(child, depth + 1);
        }
        // if (node.left != null) {
        //     printNode(node.left, depth + 1);
        // }

        // if (node.right != null) {
        //     printNode(node.right, depth + 1);
        // }
    }

    /**
     * Get the map of nodes
     * @return The nodes map
     */
    public HashMap<String, TrieNode> getNodes() {
        return nodesInTree;
    }

    /**
     * Get the root node
     * @return The root node
     */
    public TrieNode getRootNode() {
        return rootNode;
    }

    public static void main(String[] args) {
        NewickParser parser = new NewickParser();
        parser.parseNewickString("(A,B,(C,D)E)F;");
        parser.validateTree();
        System.out.println("Tree parsing complete.");
    }
}