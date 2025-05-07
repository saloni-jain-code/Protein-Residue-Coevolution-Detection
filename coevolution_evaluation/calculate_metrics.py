#!/usr/bin/env python3
"""
Calculate performance metrics for coevolution detection methods
"""
import os
import sys
import pandas as pd
import numpy as np

def calculate_metrics(ground_truth_file, predictions_file):
    """Calculate performance metrics with order-agnostic pair matching"""
    # Read ground truth
    ground_truth_set = set()
    try:
        with open(ground_truth_file, 'r') as f:
            for line in f:
                parts = line.strip().split()
                if len(parts) == 2:
                    try:
                        pos1 = int(parts[0])
                        pos2 = int(parts[1])
                        # Store both orderings to make comparison order-agnostic
                        ground_truth_set.add((min(pos1, pos2), max(pos1, pos2)))
                    except ValueError:
                        print(f"Warning: Could not parse line in ground truth: {line.strip()}")
        print(f"Loaded {len(ground_truth_set)} ground truth pairs")
    except Exception as e:
        print(f"Error reading ground truth file: {e}")

    # Read predictions
    predictions_set = set()
    if os.path.exists(predictions_file) and os.path.getsize(predictions_file) > 0:
        try:
            with open(predictions_file, 'r') as f:
                for line in f:
                    parts = line.strip().split()
                    if len(parts) == 2:
                        try:
                            pos1 = int(parts[0])
                            pos2 = int(parts[1])
                            # Store both orderings to make comparison order-agnostic
                            predictions_set.add((min(pos1, pos2), max(pos1, pos2)))
                        except ValueError:
                            print(f"Warning: Could not parse line in predictions: {line.strip()}")
            print(f"Loaded {len(predictions_set)} prediction pairs")
        except Exception as e:
            print(f"Error reading predictions file: {e}")

    # Calculate metrics
    true_positives = len(ground_truth_set.intersection(predictions_set))
    false_positives = len(predictions_set - ground_truth_set)
    false_negatives = len(ground_truth_set - predictions_set)

    precision = true_positives / (true_positives + false_positives) if (true_positives + false_positives) > 0 else 0
    recall = true_positives / (true_positives + false_negatives) if (true_positives + false_negatives) > 0 else 0
    f1_score = 2 * (precision * recall) / (precision + recall) if (precision + recall) > 0 else 0

    return {
        'precision': precision,
        'recall': recall,
        'f1score': f1_score,
        'true_positives': true_positives,
        'false_positives': false_positives,
        'false_negatives': false_negatives
    }

def main():
    if len(sys.argv) != 5:
        print("Usage: python calculate_metrics.py <ground_truth_file> <predictions_file> <replicate> <model>")
        sys.exit(1)

    ground_truth_file = sys.argv[1]
    predictions_file = sys.argv[2]
    replicate = sys.argv[3]
    model = sys.argv[4]

    # Calculate metrics
    metrics = calculate_metrics(ground_truth_file, predictions_file)

    # Output CSV row: replicate,model,method,precision,recall,f1score,true_positives,false_positives,false_negatives
    method = "old" if "old_method" in predictions_file else "new"
    print(f"{replicate},{model},{method},{metrics['precision']:.4f},{metrics['recall']:.4f},{metrics['f1score']:.4f},{metrics['true_positives']},{metrics['false_positives']},{metrics['false_negatives']}")

if __name__ == "__main__":
    main()
