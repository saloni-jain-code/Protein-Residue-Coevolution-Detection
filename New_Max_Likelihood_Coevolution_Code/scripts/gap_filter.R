library(phylotools)

# Read command line arguments:
# 1 Input PHYLIP tree
# 2 Input predicted coevolving pairs file
# 3 Output file name
cmd_args <- commandArgs(trailingOnly = TRUE)

phylip_file <- cmd_args[1]
pairs_file <- cmd_args[2]
output_file <- cmd_args[3]

allpass <- list()

# Filter any predicted pairs where the majority of implicated residues are "-"
phylip_aln <- read.phylip(phylip_file)
    
# Read in predicted pair locations
pairpos <- read.delim2(pairs_file, header = FALSE, sep = "\t")
    
passlist <- c()
pairs <- seq(1, dim(pairpos)[1])

# For each predicted pair count the ancestor node states and save only if there
# are not more than 20% gaps for those positions in the alignment
for(i in pairs)
{
  AAcombos <- table(paste(substring(phylip_aln$seq.text, pairpos[i,1], pairpos[i,1]), sep = ""),
                    paste(substring(phylip_aln$seq.text, pairpos[i,2], pairpos[i,2]), sep = ""))
  
  if("-" %in% rownames(AAcombos))
  {
    if("-" %in% colnames(AAcombos))
    {
      proportionDash<-((sum(AAcombos["-",])+sum(AAcombos[,"-"]))-AAcombos["-","-"])/sum(AAcombos)
    }
    else
    {
      proportionDash<-sum(AAcombos["-",])/sum(AAcombos)
    }
  }
  else
  {
    if("-" %in% colnames(AAcombos))
    {
      proportionDash<-sum(AAcombos[,"-"])/sum(AAcombos)
    }
    else
    {
      proportionDash<-0
    }
  }
  
  # If there are not too many "-" in a predicted page
  if(proportionDash < 0.2)
  {
    # Add this location to the pass list for this alignment
    passlist <- c(passlist, i)
  }
}

overlap_results <- read.delim2(pairs_file, header = FALSE)
overlap_results <- overlap_results[passlist,]

write.table(overlap_results, output_file, row.names = FALSE, col.names = FALSE, 
            quote = FALSE, sep = "\t")
