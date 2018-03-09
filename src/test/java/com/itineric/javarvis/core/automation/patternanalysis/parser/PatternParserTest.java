package com.itineric.javarvis.core.automation.patternanalysis.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import com.itineric.javarvis.core.automation.pattern2command.DefaultPatternMatchingContext;
import com.itineric.javarvis.core.automation.pattern2command.Pattern;
import com.itineric.javarvis.core.automation.pattern2command.PatternMatchingContext;
import com.itineric.javarvis.core.automation.patternanalysis.Expression;
import com.itineric.javarvis.core.automation.patternanalysis.OptionalExpression;
import com.itineric.javarvis.core.automation.patternanalysis.OrExpression;
import com.itineric.javarvis.core.automation.patternanalysis.ParameterExpression;
import com.itineric.javarvis.core.automation.patternanalysis.ParenthesisExpression;
import com.itineric.javarvis.core.automation.patternanalysis.WordExpression;
import org.junit.Test;

public class PatternParserTest
{
  @Test
  public void parsePattern1() throws Exception
  {
    final PatternParser patternParser = createPatternParser("allum* *? lumiere* ((de l*)|du)? ^**");
    final Pattern pattern = patternParser.pattern();
    final List<Expression> expressions = pattern.getExpressions();
    assertEquals(5, expressions.size());
    Expression expression = expressions.get(0);
    assertTrue(expression instanceof WordExpression);
    WordExpression wordExpression = (WordExpression)expression;
    assertEquals("allum*", wordExpression.getWordPattern());
    expression = expressions.get(1);
    assertTrue(expression instanceof OptionalExpression);
    OptionalExpression optionalExpression = (OptionalExpression)expression;
    final Expression subExpression = optionalExpression.getSubExpression();
    assertTrue(subExpression instanceof WordExpression);
    wordExpression = (WordExpression)subExpression;
    assertEquals("*", wordExpression.getWordPattern());
    expression = expressions.get(2);
    assertTrue(expression instanceof WordExpression);
    wordExpression = (WordExpression)expression;
    assertEquals("lumiere*", wordExpression.getWordPattern());
    expression = expressions.get(3);
    assertTrue(expression instanceof OptionalExpression);
    optionalExpression = (OptionalExpression)expression;
    expression = optionalExpression.getSubExpression();
    assertTrue(expression instanceof ParenthesisExpression);
    ParenthesisExpression parenthesisExpression = (ParenthesisExpression)expression;
    List<Expression> parenthesisExpressions = parenthesisExpression.getExpressions();
    assertEquals(1, parenthesisExpressions.size());
    expression = parenthesisExpressions.get(0);
    assertTrue(expression instanceof OrExpression);
    final OrExpression orExpression = (OrExpression)expression;
    expression = orExpression.getLeftExpression();
    assertTrue(expression instanceof ParenthesisExpression);
    parenthesisExpression = (ParenthesisExpression)expression;
    parenthesisExpressions = parenthesisExpression.getExpressions();
    assertEquals(2, parenthesisExpressions.size());
    expression = parenthesisExpressions.get(0);
    assertTrue(expression instanceof WordExpression);
    wordExpression = (WordExpression)expression;
    assertEquals("de", wordExpression.getWordPattern());
    expression = parenthesisExpressions.get(1);
    assertTrue(expression instanceof WordExpression);
    wordExpression = (WordExpression)expression;
    assertEquals("l*", wordExpression.getWordPattern());
    expression = orExpression.getRightExpression();
    assertTrue(expression instanceof WordExpression);
    wordExpression = (WordExpression)expression;
    assertEquals("du", wordExpression.getWordPattern());
    expression = expressions.get(4);
    assertTrue(expression instanceof ParameterExpression);
    final ParameterExpression parameterExpression = (ParameterExpression)expression;
    wordExpression = parameterExpression.getWordExpression();
    assertEquals("**", wordExpression.getWordPattern());

    PatternMatchingContext context = createContext("allume la lumiere du salon");
    boolean matches = pattern.matches(context);
    assertTrue(matches);
    assertEquals(Arrays.asList("salon"), context.getParameters());

    context = createContext("allume la lumiere de la salle à manger");
    matches = pattern.matches(context);
    assertTrue(matches);
    assertEquals(Arrays.asList("salle a manger"), context.getParameters());

    context = createContext("allume la lumiere de l'évier");
    matches = pattern.matches(context);
    assertTrue(matches);
    assertEquals(Arrays.asList("evier"), context.getParameters());

    matches = pattern.matches(createContext("allume lumiere de la salle à manger"));
    assertFalse(matches);
    matches = pattern.matches(createContext("fais rien"));
    assertFalse(matches);
  }

  @Test
  public void parsePattern2() throws Exception
  {
    final PatternParser patternParser = createPatternParser("present* toi");
    patternParser.pattern();
  }

  private PatternParser createPatternParser(final String pattern)
  {
    final PatternParser patternParser = new PatternParser(new StringReader(pattern));
    return patternParser;
  }

  private PatternMatchingContext createContext(final String text)
  {
    return new DefaultPatternMatchingContext(text);
  }
}
