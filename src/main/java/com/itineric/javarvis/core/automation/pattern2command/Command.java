package com.itineric.javarvis.core.automation.pattern2command;

import com.itineric.javarvis.core.automation.commandanalysis.CommandException;
import com.itineric.javarvis.core.automation.commandanalysis.Context;
import com.itineric.javarvis.core.automation.commandanalysis.Expression;

public class Command
{
  private final Expression _expression;

  public Command(final Expression expression)
  {
    _expression = expression;
  }

  public Object evaluate(final Context context)
    throws CommandException
  {
    return _expression.evaluate(context);
  }
}
