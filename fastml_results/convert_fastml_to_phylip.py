#!/usr/bin/env python3
"""
Convert FastML seq.joint.txt output to single-line PHYLIP format for MSA input
"""
from Bio import SeqIO
from Bio.Seq import Seq
from Bio.SeqRecord import SeqRecord
import sys
import os

def convert_fastml_to_phylip(input_file, output_file):
    """Convert FastML seq.joint.txt to single-line PHYLIP format"""
    # Read the FastML output file
    seqs = []
    seq_names = []

    with open(input_file, 'r') as f:
        current_seq = ""
        for line in f:
            line = line.strip()
            if line.startswith('>'):  # Sequence name line
                if current_seq and seq_names:  # If we've finished reading a sequence
                    seqs.append(current_seq)
                    current_seq = ""
                seq_names.append(line[1:])  # Remove the '>' character
            else:  # Sequence data line
                current_seq += line

        # Add the last sequence
        if current_seq:
            seqs.append(current_seq)

    # Write to PHYLIP format manually to ensure single-line format
    with open(output_file, 'w') as f:
        # Write header: number of sequences and sequence length
        f.write(f"{len(seqs)} {len(seqs[0])}\n")

        # Write sequences
        for i, (name, seq) in enumerate(zip(seq_names, seqs)):
            # Pad name to ensure alignment (typically 10 characters in PHYLIP)
            padded_name = name.ljust(10)
            f.write(f"{padded_name}{seq}\n")

    print(f"Converted {len(seqs)} sequences from {input_file} to single-line PHYLIP format at {output_file}")

def main():
    if len(sys.argv) != 3:
        print("Usage: python convert_fastml_to_phylip.py <input_file> <output_file>")
        sys.exit(1)

    input_file = sys.argv[1]
    output_file = sys.argv[2]

    convert_fastml_to_phylip(input_file, output_file)

if __name__ == "__main__":
    main()
