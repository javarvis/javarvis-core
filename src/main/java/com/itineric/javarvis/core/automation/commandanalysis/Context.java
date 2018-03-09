package com.itineric.javarvis.core.automation.commandanalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itineric.javarvis.core.automation.provider.AutomationProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Context
{
  private final Logger _logger = LogManager.getLogger(Context.class);

  private final Map<String, Object> _variables;
  private final Map<String, AutomationProvider> _automationProviders = new HashMap<String, AutomationProvider>();
  private final List<String> _parameters = new ArrayList<String>();
  private final CommandLogger _commandLogger;

  public Context(final Map<String, Object> defaultVariables,
                 final Collection<? extends AutomationProvider> automationProviders,
                 final List<String> parameters)
  {
    _variables =
      defaultVariables == null ? new HashMap<String, Object>() : new HashMap<String, Object>(defaultVariables);
    for (final AutomationProvider automationProvider : automationProviders)
    {
      _automationProviders.put(automationProvider.getName(), automationProvider);
    }
    _parameters.add(null);
    if (parameters != null)
    {
      _parameters.addAll(parameters);
    }
    _commandLogger = new CommandLogger(_logger);
  }

  public CommandLogger getLogger()
  {
    return _commandLogger;
  }

  public Object getValue(final String name)
  {
    return _variables.get(name);
  }

  public void setValue(final String name, final Object value)
  {
    _variables.put(name, value);
  }

  public Object getParameter(final int index)
  {
    return _parameters.get(index);
  }

  public Object getProvider(final String providerName)
  {
    return _automationProviders.get(providerName);
  }
}
