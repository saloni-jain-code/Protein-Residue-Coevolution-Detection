# Function to add prediction confidence intervals to certain models by using the 
# model to predict based on shuffled data
boot_pi <- function(model, pdata, n, p) {
	odata <- model$data
	lp <- (1 - p) / 2
	up <- 1 - lp
	set.seed(2016)
	seeds <- round(runif(n, 1, 1000), 0)
	boot_y <- foreach(i = 1:n, .combine = rbind, .errorhandling='remove') %dopar% {
		set.seed(seeds[i])
		bdata <- odata[sample(seq(nrow(odata)), size = nrow(odata), replace = TRUE), ]
		bpred <- predict(update(model, data = bdata), type = "response", newdata = pdata)
		rpois(length(bpred), lambda = bpred)
	}
	boot_ci <- t(apply(boot_y, 2, quantile, c(lp, up)))
	return(data.frame(pred = predict(model, newdata = pdata, type = "response"), lower = boot_ci[, 1], upper = boot_ci[, 2]))
}

# Function counts the number of single and double changes from a matrix created by
# the java code mpsAnalysis_Reconstruction_Tracking.jar
single_double_changes_count <- function(mat) {
	v <- colnames(mat)
	
	grid <- expand.grid(v,v,stringsAsFactors=FALSE)
	colnames(grid) <- c('Res1','Res2')
	grid <- grid[grid$Res1<grid$Res2,]
	grid$BranchNSingle <- NA
	grid$BranchNDouble <- NA
	
	grid[,3:4]<- t(apply(grid,1,function(x) {
		i <- unlist(x[1])
		j <- unlist(x[2])
		
		v <- rep(0,times=2)
		
		v[1] <- v[1] + sum((mat[,i]==0 & mat[,j]!=0)+0.0)
		v[1] <- v[1] + sum((mat[,i]!=0 & mat[,j]==0)+0.0)
		v[1] <- v[1] + sum((mat[,i]==1 & mat[,j]==2)+0.0)
		v[1] <- v[1] + sum((mat[,i]==2 & mat[,j]==1)+0.0)
				
		v[2] <- v[2] + sum((mat[,i]==1 & mat[,j]==1)+0.0)
		v[2] <- v[2] + sum((mat[,i]==2 & mat[,j]==2)+0.0)
		v[2] <- v[2] + sum(((mat[,i]==1 | mat[,i]==2) & mat[,j]==3)+0.0)
		v[2] <- v[2] + sum((mat[,i]==3 & (mat[,j]==1 | mat[,j]==2))+0.0)
		v[2] <- v[2] + sum((mat[,i]==3 & mat[,j]==3)+0.0)
					
		v
	}))
			
	return(grid)
}

