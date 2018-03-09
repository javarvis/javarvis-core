package com.itineric.javarvis.core.automation.pattern2command;

import java.util.List;

public class ConfigurationLine
{
  private final Pattern _pattern;
  private final List<Command> _commands;

  public ConfigurationLine(final Pattern pattern,
                           final List<Command> commands)
  {
    _pattern = pattern;
    _commands = commands;
  }

  public Pattern getPattern()
  {
    return _pattern;
  }

  public List<Command> getCommands()
  {
    return _commands;
  }
}
