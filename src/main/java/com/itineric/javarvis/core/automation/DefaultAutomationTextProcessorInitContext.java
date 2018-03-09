package com.itineric.javarvis.core.automation;

import java.util.List;

import com.itineric.javarvis.core.DefaultInitContext;
import com.itineric.javarvis.core.automation.provider.AutomationProvider;
import org.apache.commons.configuration2.Configuration;

public class DefaultAutomationTextProcessorInitContext extends DefaultInitContext
  implements AutomationTextProcessorInitContext
{
  private final List<AutomationProvider> _automationProviders;

  public DefaultAutomationTextProcessorInitContext(final Configuration configuration,
                                                   final List<AutomationProvider> automationProviders)
  {
    super(configuration);
    _automationProviders = automationProviders;
  }

  @Override
  public List<AutomationProvider> getAutomationProviders()
  {
    return _automationProviders;
  }
}
