#!/usr/bin/env python3
"""
Generate visualizations for coevolution results
"""
import os
import sys
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
from matplotlib.ticker import MaxNLocator

def create_contact_map(ground_truth_file, old_predictions_file, new_predictions_file, output_file, seq_length):
    """Create contact map visualization comparing methods"""
    # Read ground truth with proper order-agnostic pair handling
    ground_truth_set = set()
    if os.path.exists(ground_truth_file) and os.path.getsize(ground_truth_file) > 0:
        try:
            with open(ground_truth_file, 'r') as f:
                for line in f:
                    parts = line.strip().split()
                    if len(parts) == 2:
                        try:
                            pos1 = int(parts[0])
                            pos2 = int(parts[1])
                            ground_truth_set.add((min(pos1, pos2), max(pos1, pos2)))
                        except ValueError:
                            print(f"Warning: Could not parse line in ground truth: {line.strip()}")
        except Exception as e:
            print(f"Error reading ground truth file: {e}")

    # Read old method predictions with proper order-agnostic pair handling
    old_predictions_set = set()
    if os.path.exists(old_predictions_file) and os.path.getsize(old_predictions_file) > 0:
        try:
            with open(old_predictions_file, 'r') as f:
                for line in f:
                    parts = line.strip().split()
                    if len(parts) == 2:
                        try:
                            pos1 = int(parts[0])
                            pos2 = int(parts[1])
                            old_predictions_set.add((min(pos1, pos2), max(pos1, pos2)))
                        except ValueError:
                            print(f"Warning: Could not parse line in old predictions: {line.strip()}")
        except Exception as e:
            print(f"Error reading old predictions file: {e}")

    # Read new method predictions with proper order-agnostic pair handling
    new_predictions_set = set()
    if os.path.exists(new_predictions_file) and os.path.getsize(new_predictions_file) > 0:
        try:
            with open(new_predictions_file, 'r') as f:
                for line in f:
                    parts = line.strip().split()
                    if len(parts) == 2:
                        try:
                            pos1 = int(parts[0])
                            pos2 = int(parts[1])
                            new_predictions_set.add((min(pos1, pos2), max(pos1, pos2)))
                        except ValueError:
                            print(f"Warning: Could not parse line in new predictions: {line.strip()}")
        except Exception as e:
            print(f"Error reading new predictions file: {e}")

    # Create figure with 1x3 subplots
    fig, axes = plt.subplots(1, 3, figsize=(18, 6))

    # Create contact maps
    maps = []
    for i, (title, predictions_set) in enumerate([
        ('Ground Truth', ground_truth_set),
        ('Old Method', old_predictions_set),
        ('New Method', new_predictions_set)
    ]):
        # Create contact map
        contact_map = np.zeros((seq_length, seq_length))

        # Fill in contact map
        for pos1, pos2 in predictions_set:
            # Convert from 1-based to 0-based indexing for the plot
            pos1_idx = pos1 - 1
            pos2_idx = pos2 - 1

            # Check bounds
            if 0 <= pos1_idx < seq_length and 0 <= pos2_idx < seq_length:
                contact_map[pos1_idx, pos2_idx] = 1
                contact_map[pos2_idx, pos1_idx] = 1  # Mirror

        # Plot contact map
        im = axes[i].imshow(contact_map, cmap='Blues', origin='lower')
        axes[i].set_title(title)
        axes[i].set_xlabel('Position (0-based indices for plot)')
        axes[i].set_ylabel('Position (0-based indices for plot)')
        maps.append(contact_map)

    # Adjust layout
    plt.tight_layout(rect=[0, 0.05, 1, 0.95])

    # Save figure
    plt.savefig(output_file, dpi=300)
    plt.close()

    # Return the maps for potential further analysis
    return maps