# Fit the selected models to the separate and concurrent changes count dataframe
# 1: Linear 
# 2: Logarithmic
# 3: Box-Cox Transform
# 4: Poisson Regression
# 5: Negative Binomial
# 6: Poisson GAM
# 7: Negative Binomial GAM
fit_all_models <- function(df,analyses=c(1,2,3,4,5,6,7)) {
		
	df <- df[df$x != 0 | df$y != 0,]
	
	r_df <- data.frame()
	
	x <- df$x
	y <- df$y
	
	predictx <- seq(0,max(x),by=1)
	
	if (1 %in% analyses) {
		# Linear Model
		model <- rlm(y~x)
		
		fitted <- predict(model,interval='prediction',level=level,list(x=predictx))
	
		df$z <- fitted[(df$x)+1,2]
	
		if (dim(df[df$y < df$z,])[1] > 0) {
			temp_df <- df[df$y < df$z,1:4]
			temp_df$Method <- 1
			r_df <- rbind(r_df,temp_df)
		}
	}

	if (2 %in% analyses) {
		# Logarithmic Model
		model <- rlm(log1p(y)~x)
		fitted <- predict(model,interval='prediction',level=level,list(x=predictx))
	
		df$z <- fitted[(df$x)+1,2]
	
		if (dim(df[log1p(df$y) < df$z,])[1] > 0) {
				temp_df <- df[log1p(df$y) < df$z,1:4]
				temp_df$Method <- 2
				r_df <- rbind(r_df,temp_df)
		}
	}
	
	if (3 %in% analyses) {
		# Box-Cox Transform Model
		if (min(y)==0) {
			gamma <- 10^(-9)
			bc <- boxCox(y~x,plotit=FALSE,family="bcnPower",gamma=gamma,interp=TRUE)
		
			if (bc$x[bc$y==max(bc$y)]==2) {
				bc1 <- boxCox(y~x,lambda=seq(0,6,0.1),plotit=FALSE,family="bcnPower",gamma=gamma,interp=TRUE)
				if (!is.na(bc1$x[bc1$y==max(bc1$y)])) {
					bc <- bc1
				}
			} else if (bc$x[bc$y==max(bc$y)]==-2) {
				bc1 <- boxCox(y~x,lambda=seq(-6,0,0.1),plotit=FALSE,family="bcnPower",gamma=gamma,interp=TRUE)
				if (!is.na(bc1$x[bc1$y==max(bc1$y)])) {
					bc <- bc1
				}
			}
		
			if (bc$x[bc$y==max(bc$y)]==6) {
				bc2 <- boxCox(y~x,lambda=seq(0,12,0.1),plotit=FALSE,family="bcnPower",gamma=gamma,interp=TRUE)
				if (!is.na(bc2$x[bc2$y==max(bc2$y)])) {
					bc <- bc2
				}
			} else if (bc$x[bc$y==max(bc$y)]==-6) {
				bc2 <- boxCox(y~x,lambda=seq(-12,0,0.1),plotit=FALSE,family="bcnPower",gamma=gamma,interp=TRUE)
				if (!is.na(bc2$x[bc2$y==max(bc2$y)])) {
					bc <- bc2
				}
			}

			y2 <- bcnPower(y,bc$x[bc$y==max(bc$y)],gamma=gamma)
		} else {
			gamma <- 0
			bc <- boxCox(y~x,plotit=FALSE,interp=TRUE)
		
			if (bc$x[bc$y==max(bc$y)]==2) {
				bc1 <- boxCox(y~x,lambda=seq(0,6,0.1),plotit=FALSE,interp=TRUE)
				if (!is.na(bc1$x[bc1$y==max(bc1$y)])) {
					bc <- bc1
				}
			} else if (bc$x[bc$y==max(bc$y)]==-2) {
				bc1 <- boxCox(y~x,lambda=seq(-6,0,0.1),plotit=FALSE,interp=TRUE)
				if (!is.na(bc1$x[bc1$y==max(bc1$y)])) {
					bc <- bc1
				}
			}
		
			if (bc$x[bc$y==max(bc$y)]==6) {
				bc2 <- boxCox(y~x,lambda=seq(0,12,0.1),plotit=FALSE,interp=TRUE)
				if (!is.na(bc2$x[bc2$y==max(bc2$y)])) {
					bc <- bc2
				}
			} else if (bc$x[bc$y==max(bc$y)]==-6) {
				bc2 <- boxCox(y~x,lambda=seq(-12,0,0.1),plotit=FALSE,interp=TRUE)
				if (!is.na(bc2$x[bc2$y==max(bc2$y)])) {
					bc <- bc2
				}
			}
			
			y2 <- bcPower(y,bc$x[bc$y==max(bc$y)])
		}	

		if (!is.na(bc$x[bc$y==max(bc$y)])) {
			model <- rlm(y2~x)
			predictx <- seq(0,max(x),by=1)
			fitted <- predict(model,interval='prediction',level=level,list(x=predictx))
	
			df$z <- fitted[(df$x)+1,2]
	
			if (dim(df[y2 < df$z,])[1] > 0) {
				#apply(df[y2 < df$z,],1,function(x) {
				#cat('3',x[1:4],'\n',sep='\t')
				temp_df <- df[y2 < df$z,1:4]
				temp_df$Method <- 3
				r_df <- rbind(r_df,temp_df)
				#})
			}
		}
	}
	
	if (4 %in% analyses) {
		# Poisson Regression Model
		gmodel <- glm(y~x,data=df,family=poisson)
		fitted <- predict(gmodel,list(x=predictx),type='response')
		new_gdata <- data.frame('x'=predictx,'y'=round(fitted))
		boot_pi_df <- boot_pi(gmodel,new_gdata,1000,level)
	
		df$z <- boot_pi_df[(df$x)+1,2]
	
		if (dim(df[df$y < df$z,])[1] > 0) {
			temp_df <- df[df$y < df$z,1:4]
			temp_df$Method <- 4
			r_df <- rbind(r_df,temp_df)
		}
	}

	if (5 %in% analyses) {
		# Negative Binomial Regression Model
		gmodel <- glm.nb(y~x,data=df)
		gmodel$data <- df
		fitted <- predict(gmodel,list(x=predictx),type='response')
		new_gdata <- data.frame('x'=predictx,'y'=round(fitted))
		boot_pi_df <- boot_pi(gmodel,new_gdata,1000,level)
	
		df$z <- boot_pi_df[(df$x)+1,2]
	
		if (dim(df[df$y < df$z,])[1] > 0) {
			temp_df <- df[df$y < df$z,1:4]
			temp_df$Method <- 5
			r_df <- rbind(r_df,temp_df)
		}
	}

	k <- length(unique(x))	
	if (k > 2) {
		if (6 %in% analyses) {
		# Poisson GAM
		## fit smooth model to x, y data...
			if (k < 10) {
				gamodel <- gam(y~s(x,k=k),family=poisson,data=df)
			} else {
				gamodel <- gam(y~s(x),family=poisson,data=df)
			}
	
			ilink <- family(gamodel)$linkinv
			predict_df <- data.frame(x = predictx)
			fitted <- setNames(as.data.frame(predict(gamodel,predict_df, se.fit = TRUE)), c("fit", "se"))
			predict_df <- cbind(predict_df, fitted)
			predict_df <- transform(predict_df, lower = ilink(fit - (2 * se)),lambda = ilink(fit), upper = ilink(fit + (2 * se)))
			lower <- (1-level)/2
			upper <- 1 - lower
			prediction_intervals <- apply(predict_df,1,function(x){qpois(c(lower,upper),lambda=c(x['lower'],x['upper']))})
	
			df$z <- prediction_intervals[1,(df$x)+1]
		
			if (dim(df[df$y < df$z,])[1] > 0) {
				temp_df <- df[df$y < df$z,1:4]
				temp_df$Method <- 6
				r_df <- rbind(r_df,temp_df)
			}
		}
		
		if (7 %in% analyses) {
			# Negative Binomial GAM
			## fit smooth model to x, y data...
			if (k < 10) {
				gamodel <- gam(y~s(x,k=k),family=nb(link='log'),data=df)
			} else {
				gamodel <- gam(y~s(x),family=nb(link='log'),data=df)
			}
	
			lower <- (1-level)/2
			upper <- 1 - lower
			sefactor <- qnorm(upper)
			ilink <- family(gamodel)$linkinv
			predict_df <- data.frame(x = predictx)
			fitted <- setNames(as.data.frame(predict(gamodel,predict_df, se.fit = TRUE)), c("fit", "se"))
			predict_df <- cbind(predict_df, fitted)
			predict_df <- transform(predict_df, lower = ilink(fit - (sefactor * se)),mu = ilink(fit), upper = ilink(fit + (sefactor * se)))
			prediction_intervals <- apply(predict_df,1,function(x) {qnbinom(c(lower,upper),mu=c(x['lower'],x['upper']),size=10000)})
	
			df$z <- prediction_intervals[1,(df$x)+1]
			
			if (dim(df[df$y < df$z,])[1] > 0) {
				temp_df <- df[df$y < df$z,1:4]
				temp_df$Method <- 7
				r_df <- rbind(r_df,temp_df)
			}
		}
	}
	
	if (dim(r_df)[1] > 0) {
		colnames(r_df) <- c('Res1','Res2','NSingle','NDouble','Method')
	}
	
	return(r_df)
}

