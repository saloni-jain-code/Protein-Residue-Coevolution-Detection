#!/usr/bin/env python3
"""
renumber_fasta.py  input.fasta  output.fasta

Replaces each header with >1, >2, ... but keeps sequences verbatim.
"""

import sys

if len(sys.argv) != 3:
    sys.exit("Usage: renumber_fasta.py <input.fasta> <output.fasta>")

infile, outfile = sys.argv[1:3]
counter = 0

with open(infile) as fin, open(outfile, "w") as fout:
    for line in fin:
        if line.startswith(">"):
            counter += 1
            fout.write(f">{counter}\n")
        else:
            fout.write(line)
            

# import sys, fileinput

# counter = 0
# for line in fileinput.input():
#     if line.startswith(">"):
#         counter += 1
#         print(f">{counter}")
#     else:
#         print(line.rstrip())
