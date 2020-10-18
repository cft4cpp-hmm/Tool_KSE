package HybridAutoTestGen;

import javafx.util.Pair;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//Test data is a list of pair <param, value>
public class TestData
{
    private List<Pair<String, Object>> testData = new ArrayList<>();

    public TestData()
    {

    }

    public TestData(String param, Object value)
    {
        testData.add(new Pair<>(param, value));
    }

    public List<Pair<String, Object>> getTestData()
    {
        return testData;
    }

    public TestData clone()
    {
        TestData ret = new TestData();

        List<Pair<String, Object>> newTestData = new ArrayList<>();

        for (Pair<String, Object> pair: testData)
        {
            Pair<String, Object> newPair = new Pair<>(pair.getKey(), pair.getValue());

            newTestData.add(newPair);
        }
        ret.testData = newTestData;

        return ret;

    }

    public void add(Pair<String, Object> pair)
    {
        testData.add(pair);
    }

    public void add(List<Pair<String, Object>> listTestData)
    {
        testData.addAll(listTestData);
    }

    public boolean isExist(Pair<String, Object> pair)
    {
        for (Pair<String, Object> item : testData)
        {
            if (item.getKey().equals(pair.getKey()) && item.getValue().equals(pair.getValue()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isExist(String key)
    {
        for (Pair<String, Object> item : testData)
        {
            if (item.getKey().equals(key))
            {
                return true;
            }
        }
        return false;
    }

    public boolean equals(Object o)
    {
        TestData other = (TestData) o;
        boolean ret = false;

        if (o == null)
        {
            return false;
        }
        if (testData.size() != other.getTestData().size())
        {
            return false;
        }
        for (int i = 0; i < other.getTestData().size(); i++)
        {
            if (!isExist(other.getTestData().get(i)))
            {
                return false;
            }
        }
        return true;
    }

    //Parse một chuỗi dạng "x=2;y=3" thành một đối tượng TestData chứa hai cặp <x,2>; <y,3>
    public static TestData parseString(String solutionString)
    {
        TestData testData = new TestData();

        String[] solutionList = solutionString.split(";");

        //List<Pair<String, Object>> newList = new ArrayList<>();

        for (String solution : solutionList)
        {
            if (solution.contains("="))
            {
                String param = solution.split("=")[0];
                String value = solution.split("=")[1];
                value = value.replace("(", "").replace(")", "").replace(" ","").trim();

                Random rand = new Random();
                double val = 0;
                try
                {
                    val = Double.parseDouble(value);
                }
                catch (Exception ex)
                {
                    val = rand.nextInt(100);
                }

                Pair<String, Object> newResult = new Pair<>(param, val);

                //newList.add(newResult);

                testData.add(newResult);
            }
        }
        return testData;

    }

    @Override
    public java.lang.String toString()
    {
        String ret = "";
        for (Pair<String, Object> item : testData)
        {
            ret += item.getKey() + "=" + item.getValue() + ";";
        }

        return ret;
    }
}
