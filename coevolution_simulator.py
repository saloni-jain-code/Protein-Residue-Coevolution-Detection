#!/usr/bin/env python3
"""
Fixed MSA and Evolutionary Tree Generator with Coevolution

This script generates a random Multiple Sequence Alignment (MSA) and 
evolutionary tree with user-specified coevolving pairs of positions.
It includes ancestral sequences in the output FASTA file and tree.
"""

import argparse
import random
import numpy as np
import os
import sys
from Bio import SeqIO
from Bio.Align import MultipleSeqAlignment
from Bio.SeqRecord import SeqRecord
from Bio.Seq import Seq
import ast

# Amino acid alphabet
AA_ALPHABET = "ACDEFGHIKLMNPQRSTVWY"

# Co-evolution pairs (amino acid groups that can co-evolve)
COEVOLUTION_GROUPS = [
    ["A", "I", "L", "M", "V"],  # Hydrophobic
    ["F", "W", "Y"],            # Aromatic
    ["N", "Q", "S", "T"],       # Polar uncharged
    ["H", "K", "R"],            # Positively charged
    ["D", "E"],                 # Negatively charged
    ["C", "G", "P"]             # Special cases
]

class TreeNode:
    """Simple tree node implementation"""
    def __init__(self, name=None):
        self.name = name
        self.children = []
        self.parent = None
        self.dist = 0.1  # Default branch length
        
    def add_child(self, child=None, name=None):
        if child is None:
            child = TreeNode(name=name)
        child.parent = self
        self.children.append(child)
        return child
    
    def is_root(self):
        return self.parent is None
        
    def is_leaf(self):
        return len(self.children) == 0
    
    def get_leaves(self):
        """Get all leaf nodes in the tree"""
        if self.is_leaf():
            return [self]
        leaves = []
        for child in self.children:
            leaves.extend(child.get_leaves())
        return leaves
    
    def get_all_nodes(self):
        """Get all nodes in the tree including internal nodes"""
        nodes = [self]
        for child in self.children:
            nodes.extend(child.get_all_nodes())
        return nodes
    
    def print_tree(self, level=0):
        """Print tree structure for debugging"""
        indent = "  " * level
        print(f"{indent}{self.name}")
        for child in self.children:
            child.print_tree(level + 1)
    
    def to_newick(self):
        """Convert tree to Newick format string"""
        if self.is_leaf():
            return f"{self.name}:{self.dist}"
        
        newick = "("
        newick += ",".join([child.to_newick() for child in self.children])
        if self.is_root():
            newick += f"){self.name};"
        else:
            newick += f"){self.name}:{self.dist}"
        return newick

def parse_arguments():
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(description='Generate a random MSA and evolutionary tree with coevolving positions.')
    parser.add_argument('--num_proteins', type=int, required=True, help='Number of proteins in the MSA')
    parser.add_argument('--seq_length', type=int, required=True, help='Length of each protein sequence')
    parser.add_argument('--coevolving_pairs', type=str, required=True, 
                        help='List of tuples with coevolving positions, e.g., "[(0,5), (10,15)]"')
    parser.add_argument('--output_prefix', type=str, default='output', 
                        help='Prefix for output files')
    parser.add_argument('--mutation_rate', type=float, default=0.1,
                        help='Probability of mutation at each position (default: 0.05)')
    parser.add_argument('--coev_strength', type=float, default=1d,
                        help='Strength of coevolution (0-1, where 1 is perfect correlation) (default: 0.8)')
    return parser.parse_args()

def create_balanced_tree(num_proteins):
    """Create a balanced bifurcating tree."""
    # Start with root node
    root = TreeNode(name="N1")
    
    # Add extant sequences
    internal_node_count = 1
    leaves = [root]
    
    # Keep splitting leaves until we have enough
    while len(leaves) < num_proteins:
        # Take the first leaf and split it
        leaf = leaves.pop(0)
        
        # Add two internal nodes
        internal_node_count += 1
        child1 = leaf.add_child(name=f"Seq_{len(leaves) + 1}")
        
        internal_node_count += 1
        child2 = leaf.add_child(name=f"Seq_{len(leaves) + 2}")
        
        # Update the name of the internal node
        if leaf.name == "N1":
            # Keep the root name as N1
            pass
        else:
            # Update other internal nodes to follow N# pattern
            leaf.name = f"N{internal_node_count}"
        
        # Add new leaves to the list
        leaves.append(child1)
        leaves.append(child2)
    
    # If we have too many leaves, remove extras
    while len(leaves) > num_proteins:
        leaves.pop()
    
    # Rename leaves to ensure we have Seq_1 through Seq_num_proteins
    for i, leaf in enumerate(root.get_leaves(), 1):
        if i <= num_proteins:
            leaf.name = f"Seq_{i}"
    
    # Assign variable branch lengths
    for node in root.get_all_nodes():
        if not node.is_root():
            node.dist = random.uniform(0.05, 0.2)
    
    return root, internal_node_count

