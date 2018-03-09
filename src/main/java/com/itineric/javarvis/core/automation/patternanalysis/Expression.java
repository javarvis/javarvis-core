package com.itineric.javarvis.core.automation.patternanalysis;

public interface Expression
{
  boolean matches(ExpressionsMatchingContext context) throws Exception;
}
