# Optimizing a Tree-Aware Method of Protein Residue Coevolution Detection

## Requirements
#### FastML
- perl
- RaxML
- BioPerl

#### Coevolution Detection Code
- Java (> version 20)
- R
    - foreach
    - MASS
    - car
    - mgcv
    - phylotools


#### Evaluation
- python3
    - pandas
    - matplotlib
    - numpy

## Input Data

Originally, aCES was used to generate an MSA with coevolving sites, and the data we initially generated can be found in aCES_data. To create the aCES input files, you can use the input file maker. Navigate to `Everything_aCES/aCES_Input` and type the following into the terminal to run the CLI for creating aCES input files:
```
python aces_input_builder.py
```
After you create the aCES input file, run the following from the `aces_data` directory:
```
python renumber_fasta.py A_Outputs/txt/[name of output file].txt output.fasta
python unwrap_fasta.py output.fasta [name of output file].fasta
```

However, aCES does not work as expected-- it doesn't properly generate the coevolving sites we specify. 
As such, we created a different MSA generator which can be run in the terminal from the repo's root directory:
```
python coevolution_simulator.py
```
This will create input files in coevolution_evaluation/simulations.


## Running Original Max Parsimony Coevolution Code
To run the original maximum parsimony code, you can modify the file paths it takes in for the MSA file, tree file, and output file by directly modifying the shell script in `Original_Max_Parsimony_Code/run_full_test.sh`
```
echo "MSA_ALIGNMENT_FILE [filepath for .phy MSA file]" > 1aoeA.ctl
echo "TREE_FILE [filepath for newick tree file]" >> 1aoeA.ctl
echo "OUTPUT_FILE [filepath for output file]" >> 1aoeA.ctl
```

For example:
```
echo "MSA_ALIGNMENT_FILE ../aCES_data/$data_dir/results_extant.phy" > 1aoeA.ctl
echo "TREE_FILE ../aCES_data/$data_dir/results_extant.newick" >> 1aoeA.ctl
echo "OUTPUT_FILE outputs/${data_dir}/$statistical_model/mps_1aoeA" >> 1aoeA.ctl
```
Then to run the code, navigate to the `Original_Max_Parsimony_Code` folder, and run:
```
./run_full_test.sh
```
This will calculate maximum parsimony, and run all 7 statistical models. This will create 7 different folders under `outputs/$data_dir` for each of the statistical models, and the predictions will appear in `outputs/$data_dir/$statistical_model/1aoeA_filtered_predictions.tsv`.


## Running New Max Likelihood Coevolution Code
To run the new maximum likelihood coevolution code, 

Then, similar to the original max parsimony code, you can modify the file paths it takes in for the MSA file, tree file, and output file by directly modifying the shell script in `New_Max_Likelihood_Coevolution_Code/run_full_test.sh`
```
echo "MSA_ALIGNMENT_FILE [filepath for .phy MSA file]" > 1aoeA.ctl
echo "TREE_FILE [filepath for newick tree file]" >> 1aoeA.ctl
echo "OUTPUT_FILE [filepath for output file]" >> 1aoeA.ctl
echo "ANCESTRAL_FILE [filepath for ancestral reconstruction file created by FastML (usually seq.joint.txt)]" >> 1aoeA.ctl
```

For example:
```
echo "MSA_ALIGNMENT_FILE ../aCES_data/$data_dir/results.phy" > 1aoeA.ctl
echo "TREE_FILE  ../aCES_data/$data_dir/results.newick" >> 1aoeA.ctl
echo "OUTPUT_FILE outputs/$data_dir/$statistical_model/1aoeA" >> 1aoeA.ctl
echo "ANCESTRAL_FILE ../aCES_data/$data_dir/results.fasta" >> 1aoeA.ctl
```

Then to run the code, navigate to the `New_Max_Likelihood_Coevolution_Code` folder, and run:
```
./run_full_test.sh
```
This will create 7 different folders under `outputs/$data_dir` for each of the statistical models, and the predictions will appear in `outputs/$data_dir/$statistical_model/1aoeA_filtered_predictions.tsv`.

## Running the Entire Pipeline


