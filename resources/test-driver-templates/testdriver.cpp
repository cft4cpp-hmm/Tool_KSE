/*
 * TEST DRIVER FOR C++
 * @author: VNU-UET
 * Generate automatically by UETAUTO
 */

// include some necessary standard libraries
#include <cstdio>
#include <string>
#include <fstream>

#define ASSERT_ENABLE

// define maximum line of test path
#define UET_MARK_MAX 5000

// function call counter
int UET_fCall = 0;

// test case name
std::string UET_test_case_name;

typedef void (*UET_Test)();

void UET_run_test(std::string name, UET_Test test, int iterator);

int main(int argc, char *argv[]);

////////////////////////////////////////
//  BEGIN TEST PATH SECTION           //
////////////////////////////////////////

#define UET_TEST_PATH_FILE "{{INSERT_PATH_OF_TEST_PATH_HERE}}"

void UET_append_test_path(std::string content);

int UET_mark(std::string append);

////////////////////////////////////////
//  END TEST PATH SECTION             //
////////////////////////////////////////


////////////////////////////////////////
//  BEGIN TEST RESULT SECTION         //
////////////////////////////////////////

//void UET_append_test_result(std::string content);

void UET_assert_method
(
    std::string actualName, int actualVal,
    std::string expectedName, int expectedVal,
    std::string method
);

void UET_assert_double_method
(
    std::string actualName, double actualVal,
    std::string expectedName, double expectedVal,
    std::string method
);

void UET_assert_ptr_method
(
    std::string actualName, void* actualVal,
    std::string expectedName, void* expectedVal,
    std::string method
);

#define NULL_STRING ""

void UET_assert
(
    std::string actualName, int actualVal,
    std::string expectedName, int expectedVal
)
{
    UET_assert_method
    (
        actualName, actualVal,
        expectedName, expectedVal,
        NULL_STRING
    );
}

int UET_assert_double
(
    std::string actualName, double actualVal,
    std::string expectedName, double expectedVal
)
{
    UET_assert_double_method
    (
        actualName, actualVal,
        expectedName, expectedVal,
        NULL_STRING
    );
}

int UET_assert_ptr
(
    std::string actualName, void* actualVal,
    std::string expectedName, void* expectedVal
)
{
    UET_assert_ptr_method
    (
        actualName, actualVal,
        expectedName, expectedVal,
        NULL_STRING
    );
}

////////////////////////////////////////
//  END TEST RESULT SECTION           //
////////////////////////////////////////

// Some test cases need to include specific additional headers

// Include uetignore file
{{INSERT_CLONE_SOURCE_FILE_PATHS_HERE}}

////////////////////////////////////////
//  BEGIN TEST SCRIPTS SECTION        //
////////////////////////////////////////

{{INSERT_TEST_SCRIPTS_HERE}}

////////////////////////////////////////
//  END TEST SCRIPTS SECTION          //
////////////////////////////////////////

/*
 * The main() function for setting up and running the tests.
 */
int main(int argc, char *argv[])
{
    /* Compound test case setup */

    /* add & run the tests */
    {{ADD_TESTS_STM}}

    /* Compound test case teardown */

    return 0;
}

////////////////////////////////////////
//  BEGIN DEFINITIONS SECTION         //
////////////////////////////////////////

void UET_append_test_path(std::string content)
{
    static int UET_mark_iterator = 0;

    std::ofstream outfile;
    outfile.open(UET_TEST_PATH_FILE, std::ios_base::app);
    outfile << content;
    UET_mark_iterator++;

    // if the test path is too long, we need to terminate the process
    if (UET_mark_iterator >= UET_MARK_MAX) {
        outfile << "\nThe test path is too long. Terminate the program automatically!";
        outfile.close();
        exit(0);
    }

    outfile.close();
}

//void UET_append_test_result(std::string content)
//{
//    std::ofstream outfile;
//    outfile.open(UET_EXEC_TRACE_FILE, std::ios_base::app);
//    outfile << content;
//    outfile.close();
//}

int UET_mark(std::string append)
{
    UET_append_test_path(append + "\n");
    return 1;
}

#define UET_BUFFER_SIZE 1024

