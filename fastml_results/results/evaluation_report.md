# Coevolution Detection Method Evaluation Report

## Overview
This report compares the performance of old and new coevolution detection methods using FastML ancestral sequences.

## Dataset
- Original simulation replicates: 1 2 3 4
- FastML ancestral sequences for replicates: 1 2 3 4
- Sequence Length: 200
- P-Value Threshold: 0.01

## Methods
- **Old Method**: Uses maximum parsimony for ancestral reconstruction from simulation data
- **New Method**: Uses maximum likelihood with FastML ancestral sequences (converted from FASTA to PHYLIP format)

## Modifications
- Added Biopython-based conversion from FastML seq.joint.txt (FASTA format) to PHYLIP format
- Used the converted PHYLIP file as the MSA input for the new method

## Performance Summary
method,precision,recall,f1score,true_positives,false_positives,false_negatives
new,0.5,0.05,0.0909,1.0,0.0,19.0
old,1.0,0.2,0.31520000000000004,4.0,0.0,16.0

## Visualizations
- Contact maps: See plots/contact_map_replicate_*.png
- Method comparison: See plots/method_comparison.png
- F1 score comparison: See plots/f1_score_boxplot.png

