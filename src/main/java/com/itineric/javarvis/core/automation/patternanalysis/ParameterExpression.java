package com.itineric.javarvis.core.automation.patternanalysis;

public class ParameterExpression implements Expression
{
  private final WordExpression _wordExpression;

  public ParameterExpression(final WordExpression wordExpression)
  {
    _wordExpression = wordExpression;
  }

  public WordExpression getWordExpression()
  {
    return _wordExpression;
  }

  @Override
  public boolean matches(final ExpressionsMatchingContext context) throws Exception
  {
    final String parameter = _wordExpression.matchWords(context);
    if (parameter != null)
    {
      context.addParameter(parameter);
      return true;
    }
    return false;
  }
}
