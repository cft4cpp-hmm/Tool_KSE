int fibonaci(int n) //huong
{
    int a[100];
    if(n>=2)
    {
    a[0]=a[1]=1;
    for (int i=2;i<=n;i++)
        a[i]=a[i-1]+a[i-2];
    return a[n];
    }
    return 0;
}
int sum(int a, int b){
	int summ = a+b;
	return summ;
}
int simpleWhileTest(int x, int y){
	while(x < y){
		x+=1;
	}
	return 1;
}


int bmi(float b_w, float he)
{
	double c;
	c = (b_w / (he * he / 10000));
	
	{
		if (c < 19) {
			return 0;
		}

		else if (c >= 19 && c < 25) {
			return 1;

		}

		else if (c >= 25 && c < 30) {
			return 2;
		}

		else if (c >= 30) {
			return 3;

		} else {
			// (infeasible)
			return -1;
		}
	}
}

int switchCaseTest(int x){
	int a=0;
	switch(x){
		case 1: a=1;
			break;
		case 2: a=2;
			break;
		case 3: a=3;
			break;
		case 4: a=4;
			break;
		default :  break;
	}
}

int CheckValidDay(int day, int month, int year){ //Tuong doi cham
if (day>=1 && month>=1 && year>=1 && day<=31 && month<=12 && year<=2020){
	if((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month==12) && (day<=31))  return 1;
	if((month == 4 || month == 6 || month == 9 ||month == 11) && (day<=30)) return 1;
	if((month == 2) && (day <= 28))  return 1;
	if((month == 2) && (day == 29) && (year%4 == 0)&&(year%400 != 0)) return 1;
}
return 0;
}

int TriTanggle2(int a, int b, int c)//sach, khong duoc
{
int  match;
match = 0;
if(a == b) //{1}
	match = match + 1; //{2}
if(a == c) //{3}
	match = match + 2; //{4}
if(b == c) //{5}
	match = match + 3; //{6}
if(match == 0) //{7}
	if((a+b) <= c) //{8}
		return 0; //{12.1}
	else if((b+c) <= a) //{9}
				return 0; //{12.2}
			else if((a+c) <= b) //{10}
					return 0;// {12.3 Khong la TG
				else return 1;// {11} TG Thuong
else
	if(match == 1) //{13}
		if((a+c) <= b) //{14}
			return 0; //{12.4}
		else return 2; //{15.1}
	else
		if(match == 2) //{16}
			if((a+c) <= b) //{17}
				return 0; //{12.5}
			else return 2;// {15.2}
		else
			if(match == 3) //{18}
				if((b+c) <= a) //{19}
					return 0; //{12.6}
				else return 1; //{15.3}
			else return 3; //{20}
				return 0;
}//the end.





float foo(int a, int b, int c, int d)
{
	float e;
	if(a==0) return 0;
	int x=0;
	if((a==b)||(c==d))
		x=1;
	e=1/x;
	return e;
}
		

int ArrayCmp(int n, int* t1, int* t2) {//pathcrawler
int i;
for (i = 0; i <= n; i++) { /* line 10 */
if (t1[i] > t2[i]) /* line 11 */
return -1;
else if (t1[i] < t2[i]) /* line 13 */
return 1;
}
return 0;
}

/* Bubble sort of a given array 'table' of
a given length 'l' in ascending order */
void bsort (int* table, int l)  //pathcrawler
{
int i, temp, nb;
char done;
done = 0;
nb = 0;
while ( !done && (nb < l-1)){ /* line 10 */
done = 1;
for (i=0 ; i<l-1 ; i++) /* line 12 */
if (table[i] > table[i+1]){ /* line 13 */
done = 0;
temp = table[i];
table[i + 1] = temp;
}
nb++;
}
}

int uninit_var(int a[], int b[]) {   //pathcrawler
int i, k;
for(i=0; i<2; i++) { // line 3
	if(a[i] == 0) // line 4
		return 0;
	if(a[i] != a[i+1]) // line 6
		k = 0;
	else
	if(k == 2) // line 9
		return 0;
	while(b[k] != a[i]) // line 11
		if(k == 2) // line 12
			return 0;
		else
			k++;
	}
	return 1;
}

double average(double value[], double min, double max, int& tcnt, int& vcnt) //sach
{
	double sum=0;
	int i=0;
	tcnt=vcnt=0;
	while(value[i]!=-999 && tcnt<100)
	{
		tcnt++;
		if(min<=value[i]&& value[i]<=max)
		{
			sum+=value[i];
			vcnt++;
		}
		i++;
	}
	
	if(vcnt>0)
		return sum/vcnt;
		return -999;
}

