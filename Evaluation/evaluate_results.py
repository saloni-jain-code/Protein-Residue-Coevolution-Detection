import argparse
import os
import pandas as pd

def parse_MI_pairs(aces_input_file):
    """
    Parse the MI file and extract the relevant information.
    """
    with open(aces_input_file, 'r') as f:
        lines = f.readlines()
    # print("len(lines): ", len(lines))
    MI_pairs = set()
    start_MI = False
    end_MI = False
    # Extract the relevant information from the lines
    for line in lines:
        if '@@START Mutual Information Definition' in line:
            # print("Found start of MI definition")
            start_MI = True
            continue
        elif '@@END Mutual Information Definition' in line:
            # print("Found end of MI definition")
            end_MI = True
            continue
        if start_MI and not end_MI and "MI" not in line:
            # print("Found MI line")
            MI_line = line.strip().split(', ')
            # print("MI_line: ", MI_line)
            MI_pairs.add((MI_line[1], MI_line[3]))
            MI_pairs.add((MI_line[3], MI_line[1])) # add in the opposite order just in case predictions file is in the opposite order

    return MI_pairs

def parse_filtered_predictions_pairs(filtered_predictions_file):
    with open(filtered_predictions_file, 'r') as f:
        lines = f.readlines()
    
    filtered_predictions_pairs = set()
    # print(f"len(lines): {len(lines)}")
    # Extract the relevant information from the lines
    for line in lines:
        line = line.strip()
        pairs = line.split('\t')
        # print(f"pairs: {pairs}")
        filtered_predictions_pairs.add(tuple(pairs))
    return filtered_predictions_pairs


def evaluate_results(filtered_predictions_pairs, MI_pairs):
    """
    filtered_predictions_pairs: set of pairs of filtered predictions
    MI_pairs: set of pairs of MI (ground truth)

    Evaluate the results of the predictions. Return the following
    metrics:
    - precision
    - recall
    - F1 score
    - accuracy
    - true positives
    - false positives
    - false negatives
    """
    true_positive_pairs = filtered_predictions_pairs & MI_pairs
    true_positives = len(true_positive_pairs)
    false_positives = len(filtered_predictions_pairs - MI_pairs)
    false_negatives = len(MI_pairs - filtered_predictions_pairs)

    accuracy = true_positives / len(MI_pairs) if len(MI_pairs) else 0

    precision = true_positives / (true_positives + false_positives) if (true_positives + false_positives) else 0
    recall    = true_positives / (true_positives + false_negatives) if (true_positives + false_negatives) else 0
    f1_score  = 2 * precision * recall / (precision + recall) if (precision + recall) else 0

    return precision, recall, f1_score, accuracy, true_positives, false_positives, false_negatives, true_positive_pairs

# given an outputs folder, iterate through the folders labeled 1 through 7 and check if it has 1aoeA_filtered_predictions.tsv
# if it does we ned to compare it to Everything_aCES/aces_data/A_Inputs/$data_dir_aces_input.txt



