package com.itineric.javarvis.core.automation.commandanalysis;

public interface Expression
{
  String getExpressionString();

  Object evaluate(Context context)
    throws CommandException;
}
