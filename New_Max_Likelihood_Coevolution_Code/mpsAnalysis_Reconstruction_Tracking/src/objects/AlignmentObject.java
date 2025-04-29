/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import java.util.ArrayList;
import java.util.HashMap;
import utilities.ReadPhylipAlignment;

/**
 * Class for setting the alignment object. 
 * @author davidtalavera
 */
public class AlignmentObject {

    public static class Alignment {

        public HashMap<ArrayList, HashMap> columnsMap;
        public HashMap<Integer, String> columnsPositions;

        /**
         * Constructor.
         */
        public Alignment() {
        }
    }

    /*Public methods*/
    
    /**
     * Method to create the alignment object.
     * @param code
     * @param readMethod
     * @throws Exception 
     */
    public void setAlignment(Alignment code, ReadPhylipAlignment readMethod) throws Exception {
        readMethod.readFileContents();
        readMethod.readFirstLineAlignment();
        readMethod.solveAlignment();
        readMethod.compressAlignmentSingle();
 
        code.columnsMap = readMethod.getColumnsMap();
        code.columnsPositions = readMethod.getColumnsPosition();

    }
}
