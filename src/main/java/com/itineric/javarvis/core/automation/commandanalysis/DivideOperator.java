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

import java.math.BigDecimal;

public class DivideOperator extends BinaryOperator
{
  public static final DivideOperator SINGLETON = new DivideOperator();

  public DivideOperator()
  {
  }

  @Override
  public String getOperatorSymbol()
  {
    return "/";
  }

  @Override
  public Object apply(final Context context,
                      final Object leftAsObject,
                      final Object rightAsObject)
    throws CommandException
  {
    final Coercions coercions = new Coercions(context);
    final CommandLogger logger = context.getLogger();
    if (leftAsObject == null
      && rightAsObject == null)
    {
      if (logger.isLoggingWarning())
      {
        logger.logWarning("Attempt to apply operator [" + getOperatorSymbol() + "] to null value");
      }
      return 0;
    }

    if (coercions.isBigDecimal(leftAsObject)
      || coercions.isBigInteger(leftAsObject)
      || coercions.isBigDecimal(rightAsObject)
      || coercions.isBigInteger(rightAsObject))
    {

      final BigDecimal left = (BigDecimal) coercions.coerceToPrimitiveNumber(leftAsObject, BigDecimal.class);
      final BigDecimal right = (BigDecimal) coercions.coerceToPrimitiveNumber(rightAsObject, BigDecimal.class);

      try
      {
        return left.divide(right, BigDecimal.ROUND_HALF_UP);
      }
      catch (final Exception exception)
      {
        if (logger.isLoggingError())
        {
          logger.logError("An error occurred applying operator [" + getOperatorSymbol() + "] to operands ["
                            + left + "] and [" + right + "]",
                          exception);
        }
        return 0;
      }
    }
    else
    {

      final double left = coercions.coerceToPrimitiveNumber(leftAsObject, Double.class).doubleValue();
      final double right = coercions.coerceToPrimitiveNumber(rightAsObject, Double.class).doubleValue();

      try
      {
        return left / right;
      }
      catch (final Exception exception)
      {
        if (logger.isLoggingError())
        {
          logger.logError("An error occurred applying operator [" + getOperatorSymbol() + "] to operands ["
                            + left + "] and [" + right + "]",
                          exception);
        }
        return 0;
      }
    }
  }
}
