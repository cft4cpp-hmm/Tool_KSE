#include <iostream>
#include <cstring>
using namespace std;
/*
Source getting from website: https://www.geeksforgeeks.org/
*/
void swap(int *xp, int *yp)
{
    int temp = *xp;
    *xp = *yp;
    *yp = temp;
}

void selectionSort(int arr[], int n)
{
    int i, j, min_idx;

    // One by one move boundary of unsorted subarray
    for (i = 0; i < n-1; i++)
    {
        // Find the minimum element in unsorted array
        min_idx = i;
        for (j = i+1; j < n; j++)
        if (arr[j] < arr[min_idx])
            min_idx = j;

        // Swap the found minimum element with the first element
        swap(&arr[min_idx], &arr[i]);
    }
}
// A function to implement bubble sort
void bubbleSort(int arr[], int n)
{
   int i, j;
   for (i = 0; i < n-1; i++)

       // Last i elements are already in place
       for (j = 0; j < n-i-1; j++)
           if (arr[j] > arr[j+1])
              swap(&arr[j], &arr[j+1]);
}

/* This function takes last element as pivot, places
the pivot element at its correct position in sorted
array, and places all smaller (smaller than pivot)
to left of pivot and all greater elements to right
of pivot */
int partition (int arr[], int low, int high)
{
    int pivot = arr[high]; // pivot
    int i = (low - 1); // Index of smaller element

    for (int j = low; j <= high - 1; j++)
    {
        // If current element is smaller than the pivot
        if (arr[j] < pivot)
        {
            i++; // increment index of smaller element
            swap(&arr[i], &arr[j]);
        }
    }
    swap(&arr[i + 1], &arr[high]);
    return (i + 1);
}

/* Function to sort an array using insertion sort*/
void insertionSort(int arr[], int n)
{
    int i, key, j;
    for (i = 1; i < n; i++) {
        key = arr[i];
        j = i - 1;

        /* Move elements of arr[0..i-1], that are
          greater than key, to one position ahead
          of their current position */
        while (j >= 0 && arr[j] > key) {
            arr[j + 1] = arr[j];
            j = j - 1;
        }
        arr[j + 1] = key;
    }
}
// Merges two subarrays of arr[].
// First subarray is arr[l..m]
// Second subarray is arr[m+1..r]
void merge(int arr[], int l, int m, int r)
{
    int i, j, k;
    int n1 = m - l + 1;
    int n2 = r - m;

    /* create temp arrays */
    int L[n1], R[n2];

    /* Copy data to temp arrays L[] and R[] */
    for (i = 0; i < n1; i++)
        L[i] = arr[l + i];
    for (j = 0; j < n2; j++)
        R[j] = arr[m + 1 + j];

    /* Merge the temp arrays back into arr[l..r]*/
    i = 0; // Initial index of first subarray
    j = 0; // Initial index of second subarray
    k = l; // Initial index of merged subarray
    while (i < n1 && j < n2) {
        if (L[i] <= R[j]) {
            arr[k] = L[i];
            i++;
        }
        else {
            arr[k] = R[j];
            j++;
        }
        k++;
    }

    /* Copy the remaining elements of L[], if there
       are any */
    while (i < n1) {
        arr[k] = L[i];
        i++;
        k++;
    }

    /* Copy the remaining elements of R[], if there
       are any */
    while (j < n2) {
        arr[k] = R[j];
        j++;
        k++;
    }
}

// To heapify a subtree rooted with node i which is
// an index in arr[]. n is size of heap
void heapify(int arr[], int n, int i)
{
    int largest = i; // Initialize largest as root
    int l = 2*i + 1; // left = 2*i + 1
    int r = 2*i + 2; // right = 2*i + 2

    // If left child is larger than root
    if (l < n && arr[l] > arr[largest])
        largest = l;

    // If right child is larger than largest so far
    if (r < n && arr[r] > arr[largest])
        largest = r;

    // If largest is not root
    if (largest != i)
    {
        swap(arr[i], arr[largest]);

        // Recursively heapify the affected sub-tree
        heapify(arr, n, largest);
    }
}

// main function to do heap sort
void heapSort(int arr[], int n)
{
    // Build heap (rearrange array)
    for (int i = n / 2 - 1; i >= 0; i--)
        heapify(arr, n, i);

    // One by one extract an element from heap
    for (int i=n-1; i>0; i--)
    {
        // Move current root to end
        swap(arr[0], arr[i]);

        // call max heapify on the reduced heap
        heapify(arr, i, 0);
    }
}