def main():
    # get file paths from command line arguments
    parser = argparse.ArgumentParser(description='Evaluate the results of the predictions.')
    parser.add_argument('--data_dir', type=str, required=True, help='name of data you want to evaluate')
    args = parser.parse_args()
    data_dir = args.data_dir
    # check if the data_dir is in the outputs folder
    if not os.path.exists(f'../New_Max_Likelihood_Coevolution_Code/outputs/{data_dir}_aces'):
        print(f'Data directory {data_dir} does not exist in the New_Max_Likelihood_Coevolution_Code/outputs folder.')
        return
    if not os.path.exists(f'../Original_Max_Parsimony_Code/outputs/{data_dir}_aces'):
        print(f'Data directory {data_dir} does not exist in the Original_Max_Parsimony_Code/outputs folder.')
        return
    os.makedirs("outputs", exist_ok=True)

    # create data_dir_df with columns statistical_model, precision_old, precision_new, recall, recall_new, f1_score_old, f1_score_new, accuracy_old, accuracy_new, true_positives_old, true_positives_new, false_positives_old, false_positives_new, false_negatives_old, false_negatives_new, true_positive_pairs_old, true_positive_pairs_new
    data_dir_df = pd.DataFrame(columns=['statistical_model', 'precision_old', 'precision_new', 'recall_old', 'recall_new', 'f1_score_old', 'f1_score_new', 'accuracy_old', 'accuracy_new', 'true_positives_old', 'true_positives_new', 'false_positives_old', 'false_positives_new', 'false_negatives_old', 'false_negatives_new', 'true_positive_pairs_old', 'true_positive_pairs_new'])

    aces_input_file = f'../Everything_aCES/aces_data/A_Inputs/{data_dir}_aces_input.txt'
    MI_pairs = parse_MI_pairs(aces_input_file)
    # print(f"MI pairs: {MI_pairs}")
    # now go through Original_Max_Parsimony_Code/outputs and find the filtered_predictions.tsv file in each statistical model folder labeled 1 through 7
    for statistical_model in range(1, 8):
        og_statistical_model_folder = f'../Original_Max_Parsimony_Code/outputs/{data_dir}_aces/{statistical_model}'
        if not os.path.exists(og_statistical_model_folder):
            print(f'Statistical model folder {og_statistical_model_folder} does not exist.')
            continue
        # check if 1aoeA_filtered_predictions.tsv exists in the folder
        if not os.path.exists(f'{og_statistical_model_folder}/1aoeA_filtered_predictions.tsv'):
            print(f'Filtered predictions file does not exist in {og_statistical_model_folder}.')
            continue
        og_filtered_predictions_file = f'{og_statistical_model_folder}/1aoeA_filtered_predictions.tsv'
        og_filtered_predictions_pairs = parse_filtered_predictions_pairs(og_filtered_predictions_file)
        # print(f"og_filtered_predictions_pairs: {og_filtered_predictions_pairs}")
        precision_old, recall_old, f1_score_old, accuracy_old, true_positives_old, false_positives_old, false_negatives_old, true_positive_pairs_old = evaluate_results(og_filtered_predictions_pairs, MI_pairs)

        new_statistical_model_folder = f'../New_Max_Likelihood_Coevolution_Code/outputs/{data_dir}_aces/{statistical_model}'
        if not os.path.exists(new_statistical_model_folder):
            print(f'Statistical model folder {new_statistical_model_folder} does not exist.')
            continue
        # check if 1aoeA_filtered_predictions.tsv exists in the folder
        if not os.path.exists(f'{new_statistical_model_folder}/1aoeA_filtered_predictions.tsv'):
            print(f'Filtered predictions file does not exist in {new_statistical_model_folder}.')
            continue
        new_filtered_predictions_file = f'{new_statistical_model_folder}/1aoeA_filtered_predictions.tsv'
        new_filtered_predictions_pairs = parse_filtered_predictions_pairs(new_filtered_predictions_file)
        # print(f"new_filtered_predictions_pairs: {new_filtered_predictions_pairs}")
        precision, recall, f1_score, accuracy, true_positives, false_positives, false_negatives, true_positive_pairs = evaluate_results(new_filtered_predictions_pairs, MI_pairs)

        # each row in the dataframe is for a different statistical model
        new_row = pd.DataFrame([{
            'statistical_model': statistical_model,
            'precision_old': precision_old,
            'precision_new': precision,
            'recall_old': recall_old,
            'recall_new': recall,
            'f1_score_old': f1_score_old,
            'f1_score_new': f1_score,
            'accuracy_old': accuracy_old,
            'accuracy_new': accuracy,
            'true_positives_old': true_positives_old,
            'true_positives_new': true_positives,
            'false_positives_old': false_positives_old,
            'false_positives_new': false_positives,
            'false_negatives_old': false_negatives_old,
            'false_negatives_new': false_negatives,
            'true_positive_pairs_old': true_positive_pairs_old,
            'true_positive_pairs_new': true_positive_pairs
        }])
        if data_dir_df.empty:
            data_dir_df = new_row
        else:
            data_dir_df = pd.concat([data_dir_df, new_row], ignore_index=True)

        # save as csv file
    data_dir_df.to_csv(f"outputs/{data_dir}_evaluation_results.csv", index=False)


if __name__ == '__main__':
    main()

