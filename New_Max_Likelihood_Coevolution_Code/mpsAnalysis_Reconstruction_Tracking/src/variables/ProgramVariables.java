/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package variables;
import java.util.TreeMap;

/**
 * Class to store the input/output variables.
 * @author davidtalavera
 */
public class ProgramVariables {
    
    /**
     * User-defined variables (input/output files).
     */
    public static class UserDefinedVariables {

        public static String msAlignmentFile;
        public static String treeFile;
        public static String outputFile;
        public static String pairsFile;

        // added by Amanda
        public static String ancestralFile;
        public static boolean usingProvidedAncestors = false;
    }

    /**
     * Internal variables (input/output files).
     */
    public static class InternalVariables {

        public static String msAlignmentFile;
        public static String treeFile;
        public static String outputFile;
        public static String pairsFile;

        // added by Amanda
        public static String ancestralFile;
        public static boolean usingProvidedAncestors = false;
    }
}
