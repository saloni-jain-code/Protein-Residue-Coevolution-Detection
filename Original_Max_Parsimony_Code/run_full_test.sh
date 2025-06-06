#!/bin/bash
# This file runs the complete pipeline for coevolution detection on an alignment and tree for sequences related
# to the PDB entry 1AOE, taken from the PSICOV paper, Jones et al. (2012) https://academic.oup.com/bioinformatics/article/28/2/184/198108
for letter in V2
do
    for num_sequences in 300

    do
        data_dir="${letter}_${num_sequences}"
        echo "Running data directory: $data_dir"
        for statistical_model in {1..7}
        do
            echo Running statistical model: $statistical_model
            #### First run the mps module
            mkdir outputs
            mkdir outputs/${data_dir}
            mkdir outputs/${data_dir}/$statistical_model
            # Input is a PHYLIP sequence alignment and a Newick phylogenetic tree produced from it
            # These must be entered into a ctl file as follows
            echo "MSA_ALIGNMENT_FILE ../aCES_data/$data_dir/results_extant.phy" > 1aoeA.ctl
            echo "TREE_FILE ../aCES_data/$data_dir/results_extant.newick" >> 1aoeA.ctl
            echo "OUTPUT_FILE outputs/${data_dir}/$statistical_model/mps_1aoeA" >> 1aoeA.ctl

            # Create log output folder
            mkdir logs
            mkdir logs/$data_dir
            java -Xms3000m -Xmx3000m -jar "mps_code/dist/mpsAnalysis_Reconstruction_Tracking.jar" 1aoeA.ctl > logs/1aoeA.log

            #### Run the statistical modelling of separate and concurrent changes
            # Run the branches method on a single statistical model (3 = Box-Cox)
            Rscript --vanilla scripts/covariation_branches_model.R outputs/${data_dir}/$statistical_model/mps_1aoeA.binary_branches.tab outputs/${data_dir}/$statistical_model/1aoeA_branches 0.01 $statistical_model

            # Run the nodes method on a single statistical model (3 = Box-Cox)
            Rscript --vanilla scripts/covariation_nodes_model.R outputs/${data_dir}/$statistical_model/mps_1aoeA.binary_nodes.tab outputs/${data_dir}/$statistical_model/1aoeA_nodes 0.01 $statistical_model

            # Extract only overlapping predictions and remove the header line
            comm -12 <(cut -f1-2 -d ' ' outputs/${data_dir}/$statistical_model/1aoeA_branches_predictions.txt | sort) <(cut -f1-2 -d ' ' outputs/${data_dir}/$statistical_model/1aoeA_nodes_predictions.txt | sort) | awk '$1 ~ /^[0-9]+$/ {print $1"\t"$2}' > outputs/${data_dir}/$statistical_model/1aoeA_overlap_predictions.txt

            #### Run the mps module this time to extract ancestral identities for coevolving pairs
            # Input is the same PHYLIP sequence alignment and a Newick phylogenetic tree produced from it
            # This time a set of tab delimited predicted coevolving pairs are supplies
            # These must be entered into a ctl file as follows
            echo "MSA_ALIGNMENT_FILE ../aCES_data/$data_dir/results_extant.phy" > 1aoeA.ctl
            echo "TREE_FILE ../aCES_data/$data_dir/results_extant.newick" >> 1aoeA.ctl
            echo "OUTPUT_FILE outputs/${data_dir}/$statistical_model/mps_1aoeA" >> 1aoeA.ctl


            java -Xms3000m -Xmx3000m -jar "mps_code/dist/mpsAnalysis_Reconstruction_Tracking.jar" 1aoeA.ctl >> logs/$data_dir/1aoeA.log

            # Run a filter to exclude pairs with a high proportion of gaps in the alignment
            Rscript --vanilla scripts/gap_filter.R data/1aoeA.phy outputs/${data_dir}/$statistical_model/1aoeA_overlap_predictions.txt outputs/${data_dir}/$statistical_model/1aoeA_filtered_predictions.tsv
        done
        echo "Completed data directory: $data_dir"
        echo "----------------------------------------"
    done
done