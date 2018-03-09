package com.itineric.javarvis.core.automation.commandanalysis;

public class ParameterValue implements Expression
{
  private final int _index;

  public ParameterValue(final String indexAsString)
  {
    _index = Integer.parseInt(indexAsString);
  }

  @Override
  public String getExpressionString()
  {
    return "^" + _index;
  }

  @Override
  public Object evaluate(final Context context)
    throws CommandException
  {
    return context.getParameter(_index);
  }
}
