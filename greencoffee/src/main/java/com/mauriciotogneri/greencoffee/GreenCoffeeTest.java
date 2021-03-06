package com.mauriciotogneri.greencoffee;

import com.mauriciotogneri.greencoffee.exceptions.DuplicatedStepDefinitionException;
import com.mauriciotogneri.greencoffee.exceptions.StepDefinitionNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gherkin.ast.Node;
import gherkin.ast.Step;

public class GreenCoffeeTest
{
    private final Scenario scenario;
    private final TestLog testLog;
    private final ScreenCapture screenCapture;

    public GreenCoffeeTest(Scenario scenario, String screenshotsPath)
    {
        this.scenario = scenario;
        this.testLog = new TestLog();
        this.screenCapture = new ScreenCapture(screenshotsPath);
    }

    public GreenCoffeeTest(Scenario scenario)
    {
        this.scenario = scenario;
        this.testLog = new TestLog();
        this.screenCapture = null;
    }

    protected void start(GreenCoffeeSteps firstTarget, GreenCoffeeSteps... restTargets)
    {
        testLog.logScenario(scenario);

        List<StepDefinition> stepDefinitions = firstTarget.stepDefinitions();

        for (GreenCoffeeSteps greenCoffeeSteps : restTargets)
        {
            stepDefinitions.addAll(greenCoffeeSteps.stepDefinitions());
        }

        validateStepDefinitions(stepDefinitions);

        try
        {
            for (Step step : scenario.steps())
            {
                processStep(step, stepDefinitions);
            }
        }
        catch (Exception e)
        {
            if (screenCapture != null)
            {
                screenCapture.takeScreenshot();
            }

            throw e;
        }
    }

    private void validateStepDefinitions(List<StepDefinition> stepDefinitions)
    {
        Set<String> patterns = new HashSet<>();

        for (StepDefinition stepDefinition : stepDefinitions)
        {
            String pattern = stepDefinition.pattern();

            if (!patterns.contains(pattern))
            {
                patterns.add(pattern);
            }
            else
            {
                throw new DuplicatedStepDefinitionException(stepDefinition.method(), pattern);
            }
        }
    }

    private void processStep(Step step, List<StepDefinition> stepDefinitions)
    {
        String keyword = step.getKeyword().trim();
        String text = step.getText().trim();
        Node argument = step.getArgument();

        testLog.logStep(keyword, text);

        for (StepDefinition stepDefinition : stepDefinitions)
        {
            if (stepDefinition.matches(text))
            {
                stepDefinition.invoke(text, argument);
                return;
            }
        }

        throw new StepDefinitionNotFoundException(keyword, text);
    }
}