package com.itineric.javarvis.core.automation.provider;

import java.util.Map;

import com.itineric.javarvis.core.InitContext;

public interface AutomationProviderInitContext extends InitContext
{
  Map<Class<?>, ?> getProviders();
}
