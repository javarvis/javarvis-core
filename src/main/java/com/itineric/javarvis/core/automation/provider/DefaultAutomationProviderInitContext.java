package com.itineric.javarvis.core.automation.provider;

import java.util.Map;

import com.itineric.javarvis.core.DefaultInitContext;
import org.apache.commons.configuration2.Configuration;

public class DefaultAutomationProviderInitContext extends DefaultInitContext
  implements AutomationProviderInitContext
{
  private final Map<Class<?>, ?> _providers;

  public DefaultAutomationProviderInitContext(final Configuration configuration,
                                              final Map<Class<?>, ?> providers)
  {
    super(configuration);
    _providers = providers;
  }

  @Override
  public Map<Class<?>, ?> getProviders()
  {
    return _providers;
  }
}