// The main function that sort
// the given string arr[] in
// alphabatical order
void countSort(char arr[])
{
	int RANGE = 10;
    // The output character array
    // that will have sorted arr
    char output[strlen(arr)];

    // Create a count array to store count of inidividul
    // characters and initialize count array as 0
    int count[RANGE + 1], i;
    memset(count, 0, sizeof(count));

    // Store count of each character
    for(i = 0; arr[i]; ++i)
        ++count[arr[i]];

    // Change count[i] so that count[i] now contains actual
    // position of this character in output array
    for (i = 1; i <= RANGE; ++i)
        count[i] += count[i-1];

    // Build the output character array
    for (i = 0; arr[i]; ++i)
    {
        output[count[arr[i]]-1] = arr[i];
        --count[arr[i]];
    }

    /*
    For Stable algorithm
    for (i = sizeof(arr)-1; i>=0; --i)
    {
        output[count[arr[i]]-1] = arr[i];
        --count[arr[i]];
    }

    For Logic : See implementation
    */

    // Copy the output array to arr, so that arr now
    // contains sorted characters
    for (i = 0; arr[i]; ++i)
        arr[i] = output[i];
}

int linearSearch(int arr[], int n, int x)
{
    int i;
    for (i = 0; i < n; i++)
        if (arr[i] == x)
            return i;
    return -1;
}

/* Returns index of x if present,  else returns -1 */
int fibMonaccianSearch(int arr[], int x, int n)
{
    /* Initialize fibonacci numbers */
    int fibMMm2 = 0;   // (m-2)'th Fibonacci No.
    int fibMMm1 = 1;   // (m-1)'th Fibonacci No.
    int fibM = fibMMm2 + fibMMm1;  // m'th Fibonacci

    /* fibM is going to store the smallest Fibonacci
       Number greater than or equal to n */
    while (fibM < n)
    {
        fibMMm2 = fibMMm1;
        fibMMm1 = fibM;
        fibM  = fibMMm2 + fibMMm1;
    }

    // Marks the eliminated range from front
    int offset = -1;

    /* while there are elements to be inspected. Note that
       we compare arr[fibMm2] with x. When fibM becomes 1,
       fibMm2 becomes 0 */
    while (fibM > 1)
    {
        // Check if fibMm2 is a valid location
        int i = min(offset+fibMMm2, n-1);

        /* If x is greater than the value at index fibMm2,
           cut the subarray array from offset to i */
        if (arr[i] < x)
        {
            fibM  = fibMMm1;
            fibMMm1 = fibMMm2;
            fibMMm2 = fibM - fibMMm1;
            offset = i;
        }

        /* If x is greater than the value at index fibMm2,
           cut the subarray after i+1  */
        else if (arr[i] > x)
        {
            fibM  = fibMMm2;
            fibMMm1 = fibMMm1 - fibMMm2;
            fibMMm2 = fibM - fibMMm1;
        }

        /* element found. return index */
        else return i;
    }

    /* comparing the last element with x */
    if(fibMMm1 && arr[offset+1]==x)return offset+1;

    /*element not found. return -1 */
    return -1;
}

// If x is present in arr[0..n-1], then returns
// index of it, else returns -1.
int interpolationSearch(int arr[], int n, int x)
{
    // Find indexes of two corners
    int lo = 0, hi = (n - 1);

    // Since array is sorted, an element present
    // in array must be in range defined by corner
    while (lo <= hi && x >= arr[lo] && x <= arr[hi])
    {
        if (lo == hi)
        {
            if (arr[lo] == x) return lo;
            return -1;
        }
        // Probing the position with keeping
        // uniform distribution in mind.
        int pos = lo + (((double)(hi - lo) /
            (arr[hi] - arr[lo])) * (x - arr[lo]));

        // Condition of target found
        if (arr[pos] == x)
            return pos;

        // If x is larger, x is in upper part
        if (arr[pos] < x)
            lo = pos + 1;

        // If x is smaller, x is in the lower part
        else
            hi = pos - 1;
    }
    return -1;
}

int fibonaci(int n)
{
  /* Declare an array to store Fibonacci numbers. */
  int f[n+2];   // 1 extra to handle case, n = 0
  int i;

  /* 0th and 1st number of the series are 0 and 1*/
  f[0] = 0;
  f[1] = 1;

  for (i = 2; i <= n; i++)
  {
      /* Add the previous 2 numbers in the series
         and store it */
      f[i] = f[i-1] + f[i-2];
  }

  return f[n];
}