void UET_assert_method
(
    std::string actualName, int actualVal,
    std::string expectedName, int expectedVal,
    std::string userCode
)
{
    std::string buf = "{\n";

    buf.append("\"tag\": \"UET function calls: ");
    char temp0[UET_BUFFER_SIZE];
    sprintf(temp0, "%d\",", UET_fCall);
    buf.append(temp0);
    buf.append("\n");

    if (!userCode.empty())
    {
        buf.append("\"userCode\": \"");
        buf.append(userCode);
        buf.append("\",\n");
    }

    buf.append("\"actualName\": \"");
    buf.append(actualName);
    buf.append("\",\n");
    char temp1[UET_BUFFER_SIZE];
    sprintf(temp1, "\"actualVal\": \"%d\",", actualVal);
    buf.append(temp1);
    buf.append("\n");

    buf.append("\"expectedName\": \"");
    buf.append(expectedName);
    buf.append("\",\n");
    char temp2[UET_BUFFER_SIZE];
    sprintf(temp2, "\"expectedVal\": \"%d\"", expectedVal);
    buf.append(temp2);
    buf.append("\n},\n");

//    UET_append_test_result(buf);
}

void UET_assert_double_method
(
    std::string actualName, double actualVal,
    std::string expectedName, double expectedVal,
    std::string userCode
)
{
    std::string buf = "{\n";

    buf.append("\"tag\": \"UET function calls: ");
    char temp0[UET_BUFFER_SIZE];
    sprintf(temp0, "%d\",", UET_fCall);
    buf.append(temp0);
    buf.append("\n");

    if (!userCode.empty())
    {
        buf.append("\"userCode\": \"");
        buf.append(userCode);
        buf.append("\",\n");
    }

    buf.append("\"actualName\": \"");
    buf.append(actualName);
    buf.append("\",\n");

    char temp1[UET_BUFFER_SIZE];
    sprintf(temp1, "\"actualVal\": \"%lf\",", actualVal);
    buf.append(temp1);
    buf.append("\n");

    buf.append("\"expectedName\": \"");
    buf.append(expectedName);
    buf.append("\",\n");

    char temp2[UET_BUFFER_SIZE];
    sprintf(temp2, "\"expectedVal\": \"%lf\"", expectedVal);
    buf.append(temp2);
    buf.append("\n},\n");

//    UET_append_test_result(buf);
}

void UET_assert_ptr_method
(
    std::string actualName, void * actualVal,
    std::string expectedName, void * expectedVal,
    std::string userCode
)
{
    std::string buf = "{\n";

    buf.append("\"tag\": \"UET function calls: ");
    char temp0[UET_BUFFER_SIZE];
    sprintf(temp0, "%d\",", UET_fCall);
    buf.append(temp0);
    buf.append("\n");

    if (!userCode.empty())
    {
        buf.append("\"userCode\": \"");
        buf.append(userCode);
        buf.append("\",\n");
    }

    buf.append("\"actualName\": \"");
    buf.append(actualName);
    buf.append("\",\n");

    char temp1[UET_BUFFER_SIZE];
    sprintf(temp1, "\"actualVal\": \"%x\",", actualVal);
    buf.append(temp1);
    buf.append("\n");

    buf.append("\"expectedName\": \"");
    buf.append(expectedName);
    buf.append("\",\n");

    char temp2[UET_BUFFER_SIZE];
    sprintf(temp2, "\"expectedVal\": \"%x\"", expectedVal);
    buf.append(temp2);
    buf.append("\n},\n");

//    UET_append_test_result(buf);
}

void UET_run_test(std::string name, UET_Test test, int iterator)
{
    std::string begin = "BEGIN OF " + name;
    UET_mark(begin);

    int i;
    for (i = 0; i < iterator; i++) {
        test();
    }

    std::string end = "END OF " + name;
    UET_mark(end);
}

void UET_set_up()
{
    /*{{INSERT_SET_UP_HERE}}*/
}

void UET_tear_down()
{
    /*{{INSERT_TEAR_DOWN_HERE}}*/
}

////////////////////////////////////////
//  END DEFINITIONS SECTION           //
////////////////////////////////////////