# Build a matrix to represent the levels of separate and concurrent
# changes for given tree depth, sequence length, evolutionary strengths and
# based on a matrix representing the levels of coevolution between 
# pairs.
correlated_matrix_simulation <- function(nrow=100,ncol=100,row_prob=NULL,col_prob=NULL,correlation_matrix=NULL) {
		
	if(is.null(row_prob)) {
		row_prob <- rep(1,times=nrow)
	}
	else {
		row_prob <- round(row_prob*ncol)
	}
	if(is.null(col_prob)) {
		col_prob <- rep(1,times=2*ncol)
	}
	else {
		col_prob <- c(col_prob,col_prob)
	}
	
	mat <- matrix(0,nrow=nrow,ncol=2*ncol)
	
	pos_index <- seq(1,2*ncol,by=1)

	changes_list <- lapply(row_prob,function(x) {
		
		tmp_col_prob <- col_prob
		sample_vector <- vector()
		i <- 0
		while (i < x) {
			sampled_pos <- sample(pos_index,1,replace=FALSE,prob=tmp_col_prob)
			tmp_col_prob1 <- tmp_col_prob[1:ncol]
			tmp_col_prob2 <- tmp_col_prob[(ncol+1):(2*ncol)]
			if (sampled_pos > ncol) {
				mat_pos <- sampled_pos-ncol
				tmp_col_prob2 <- tmp_col_prob2 * correlation_matrix[mat_pos,]
			}
			else {
				mat_pos <- sampled_pos
				tmp_col_prob1 <- tmp_col_prob1 * correlation_matrix[mat_pos,]	
			}
			tmp_col_prob <- c(tmp_col_prob1,tmp_col_prob2)
			tmp_col_prob[sampled_pos] <- 0
			
			sample_vector <- c(sample_vector,sampled_pos)
			
			i <- i+1
		}
		sample_vector <- sort(sample_vector)
	})
		
	for (i in 1:nrow) {
		mat[i,c(changes_list[[i]])] <- 1
	}
	
	rownames(mat) <- seq(1,nrow,by=1)
	colnames(mat) <- seq(1,2*ncol,by=1)
	return(mat)
}

# Build a matrix to represent the levels of coevolution between pairs
# based on supplied pairs and levels
build_multiplicative_matrix_with_known_parameters <- function(nsites=100,correlation_df=NULL,pos1_vector=NULL,pos2_vector=NULL,correlation_factor_vector=NULL) {
	
	if(!is.null(correlation_df)) {
		pos1_vector <- as.matrix(correlation_df[,1])
		pos2_vector <- as.matrix(correlation_df[,2] )
		correlation_factor_vector <- as.matrix(correlation_df[,3] )
	}
			
	mat <- matrix(1,nrow=nsites,ncol=nsites)
	
	for (i in 1:length(pos1_vector)) {
		mat[pos1_vector[i], pos2_vector[i]] <- correlation_factor_vector[i]
		mat[pos2_vector[i], pos1_vector[i]] <- correlation_factor_vector[i]
	}
			
	rownames(mat) <- seq(1,nsites,by=1)
	colnames(mat) <- seq(1,nsites,by=1)
	return(mat)
}

# Build a matrix to represent the levels of coevolution between pairs
# based on a gamma distribution of supplied shape and scale
simulate_multiplicative_matrix <- function(nsites=100,shape=NULL,scale=NULL) {
	
	npairs <- nsites*(nsites-1)/2
	
	m <- matrix(0,nrow=nsites,ncol=nsites)
	d <- as.dist(m)
	v <- round(rgamma(npairs,shape,scale=scale))
			
	mat <- as.matrix(as.dist(d+v,diag=TRUE,upper=TRUE))
	
	mat <- mat + 1.0
		
	rownames(mat) <- seq(1,nsites,by=1)
	colnames(mat) <- seq(1,nsites,by=1)
	return(mat)
}

# Read command line arguments:
# 1 Number of tree bifurcations 
# 2 Length of simulated protein
# 3 Shape statistic 1 for the beta distribution used to 
#   simulate different evolutionary rates for each position 
# 4 Shape statistic 2 for the beta distribution used to 
#   simulate different evolutionary rates for each position 
# 5 Shape statistic 1 for the beta distribution used to assign the
#   proportion of positions that needed to be replaced in each tree bifurcation
# 6 Shape statistic 2 for the beta distribution used to assign the
#   proportion of positions that needed to be replaced in each tree bifurcation
# 7 If a 7th argument is supplied then it should be the output file prefix
# If 8 arguments are supplied:
# 8 A tab delimited file containing pairs to simulate as coevolving and the 
#   strength of the multiplicative effect e.g.:
#   12  15  3
#   20  35  10
# If 9 arguments are supplied:
# 8 The shape parameter for a gamma distribution to be used to assign a
#   pairs a chance of being linked by coevolution
# 8 The scale parameter for a gamma distribution to be used to assign a
#   pairs a chance of being linked by coevolution
cmd_args <- commandArgs(trailingOnly = TRUE)

nevents <- as.numeric(cmd_args[1])
nsites <- as.numeric(cmd_args[2])
beta_shape1_1 <- as.numeric(cmd_args[3])
beta_shape1_2 <- as.numeric(cmd_args[4])
beta_shape2_1 <- as.numeric(cmd_args[5])
beta_shape2_2 <- as.numeric(cmd_args[6])
if (length(cmd_args)==7) {
	ofile_basis <- cmd_args[7]
} else if (length(cmd_args)==8) {
	ifile <- cmd_args[8]
	correlation_df <- read.table(ifile)
	ofile_basis <- cmd_args[7]
} else if (length(cmd_args)==9) {
	gamma_shape <- as.numeric(cmd_args[8])
	gamma_scale <- as.numeric(cmd_args[9])
	ofile_basis <- cmd_args[7]
}

probability_of_sites_mutation <- rbeta(nsites,beta_shape1_1,beta_shape1_2)

probability_of_mutation_per_event <- rbeta(nevents,beta_shape2_1,beta_shape2_2)

if (length(cmd_args)==8) {
	multiplicative_matrix <- build_multiplicative_matrix_with_known_parameters(nsites=nsites,correlation_df=correlation_df)
} else if (length(cmd_args)==9) {
	multiplicative_matrix <- simulate_multiplicative_matrix(nsites=nsites,shape=gamma_shape,scale=gamma_scale)
	ofile3 <- paste(ofile_basis,'_matrix.txt',sep='')
	write(t(multiplicative_matrix),ncolumns=nsites,ofile3)
} else {
	multiplicative_matrix <- matrix(1,nrow=nsites,ncol=nsites)
}

binary_matrix <-correlated_matrix_simulation(nrow=nevents,ncol=nsites,row_prob=probability_of_mutation_per_event,col_prob=probability_of_sites_mutation,correlation_matrix=multiplicative_matrix) 

binary_matrix_1 <- 	binary_matrix[,1:nsites]	
binary_matrix_2 <- 	binary_matrix[,(nsites+1):(2*nsites)]	
binary_matrix_2[binary_matrix_2==1] <- 2

nodes_matrix <- binary_matrix_1+binary_matrix_2
branches_matrix <- cbind(binary_matrix_1,binary_matrix_2)

ofile1 <- paste(ofile_basis,'_nodes.RData',sep='')
ofile2 <- paste(ofile_basis,'_branches.RData',sep='')

save(nodes_matrix,file=ofile1)
save(branches_matrix,file=ofile2)
