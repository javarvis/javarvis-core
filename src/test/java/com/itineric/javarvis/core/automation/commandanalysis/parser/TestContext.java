package com.itineric.javarvis.core.automation.commandanalysis.parser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.itineric.javarvis.core.automation.commandanalysis.Context;
import com.itineric.javarvis.core.automation.provider.AutomationProvider;

public class TestContext extends Context
{
  public TestContext(final Map<String, Object> variables,
                     final Collection<AutomationProvider> automationProviders,
                     final List<String> parameters)
  {
    super(variables,
          automationProviders,
          parameters);
  }
}