library(foreach)
library(MASS)
library(car)
library(mgcv)

# Read command line arguments:
# 1 Input matrix "mps_..._binary_branches.tab" created by mpsAnalysis_Reconstruction_Tracking.jar
# 2 Output prefix
# 3 Alpha threshold for predictions
# 4 Comma separated list of statistical models to run:
# 1: Linear 
# 2: Logarithmic
# 3: Box-Cox Transform
# 4: Poisson Regression
# 5: Negative Binomial
# 6: Poisson GAM
# 7: Negative Binomial GAM
cmd_args <- commandArgs(trailingOnly = TRUE)

ifile <- cmd_args[1]
ofile_basis <- cmd_args[2]
alpha <- as.numeric(cmd_args[3])
setOfAnalyses <- cmd_args[4]

nodes_matrix<-read.delim2(ifile, header = FALSE, sep = " ")

# output from java program needs matrix transposition
mps_changes_nodes_matrix <- t(as.matrix(nodes_matrix))

nrow <- nrow(mps_changes_nodes_matrix)
ncol <- ncol(mps_changes_nodes_matrix)
rownames(mps_changes_nodes_matrix) <- seq(1,nrow,by=1)
colnames(mps_changes_nodes_matrix) <- seq(1,ncol,by=1)

mps_all_df <- single_double_changes_count(mps_changes_nodes_matrix)

nresidues <- dim(mps_changes_nodes_matrix)[2]

all_changes_list <- list()
for (i in 1:nresidues) {
	all_changes_list[[i]] <- mps_all_df[mps_all_df$Res1==i | mps_all_df$Res2==i,]
}

level <- 1-(alpha/nresidues)
analyses_vector <- as.numeric(unlist(strsplit(setOfAnalyses,',')))

results_df <- data.frame()
for (r in 1:nresidues) {

	df <- all_changes_list[[r]]
	colnames(df) <- c('P1','P2','y','x')
	
	if (min(df$x)!=max(df$x)) {
		results_df <- rbind(results_df,fit_all_models(df,analyses=analyses_vector))
	}
}

prediction_df <- results_df[duplicated(results_df),]

ofile1 <- paste(ofile_basis,'_all_outliers.txt',sep='')
ofile2 <- paste(ofile_basis,'_predictions.txt',sep='')

write.table(results_df,file=ofile1,quote=FALSE,row.names=FALSE)
write.table(prediction_df,file=ofile2,quote=FALSE,row.names=FALSE)
