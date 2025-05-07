#!/bin/bash
# Modified coevolution evaluation pipeline without R visualization dependencies
# This script:
# 1. Generates simulated MSAs with known coevolving pairs
# 2. Runs both old and new coevolution detection methods
# 3. Calculates metrics using Python instead of R
# 4. Creates simple visualizations with Python

#=====================================================================
# Configuration
#=====================================================================
NUM_REPLICATES=10             # Number of simulation replicates
NUM_PROTEINS=200          # Number of extant sequences
SEQ_LENGTH=100               # Length of each protein sequence
MUTATION_RATE=0.1            # Mutation probability per site
COEV_STRENGTH=1             # Strength of coevolution (0-1)
P_VALUE_THRESHOLD=0.01       # P-value threshold for predictions

# Known ground truth coevolving pairs - make sure to escape brackets for bash
COEVOLVING_PAIRS="[(1,2), (3,4), (5,6), (7,8), (9,10),(11,12),(13,14),(15,16),(17,18),(19,20),(21,22),(23,24),(25,26),(27,28),(29,30),(31,32),(33,34),(35,36),(37,38),(39,40)]"

# Path configuration
PROJECT_ROOT="$(pwd)"
SIMULATOR="${PROJECT_ROOT}/coevolution_simulator.py"

# JAR file paths - update these if needed
OLD_JAR="${PROJECT_ROOT}/Original_Max_Parsimony_Code/mps_code/dist/mpsAnalysis_Reconstruction_Tracking.jar"
NEW_JAR="${PROJECT_ROOT}/New_Max_Likelihood_Coevolution_Code/mpsAnalysis_Reconstruction_Tracking/mpsAnalysis_Reconstruction_Tracking.jar"

# R script paths - update these if needed
OLD_SCRIPTS="${PROJECT_ROOT}/Original_Max_Parsimony_Code/scripts"
NEW_SCRIPTS="${PROJECT_ROOT}/New_Max_Likelihood_Coevolution_Code/scripts"

# Base directory for outputs
BASE_DIR="coevolution_evaluation"
SIMULATIONS_DIR="${BASE_DIR}/simulations"
OLD_METHOD_DIR="${BASE_DIR}/old_method"
NEW_METHOD_DIR="${BASE_DIR}/new_method"
RESULTS_DIR="${BASE_DIR}/results"
METRICS_DIR="${BASE_DIR}/metrics"
PLOTS_DIR="${BASE_DIR}/plots"
DEBUG_DIR="${BASE_DIR}/debug"

# Java memory settings
JAVA_MEM="-Xms2000m -Xmx2000m"

# Statistical models to evaluate
STAT_MODELS=(6 3 2 1)  # Using just model 2 for simplicity

#=====================================================================
# Create directory structure
#=====================================================================
mkdir -p "${SIMULATIONS_DIR}"
mkdir -p "${OLD_METHOD_DIR}" "${NEW_METHOD_DIR}"
mkdir -p "${RESULTS_DIR}" "${METRICS_DIR}" "${PLOTS_DIR}" "${DEBUG_DIR}"
mkdir -p "${BASE_DIR}/logs"

#=====================================================================
# Check for file availability
#=====================================================================
echo "Checking for required files..." > "${DEBUG_DIR}/file_check.log"
echo "Old JAR exists: $([ -f "${OLD_JAR}" ] && echo "YES" || echo "NO")" >> "${DEBUG_DIR}/file_check.log"
echo "New JAR exists: $([ -f "${NEW_JAR}" ] && echo "YES" || echo "NO")" >> "${DEBUG_DIR}/file_check.log"
echo "Old scripts directory exists: $([ -d "${OLD_SCRIPTS}" ] && echo "YES" || echo "NO")" >> "${DEBUG_DIR}/file_check.log"
echo "New scripts directory exists: $([ -d "${NEW_SCRIPTS}" ] && echo "YES" || echo "NO")" >> "${DEBUG_DIR}/file_check.log"

