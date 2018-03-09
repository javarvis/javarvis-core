package com.itineric.javarvis.core.automation.commandanalysis;

public class AssignExpression implements Expression
{
  private final NamedValue _variableExpression;
  private final Expression _valueExpression;

  public AssignExpression(final NamedValue variableExpression,
                          final Expression valueExpression)
  {
    _variableExpression = variableExpression;
    _valueExpression = valueExpression;
  }

  @Override
  public String getExpressionString()
  {
    return _variableExpression.getExpressionString() + " = " + _valueExpression.getExpressionString();
  }

  @Override
  public Object evaluate(final Context context) throws CommandException
  {
    final String variableName = _variableExpression.getName();
    final Object value = _valueExpression.evaluate(context);
    context.setValue(variableName, value);
    return value;
  }

}
