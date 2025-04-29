import argparse

def parse_MI_pairs(aces_input_file):
    """
    Parse the MI file and extract the relevant information.
    """
    with open(aces_input_file, 'r') as f:
        lines = f.readlines()

    MI_pairs = set()
    start_MI = False
    end_MI = False
    # Extract the relevant information from the lines
    for line in lines:
        if '@@START Mutual Information Defintion' in line:
            start_MI = True
        elif '@@END Mutual Information Defintion' in line:
            end_MI = True
        if start_MI and not end_MI and "MI" not in line:
            MI_line = line.strip().split(',')
            MI_pairs.add((MI_line[1], MI_line[3]))
            MI_pairs.add((MI_line[3], MI_line[1])) # add in the opposite order just in case predictions file is in the opposite order

    return MI_pairs

def parse_filtered_predictions_pairs(filtered_predictions_file):
    with open(filtered_predictions_file, 'r') as f:
        lines = f.readlines()
    
    filtered_predictions_pairs = set()
    # Extract the relevant information from the lines
    for line in lines:
        if 'filtered_predictions' in line:
            pairs = line.split('\t')
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
    true_positives = len(filtered_predictions_pairs & MI_pairs)
    false_positives = len(filtered_predictions_pairs - MI_pairs)
    false_negatives = len(MI_pairs - filtered_predictions_pairs)

    accuracy = true_positives / len(MI_pairs)

    precision = true_positives / (true_positives + false_positives) if (true_positives + false_positives) else 0
    recall    = true_positives / (true_positives + false_negatives) if (true_positives + false_negatives) else 0
    f1_score  = 2 * precision * recall / (precision + recall) if (precision + recall) else 0

    return precision, recall, f1_score, accuracy, true_positives, false_positives, false_negatives

def main():
    # get file paths from command line arguments
    parser = argparse.ArgumentParser(description='Evaluate the results of the predictions.')
    parser.add_argument('--aces_input_file', type=str, required=True, help='Path to the ACES input file')
    parser.add_argument('--filtered_predictions_file', type=str, required=True, help='Path to the filtered predictions file')
    args = parser.parse_args()
    aces_input_file = args.aces_input_file
    filtered_predictions_file = args.filtered_predictions_file
    MI_pairs = parse_MI_pairs(aces_input_file)
    filtered_predictions_pairs = parse_filtered_predictions_pairs(filtered_predictions_file)
    precision, recall, f1_score, accuracy, true_positives, false_positives, false_negatives = evaluate_results(filtered_predictions_pairs, MI_pairs)
    print(f'Precision: {precision}')
    print(f'Recall: {recall}')
    print(f'F1 score: {f1_score}')
    print(f'Accuracy: {accuracy}')
    print(f'Number of filtered predictions pairs: {len(filtered_predictions_pairs)}')


if __name__ == '__main__':
    main()

