/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.itineric.javarvis.core.automation.commandanalysis;

import java.util.List;

/**
 *
 * <p>An expression representing a binary operator on a value
 *
 * @author Nathan Abramson - Art Technology Group
 * @author Shawn Bayern
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: luehe $
 **/

public class BinaryOperatorExpression implements Expression
{
  private Expression _expression;
  private List<BinaryOperator> _operators;
  private List<Expression> _expressions;

  public BinaryOperatorExpression(final Expression expression,
                                  final List<BinaryOperator> operators,
                                  final List<Expression> expressions)
  {
    _expression = expression;
    _operators = operators;
    _expressions = expressions;
  }

  public Expression getExpression()
  {
    return _expression;
  }

  public void setExpression(final Expression expression)
  {
    _expression = expression;
  }

  public List<BinaryOperator> getOperators()
  {
    return _operators;
  }

  public void setOperators(final List<BinaryOperator> operators)
  {
    _operators = operators;
  }

  public List<Expression> getExpressions()
  {
    return _expressions;
  }

  public void setExpressions(final List<Expression> expressions)
  {
    _expressions = expressions;
  }

  @Override
  public String getExpressionString()
  {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("(");
    stringBuilder.append(_expression.getExpressionString());
    for (int i = 0 ; i < _operators.size() ; i++)
    {
      final BinaryOperator operator = _operators.get(i);
      final Expression expression = _expressions.get(i);
      stringBuilder.append(" ");
      stringBuilder.append(operator.getOperatorSymbol());
      stringBuilder.append(" ");
      stringBuilder.append(expression.getExpressionString());
    }
    stringBuilder.append(")");

    return stringBuilder.toString();
  }

  @Override
  public Object evaluate(final Context context)
    throws CommandException
  {
    final Coercions coercions = new Coercions(context);
    Object value = _expression.evaluate(context);
    for (int i = 0 ; i < _operators.size() ; i++)
    {
      final BinaryOperator operator = _operators.get(i);

      if (operator.shouldCoerceToBoolean())
      {
        value = coercions.coerceToBoolean(value);
      }

      if (operator.shouldEvaluate(value))
      {
        final Expression expression = _expressions.get(i);
        final Object nextValue = expression.evaluate(context);

        value = operator.apply(context, value, nextValue);
      }
    }
    return value;
  }
}
