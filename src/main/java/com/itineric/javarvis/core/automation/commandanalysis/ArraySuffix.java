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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 *
 * <p>Represents an operator that obtains a Map entry, an indexed
 * value, a property value, or an indexed property value of an object.
 * The following are the rules for evaluating this operator:
 *
 * <ul><pre>
 * Evaluating a[b] (assuming a.b == a["b"])
 *   a is null
 *     return null
 *   b is null
 *     return null
 *   a is Map
 *     !a.containsKey (b)
 *       return null
 *     a.get(b) == null
 *       return null
 *     otherwise
 *       return a.get(b)
 *   a is List or array
 *     coerce b to int (using coercion rules)
 *     coercion couldn't be performed
 *       error
 *     a.get(b) or Array.get(a, b) throws ArrayIndexOutOfBoundsException or IndexOutOfBoundsException
 *       return null
 *     a.get(b) or Array.get(a, b) throws other exception
 *       error
 *     return a.get(b) or Array.get(a, b)
 *
 *   coerce b to String
 *   b is a readable property of a
 *     getter throws an exception
 *       error
 *     otherwise
 *       return result of getter call
 *
 *   otherwise
 *     error
 * </pre></ul>
 *
 * @author Nathan Abramson - Art Technology Group
 * @author Shawn Bayern
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: luehe $
 **/

public class ArraySuffix extends ValueSuffix
{
  private Expression _index;

  public ArraySuffix(final Expression index)
  {
    _index = index;
  }

  public Expression getIndex()
  {
    return _index;
  }

  public void setIndex(final Expression index)
  {
    _index = index;
  }

  Object evaluateIndex(final Context context)
    throws CommandException
  {
    return _index.evaluate(context);
  }

  String getOperatorSymbol()
  {
    return "[]";
  }

  @Override
  public String getExpressionString()
  {
    return "[" + _index.getExpressionString() + "]";
  }

  @Override
  public Object evaluate(final Context context, final Object value) throws CommandException
  {
    final Coercions coercions = new Coercions(context);
    final CommandLogger logger = context.getLogger();
    Object indexVal;
    String indexStr;

    if (value == null)
    {
      if (logger.isLoggingWarning())
      {
        logger.logWarning("Attempt to apply the [" + getOperatorSymbol() + "] operator to a null value");
      }
      return null;
    }
    else if ((indexVal = evaluateIndex(context)) == null)
    {
      if (logger.isLoggingWarning())
      {
        logger.logWarning("Attempt to apply a null index to the [" + getOperatorSymbol() + "] operator");
      }
      return null;
    }
    else if (value instanceof Map)
    {
      final Map<?, ?> val = (Map<?, ?>) value;
      return val.get(indexVal);
    }
    else if (value instanceof List
      || value.getClass().isArray())
    {
      final Integer indexObj = coercions.coerceToInteger(indexVal);
      if (indexObj == null)
      {
        if (logger.isLoggingError())
        {
          logger.logError("The [" + getOperatorSymbol() + "] operator was supplied with an index value of type ["
                            + indexVal.getClass().getName() + "] to be applied to a List or array, but "
                            + "that value cannot be converted to an integer.");
        }
        return null;
      }
      else if (value instanceof List)
      {
        try
        {
          @SuppressWarnings("unchecked")
          final List<Object> values = (List<Object>) value;
          return values.get(indexObj.intValue());
        }
        catch (final Exception exception)
        {
          if (logger.isLoggingWarning())
          {
            logger.logWarning("An exception occurred while trying to access index [" + indexObj + "] of a List",
                              exception);
          }
          return null;
        }
      }
      else
      {
        try
        {
          return Array.get(value, indexObj.intValue());
        }
        catch (final Exception exception)
        {
          if (logger.isLoggingError())
          {
            logger.logError("An exception occurred while trying to access index [" + indexObj + "] of an Array",
                            exception);
          }
          return null;
        }
      }
    }
    else if ((indexStr = coercions.coerceToString(indexVal)) == null)
    {
      return null;
    }
    else
    {
      final Class<?> clazz = value.getClass();
      BeanInfo beanInfo;
      try
      {
        beanInfo = Introspector.getBeanInfo(clazz);
      }
      catch (final IntrospectionException exception)
      {
        logger.logError(exception);
        return null;
      }

      for (final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors())
      {
        if (indexStr.equals(propertyDescriptor.getName()))
        {
          final Method getMethod = propertyDescriptor.getReadMethod();
          try
          {
            return getMethod.invoke(value);
          }
          catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception)
          {
            logger.logError(exception);
            return null;
          }
        }
      }

      if (logger.isLoggingError())
      {
        logger.logError("Unable to find a value for [" + indexVal + "] in object of class ["
                          + value.getClass().getName() + "] using operator [" + getOperatorSymbol() + "]");
      }
      return null;
    }
  }
}
