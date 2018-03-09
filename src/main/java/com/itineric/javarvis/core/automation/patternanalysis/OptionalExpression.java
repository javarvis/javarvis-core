package com.itineric.javarvis.core.automation.patternanalysis;

public class OptionalExpression implements Expression
{
  private final Expression _subExpression;

  public OptionalExpression(final Expression expression)
  {
    _subExpression = expression;
  }

  public Expression getSubExpression()
  {
    return _subExpression;
  }

  @Override
  public boolean matches(final ExpressionsMatchingContext context) throws Exception
  {
    context.push();
    final boolean matches = _subExpression.matches(context);
    if (!matches)
    {
      context.pop();
    }
    return true;
  }
}
