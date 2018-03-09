package com.itineric.javarvis.core.automation.patternanalysis;

import java.util.List;

public interface ExpressionsMatchingContext
{
  void push();

  void pop();

  boolean hasNextExpression();

  Expression nextExpression();

  String consumeWord();

  boolean hasRemainingWords();

  boolean nextExpressionMatches(ExpressionsMatchingContext context) throws Exception;

  void addParameter(String parameter);

  List<String> getParameters();
}