int BinarySearch(int key, int array[], int size)
{
	int lo = 0;
	int hi = size;
	int mid;

	while (lo <= hi) {
		mid = (lo + hi) / 2;

		if (key == array[mid]) {
			return mid;
		}

		if (key < array[mid]) {
			hi = mid - 1;
		}

		else {
			lo = mid + 1;
		}
	}

	return -1;
}

int TestBien_NgayThangHopLe(int day, int month, int year) //Tuong doi cham
{
if (day>=1 && month>=1 && year>=1 && day<=31 && month<=12 && year<=2020)
{
if((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month==12) && (day<=31))  return 1;
if((month == 4 || month == 6 || month == 9 ||month == 11) && (day<=30)) return 1;
if((month == 2) && (day <= 28))  return 1;
if((month == 2) && (day == 29) && (year%4 == 0)&&(year%400 != 0)) return 1;
}
return 0;
}
int SelectionSort(int a[], int n) 
{
	//int n = sizeof(a)/sizeof(a[0]);
 int i,j,min;
   for (i=0; i < n-1; i++)
   {
   min=i;
   for (j=i+1; j<n; j++)
   	if (a[j]>a[min]) min=j;
	int tem=a[i];
   	a[i]=a[min];
   	a[min]=tem;
   	}
    return 0;		
}
int PlusTest(int x, int y){
	//int z = y + x;
	
	if (321 + y * x *1234> 1000)
		return 1;
	else
		return 0;
}

int GCD(int a, int b){ //sach
	if(a<0) a=-a;
	if(b<0) b=-b;
	if(a==0) return b;
	if(b==0) return a;
	while (a!=b)
	{
			if(a>b)
				a=a-b;
			else 
				b=b-a;
	}
	return a;
}

int multiConditionTest(int x){
	if(x<0){
		if(x>-10){
			if(x>-5){
				return 1;
			}
			else if(x<=-5 && x>=-8){
				return 2;
			}
			else return 3;
		}
		else return -1;
	}
	else if(x>0&&x<100){
		if(x<50){
			if(x<20){
				return 4;
			}
			else return 5;
		}
		else if(x>=50&&x<80){
			if(x==60) return 6;
			else return 7;
		}
		else return 8;
	}
	else if(x==101){
		return 9;
	}
	else return x;
}

float distanceTest(float x){
	if(x>1){
		if(x<1.001){
			if(x<1.00001){
				return 1;
			}
			else return 2;
		}
		else return 3;
	}
	else if(x<=1 && x>=0.999){
		return 4;
	}
	return 5;
}

double smallIntervalTest(double x){
	if(x>1.0){
		if(x>2.00001 && x<2.000015){
			return 1;
		}
		else if(x<=2.00001 && x>=1.9999){
			return 2;
		}
		else return 3;
	}
	return 4;
}
int mmin(int a, int b){
	return 1;
}

float maxx(int a){
			int maxx = 5;
			float e;
			int x=0;
			 if (a < maxx){
				x = 1;
				if (a == maxx){
					x = 2;
				}
			}
			e = 1/x;
			return e;
}


float maxmin(int a, int b){
			int max = 50;
			int min = 10;
			float e;
			int x=0;
			int y=0;
			
			 if (a < max && b > min){
				x = 1; y=1;
				if ((a == max) && (b == min )){
					x = 2; y=3;
				}
				
			}
			e = 1/(x*y);
			return e;
}


int gcd(int a, int b)
{
	int temp;

	while (b > 0) {
		temp = a % b;
		a = b;
		b = temp;
	}

	return a;
}
// author: dominhkha
// Function Description:
// intput averageGrade
// averageGrade
// in 90-100, EO: A
// in 80-89, EO: B
// in 70-79, EO: C
// in 60-69, EO: D
// in 0-59: EO: F
// else, EO: I

// Error in function
// Number of error: 5
// if averageGrade is in {90,80,70,60,0} the function will return unexpected result
char grade(int averageGrade){
	if(averageGrade>95 && averageGrade <100)
		return 'A';
	else if(averageGrade>80 && averageGrade< 90)
		return 'B';
	else if(averageGrade>70 && averageGrade<80)
		return 'C';
	else if(averageGrade>60 && averageGrade< 70)
		return 'D';
	else if(averageGrade>0 && averageGrade < 60)
		return 'F';
	return 'I';
}

/*
 author: dominhkha
 input: a,b,c
 Should return 1, if a,b,c represent a Triangle, otherwise return 0;
 Detail: a<b+c && b<a+c && c<a+b, EO: 1
 		otherwise, EO: 0

 Error in function
 Number of error: 2
 if a=b+c or b= a+c, the function will return unexpected result
*/
int isTriangle(int a, int b, int c){
	if((a <= b+c) && (b <= a+c) && (c< a+b)){
		return 1;
	}
	else return 0;
}