#=====================================================================
# Generate simulated data with known coevolving positions
#=====================================================================
echo "===== GENERATING SIMULATED DATA ====="
for replicate in $(seq 1 ${NUM_REPLICATES}); do
    echo "Generating simulation replicate ${replicate}..."

    # Create simulation directory
    SIM_DIR="${SIMULATIONS_DIR}/sim_${replicate}"
    mkdir -p "${SIM_DIR}"

    # Run the Python simulator
    python3 "${SIMULATOR}" \
        --num_proteins ${NUM_PROTEINS} \
        --seq_length ${SEQ_LENGTH} \
        --coevolving_pairs "${COEVOLVING_PAIRS}" \
        --mutation_rate ${MUTATION_RATE} \
        --coev_strength ${COEV_STRENGTH} \
        --output_prefix "${SIM_DIR}/sim_${replicate}"

    # Save the ground truth coevolving pairs in a standard format
    # IMPROVED: Better parsing and formatting
    echo "${COEVOLVING_PAIRS}" |
      sed 's/\[\(.*\)\]/\1/g' |                     # Remove outer brackets
      tr -d ' ' |                                    # Remove spaces
      sed 's/),(/\n/g' |                            # Replace ),( with newlines
      sed 's/^(//' |                                # Remove leading (
      sed 's/)$//' |                                # Remove trailing )
      sed 's/,/\t/g' |                              # Replace commas with TABS
      awk '{print $1+1"\t"$2+1}' > "${SIM_DIR}/ground_truth.tsv"

    echo "Ground truth coevolving pairs (1-based indexing) for replicate ${replicate}:"
    cat "${SIM_DIR}/ground_truth.tsv"

    # Also save the original 0-based ground truth for reference
    echo "${COEVOLVING_PAIRS}" |
      sed 's/\[\(.*\)\]/\1/g' |                     # Remove outer brackets
      tr -d ' ' |                                    # Remove spaces
      sed 's/),(/\n/g' |                            # Replace ),( with newlines
      sed 's/^(//' |                                # Remove leading (
      sed 's/)$//' |                                # Remove trailing )
      sed 's/,/\t/g' > "${SIM_DIR}/ground_truth_0based.tsv"

    echo "Original ground truth coevolving pairs (0-based indexing) for replicate ${replicate}:"
    cat "${SIM_DIR}/ground_truth_0based.tsv"
done

#=====================================================================
# Run evaluation for each statistical model
#=====================================================================
echo "===== EVALUATING STATISTICAL MODELS ====="

# Function to run analysis with old method (Max Parsimony)
run_old_method() {
    local sim_dir=$1
    local stat_model=$2
    local output_dir="${OLD_METHOD_DIR}/sim_${replicate}/model_${stat_model}"

    mkdir -p "${output_dir}"

    # Create control file for MPS analysis
    echo "MSA_ALIGNMENT_FILE ${sim_dir}/sim_${replicate}_extant.phy" > mps_old.ctl
    echo "TREE_FILE ${sim_dir}/sim_${replicate}_extant.newick" >> mps_old.ctl
    echo "OUTPUT_FILE ${output_dir}/mps_result" >> mps_old.ctl

    # Check if JAR file exists before running
    if [ -f "${OLD_JAR}" ]; then
        echo "Running old method analysis with JAR: ${OLD_JAR}"
        java ${JAVA_MEM} -jar "${OLD_JAR}" \
            mps_old.ctl > "${BASE_DIR}/logs/old_method_sim${replicate}_model${stat_model}.log" 2>&1

        # Check if output files were created
        if [ -f "${output_dir}/mps_result.binary_branches.tab" ] && [ -f "${output_dir}/mps_result.binary_nodes.tab" ]; then
            echo "MPS analysis completed successfully for old method."

            # Check if R scripts directory exists
            if [ -d "${OLD_SCRIPTS}" ]; then
                # Run statistical modeling
                echo "Running branches model with R script: ${OLD_SCRIPTS}/covariation_branches_model.R"
                Rscript --vanilla "${OLD_SCRIPTS}/covariation_branches_model.R" \
                    "${output_dir}/mps_result.binary_branches.tab" \
                    "${output_dir}/branches_result" \
                    ${P_VALUE_THRESHOLD} ${stat_model}

                echo "Running nodes model with R script: ${OLD_SCRIPTS}/covariation_nodes_model.R"
                Rscript --vanilla "${OLD_SCRIPTS}/covariation_nodes_model.R" \
                    "${output_dir}/mps_result.binary_nodes.tab" \
                    "${output_dir}/nodes_result" \
                    ${P_VALUE_THRESHOLD} ${stat_model}

                # Extract overlapping predictions
                if [ -f "${output_dir}/branches_result_predictions.txt" ] && [ -f "${output_dir}/nodes_result_predictions.txt" ]; then
                    echo "Statistical modeling completed successfully for old method."
                    comm -12 \
                        <(cut -f1-2 -d ' ' "${output_dir}/branches_result_predictions.txt" | sort) \
                        <(cut -f1-2 -d ' ' "${output_dir}/nodes_result_predictions.txt" | sort) | \
                        awk '$1 ~ /^[0-9]+$/ {print $1" "$2}' > "${output_dir}/overlap_predictions.txt"

                    echo "Number of overlapping predictions: $(wc -l < ${output_dir}/overlap_predictions.txt)"

                    # For debugging - show the first few predictions
                    echo "First few predictions (1-based indexing):"
                    head -n 5 "${output_dir}/overlap_predictions.txt"
                else
                    echo "WARNING: Statistical modeling did not produce prediction files for old method."
                    touch "${output_dir}/overlap_predictions.txt"
                fi
            else
                echo "WARNING: Old R scripts directory not found. Skipping statistical modeling."
                touch "${output_dir}/overlap_predictions.txt"
            fi
        else
            echo "WARNING: MPS analysis did not produce output files for old method."
            touch "${output_dir}/overlap_predictions.txt"
        fi
    else
        echo "WARNING: Old JAR file not found. Skipping old method analysis."
        touch "${output_dir}/overlap_predictions.txt"
    fi
}

