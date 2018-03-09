package com.itineric.javarvis.core.automation.patternanalysis;

public class WordExpression implements Expression
{
  private final String _wordPattern;

  public WordExpression(final String wordPattern)
  {
    _wordPattern = wordPattern;
  }

  public String getWordPattern()
  {
    return _wordPattern;
  }

  @Override
  public boolean matches(final ExpressionsMatchingContext context) throws Exception
  {
    return matchWords(context) != null;
  }

  String matchWords(final ExpressionsMatchingContext context) throws Exception
  {
    String word = context.consumeWord();
    if (word == null)
    {
      return null;
    }

    if ("**".equals(_wordPattern))
    {
      String words = word;
      boolean nextMatches;
      do
      {
        nextMatches = context.nextExpressionMatches(context);
        if (!nextMatches)
        {
          word = context.consumeWord();
          if (word != null)
          {
            words += " " + word;
          }
        }
      }
      while (!nextMatches && word != null);
      return words;
    }
    else
    {
      if ("*".equals(_wordPattern))
      {
        return word;
      }
      else
      {
        String wordRegex = null;
        if (_wordPattern.endsWith("*"))
        {
          wordRegex = _wordPattern.substring(0, _wordPattern.length() - 1);
          wordRegex += ".*";
        }

        if (wordRegex != null)
        {
          if (word.matches(wordRegex))
          {
            return word;
          }
        }
        else
        {
          if (word.equals(_wordPattern))
          {
            return word;
          }
        }
      }
    }

    return null;
  }
}
