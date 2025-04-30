for letter in A B C
do
    for num_sequences in 200 300
    do
        echo "Running FastML for ${letter}_${num_sequences}_aces data"
        perl FastML.v3.11/www/fastml/FastML_Wrapper.pl -D --MSA_File /Users/salonijain/Documents/Spring_2025/Genomics_Project/Protein-Residue-Coevolution-Detection/aCES_data/${letter}_${num_sequences}_aces/1aoeA.fasta --outDir /Users/salonijain/Documents/Spring_2025/Genomics_Project/Protein-Residue-Coevolution-Detection/aCES_data/${letter}_${num_sequences}_aces --seqType aa --jointReconstruction --TreeAlg
    done
done
