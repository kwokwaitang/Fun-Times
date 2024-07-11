package com.ford;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {
                "progress",
                "html:build/report/html",
                "junit:build/report/junit/cucumber-report.xml",
                "json:build/report/json/cucumber-report.json"
        },
        glue = {"com.ford"},
        features = {"src/test/resources/com/ford/features"}
)
public class CucumberRunnerTest {
}
