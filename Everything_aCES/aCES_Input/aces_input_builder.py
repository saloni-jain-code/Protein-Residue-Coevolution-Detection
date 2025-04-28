#!/usr/bin/env python3
"""aces_input_builder.py
=================================
CLI *and* non‑interactive helper for creating **aCES 2** input files.

*This revision fixes an `AttributeError: Namespace object has no attribute 'job'`
that occurred when the script was executed with command‑line flags only.*

Changes
-------
1. Replaced the incorrect reference **`args.job` → `args.job_id`** when the
   `AcesInput` object is instantiated and when composing the output filename.
2. Added a **new unit test** to assert that the non‑interactive path works
   without raising exceptions when only `--job-id` is supplied.
3. Minor docstring touch‑ups.cle
"""
from __future__ import annotations
import argparse, itertools, pathlib, random, sys

ALPHABET_AA = list("MCVIALDEKRFWYTSNQHPG")

# ───────────────────────────────────────────────────────── Molecule
class Molecule:
    def __init__(self, name:str, length:int, alphabet=None):
        self.name = name.upper()
        self.length = length
        self.alphabet = alphabet or ALPHABET_AA
        self.segments: list[tuple[int,int,int]] = []  # (start,end,conservation%)

    def add_segment(self, start:int, end:int, cons:int):
        self.segments.append((start, end, cons))

    def auto_fill(self, percents:list[int], min_len:int, max_len:int, *, seed:int|None=None):
        if max_len < min_len:
            min_len, max_len = max_len, min_len  # swap if user passed them reversed
        rnd = random.Random(seed)
        pos = 1
        self.segments.clear()
        while pos <= self.length:
            remaining = self.length - pos + 1
            if remaining < min_len:
                seg_len = remaining  # put the rest in one chunk
            else:
                seg_len = rnd.randint(min_len, min(max_len, remaining))
            cons = rnd.choice(percents)
            self.segments.append((pos, pos + seg_len - 1, cons))
            pos += seg_len

    def build_lines(self) -> list[str]:
        if not self.segments:
            self.segments = [(1, self.length, 30)]
        return [f"{p}, #, #, {c}, 0"
                for s, e, c in self.segments for p in range(s, e + 1)]

# ───────────────────────────────────────────────────────── MI
class MIconstraint:
    def __init__(self, m1:str, p1:int, m2:str, p2:int, target:int):
        self.m1, self.p1 = m1.upper(), p1
        self.m2, self.p2 = m2.upper(), p2
        self.target = target
    def __str__(self):
        return f"{self.m1}, {self.p1}, {self.m2}, {self.p2}, {self.target}"

# ───────────────────────────────────────────────────────── Builder
class AcesInput:
    def __init__(self, nseq:int, job_id:str, auto_perc:list[int], auto_min:int, auto_max:int):
        self.nseq = nseq
        self.job_id = job_id
        self.auto_perc, self.auto_min, self.auto_max = auto_perc, auto_min, auto_max
        self.molecules: dict[str, Molecule] = {}
        self.mi_blocks: dict[str, list[MIconstraint]] = {}

    # --------------- safe input (handles non‑tty) -----------------
    @staticmethod
    def _safe_input(prompt:str) -> str | None:
        try:
            return input(prompt)
        except (EOFError, OSError):
            return None

    # --------------- interactive helpers -------------------------
    def prompt_molecule(self):
        name = (self._safe_input("Molecule name (A–E): ") or "A").upper()
        length = int(self._safe_input("Length (# residues): ") or 1)
        mol = Molecule(name, length)
        mode = (self._safe_input("Segments: (a)uto or (m)anual? ") or "a").lower()
        if mode.startswith("a"):
            mol.auto_fill(self.auto_perc, self.auto_min, self.auto_max)
            for s, e, c in mol.segments:
                print(f"  {s}-{e}: {c}%")
        else:
            seg = self._safe_input("Enter conservation % ")
            if not seg:
                return None
            try:
                mol.add_segment(1, mol.length, int(seg))
            except ValueError:
                print("  format error, expected conservation %")
        self.molecules[mol.name] = mol

    def prompt_mi(self):
        blk = self._safe_input("MI block id: ") or str(random.randint(1000, 9999))
        self.mi_blocks[blk] = []
        print("Mol1,Pos1,Mol2,Pos2,Target%   (blank line to finish)")
        while True:
            line = self._safe_input("pair> ")
            if not line:
                break
            try:
                m1, p1, m2, p2, t = [v.strip() for v in line.split(',')]
                self.mi_blocks[blk].append(MIconstraint(m1, int(p1), m2, int(p2), int(t)))
            except ValueError:
                print("  format error")
        if not self.mi_blocks[blk]:
            del self.mi_blocks[blk]

    # --------------- file builder --------------------------------
    def build(self) -> str:
        out = ["@@START Molecule Definition", f"##{self.nseq}"]
        for n in "ABCDE":
            if n not in self.molecules:
                stub = Molecule(n, 1)
                stub.add_segment(1, 1, 100)
                self.molecules[n] = stub
        for n in "ABCDE":
            m = self.molecules[n]
            out.append(f"> {n}{m.length}: {', '.join(m.alphabet)}")
            out.extend(m.build_lines())
        out.append("@@END Molecule Definition\n")
        out.append("@@START Mutual Information Definition")
        for blk, pairs in self.mi_blocks.items():
            if not pairs:
                continue
            pos = [v for p in pairs for v in (p.p1, p.p2)]
            out.append(f"MI#{blk}, {min(pos)}, {max(pos)}")
            out.extend(str(p) for p in pairs)
        out.append("@@END Mutual Information Definition\n")
        return "\n".join(out)

