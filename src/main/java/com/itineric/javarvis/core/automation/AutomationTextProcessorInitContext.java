package com.itineric.javarvis.core.automation;

import java.util.List;

import com.itineric.javarvis.core.InitContext;
import com.itineric.javarvis.core.automation.provider.AutomationProvider;

public interface AutomationTextProcessorInitContext extends InitContext
{
  List<AutomationProvider> getAutomationProviders();
}