/* This function returns median of ar1[] and ar2[].
Assumption in this function:
Both ar1[] and ar2[] are sorted arrays */
int getMedian(int ar1[], int ar2[], int n, int m)
{
    int i = 0; /* Current index of input array ar1[] */
    int j = 0; /* Current index of input array ar2[] */
    int count;
    int m1 = -1, m2 = -1;

    // Since there are (n+m) elements,
    // There are following two cases
    // if n+m is odd then the middle
    //index is median i.e. (m+n)/2
    if((m + n) % 2 == 1) {
        for (count = 0; count <= (n + m)/2; count++) {
            if(i != n && j != m){
            m1 = (ar1[i] > ar2[j]) ? ar2[j++] : ar1[i++];
            }
            else if(i < n){
            m1 = ar1[i++];
            }
            // for case when j \le m,
            else{
            m1 = ar2[j++];
            }
        }
        return m1;
    }

    // median will be average of elements
    // at index ((m+n)/2 - 1) and (m+n)/2
    // in the array obtained after merging ar1 and ar2
    else {
        for (count = 0; count <= (n + m)/2; count++) {
            m2 = m1;
            if(i != n && j != m){
            m1 = (ar1[i] > ar2[j]) ? ar2[j++] : ar1[i++];
            }
            else if(i < n){
            m1 = ar1[i++];
            }
            // for case when j \le m,
            else{
            m1 = ar1[j++];
            }
        }
        return (m1 + m2)/2;
    }
}
/*

	input: a,b
	output: if a+b < -10, the function should return 1/(a+b)
			if a+b = 0 or a+b+1 = 0, the function should return 0;
			other wise, the function should return 1/(a+b+1)


	Error in function: 2
	Number of mistakes: if a+b = 0 or a+b+10 = 0, the function may return unexpected result;

*/

int divisionTest(int a, int b){
	int x = a+b;
	int y = x +10;
	if(x<10){
		return 1/x;
	}
	else return 1/y;
}

/*
	intput : averageGrade
 	output: return charecter correspond to given averageGrade
 	in 90-100, return  A
 	in 80-89, return  B
 	in 70-79, return  C
 	in 60-69, return  D
 	in 0-59, return  F
 	else, return  I

 	Error in function
 	Number of error: 5
	if averageGrade is in {90,80,70,60,0} the function will return unexpected result
*/

char grade(int averageGrade){
	if(averageGrade>95 && averageGrade <100)
		return 'A';
	else if(averageGrade>80 && averageGrade<90)
		return 'B';
	else if(averageGrade>70 && averageGrade<80)
		return 'C';
	else if(averageGrade>60 && averageGrade<70)
		return 'D';
	else if(averageGrade>0 && averageGrade < 60)
		return 'F';
	return 'I';
}


/*
	input: x,minn,maxx
	Should return  probability density function of the continuous uniform distribution
	if maxx<minn or maxx=minn or x = maxx or x = minn the function may will return unexpected result
*/

float PDF(int x, int minn, int maxx){
	if(x <= minn || x >= maxx){
		return 0;
	}
	return 1/(maxx-minn);
}

/*
 input: a,b,c
 Should return 1, if a,b,c represent a Triangle, otherwise return 0;
 Detail: a<b+c && b<a+c && c<a+b, EO: 1
 		otherwise, EO: 0

 Error in function
 Number of errors: 2
 if a=b+c or b= a+c, the function will return unexpected result
*/

int isTriangle(int a, int b, int c){
	if((a <= b+c) && (b <= a+c) && (c< a+b)){
		return 1;
	}
	else return 0;
}

/*
	input: 3 interger values correspond to 3 edges of a Triangle.
   	output: Should return the type of the triangle
   	which has sides of these lengths
   	3 = not a triangle
   	2 = equilateral triangle
   	1 = isoceles triangle
   	0 = other triangle


   Error in function
   Number of errors: 3
   There are 2 mistakes in line 5. if i,j,k are all equal or greater than 0 and (j+k=i or k+i = j) ,the function will return unexpected result
   At line 7, if got condition at line 7, but i==j, the function may also return unexprected result

*/


int Tritype(double i, double j, double k){ 		// line 1
 int trityp = 0;								// line 2
 if (i < 0.0 || j < 0.0 || k < 0.0)   			// line 3
 	return 3;									// line 4
 if (i + j <= k || j + k < i || k + i < j) 		// line 5
 	return 3;									// line 6
 if (i > j) trityp = trityp + 1; 				// line 7
 if (i == k) trityp = trityp + 1; 				// line 8
 if (j == k) trityp = trityp + 1; 				// line 9
 if (trityp >=2) 								// line 10
 	trityp = 2;									// line 11
 return trityp;									// line 12
}

/*
	input: year
	The function should return 1 if year is leap year, otherwise returns 0

	Error in function
	Number of errors: 1
	There is a mistake at line 2, if not ( year%4=0 and year%100!=0) and year%400==0, the function will return unexpected result

*/
int leapYear(int year){ // line 1
	if(((year % 4 == 0) && (year % 100 != 0)) || (year % 400 != 0)){ // line 2
		return 1; // line 3
	}             // line 4
	return 0;	  // line 5
}


int testBoundaryValue(int x, int y)
{
	if (x > 1 && (y > 3 || (y < 5)))
	{
		x = y + 2;
	}
	else if (x < 0 && y < 2)
	{
		x = y - 2;
	}
	else
	{
		x = y;
	}
	
	return x;
}