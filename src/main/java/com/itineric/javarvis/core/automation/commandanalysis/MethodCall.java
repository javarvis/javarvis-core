package com.itineric.javarvis.core.automation.commandanalysis;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MethodCall implements Expression
{
  private static final Logger _logger = LogManager.getLogger(MethodCall.class);

  private final Expression _providerExpression;
  private final Expression _methodExpression;
  private final List<Expression> _arguments;

  public MethodCall(final Expression providerExpression,
                    final Expression methodExpression,
                    final List<Expression> arguments)
  {
    _providerExpression = providerExpression;
    _methodExpression = methodExpression;
    _arguments = arguments;
  }

  @Override
  public String getExpressionString()
  {
    final StringBuilder b = new StringBuilder();
    b.append(_providerExpression.getExpressionString());
    b.append(":");
    b.append(_methodExpression.getExpressionString());
    b.append("(");
    final Iterator<Expression> iterator = _arguments.iterator();
    while (iterator.hasNext())
    {
      b.append(((Expression) iterator.next()).getExpressionString());
      if (iterator.hasNext())
      {
        b.append(", ");
      }
    }
    b.append(")");
    return b.toString();
  }

  @Override
  public Object evaluate(final Context context)
    throws CommandException
  {
    final String providerName = evaluate(_providerExpression, context);
    final String methodName = evaluate(_methodExpression, context);
    final Object[] arguments = new Object[_arguments.size()];
    for (int i = 0 ; i < arguments.length ; i++)
    {
      final Expression argumentExpression = _arguments.get(i);
      final Object argument = argumentExpression.evaluate(context);
      arguments[i] = argument;
    }

    final Object provider = context.getProvider(providerName);
    if (provider == null)
    {
      if (_logger.isErrorEnabled())
      {
        _logger.error("No provider named [" + providerName + "] is available");
      }
    }

    try
    {
      return MethodUtils.invokeMethod(provider, methodName, arguments);
    }
    catch (final Exception exception)
    {
      if (_logger.isErrorEnabled())
      {
        _logger.error(exception.getMessage(), exception);
      }
    }
    return null;
  }

  private String evaluate(final Expression expression, final Context context) throws CommandException
  {
    if (expression instanceof NamedValue)
    {
      return ((NamedValue) expression).getName();
    }
    else
    {
      return (String)expression.evaluate(context);
    }
  }
}
