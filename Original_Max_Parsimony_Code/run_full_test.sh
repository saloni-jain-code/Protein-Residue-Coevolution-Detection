#!/usr/bin/env bash
# This file runs the complete pipeline for coevolution detection on an alignment and tree for sequences related
# to the PDB entry 1AOE, taken from the PSICOV paper, Jones et al. (2012) https://academic.oup.com/bioinformatics/article/28/2/184/198108

#### First run the mps module
mkdir outputs
# Input is a PHYLIP sequence alignment and a Newick phylogenetic tree produced from it
# These must be entered into a ctl file as follows
echo "MSA_ALIGNMENT_FILE A_new_data/A_300_aces/1aoeA.phy" > 1aoeA.ctl
echo "TREE_FILE A_new_data/A_300_aces/1aoeA.txt" >> 1aoeA.ctl
echo "OUTPUT_FILE outputs/mps_1aoeA" >> 1aoeA.ctl

# Create log output folder
mkdir logs
java -Xms3000m -Xmx3000m -jar "mps_code/dist/mpsAnalysis_Reconstruction_Tracking.jar" 1aoeA.ctl > logs/1aoeA.log

#### Run the statistical modelling of separate and concurrent changes
# Run the branches method on a single statistical model (3 = Box-Cox)
echo running on binary branches
Rscript --vanilla scripts/covariation_branches_model.R outputs/mps_1aoeA.binary_branches.tab outputs/1aoeA_branches 0.01 4

echo finished running on binary branches

echo running on binary nodes
# Run the nodes method on a single statistical model (3 = Box-Cox)
Rscript --vanilla scripts/covariation_nodes_model.R outputs/mps_1aoeA.binary_nodes.tab outputs/1aoeA_nodes 0.01 4
echo finished running on binary nodes

# Extract only overlapping predictions and remove the header line
comm -12 <(cut -f1-2 -d ' ' outputs/1aoeA_branches_predictions.txt | sort) <(cut -f1-2 -d ' ' outputs/1aoeA_nodes_predictions.txt | sort) | awk '$1 ~ /^[0-9]+$/ {print $1"\t"$2}' > outputs/1aoeA_overlap_predictions.txt

#### Run the mps module this time to extract ancestral identities for coevolving pairs
# Input is the same PHYLIP sequence alignment and a Newick phylogenetic tree produced from it
# This time a set of tab delimited predicted coevolving pairs are supplies
# These must be entered into a ctl file as follows
echo "MSA_ALIGNMENT_FILE A_new_data/A_300_aces/1aoeA.phy" > 1aoeA.ctl
echo "TREE_FILE A_new_data/A_300_aces/1aoeA.txt" >> 1aoeA.ctl
echo "OUTPUT_FILE outputs/mps2_1aoeA" >> 1aoeA.ctl
echo "PAIRS_FILE outputs/1aoeA_overlap_predictions.txt" >> 1aoeA.ctl

java -Xms3000m -Xmx3000m -jar "mps_code/dist/mpsAnalysis_Reconstruction_Tracking.jar" 1aoeA.ctl >> logs/1aoeA.log

echo running filter
# Run a filter to exclude pairs with a high proportion of gaps in the alignment
Rscript --vanilla scripts/gap_filter.R A_new_data/A_300_aces/1aoeA.phy outputs/1aoeA_overlap_predictions.txt 1aoeA_filtered_predictions.tsv 