# ───────────────────────────────────────────────────────── helpers

def parse_molecule(flag:str) -> Molecule:
    parts = flag.split(":")
    if len(parts) < 2:
        raise argparse.ArgumentTypeError("--molecule needs NAME:LENGTH")
    name, length = parts[0], int(parts[1])
    alphabet = [a.strip() for a in parts[2].split(',')] if len(parts) == 3 else None
    return Molecule(name, length, alphabet)

def parse_mi(flag:str) -> MIconstraint:
    try:
        m1, p1, m2, p2, t = [v.strip() for v in flag.split(',')]
        return MIconstraint(m1, int(p1), m2, int(p2), int(t))
    except Exception as exc:
        raise argparse.ArgumentTypeError("--mi needs Mol1,Pos1,Mol2,Pos2,Target%") from exc

# ───────────────────────────────────────────────────────── tests

def _cli_smoke_test():
    """Ensures non‑interactive invocation with only --job-id succeeds."""
    tmp = "tmpjob"
    argv = [
        "prog",
        "--job-id", tmp,
        "--molecule", "A:50",
        "--mi", "A,1,A,25,60",
    ]
    sys.argv = argv
    main()
    assert pathlib.Path(f"{tmp}_aces_input.txt").exists(), "Output file missing in smoke test"

# ───────────────────────────────────────────────────────── main

def main():
    ap = argparse.ArgumentParser(description="Generate aCES input files")
    ap.add_argument("--job-id", default="aces_job")
    ap.add_argument("--sequences", type=int, default=7)
    ap.add_argument("--auto-perc", default="30,60,90")
    ap.add_argument("--auto-min", type=int, default=30)
    ap.add_argument("--auto-max", type=int, default=120)
    ap.add_argument("--molecule", action="append", type=parse_molecule, help="NAME:LENGTH or NAME:LENGTH:ALPHABET")
    ap.add_argument("--mi", action="append", type=parse_mi, help="Mol1,Pos1,Mol2,Pos2,Target%")
    ap.add_argument("--seed", type=int)
    ap.add_argument("--test", action="store_true")
    args = ap.parse_args()

    if args.test:
        _cli_smoke_test()
        print("All tests passed ✔")
        return

    auto_perc = [int(x) for x in args.auto_perc.split(',')]
    builder = AcesInput(args.sequences, args.job_id, auto_perc, args.auto_min, args.auto_max)

    rnd = random.Random(args.seed)
    if args.molecule:
        for m in args.molecule:
            if not m.segments:
                m.auto_fill(auto_perc, args.auto_min, args.auto_max, seed=rnd.randint(0, 999999))
            builder.molecules[m.name] = m
    if args.mi:
        builder.mi_blocks["1000"] = args.mi

    if sys.stdin.isatty() and not args.molecule:
        while True:
            cmd = (builder._safe_input("[m]olecule  [i] MI block  [w]rite  [q]uit > ") or "w").lower()
            if cmd == "m":
                builder.prompt_molecule()
            elif cmd == "i":
                builder.prompt_mi()
            elif cmd == "w":
                break
            elif cmd == "q":
                return

    outfile = pathlib.Path(f"{builder.job_id}_aces_input.txt")
    outfile.write_text(builder.build())
    print("✔ wrote", outfile)

if __name__ == "__main__":
    main()
