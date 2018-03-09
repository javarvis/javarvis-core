package com.itineric.javarvis.core.automation.pattern2command;

import java.util.Arrays;
import java.util.List;

import com.itineric.javarvis.core.automation.patternanalysis.DefaultExpressionsMatchingContext;
import com.itineric.javarvis.core.automation.patternanalysis.Expression;
import com.itineric.javarvis.core.automation.patternanalysis.ExpressionsMatchingContext;
import com.itineric.javarvis.core.util.TextHelper;

public class Pattern
{
  private final List<Expression> _expressions;
  private String _rawRepresentation;

  public Pattern(final List<Expression> expressions)
  {
    _expressions = expressions;
  }

  public String getRawRepresentation()
  {
    return _rawRepresentation;
  }

  public List<Expression> getExpressions()
  {
    return _expressions;
  }

  public void setRawRepresentation(final String rawRepresentation)
  {
    _rawRepresentation = rawRepresentation;
  }

  public boolean matches(final PatternMatchingContext context)
    throws Exception
  {
    final String text = context.getText();
    final String normalizedText = TextHelper.normalizeText(text);

    final List<String> words = Arrays.asList(normalizedText.split(" "));

    final ExpressionsMatchingContext expressionsContext =
      new DefaultExpressionsMatchingContext(words,
                                            _expressions);
    while (expressionsContext.hasNextExpression())
    {
      final Expression expression = expressionsContext.nextExpression();
      final boolean matches = expression.matches(expressionsContext);
      if (!matches)
      {
        return false;
      }
    }

    if (!expressionsContext.hasRemainingWords())
    {
      context.setParameters(expressionsContext.getParameters());
      return true;
    }

    return false;
  }
}
