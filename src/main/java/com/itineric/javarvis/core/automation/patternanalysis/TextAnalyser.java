package com.itineric.javarvis.core.automation.patternanalysis;

import java.io.StringReader;

import com.itineric.javarvis.core.automation.pattern2command.Pattern;
import com.itineric.javarvis.core.automation.patternanalysis.parser.PatternParser;

public class TextAnalyser
{
  public Pattern parsePattern(final String patternAsString) throws Exception
  {
    final PatternParser patternParser = new PatternParser(new StringReader(patternAsString));
    final Pattern pattern = patternParser.pattern();
    pattern.setRawRepresentation(patternAsString);
    return pattern;
  }
}