def find_coevolution_group(aa):
    """Find which coevolution group an amino acid belongs to."""
    for group_idx, group in enumerate(COEVOLUTION_GROUPS):
        if aa in group:
            return group_idx
    return -1  # Not found in any group

def generate_msa_with_coevolution(tree, seq_length, coevolving_pairs, mutation_rate, coev_strength):
    """Generate MSA with coevolving positions based on the tree."""
    # Parse coevolving pairs if it's a string
    if isinstance(coevolving_pairs, str):
        coevolving_pairs = ast.literal_eval(coevolving_pairs)
    
    # Generate a random root sequence
    root_seq = ''.join(random.choice(AA_ALPHABET) for _ in range(seq_length))
    
    # Dictionary to store sequences for each node
    sequences = {tree.name: root_seq}
    
    # Process each node in the tree
    nodes_to_process = [(tree, None)]  # (node, parent)
    
    while nodes_to_process:
        node, parent = nodes_to_process.pop(0)
        
        # Skip the root as it already has a sequence
        if parent is not None:
            parent_seq = sequences[parent.name]
            child_seq = list(parent_seq)  # Convert to list for easier manipulation
            
            # Introduce mutations
            for pos in range(seq_length):
                if random.random() < mutation_rate:  # Random mutation at this position
                    # Check if this position is in a coevolving pair
                    in_coev_pair = False
                    paired_pos = None
                    
                    for pair in coevolving_pairs:
                        if pos == pair[0]:
                            in_coev_pair = True
                            paired_pos = pair[1]
                            break
                        elif pos == pair[1]:
                            in_coev_pair = True
                            paired_pos = pair[0]
                            break
                    
                    if in_coev_pair and random.random() < coev_strength:
                        # This is a coevolving position and we're applying coevolution
                        current_aa = parent_seq[pos]
                        paired_aa = parent_seq[paired_pos]
                        
                        # Find the group these amino acids belong to
                        current_group = find_coevolution_group(current_aa)
                        paired_group = find_coevolution_group(paired_aa)
                        
                        # Correlate the mutations
                        if current_group != -1 and paired_group != -1:
                            # Get new amino acids from the same groups
                            new_aa = random.choice(COEVOLUTION_GROUPS[current_group])
                            new_paired_aa = random.choice(COEVOLUTION_GROUPS[paired_group])
                            
                            child_seq[pos] = new_aa
                            child_seq[paired_pos] = new_paired_aa
                        else:
                            # If not in defined groups, just make random mutations
                            child_seq[pos] = random.choice(AA_ALPHABET.replace(parent_seq[pos], ''))
                            child_seq[paired_pos] = random.choice(AA_ALPHABET.replace(parent_seq[paired_pos], ''))
                    else:
                        # Regular random mutation
                        child_seq[pos] = random.choice(AA_ALPHABET.replace(parent_seq[pos], ''))
            
            sequences[node.name] = ''.join(child_seq)
        
        # Add children to the processing queue
        for child in node.children:
            nodes_to_process.append((child, node))
    
    return sequences

def write_fasta(sequences, output_file):
    """Write sequences to a FASTA file."""
    with open(output_file, 'w') as f:
        for name, seq in sequences.items():
            f.write(f">{name}\n{seq}\n")

def write_phylip(sequences, output_file):
    """Write sequences to a Phylip file."""
    with open(output_file, 'w') as f:
        # Write Phylip header (number of sequences and sequence length)
        f.write(f" {len(sequences)} {len(next(iter(sequences.values())))}\n")
        
        # Write sequences in Phylip format
        for name, seq in sequences.items():
            # Ensure the ID is padded to 10 characters for standard Phylip format
            padded_id = name.ljust(10)[:10]
            f.write(f"{padded_id} {seq}\n")

def write_tree(tree, output_file):
    """Write tree to a Newick file with all node labels."""
    with open(output_file, 'w') as f:
        f.write(tree.to_newick())

def write_extant_only_phylip(sequences, output_file):
    """Write only extant sequences to a Phylip file."""
    # Filter sequences to include only those starting with "Seq_"
    extant_sequences = {name: seq for name, seq in sequences.items() if name.startswith("Seq_")}
    
    with open(output_file, 'w') as f:
        # Write Phylip header (number of sequences and sequence length)
        f.write(f" {len(extant_sequences)} {len(next(iter(extant_sequences.values())))}\n")
        
        # Write sequences in Phylip format
        for name, seq in extant_sequences.items():
            # Ensure the ID is padded to 10 characters for standard Phylip format
            padded_id = name.ljust(10)[:10]
            f.write(f"{padded_id} {seq}\n")