def create_metric_plots(metrics_file, output_dir):
    """Create plots from metrics data with proper data type handling"""
    if not os.path.exists(metrics_file):
        print(f"Error: Metrics file {metrics_file} not found")
        return None

    # Read metrics data with proper type conversion
    try:
        metrics = pd.read_csv(metrics_file)

        # Ensure numeric columns are properly typed
        for col in ["precision", "recall", "f1score", "true_positives", "false_positives", "false_negatives"]:
            metrics[col] = pd.to_numeric(metrics[col], errors="coerce")

        print("Data types after conversion:")
        print(metrics.dtypes)

        if metrics.empty:
            print("Warning: Metrics file is empty")
            return None
    except Exception as e:
        print(f"Error reading metrics file: {e}")
        return None

    # Create output directory
    os.makedirs(output_dir, exist_ok=True)

    # Create a simple summary
    try:
        summary = metrics.groupby('method')[['precision', 'recall', 'f1score',
                                            'true_positives', 'false_positives',
                                            'false_negatives']].mean().reset_index()
        summary.to_csv(os.path.join(output_dir, 'summary_statistics.csv'), index=False)
    except Exception as e:
        print(f"Error creating summary statistics: {e}")
        return None

    # Create plots
    try:
        # 1. Bar chart comparing methods (simpler version)
        plt.figure(figsize=(10, 6))

        methods = metrics["method"].unique()
        x = np.arange(len(methods))
        width = 0.25

        precision_means = [metrics[metrics["method"] == m]["precision"].mean() for m in methods]
        recall_means = [metrics[metrics["method"] == m]["recall"].mean() for m in methods]
        f1_means = [metrics[metrics["method"] == m]["f1score"].mean() for m in methods]

        plt.bar(x - width, precision_means, width, label="Precision")
        plt.bar(x, recall_means, width, label="Recall")
        plt.bar(x + width, f1_means, width, label="F1 Score")

        plt.xlabel("Method")
        plt.ylabel("Score")
        plt.title("Coevolution Detection Performance")
        plt.xticks(x, methods)
        plt.legend()
        plt.grid(axis="y", linestyle="--", alpha=0.7)

        plt.savefig(os.path.join(output_dir, "method_comparison.png"), dpi=300)
        plt.close()

        # 2. Box plot of F1 scores by method
        plt.figure(figsize=(8, 6))
        plt.boxplot([metrics[metrics.method == 'old']['f1score'],
                    metrics[metrics.method == 'new']['f1score']],
                    labels=['Old Method', 'New Method'])
        plt.title('F1 Score Comparison')
        plt.ylabel('F1 Score')
        plt.grid(axis='y', linestyle='--', alpha=0.7)
        plt.tight_layout()
        plt.savefig(os.path.join(output_dir, 'f1_score_boxplot.png'), dpi=300)
        plt.close()

        # 3. True positives comparison
        plt.figure(figsize=(8, 6))
        tp_by_method = metrics.groupby('method')['true_positives'].sum()
        tp_by_method.plot(kind='bar')
        plt.title('Total True Positives by Method')
        plt.ylabel('Count')
        plt.grid(axis='y', linestyle='--', alpha=0.7)
        plt.tight_layout()
        plt.savefig(os.path.join(output_dir, 'true_positives.png'), dpi=300)
        plt.close()

    except Exception as e:
        print(f"Error creating plots: {e}")

    return summary

def main():
    if len(sys.argv) < 6:
        print("Usage: python generate_visualizations.py <metrics_file> <output_dir> <simulations_dir> <old_method_dir> <new_method_dir> <seq_length>")
        sys.exit(1)

    metrics_file = sys.argv[1]
    output_dir = sys.argv[2]
    simulations_dir = sys.argv[3]
    old_method_dir = sys.argv[4]
    new_method_dir = sys.argv[5]
    seq_length = int(sys.argv[6])

    # Create metric plots (with robust error handling)
    summary = create_metric_plots(metrics_file, output_dir)
    if summary is not None:
        print("Created metric plots successfully.")

    # Create contact maps for each replicate
    try:
        # Get list of replicates from the old_method_dir
        replicate_dirs = [d for d in os.listdir(old_method_dir) if d.startswith('sim_')]
        for replicate_dir in replicate_dirs:
            replicate = replicate_dir.split('_')[1]

            ground_truth_file = os.path.join(simulations_dir, f"sim_{replicate}", "ground_truth.tsv")

            # Use model 2 by default
            model = 2
            old_predictions_file = os.path.join(old_method_dir, f'sim_{replicate}', f'model_{model}', 'overlap_predictions.txt')
            new_predictions_file = os.path.join(new_method_dir, f'sim_{replicate}', f'model_{model}', 'overlap_predictions.txt')

            output_file = os.path.join(output_dir, f'contact_map_replicate_{replicate}.png')

            # Verify files exist before creating contact map
            if not os.path.exists(ground_truth_file):
                print(f"Warning: Ground truth file not found for replicate {replicate}")
                continue

            maps = create_contact_map(ground_truth_file, old_predictions_file, new_predictions_file, output_file, seq_length)
            print(f"Created contact map for replicate {replicate}.")
    except Exception as e:
        print(f"Error creating contact maps: {e}")

    print("Visualization complete.")

if __name__ == "__main__":
    main()
