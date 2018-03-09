package com.itineric.javarvis.core.automation.pattern2command;

import java.util.List;

public class DefaultPatternMatchingContext implements PatternMatchingContext
{
  private List<String> _parameters;
  private final String _text;

  public DefaultPatternMatchingContext(final String text)
  {
    _text = text;
  }

  @Override
  public String getText()
  {
    return _text;
  }

  @Override
  public List<String> getParameters()
  {
    return _parameters;
  }

  @Override
  public void setParameters(final List<String> parameters)
  {
    _parameters = parameters;
  }
}
