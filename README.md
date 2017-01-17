# Minimum Unmixed Perfect Phylogeny v0.1 
**For questions or problems with this code, contact [edinehusic@gmail.com](mailto:edinehusic@gmail.com),
or open an ISSUE on github.**

This repository contains implementations of the two Integer Linear Programs from a paper to **appear** 
and implementations of one greedy and one local search heuristics which are not mentioned in the paper and have no constant
approximation guaranties.

## 1. Background and previous results
The problems addresed are **Minimum Conflict-Free Row Split problems**. They were originaly proposed
by *I. Hajirasouliha, B. Raphael, Reconstructing Mutational 
History in Multiply Sampled Tumors Using Perfect Phylogeny Mixtures. 
WABI 2014: 354-367 doi:[10.1007/978-3-662-44753-6_27](http://dx.doi.org/10.1007/978-3-662-44753-6_27)*.
Later, NP-hardness of the problems was confirmed by
Hujdurovic, A., Kacar, U., Milanic, M., Ries, B., and Tomescu, A. I. (2016).
Complexity and algorithms for finding a perfect phylogeny from mixed tumor
samples. IEEE/ACM Transactions on Computational Biology and Bioinformatics.
Available online, DOI: [10.1109/TCBB.2016.2606620](http://ieeexplore.ieee.org/document/7589999/).

A shorter version of the last paper appeared in the Proceedings of WABI 2015:

*Ademir Hujdurović, Urša Kačar, Martin Milanič, Bernard Ries, and Alexandru I. Tomescu, Finding a Perfect
Phylogeny from Mixed Tumor Samples. WABI 2015, LNCS 9289, pp. 80-92, extended version available at 
[http://arxiv.org/abs/1506.07675](http://arxiv.org/abs/1506.07675).*

The above papers also proposed heuristics and gave an exact algorithm for a particular class of inputs. 
The implementation is available at https://github.com/alexandrutomescu/MixedPerfectPhylogeny.
Slides and a visual description of the these results are available 
[**here**](https://www.cs.helsinki.fi/u/tomescu/perfect-phylogeny-tumors.pdf).

## 2. Our approach
We consider two problems:
- split each row of a given binary matrix into a bitwise *OR* of a set of 
rows so that the resulting matrix corresponds to a perfect phylogeny and has the minimum number of rows among all matrices
with this property. **(MCRS)**
- split each row as above, but the task is to minimize the number of distinct rows of the resulting matrix. **(MCDRS)**

We gave new formulations of the two problems, showing that the problems are equivalent to 
two optimization problems on branchings in a derived directed acyclic graph. 
Building on these formulations, we can model the two problems with simple polynomially-sized integer
linear programs. This git repository contains implementation of the results. 
As an input we take a matrix, construct corresponding digraph and consequently ILP formulations. 
ILP are solved using [IBM CPLEX](https://www-01.ibm.com/software/commerce/optimization/cplex-optimizer/).

#  Input formats
## 3. {0,1} matrices
The input for the problem is a **m**x**n** binary matrix in .csv format separeted by ";". 
The first column must contain the row (i.e., sample) names,
and the first row must contain the column (i.e., mutation location) names. 
For example, the following table

|   | c1| c2| c3| c4| c5| c3'|
|---|---|---|---|---|---|----|
| r1|  1|  0|  0|  0|  0|   0|
| r2|  1|  1|  1|  1|  0|   1|
| r3|  0|  1|  0|  1|  0|   0|
| r4|  0|  1|  1|  0|  1|   1|
| r5|  0|  1|  0|  0|  0|   0|

is encoded as:

	;c1;c2;c3;c4;c5;c3'
	r1;1;0;0;0;0;0
	r2;1;1;1;1;0;1
	r3;0;1;0;1;0;0
	r4;0;1;1;0;1;1
	r5;0;1;0;0;0;0

### 3.1 Output

### 3.2 Running
 
## 4. Transpose matrix of single nucleotide variants (SNVs) and a threshold

### Output

### Running

## * Results
To appear.
