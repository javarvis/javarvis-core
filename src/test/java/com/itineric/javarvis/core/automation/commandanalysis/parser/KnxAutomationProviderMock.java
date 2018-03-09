package com.itineric.javarvis.core.automation.commandanalysis.parser;

import com.itineric.javarvis.core.automation.provider.AutomationProvider;
import com.itineric.javarvis.core.automation.provider.AutomationProviderInitContext;

public class KnxAutomationProviderMock implements AutomationProvider
{
  @Override
  public void init(final AutomationProviderInitContext context) throws Exception
  {
  }

  @Override
  public void destroy()
  {
  }

  @Override
  public String getName()
  {
    return "knx";
  }

  public boolean write(final String componentType, final String name, final boolean value)
  {
    return true;
  }
}
