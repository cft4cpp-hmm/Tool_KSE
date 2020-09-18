package coverage;

public class EnviroCoverageTypeNode extends AbstractEnvironmentNode {
    private String coverageType = STATEMENT;

    public String getCoverageType() {
        return coverageType;
    }

    public void setCoverageType(String coverageType) {
        this.coverageType = coverageType;
    }


    @Override
    public String toString() {
        return super.toString() + ": type = " + getCoverageType();
    }

    @Override
    public String exportToFile() {
        return ENVIRO_COVERAGE_TYPE + " " + getCoverageType();
    }

    public static final String STATEMENT = "STATEMENT";
    public static final String BRANCH = "BRANCH";
    public static final String BASIS_PATH = "BASIS_PATH";
    public static final String MCDC = "MCDC";
    public static final String STATEMENT_AND_MCDC = "STATEMENT+MC/DC";
    public static final String STATEMENT_AND_BRANCH = "STATEMENT+BRANCH";
}