def write_extant_only_fasta(sequences, output_file):
    """Write only extant sequences to a FASTA file."""
    # Filter sequences to include only those starting with "Seq_"
    extant_sequences = {name: seq for name, seq in sequences.items() if name.startswith("Seq_")}
    
    with open(output_file, 'w') as f:
        for name, seq in extant_sequences.items():
            f.write(f">{name}\n{seq}\n")

def create_extant_only_tree(tree):
    """Create a version of the tree with only extant sequences."""
    # Create a new tree with just the extant sequences
    extant_tree = TreeNode(name="Root")
    
    # Get all leaf nodes from original tree
    leaves = tree.get_leaves()
    
    # Add each leaf directly under the root in the new tree
    for leaf in leaves:
        extant_tree.add_child(name=leaf.name)
    
    # Set random branch lengths
    for node in extant_tree.get_all_nodes():
        if not node.is_root():
            node.dist = random.uniform(0.05, 0.2)
    
    return extant_tree

def write_extant_only_tree(tree, output_file):
    """Write a tree with only extant sequences to a Newick file."""
    extant_tree = create_extant_only_tree(tree)
    with open(output_file, 'w') as f:
        f.write(extant_tree.to_newick())

def main():
    # Set up for IDLE - comment these out when running from command line
    if 'idlelib' in sys.modules:
        sys.argv = [
            'coevolution_simulator.py',
            '--num_proteins', '10',
            '--seq_length', '100',
            '--coevolving_pairs', '[(0,5), (10,15), (20,30)]',
            '--output_prefix', 'my_simulation'
        ]
    
    # Parse arguments
    args = parse_arguments()
    
    # Create output directory if it doesn't exist
    output_dir = os.path.dirname(args.output_prefix)
    if output_dir and not os.path.exists(output_dir):
        os.makedirs(output_dir)
    
    # Create a balanced tree
    print("Generating evolutionary tree...")
    tree, internal_node_count = create_balanced_tree(args.num_proteins)
    
    # For debugging - print the tree structure
    print("Tree structure:")
    tree.print_tree()
    
    # Generate sequences with coevolution
    print("Generating sequences with coevolution...")
    sequences = generate_msa_with_coevolution(
        tree, 
        args.seq_length, 
        args.coevolving_pairs, 
        args.mutation_rate, 
        args.coev_strength
    )
    
    # Verify sequences were generated for all nodes
    print(f"Generated {len(sequences)} sequences (expected {args.num_proteins + internal_node_count})")
    
    # Write FASTA file with all sequences (including ancestors)
    fasta_file = f"{args.output_prefix}.fasta"
    print(f"Writing all sequences to {fasta_file}...")
    write_fasta(sequences, fasta_file)
    
    # Write Phylip file with all sequences
    phylip_file = f"{args.output_prefix}.phy"
    print(f"Writing all sequences MSA to {phylip_file}...")
    write_phylip(sequences, phylip_file)
    
    # Write tree file with all nodes
    tree_file = f"{args.output_prefix}.newick"
    print(f"Writing complete tree to {tree_file}...")
    write_tree(tree, tree_file)
    
    # Write extant-only versions
    extant_fasta_file = f"{args.output_prefix}_extant.fasta"
    print(f"Writing extant-only sequences to {extant_fasta_file}...")
    write_extant_only_fasta(sequences, extant_fasta_file)
    
    extant_phylip_file = f"{args.output_prefix}_extant.phy"
    print(f"Writing extant-only MSA to {extant_phylip_file}...")
    write_extant_only_phylip(sequences, extant_phylip_file)
    
    extant_tree_file = f"{args.output_prefix}_extant.newick"
    print(f"Writing extant-only tree to {extant_tree_file}...")
    write_extant_only_tree(tree, extant_tree_file)
    
    # Print summary
    print("\nSummary:")
    print(f"  Number of sequences: {len(sequences)}")
    print(f"  Number of extant sequences: {args.num_proteins}")
    print(f"  Number of ancestral sequences: {internal_node_count}")
    print(f"  Sequence length: {args.seq_length}")
    print(f"  Coevolving pairs: {args.coevolving_pairs}")
    print("\nFull MSA and tree (including ancestors):")
    print(f"  FASTA file: {fasta_file}")
    print(f"  Phylip file: {phylip_file}")
    print(f"  Tree file: {tree_file}")
    print("\nExtant-only MSA and tree:")
    print(f"  FASTA file: {extant_fasta_file}")
    print(f"  Phylip file: {extant_phylip_file}")
    print(f"  Tree file: {extant_tree_file}")
    
    print("\nDone.")

if __name__ == "__main__":
    main()
