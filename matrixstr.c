#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>


//Matrix operations by struct.


struct Matrix
{
    int row;
    int coln;
    float **values;
};

int allocateMatrix(struct Matrix *matrix, int n, int m)
{

    int i, j;   

    matrix->values = malloc(n * sizeof(float*));

    for(i = 0; i < matrix->row; i++)
    {
        matrix->values[i] = malloc(m * sizeof(float));
    
    }
}


int setMatrixProps(struct Matrix *matrix, int i, int j)
{
    
    matrix->row = i;
    matrix->coln = j;
    return allocateMatrix(matrix, i, j);
}

void freeMatrixMem(struct Matrix *myMatrix)
{
    int i;
    for(i = 0; i < myMatrix->row; i++)
    {
        free(myMatrix->values[i]);
    }
    free(myMatrix->values);
myMatrix->values = NULL;
}

void printMatrix(struct Matrix *matr)
{
	int n,m ;

  for(n = 0; n < matr->row; n++)
    {
        for(m = 0; m < matr->coln; m++)
        {
            printf("%f ", matr->values[n][m]);
        }
        printf("\n");
    }
    

}


struct Matrix TransposeM(struct Matrix mat){
	struct Matrix result;
	setMatrixProps(&result,mat.coln,mat.row);
	int n, m;
		for(n = 0; n < mat.row; n++)	
		{
			for(m = 0; m < mat.coln; m++)
			{
			result.values[m][n] = mat.values[n][m];
			}
		}
	return result;
}

int choose(float i1,int i2,int i3,int i4){

	if(i1==0) return i2;	
	if(i1>0)  return i3;
	if(i1<0)  return i4;

}

struct Matrix Matrixmult(struct Matrix mat1, struct Matrix mat2){
	
	struct Matrix result;
	setMatrixProps(&result,mat1.row,mat2.coln);
	int i,j,k;
	
	for(i =0;i<mat1.row;i++){
		for(k=0;k<mat2.coln;k++){

			float sum =0;
			for(j=0;j<mat1.coln;j++){
				sum += mat1.values[i][j]*mat2.values[j][k]; 
			}
			result.values[i][k] = sum;
		}
	}

	return result;
}

struct Matrix Matrixmult2(struct Matrix mat1, float f){
	
	struct Matrix result;
	setMatrixProps(&result,mat1.row,mat1.coln);
	int i,j;
		for(i =0;i<mat1.row;i++){
			for(j=0;j<mat1.coln;j++){
				result.values[i][j] = mat1.values[i][j]*f;
			}
		}
	
	

	return result;
}

struct Matrix MatrixOpr(struct Matrix mat1, struct Matrix mat2,char op){
	
		
	struct Matrix result;
	setMatrixProps(&result,mat1.row,mat2.coln);
	int i,j,k;
	if (op=='+'){
	for(i =0;i<mat1.row;i++){
		for(k=0;k<mat2.coln;k++){
			result.values[i][k] = mat1.values[i][k]+mat2.values[i][k]; 
		}
	}
	}
	if (op=='-'){
	for(i =0;i<mat1.row;i++){
		for(k=0;k<mat2.coln;k++){
			result.values[i][k] = mat1.values[i][k]-mat2.values[i][k]; 
		}
	}
	}
	return result;
	

}

int main(int argc, char *argv[])
{



