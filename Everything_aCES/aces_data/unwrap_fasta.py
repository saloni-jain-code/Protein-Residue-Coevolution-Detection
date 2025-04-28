#!/usr/bin/env python3
import sys

inp, outp = sys.argv[1:3]
with open(inp) as fin, open(outp, "w") as fout:
    seq = ""
    for line in fin:
        if line.startswith(">"):
            if seq:
                fout.write(seq + "\n")
            fout.write(line.rstrip() + "\n")
            seq = ""
        else:
            seq += line.strip()
    if seq:
        fout.write(seq + "\n")