# Function to run analysis with new method (Max Likelihood)
run_new_method() {
    local sim_dir=$1
    local stat_model=$2
    local output_dir="${NEW_METHOD_DIR}/sim_${replicate}/model_${stat_model}"

    mkdir -p "${output_dir}"

    # Create control file for MPS analysis
    echo "MSA_ALIGNMENT_FILE ${sim_dir}/sim_${replicate}.phy" > mps_new.ctl
    echo "TREE_FILE ${sim_dir}/sim_${replicate}.newick" >> mps_new.ctl
    echo "OUTPUT_FILE ${output_dir}/mps_result" >> mps_new.ctl
    cs

    # Check if JAR file exists before running
    if [ -f "${NEW_JAR}" ]; then
        echo "Running new method analysis with JAR: ${NEW_JAR}"
        java ${JAVA_MEM} -jar "${NEW_JAR}" \
            mps_new.ctl > "${BASE_DIR}/logs/new_method_sim${replicate}_model${stat_model}.log" 2>&1

        # Check if output files were created
        if [ -f "${output_dir}/mps_result.binary_branches.tab" ] && [ -f "${output_dir}/mps_result.binary_nodes.tab" ]; then
            echo "MPS analysis completed successfully for new method."

            # Check if R scripts directory exists
            if [ -d "${NEW_SCRIPTS}" ]; then
                # Run statistical modeling
                echo "Running branches model with R script: ${NEW_SCRIPTS}/covariation_branches_model.R"
                Rscript --vanilla "${NEW_SCRIPTS}/covariation_branches_model.R" \
                    "${output_dir}/mps_result.binary_branches.tab" \
                    "${output_dir}/branches_result" \
                    ${P_VALUE_THRESHOLD} ${stat_model}

                echo "Running nodes model with R script: ${NEW_SCRIPTS}/covariation_nodes_model.R"
                Rscript --vanilla "${NEW_SCRIPTS}/covariation_nodes_model.R" \
                    "${output_dir}/mps_result.binary_nodes.tab" \
                    "${output_dir}/nodes_result" \
                    ${P_VALUE_THRESHOLD} ${stat_model}

                # Extract overlapping predictions
                if [ -f "${output_dir}/branches_result_predictions.txt" ] && [ -f "${output_dir}/nodes_result_predictions.txt" ]; then
                    echo "Statistical modeling completed successfully for new method."
                    comm -12 \
                        <(cut -f1-2 -d ' ' "${output_dir}/branches_result_predictions.txt" | sort) \
                        <(cut -f1-2 -d ' ' "${output_dir}/nodes_result_predictions.txt" | sort) | \
                        awk '$1 ~ /^[0-9]+$/ {print $1" "$2}' > "${output_dir}/overlap_predictions.txt"

                    echo "Number of overlapping predictions: $(wc -l < ${output_dir}/overlap_predictions.txt)"

                    # For debugging - show the first few predictions
                    echo "First few predictions (1-based indexing):"
                    head -n 5 "${output_dir}/overlap_predictions.txt"
                else
                    echo "WARNING: Statistical modeling did not produce prediction files for new method."
                    touch "${output_dir}/overlap_predictions.txt"
                fi
            else
                echo "WARNING: New R scripts directory not found. Skipping statistical modeling."
                touch "${output_dir}/overlap_predictions.txt"
            fi
        else
            echo "WARNING: MPS analysis did not produce output files for new method."
            touch "${output_dir}/overlap_predictions.txt"
        fi
    else
        echo "WARNING: New JAR file not found. Skipping new method analysis."
        touch "${output_dir}/overlap_predictions.txt"
    fi
}

# Run methods for each replicate and statistical model
for replicate in $(seq 1 ${NUM_REPLICATES}); do
    SIM_DIR="${SIMULATIONS_DIR}/sim_${replicate}"

    # For each statistical model
    for stat_model in "${STAT_MODELS[@]}"; do
        echo "Running replicate ${replicate}, statistical model ${stat_model}..."

        # Run old method
        run_old_method "${SIM_DIR}" "${stat_model}"

        # Run new method
        run_new_method "${SIM_DIR}" "${stat_model}"
    done
