/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Class for setting the tree object.
 * @author davidtalavera
 */
public class TreeObject {

    public static class Node {

        public Node left;
        public Node right;
        public Node parent;
        public String value;

        /**
         * Constructor.
         * @param value
         */
        public Node(String value) {
            this.value = value;
        }
    }

    public static class TrieNode {

        public final ArrayList<TrieNode> children = new ArrayList<>();
        public TrieNode parent;
        public String value;

        /**
         * Constructor.
         * @param value
         */
        public TrieNode(String value) {
            this.value = value;
        }
    }

    /*Public methods*/
    
    /**
     * Method to insert nodes in the tree.
     * @param parent
     * @param lChild
     * @param rChild 
     */
    public void insertNode(Node parent, Node lChild, Node rChild) {
        parent.left = lChild;
        lChild.parent = parent;
        parent.right = rChild;
        rChild.parent = parent;
    }

    /**
     * Method to delete nodes from a tree.
     * @param node 
     */
    public void deleteNode(Node node) {
        if (node.left.value != null) {
            node.left.parent = null;
            node.left = null;
        }
        if (node.right.value != null) {
            node.right.parent = null;
            node.right = null;
        }
        if (node.parent != null) {
            if (node.parent.left.equals(node)) {
                node.parent.left = null;
            } else if (node.parent.right.equals(node)) {
                node.parent.right = null;
            }
            node.parent = null;
        }
    }

    /**
     * Method to unroot the tree.
     * @param node
     * @throws IOException 
     */
    public void unrootTree(Node node) throws IOException {
        if (node.parent == null) {
            Node nl = node.left;
            Node nr = node.right;
            deleteNode(node);
            nl.parent = nr;
            nr.parent = nl;
        } else {
            throw new IOException("Deletion of wrong node when attempting to unroot tree.");
        }
    }
}
