package com.itineric.javarvis.core.automation.patternanalysis;

import java.util.List;

public class ParenthesisExpression implements Expression
{
  private final List<Expression> _expressions;

  public ParenthesisExpression(final List<Expression> expressions)
  {
    _expressions = expressions;
  }

  public List<Expression> getExpressions()
  {
    return _expressions;
  }

  @Override
  public boolean matches(final ExpressionsMatchingContext context) throws Exception
  {
    for (final Expression expression : _expressions)
    {
      final boolean matches = expression.matches(context);
      if (!matches)
      {
        return false;
      }
    }
    return true;
  }
}
