package testcase_manager;

import config.CommandConfig;

import java.time.LocalDateTime;
import java.util.List;

public interface ITestCase {
    String POSTFIX_TESTCASE_BY_USER = ".manual";
    String POSTFIX_TESTCASE_BY_RANDOM = ".random";
    String POSTFIX_TESTCASE_BY_DIRECTED_METHOD = ".directed";

    String COMPOUND_SIGNAL = "COMPOUND";
    String PROTOTYPE_SIGNAL = "PROTOTYPE_";
    String AKA_SIGNAL = ".";

    String STATUS_NA = "N/A";
    String STATUS_EXECUTING = "executing";
    String STATUS_SUCCESS = "success";
    String STATUS_FAILED = "failed";
    String STATUS_RUNTIME_ERR = "runtime error";
    String STATUS_EMPTY = "no status";

    String getName();

    void setName(String name);

    String STATEMENT_COVERAGE_FILE_EXTENSION = ".stm.cov";
    String BRANCH_COVERAGE_FILE_EXTENSION = ".branch.cov";

    String STATEMENT_PROGRESS_FILE_EXTENSION = ".stm.pro";
    String BRANCH_PROGRESS_FILE_EXTENSION = ".branch.pro";
}