done

#=====================================================================
# Calculate performance metrics using Python
#=====================================================================
echo "===== CALCULATING PERFORMANCE METRICS ====="

# Create CSV file for metrics
echo "replicate,model,method,precision,recall,f1score,true_positives,false_positives,false_negatives" \
    > "${METRICS_DIR}/all_metrics.csv"

# Python script for metrics calculation with order-agnostic pair matching
cat > "${BASE_DIR}/calculate_metrics.py" << 'EOF'
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
EOF

chmod +x "${BASE_DIR}/calculate_metrics.py"

# Calculate metrics for each replicate and model
for replicate in $(seq 1 ${NUM_REPLICATES}); do
    SIM_DIR="${SIMULATIONS_DIR}/sim_${replicate}"
    GROUND_TRUTH="${SIM_DIR}/ground_truth.tsv"

    for stat_model in "${STAT_MODELS[@]}"; do
        # Old method metrics
        OLD_PREDICTIONS="${OLD_METHOD_DIR}/sim_${replicate}/model_${stat_model}/overlap_predictions.txt"
        python3 "${BASE_DIR}/calculate_metrics.py" "${GROUND_TRUTH}" "${OLD_PREDICTIONS}" "${replicate}" "${stat_model}" >> "${METRICS_DIR}/all_metrics.csv"

        # New method metrics
        NEW_PREDICTIONS="${NEW_METHOD_DIR}/sim_${replicate}/model_${stat_model}/overlap_predictions.txt"
        python3 "${BASE_DIR}/calculate_metrics.py" "${GROUND_TRUTH}" "${NEW_PREDICTIONS}" "${replicate}" "${stat_model}" >> "${METRICS_DIR}/all_metrics.csv"
    done
done

#=====================================================================
# Generate visualizations using Python
#=====================================================================
echo "===== GENERATING VISUALIZATIONS ====="

cat > "${BASE_DIR}/generate_visualizations.py" << 'EOF'
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
        replicate_dirs = [d for d in os.listdir(simulations_dir) if d.startswith('sim_')]
        for replicate_dir in replicate_dirs:
            replicate = replicate_dir.split('_')[1]

            ground_truth_file = os.path.join(simulations_dir, replicate_dir, 'ground_truth.tsv')

            # Use model 2 by default (based on your output log)
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
EOF

chmod +x "${BASE_DIR}/generate_visualizations.py"

# Run visualization script
python3 "${BASE_DIR}/generate_visualizations.py" \
    "${METRICS_DIR}/all_metrics.csv" \
    "${PLOTS_DIR}" \
    "${SIMULATIONS_DIR}" \
    "${OLD_METHOD_DIR}" \
    "${NEW_METHOD_DIR}" \
    "${SEQ_LENGTH}"

#=====================================================================
# Generate final report
#=====================================================================
echo "===== GENERATING FINAL REPORT ====="

cat > "${RESULTS_DIR}/evaluation_report.md" << EOF
# Coevolution Detection Method Evaluation Report

## Overview
This report compares the performance of old and new coevolution detection methods across ${NUM_REPLICATES} simulation replicates.

## Simulation Parameters
- Number of Proteins: ${NUM_PROTEINS}
- Sequence Length: ${SEQ_LENGTH}
- Mutation Rate: ${MUTATION_RATE}
- Coevolution Strength: ${COEV_STRENGTH}
- Ground Truth Coevolving Pairs (0-based Python indexing): ${COEVOLVING_PAIRS}
- Ground Truth Coevolving Pairs (1-based coevolution code indexing): +1 to each position

## Indexing Note
This evaluation handles a critical indexing difference:
- Python simulator uses 0-based indexing (e.g., position "10" is the 11th position)
- Coevolution code uses 1-based indexing (e.g., position "11" is the 11th position)
- All conversions are handled internally by the evaluation pipeline

## Performance Summary
$(cat "${PLOTS_DIR}/summary_statistics.csv" 2>/dev/null || echo "Performance metrics calculation pending.")

## Visualizations
- Contact maps: See plots/contact_map_replicate_*.png
- Method comparison: See plots/method_comparison.png
- F1 score comparison: See plots/f1_score_boxplot.png

## Method Comparison
- Old method: Max parsimony for ancestral reconstruction
- New method: Max likelihood with ancestral sequences

EOF

echo "===== EVALUATION COMPLETE ====="
echo "Results available in ${BASE_DIR}"
echo "Final report: ${RESULTS_DIR}/evaluation_report.md"
echo "Performance visualizations: ${PLOTS_DIR}"