/* 
   Should return the type of the triangle
   which has sides of these lengths
   3 = not a triangle
   2 = equilateral triangle
   1 = isoceles triangle
   0 = other triangle
   (PathCrawler)
   
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
	Function Description
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
/*

	if maxx<minn, maxx=minn, x = maxx , x = minn the function may will return unexpected result
*/

float PDF(int x, int minn, int maxx){
	if(x <= minn || x >= maxx){
		return 0;
	}
	return 1/(maxx-minn);
}


/*
	Function Description
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


struct Date{
	int date;
	int month;
	int year;
};

int test2(Date Date){
	if(Date.date<3){
		return 1;
	}
	else return 2;
}

Date calculateAge(Date born, Date today)
{	
	int x, y, z;
	Date result;

	int date = today.date;
	int month = today.month;
	int year = today.year;

	int date1 = born.date;
	int month1 = born.month;
	int year1 = born.year;	

	x = ((year) - (year1 + 1));
	{
		if ((month >= month1) && (date >= date1)) {
			{
				y = (month - month1);
				x = x + 1;
				z = (date - date1);
			}
		} else if (((month > month1) && (date < date1)) && ((month == 5)  || (month == 7) || (month == 10)  || (month == 12))) {
			{
				y = ((month - month1) - 1);
				x = x + 1;
				z = ((30 - date1) + date);
			}
		} else if (((month > month1) && (date < date1)) && ((month == 4) || (month == 1) || (month == 2) || (month == 6) || (month == 8) || (month == 9) || (month == 11))) {
			{
				y = ((month - month1) - 1);
				x = x + 1;
				z = ((31 - date1) + date);
			}
		}

		else if ((month > month1) && (date < date1) && (month == 3)) {
			{
				y = ((month - month1) - 1);
				x = x + 1;
				z = ((28 - date1) + date);
			}
		}

		else if (((month == month1) && (date < date1)) && ((month == 1) || (month == 2) || (month == 4)  || (month == 6) || (month == 8) || (month == 9) || (month == 11))) {
			{
				y = 11;
				z = ((31 - date1) + date);
			}
		}

		else if (((month == month1) && (date < date1)) && ((month == 5) || (month == 7) || (month == 10)  || (month == 12))) {
			{
				z = ((30 - date1) + date);
			}
		}

		else if (month == month1 && date < date1 && month == 3) {
			{
				z = ((28 - date1) + date);
			}
		}

		else if (((month < month1) && (date > date1)) && ((month == 1) || (month == 3) || (month == 5)  || (month == 7) || (month == 8) || (month == 10) || (month == 12))) {
			{
				y = ((12 - month1) + month);
				z = date - date1;
			}
		}

		else if (((month < month1) && (date > date1)) && ((month == 4) || (month == 6) || (month == 9)  || (month == 11))) {
			{
				y = ((12 - month1) + month);
				z = date - date1;
			}
		}

		else if ((month < month1) && (date > date1) && (month == 2)) {
			{
				y = ((12 - month1) + month);
				z = date - date1;
			}
		}

		else if (((month < month1) && (date < date1)) && ((month == 1) || (month == 2) || (month == 4)  || (month == 6) || (month == 8) || (month == 9) || (month == 11))) {
			{
				y = ((12 - month1) + month - 1);
				z = ((31 - date1) + date);
			}
		}

		else if (((month < month1) && (date < date1)) && ((month == 5) || (month == 7) || (month == 10)  || (month == 12))) {
			{
				y = ((12 - month1) + month - 1);
				z = ((30 - date1) + date);
			}
		} else if ((month < month1) && (date < date1) && (month == 3)) {
			{
				y = ((12 - month1) + month - 1);
				z = ((28 - date1) + date);
			}
		}

		else {
			x = y = z = -1;
		}
	}

	result.date = z;
	result.month = y;
	result.year = x;
	
	return result;
}
//
//int calculateZodiac(Date born){
//	int date1 = born.date;
//	int month1 = born.month;
//	int year1 = born.year;	
//	int t;
//	{
//
//		if (((month1 == 3) && (date1 >= 21) && (date1 <= 31)) || ((month1 == 4) && (date1 <= 19))){
//			cout << "\n\n\t\t\tYour Zodiac sign is ARIES";
//			cout << "\n\n 2012 would be a good year overall. You will experience a rise in financial luck and inflows";
//			cout << "\n\n\t\t Best of luck for Your Future ";
//			t = 1;
//		}
//
//		else if (((month1 == 4) && (date1 >= 20) && (date1 <= 30)) || ((month1 == 5) && (date1 <= 20))) {
//			cout << "\n\n\t\tYour Zodiac sign is TAURUS";
//			cout << "\n\n A very eventful year. Career would be on a high growth trajectory and bring in major progress and achievements.";
//			cout << "\n\n\t\t Best of luck for Your Future ";
//			t = 2;
//		}
//
//		else if (((month1 == 5) && (date1 >= 21) && (date1 <= 31)) || ((month1 == 6) && (date1 <= 20))) {
//			cout << "\n\n\t\tYour Zodiac sign is GEMINI";
//			cout << "\n\nA very positive year. Income & professional growth would be immense. You will find the ability to make some very profitable deals now.";
//			cout << "\n\n\t\t Best of luck for Your Future ";
//			t = 3;
//		}
//
//		else if (((month1 == 6) && (date1 >= 21) && (date1 <= 30)) || ((month1 == 7) && (date1 <= 22))) {
//			cout << "\n\n\t\tYour Zodiac sign is CANCER";
//			cout << "\n\nA very eventful year, although negative thoughts and unnecessary pessimism could spoil your spirit and bring in unnecessary tension.";
//			cout << "\n\n\t\t Best of luck for Your Future ";
//			t = 4;
//		}
//
//		else if (((month1 == 7) && (date1 >= 23) && (date1 <= 31)) || ((month1 == 8) && (date1 <= 22))) {
//			cout << "\n\n\t\tYour Zodiac sign is LEO";
//			cout << "\n\nAn exceptional year again. You will see a rise in status and expansion in career this year too. Luck will favor you throughout.";
//			cout << "\n\n\t\t Best of luck for Your Future ";
//			t = 5;
//		}
//
//		else if (((month1 == 8) && (date1 >= 23) && (date1 <= 31)) || ((month1 == 9) && (date1 <= 22))) {
//			cout << "\n\n\t\tYour Zodiac sign is VIRGO";
//			cout << "\n\nA brilliant & positive year, where you will make things happen on your own strength, rather than seeking support of others.";
//			cout << "\n\n\t\t Best of luck for Your Future ";
//			t = 6;
//		}
//
//		else if (((month1 == 9) && (date1 >= 23) && (date1 <= 30)) || ((month1 == 10) && (date1 <= 22))) {
//			cout << "\n\n\t\tYour Zodiac sign is LIBRA";
//			cout << "\n\nA powerful phase will be in operation this month. You will find your role as defined by nature will change and all efforts and activities carried out by you will assume higher importance and effectiveness.";
//			cout << "\n\n\t\t Best of luck for Your Future ";
//			t = 7;
//		}
//
//		else if (((month1 == 10) && (date1 >= 23) && (date1 <= 31)) || ((month1 == 11) && (date1 <= 21))) {
//			cout << "\n\n\t\tYour Zodiac sign is SCORPIO";
//			cout << "\n\nPositive period would continue, although you need to be careful that throwing good money after bad money is not a great idea.";
//			cout << "\n\n\t\t Best of luck for Your Future ";
//			t = 8;
//		}
//
//		else if (((month1 == 11) && (date1 >= 22) && (date1 <= 31)) || ((month1 == 12) && (date1 <= 21))) {
//			cout << "\n\n\t\tYour Zodiac sign is SAGITTARIUS";
//			cout << "\n\n2012 brings in promise and progress. New ideas and cerebral approach to matters will bring in much progress this year. You will be at your creative best till May 2012 and thereafter dynamic activity will take you along.";
//			cout << "\n\n\t\t Best of luck for Your Future ";
//			t = 9;
//		}
//
//		else if (((month1 == 12) && (date1 >= 22) && (date1 <= 30)) || ((month1 == 1) && (date1 <= 19))) {
//			cout << "\n\n\t\tYour Zodiac sign is CAPRICORN";
//			cout << "\n\nA very positive year for you. You would be at your creative best and luck related peak in most of the works you get into.";
//			cout << "\n\n\t\t Best of luck for Your Future ";
//			t = 10;
//		}
//
//		else if (((month1 == 1) && (date1 >= 20) && (date1 <= 31)) || ((month1 == 2) && (date1 <= 18))) {
//			cout << "\n\n\t\tYour Zodiac sign is AQUARIUS";
//			cout << "\n\nA much better year in comparison to 2010 & 2011. You will feel a surge in your luck, productivity and general sense of positive outlook.";
//			cout << "\n\n\t\t Best of luck for Your Future ";
//			t = 11;
//		}
//
//		else if (((month1 == 2) && (date1 >= 19) && (date1 <= 29)) || ((month1 == 3) && (date1 <= 20))) {
//			cout << "\n\n\t\tYour Zodiac sign is PISCES";
//			cout << "\n\nSome amount of struggle and hurdles could come about in life this year. You will have a positive and gainful period till May 2012.";
//			cout << "\n\n\t\t Best of luck for Your Future ";
//			t = 12;
//		}
//		else {
//			t = -1;
//		}
//
//	}
//	
//	return t;
//}

