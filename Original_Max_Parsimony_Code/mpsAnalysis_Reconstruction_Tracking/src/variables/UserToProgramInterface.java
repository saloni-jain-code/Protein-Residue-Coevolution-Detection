/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package variables;

import utilities.ReaderForFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import variables.ProgramVariables.InternalVariables;
import variables.ProgramVariables.UserDefinedVariables;

/**
 * Class to read the input variables and store them as internal variables.
 * @author davidtalavera
 */
public class UserToProgramInterface {
    
    /**
     * Constructor.
     */
    public UserToProgramInterface() {
    }

    /*Public methods*/
    
    /**
     * Method to read the control file and set the user-defined variables.
     * @param file
     * @throws FileNotFoundException
     * @throws Exception 
     */
    public void getUDVariablesFile(String file) throws FileNotFoundException, Exception {

        try {
            ArrayList<String> lines = new ArrayList();

            ReaderForFile reader = new ReaderForFile(file);
            reader.readFileContents();
            lines = reader.getLines();

            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("#") == false) {
                    String[] controls = lines.get(i).split("\\s+");
                    setUDVariable(controls[0], controls[1]);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(UserToProgramInterface.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Method to set the internal variables from the user-defined variables.
     * @throws IOException 
     */
    public void setIVariables() throws IOException {

        InternalVariables.msAlignmentFile = UserDefinedVariables.msAlignmentFile;
        InternalVariables.outputFile = UserDefinedVariables.outputFile;
        InternalVariables.treeFile = UserDefinedVariables.treeFile;
        if (UserDefinedVariables.pairsFile == null) {
            InternalVariables.pairsFile = null;
        } else {
            InternalVariables.pairsFile = UserDefinedVariables.pairsFile;
        }     
    }

    /*Private methods*/
    
    /**
     * Method to set the user-defined variables from the values in the control file.
     * @param flag
     * @param value 
     */
    private void setUDVariable(String flag, String value) {
        if (flag.equalsIgnoreCase("MSA_ALIGNMENT_FILE")) {
            UserDefinedVariables.msAlignmentFile = value;
        } else if (flag.equalsIgnoreCase("OUTPUT_FILE")) {
            UserDefinedVariables.outputFile = value;
        } else if (flag.equalsIgnoreCase("TREE_FILE")) {
            UserDefinedVariables.treeFile = value;
        } else if (flag.equalsIgnoreCase("PAIRS_FILE")) {
            UserDefinedVariables.pairsFile = value;
        }
    }
}
