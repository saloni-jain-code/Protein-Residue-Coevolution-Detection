# Coevolution Detection Method Evaluation Report

## Overview
This report compares the performance of old and new coevolution detection methods across 10 simulation replicates.

## Simulation Parameters
- Number of Proteins: 200
- Sequence Length: 100
- Mutation Rate: 0.1
- Coevolution Strength: 1
- Ground Truth Coevolving Pairs (0-based Python indexing): [(1,2), (3,4), (5,6), (7,8), (9,10),(11,12),(13,14),(15,16),(17,18),(19,20),(21,22),(23,24),(25,26),(27,28),(29,30),(31,32),(33,34),(35,36),(37,38),(39,40)]
- Ground Truth Coevolving Pairs (1-based coevolution code indexing): +1 to each position

## Indexing Note
This evaluation handles a critical indexing difference:
- Python simulator uses 0-based indexing (e.g., position "10" is the 11th position)
- Coevolution code uses 1-based indexing (e.g., position "11" is the 11th position)
- All conversions are handled internally by the evaluation pipeline

## Performance Summary
method,precision,recall,f1score,true_positives,false_positives,false_negatives
new,0.3304825,0.13874999999999998,0.1687375,2.775,0.3,17.225
old,0.325,0.06,0.09681500000000001,1.2,0.0,18.8

## Visualizations
- Contact maps: See plots/contact_map_replicate_*.png
- Method comparison: See plots/method_comparison.png
- F1 score comparison: See plots/f1_score_boxplot.png

## Method Comparison
- Old method: Max parsimony for ancestral reconstruction
- New method: Max likelihood with ancestral sequences

