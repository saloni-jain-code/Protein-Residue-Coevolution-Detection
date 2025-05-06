#!/bin/bash

# Base directory for input/output files
BASE_DIR="/Users/amandaarnold/Desktop/Computational_Genomics/Protein-Residue-Coevolution-Detection/coevolution_evaluation"
SIMULATIONS_DIR="${BASE_DIR}/simulations"
FASTML_OUTPUT_DIR="${BASE_DIR}/fastml_output"

# Function to remove labeled ancestors from Newick files
function convert_newick_tree() {
    input_file=$1
    output_file=$2
    
    # Read the original Newick tree
    original_tree=$(cat "$input_file")
    
    # Remove internal node labels (patterns like ")N123:")
    # and replace them with "):"
    converted_tree=$(echo "$original_tree" | sed -E 's/\)N[0-9]+:/\):/g')
    
    # Remove the root node label if present (pattern like ")N1;")
    converted_tree=$(echo "$converted_tree" | sed -E 's/\)N[0-9]+;$/\);/')
    
    # Write the converted tree to the output file
    echo "$converted_tree" > "$output_file"
    
    echo "Converted Newick tree saved to: $output_file"
}

# Process each simulation
for number in 2 3 4 5 6 7 8 9 10
do
    echo "Processing sim_${number} data"
    
    # Create output directory if it doesn't exist
    mkdir -p "${FASTML_OUTPUT_DIR}/sim_${number}"
    
    # Define input and output file paths
    input_newick="${SIMULATIONS_DIR}/sim_${number}/sim_${number}.newick"
    output_newick="${SIMULATIONS_DIR}/sim_${number}/sim_${number}_extantnew.newick"
    input_fasta="${SIMULATIONS_DIR}/sim_${number}/sim_${number}_extant.fasta"
    
    # Convert the Newick tree
    echo "Converting Newick tree for sim_${number}"
    convert_newick_tree "$input_newick" "$output_newick"
    
    # Run FastML with the converted tree
    echo "Running FastML for sim_${number} data"
    perl ./FastML.v3.11/www/fastml/FastML_Wrapper.pl -D \
        --MSA_File "$input_fasta" \
        --outDir "${FASTML_OUTPUT_DIR}/sim_${number}" \
        --seqType aa \
        --Tree "$output_newick"
    
    echo "Completed processing for sim_${number}"
    echo "----------------------------------------"
done

echo "All simulations processed successfully"