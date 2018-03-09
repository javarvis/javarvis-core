package com.itineric.javarvis.core.automation.patternanalysis;

public class OrExpression implements Expression
{
  private final Expression _leftExpression;
  private final Expression _rightExpression;

  public OrExpression(final Expression leftExpression, final Expression rightExpression)
  {
    _leftExpression = leftExpression;
    _rightExpression = rightExpression;
  }

  public Expression getLeftExpression()
  {
    return _leftExpression;
  }

  public Expression getRightExpression()
  {
    return _rightExpression;
  }

  @Override
  public boolean matches(final ExpressionsMatchingContext context) throws Exception
  {
    context.push();
    final boolean matches = _leftExpression.matches(context);
    if (!matches)
    {
      context.pop();
    }
    else
    {
      return true;
    }
    return _rightExpression.matches(context);
  }
}
