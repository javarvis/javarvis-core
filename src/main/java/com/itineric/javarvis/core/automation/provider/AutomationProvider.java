package com.itineric.javarvis.core.automation.provider;

public interface AutomationProvider
{
  void init(AutomationProviderInitContext context)
    throws Exception;

  void destroy();

  String getName();
}
