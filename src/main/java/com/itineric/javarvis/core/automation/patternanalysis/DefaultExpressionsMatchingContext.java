package com.itineric.javarvis.core.automation.patternanalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultExpressionsMatchingContext implements ExpressionsMatchingContext
{
  private static class ContextState
  {
    private final int _wordIndex;
    private final int _expressionIndex;
    private final List<String> _parameters;

    ContextState(final int wordIndex, final int expressionIndex, final List<String> parameters)
    {
      _wordIndex = wordIndex;
      _expressionIndex = expressionIndex;
      _parameters = parameters;
    }

    public int getWordIndex()
    {
      return _wordIndex;
    }

    public int getExpressionIndex()
    {
      return _expressionIndex;
    }

    public List<String> getParameters()
    {
      return _parameters;
    }
  }

  private final List<String> _words;
  private final AtomicInteger _wordIndex = new AtomicInteger();
  private final List<Expression> _expressions;
  private final AtomicInteger _expressionIndex = new AtomicInteger();
  private final List<String> _parameters = new ArrayList<String>();
  private final Stack<ContextState> _statesStacks = new Stack<ContextState>();

  public DefaultExpressionsMatchingContext(final List<String> words,
                                           final List<Expression> expressions)
  {
    _words = words;
    _expressions = expressions;
  }

  @Override
  public void push()
  {
    _statesStacks.push(new ContextState(_wordIndex.get(),
                                        _expressionIndex.get(),
                                        new ArrayList<String>(_parameters)));
  }

  @Override
  public void pop()
  {
    final ContextState state = _statesStacks.pop();
    _wordIndex.set(state.getWordIndex());
    _expressionIndex.set(state.getExpressionIndex());
    _parameters.clear();
    _parameters.addAll(state.getParameters());
  }

  @Override
  public boolean hasNextExpression()
  {
    return _expressionIndex.get() < _expressions.size();
  }

  @Override
  public Expression nextExpression()
  {
    return _expressions.get(_expressionIndex.getAndIncrement());
  }

  @Override
  public boolean hasRemainingWords()
  {
    return _words.size() > _wordIndex.get();
  }

  @Override
  public String consumeWord()
  {
    if (!hasRemainingWords())
    {
      return null;
    }
    return _words.get(_wordIndex.getAndIncrement());
  }

  @Override
  public boolean nextExpressionMatches(final ExpressionsMatchingContext context) throws Exception
  {
    final int nextIndex = _expressionIndex.get() + 1;
    if (nextIndex >= _expressions.size())
    {
      return false;
    }
    final Expression expression = _expressions.get(nextIndex);
    return expression.matches(context);
  }

  @Override
  public void addParameter(final String parameter)
  {
    _parameters.add(parameter);
  }

  @Override
  public List<String> getParameters()
  {
    return _parameters;
  }
}
