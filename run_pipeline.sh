#!/bin/bash
# Run FastML on aCES data to reconstruct ancestral states 

echo "Running Original Coevolution Code"
cd Original_Max_Parsimony_Code
./run_full_test.sh

echo "Running New Coevolution Code"
cd ..
cd New_Max_Likelihood_Coevolution_Code
./run_full_test.sh
cd ..

for letter in A
do
    for num_sequences in 200
    do
        echo "Evaluating Results between Original and New Coevolution Code for ${letter}_${num_sequences}_aces data"
        cd Evaluation
        python evaluate_results.py --data_diir "${letter}_${num_sequences}_aces"

    done
done

# for num_sequences in 200 300
# do
#     echo "Running FastML for ${letter}_${num_sequences}_aces data"
#     perl FastML.v3.11/www/fastml/FastML_Wrapper.pl -D --MSA_File /Users/salonijain/Documents/Spring_2025/Genomics_Project/Protein-Residue-Coevolution-Detection/aCES_data/${letter}_${num_sequences}_aces/1aoeA.fasta --outDir /Users/salonijain/Documents/Spring_2025/Genomics_Project/Protein-Residue-Coevolution-Detection/aCES_data/${letter}_${num_sequences}_aces --seqType aa --jointReconstruction --TreeAlg RAxML
# done

# Run modified coevolution code