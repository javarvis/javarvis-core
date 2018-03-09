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
import java.math.BigInteger;

/**
 *
 * <p>This class contains the logic for coercing data types before
 * operators are applied to them.
 *
 * <p>The following is the list of rules applied for various type
 * conversions.
 *
 * <ul><pre>
 * Applying arithmetic operator
 *   Binary operator - A {+,-,*} B
 *     if A and B are null
 *       return 0
 *     if A or B is BigDecimal, coerce both to BigDecimal and then:
 *       if operator is +, return <code>A.add(B)</code>
 *       if operator is -, return <code>A.subtract(B)</code>
 *       if operator is *, return <code>A.multiply(B)</code>
 *     if A or B is Float, Double, or String containing ".", "e", or "E"
 *       if A or B is BigInteger, coerce both A and B to BigDecimal and apply operator
 *       coerce both A and B to Double and apply operator
 *     if A or B is BigInteger, coerce both to BigInteger and then:
 *       if operator is +, return <code>A.add(B)</code>
 *       if operator is -, return <code>A.subtract(B)</code>
 *       if operator is *, return <code>A.multiply(B)</code>
 *     otherwise
 *       coerce both A and B to Long
 *       apply operator
 *     if operator results in exception (such as divide by 0), error
 *
 *   Binary operator - A {/,div} B
 *     if A and B are null
 *       return 0
 *     if A or B is a BigDecimal or BigInteger, coerce both to BigDecimal and
 *      return <code>A.divide(B, BigDecimal.ROUND_HALF_UP)</code>
 *     otherwise
 *       coerce both A and B to Double
 *       apply operator
 *     if operator results in exception (such as divide by 0), error
 *
 *   Binary operator - A {%,mod} B
 *     if A and B are null
 *       return 0
 *     if A or B is BigDecimal, Float, Double, or String containing ".", "e" or "E"
 *       coerce both to Double
 *       apply operator
 *     if A or B is BigInteger, coerce both to BigInteger and return
 *      <code>A.remainder(B)</code>
 *     otherwise
 *       coerce both A and B to Long
 *       apply operator
 *     if operator results in exception (such as divide by 0), error
 *
 *   Unary minus operator - -A
 *     if A is null
 *       return 0
 *     if A is BigInteger or BigDecimal, return <code>A.negate()</code>
 *     if A is String
 *       if A contains ".", "e", or "E"
 *         coerce to Double, apply operator
 *       otherwise
 *         coerce to a Long and apply operator
 *     if A is Byte,Short,Integer,Long,Float,Double
 *       retain type, apply operator
 *     if operator results in exception, error
 *     otherwise
 *       error
 *
 * Applying "empty" operator - empty A
 *   if A is null
 *     return true
 *   if A is zero-length String
 *     return true
 *   if A is zero-length array
 *     return true
 *   if A is List and ((List) A).isEmpty()
 *     return true
 *   if A is Map and ((Map) A).isEmpty()
 *     return true
 *   if A is Collection an ((Collection) A).isEmpty()
 *     return true
 *   otherwise
 *     return false
 *
 * Applying logical operators
 *   Binary operator - A {and,or} B
 *     coerce both A and B to Boolean, apply operator
 *   NOTE - operator stops as soon as expression can be determined, i.e.,
 *     A and B and C and D - if B is false, then only A and B is evaluated
 *   Unary not operator - not A
 *     coerce A to Boolean, apply operator
 *
 * Applying relational operator
 *   A {<,>,<=,>=,lt,gt,lte,gte} B
 *     if A==B
 *       if operator is >= or <=
 *         return true
 *       otherwise
 *         return false
 *     if A or B is null
 *       return false
 *     if A or B is BigDecimal, coerce both A and B to BigDecimal and use the
 *      return value of <code>A.compareTo(B)</code>
 *     if A or B is Float or Double
 *       coerce both A and B to Double
 *       apply operator
 *     if A or B is BigInteger, coerce both A and B to BigInteger and use the
 *      return value of <code>A.compareTo(B)</code>
 *     if A or B is Byte,Short,Character,Integer,Long
 *       coerce both A and B to Long
 *       apply operator
 *     if A or B is String
 *       coerce both A and B to String, compare lexically
 *     if A is Comparable
 *       if A.compareTo (B) throws exception
 *         error
 *       otherwise
 *         use result of A.compareTo(B)
 *     if B is Comparable
 *       if B.compareTo (A) throws exception
 *         error
 *       otherwise
 *         use result of B.compareTo(A)
 *     otherwise
 *       error
 *
 * Applying equality operator
 *   A {==,!=} B
 *     if A==B
 *       apply operator
 *     if A or B is null
 *       return false for ==, true for !=
 *     if A or B is BigDecimal, coerce both A and B to BigDecimal and then:
 *       if operator is == or eq, return <code>A.equals(B)</code>
 *       if operator is != or ne, return <code>!A.equals(B)</code>
 *     if A or B is Float or Double
 *       coerce both A and B to Double
 *       apply operator
 *     if A or B is BigInteger, coerce both A and B to BigInteger and then:
 *       if operator is == or eq, return <code>A.equals(B)</code>
 *       if operator is != or ne, return <code>!A.equals(B)</code>
 *     if A or B is Byte,Short,Character,Integer,Long
 *       coerce both A and B to Long
 *       apply operator
 *     if A or B is Boolean
 *       coerce both A and B to Boolean
 *       apply operator
 *     if A or B is String
 *       coerce both A and B to String, compare lexically
 *     otherwise
 *       if an error occurs while calling A.equals(B)
 *         error
 *       apply operator to result of A.equals(B)
 *
 * coercions
 *
 *   coerce A to String
 *     A is String
 *       return A
 *     A is null
 *       return ""
 *     A.toString throws exception
 *       error
 *     otherwise
 *       return A.toString
 *
 *   coerce A to Number type N
 *     A is null or ""
 *       return 0
 *     A is Character
 *       convert to short, apply following rules
 *     A is Boolean
 *       error
 *     A is Number type N
 *       return A
 *     A is Number, coerce quietly to type N using the following algorithm
 *         If N is BigInteger
 *             If A is BigDecimal, return <code>A.toBigInteger()</code>
 *             Otherwise, return <code>BigInteger.valueOf(A.longValue())</code>
 *        if N is BigDecimal
 *             If A is a BigInteger, return <code>new BigDecimal(A)</code>
 *             Otherwise, return <code>new BigDecimal(A.doubleValue())</code>
 *        If N is Byte, return <code>new Byte(A.byteValue())</code>
 *        If N is Short, return <code>new Short(A.shortValue())</code>
 *        If N is Integer, return <code>new Integer(A.integerValue())</code>
 *        If N is Long, return <code>new Long(A.longValue())</code>
 *        If N is Float, return <code>new Float(A.floatValue())</code>
 *        If N is Double, return <code>new Double(A.doubleValue())</code>
 *        otherwise ERROR
 *     A is String
 *       If N is BigDecimal then:
 *            If <code>new BigDecimal(A)</code> throws an exception then ERROR
 *            Otherwise, return <code>new BigDecimal(A)</code>
 *       If N is BigInteger then:
 *            If <code>new BigInteger(A)</code> throws an exception, then ERROR
 *            Otherwise, return <code>new BigInteger(A)</code>
 *       new <code>N.valueOf(A)</code> throws exception
 *         error
 *       return <code>N.valueOf(A)</code>
 *     otherwise
 *       error
 *
 *   coerce A to Character should be
 *     A is null or ""
 *       return (char) 0
 *     A is Character
 *       return A
 *     A is Boolean
 *       error
 *     A is Number with less precision than short
 *       coerce quietly - return (char) A
 *     A is Number with greater precision than short
 *       coerce quietly - return (char) A
 *     A is String
 *       return A.charAt (0)
 *     otherwise
 *       error
 *
 *   coerce A to Boolean
 *     A is null or ""
 *       return false
 *     A is Boolean
 *       return A
 *     A is String
 *       Boolean.valueOf(A) throws exception
 *         error
 *       return Boolean.valueOf(A)
 *     otherwise
 *       error
 *
 *   coerce A to any other type T
 *     A is null
 *       return null
 *     A is assignable to T
 *       coerce quietly
 *     A is String
 *       T has no PropertyEditor
 *         if A is "", return null
 *         otherwise error
 *       T's PropertyEditor throws exception
 *         if A is "", return null
 *         otherwise error
 *       otherwise
 *         apply T's PropertyEditor
 *     otherwise
 *       error
 * </pre></ul>
 *
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: luehe $
 **/

public class Coercions
{
  private static final Number ZERO = new Integer(0);

  private final CommandLogger _logger;

  public Coercions(final Context context)
  {
    _logger = context.getLogger();
  }

  // -------------------------------------
  /**
   * Coerces the specified value to a String
   **/
  public String coerceToString(final Object value)
    throws CommandException
  {
    if (value == null)
    {
      return "";
    }
    else if (value instanceof String)
    {
      return (String) value;
    }
    else
    {
      try
      {
        return value.toString();
      }
      catch (final Exception exception)
      {
        if (_logger.isLoggingError())
        {
          _logger.logError("An object of type [" + value.getClass().getName()
                             + "] threw an exception in its toString() method while trying to be coerced to a String",
                           exception);
        }
        return "";
      }
    }
  }

  // -------------------------------------
  /**
   * Coerces a value to the given primitive number class
   **/
  public Number coerceToPrimitiveNumber(final Object value,
                                        final Class<?> clazz)
    throws CommandException
  {
    if (value == null ||
        "".equals(value))
    {
      return coerceToPrimitiveNumber(ZERO, clazz);
    }
    else if (value instanceof Character)
    {
      final char val = ((Character) value).charValue();
      return coerceToPrimitiveNumber(new Short((short) val), clazz);
    }
    else if (value instanceof Boolean)
    {
      if (_logger.isLoggingError())
      {
        _logger.logError("Attempt to coerce a boolean value [" + value + "] to type [" + clazz.getName() + "]");
      }
      return coerceToPrimitiveNumber(ZERO, clazz);
    }
    else if (value.getClass() == clazz)
    {
      return (Number) value;
    }
    else if (value instanceof Number)
    {
      return coerceToPrimitiveNumber((Number) value, clazz);
    }
    else if (value instanceof String)
    {
      try
      {
        return coerceToPrimitiveNumber((String) value, clazz);
      }
      catch (final Exception exc)
      {
        if (_logger.isLoggingError())
        {
          _logger.logError("An exception occured trying to convert String [" + value + "] to type ["
                             + clazz.getName() + "]");
        }
        return coerceToPrimitiveNumber(ZERO, clazz);
      }
    }
    else
    {
      if (_logger.isLoggingError())
      {
        _logger.logError("Attempt to coerce a value of type [" + value.getClass().getName() + "] to type ["
                           + clazz.getName() + "]");
      }
      return coerceToPrimitiveNumber(0, clazz);
    }
  }

  // -------------------------------------
  /**
   * Coerces a value to an Integer, returning null if the coercion isn't possible.
   **/
  public Integer coerceToInteger(final Object value)
    throws CommandException
  {
    if (value == null)
    {
      return null;
    }
    else if (value instanceof Character)
    {
      return (int) (((Character) value).charValue());
    }
    else if (value instanceof Boolean)
    {
      if (_logger.isLoggingWarning())
      {
        _logger.logWarning("Attempt to coerce a boolean value [" + value + "] to type ["
                             + Integer.class.getName() + "]");
      }
      return ((Boolean) value).booleanValue() ? 1 : 0;
    }
    else if (value instanceof Integer)
    {
      return (Integer) value;
    }
    else if (value instanceof Number)
    {
      return ((Number) value).intValue();
    }
    else if (value instanceof String)
    {
      try
      {
        return Integer.valueOf((String) value);
      }
      catch (final Exception exc)
      {
        if (_logger.isLoggingWarning())
        {
          _logger.logWarning("An exception occured trying to convert String [" + value + "] to type ["
                               + Integer.class.getName() + "]");
        }
        return null;
      }
    }
    else
    {
      if (_logger.isLoggingWarning())
      {
        _logger.logWarning("Attempt to coerce a value of type [" + value.getClass().getName() + "] to type ["
                             + Integer.class.getName() + "]");
      }
      return null;
    }
  }

  // -------------------------------------
  /**
   * Coerces a value to a Boolean
   **/
  public Boolean coerceToBoolean(final Object value)
    throws CommandException
  {
    if (value == null
      || "".equals(value))
    {
      return Boolean.FALSE;
    }
    else if (value instanceof Boolean)
    {
      return (Boolean) value;
    }
    else if (value instanceof String)
    {
      final String str = (String) value;
      try
      {
        return Boolean.valueOf(str);
      }
      catch (final Exception exception)
      {
        if (_logger.isLoggingError())
        {
          _logger.logError("An exception occurred trying to convert String [" + value + "] to type Boolean",
                           exception);
        }
        return Boolean.FALSE;
      }
    }
    else
    {
      if (_logger.isLoggingError())
      {
        _logger.logError("Attempt to coerce a value of type [" + value.getClass().getName() + "] to Boolean");
      }
      return Boolean.TRUE;
    }
  }

  // -------------------------------------
  // Applying operators
  // -------------------------------------
  /**
   * Performs all of the necessary type conversions, then calls on the appropriate operator.
   **/
  public Object applyArithmeticOperator(final Object leftAsObject,
                                        final Object rightAsObject,
                                        final ArithmeticOperator operator)
    throws CommandException
  {
    if (leftAsObject == null &&
        rightAsObject == null)
    {
      if (_logger.isLoggingWarning())
      {
        _logger.logWarning("Attempt to apply operator [" + operator.getOperatorSymbol() + "] to null value");
      }
      return 0;
    }

    else if (isBigDecimal(leftAsObject) || isBigDecimal(rightAsObject))
    {
      final BigDecimal left = (BigDecimal) coerceToPrimitiveNumber(leftAsObject, BigDecimal.class);
      final BigDecimal right = (BigDecimal) coerceToPrimitiveNumber(rightAsObject, BigDecimal.class);
      return operator.apply(left, right);
    }

    else if (isFloatingPointType(leftAsObject)
      || isFloatingPointType(rightAsObject)
      || isFloatingPointString(leftAsObject)
      || isFloatingPointString(rightAsObject))
    {
      if (isBigInteger(leftAsObject) || isBigInteger(rightAsObject))
      {
        final BigDecimal left = (BigDecimal) coerceToPrimitiveNumber(leftAsObject, BigDecimal.class);
        final BigDecimal right = (BigDecimal) coerceToPrimitiveNumber(rightAsObject, BigDecimal.class);
        return operator.apply(left, right);
      }
      else
      {
        final double left = coerceToPrimitiveNumber(leftAsObject, Double.class).doubleValue();
        final double right = coerceToPrimitiveNumber(rightAsObject, Double.class).doubleValue();
        return operator.apply(left, right);
      }
    }

    else if (isBigInteger(leftAsObject) || isBigInteger(rightAsObject))
    {
      final BigInteger left = (BigInteger) coerceToPrimitiveNumber(leftAsObject, BigInteger.class);
      final BigInteger right = (BigInteger) coerceToPrimitiveNumber(rightAsObject, BigInteger.class);
      return operator.apply(left, right);
    }

    else
    {
      final long left = coerceToPrimitiveNumber(leftAsObject, Long.class).longValue();
      final long right = coerceToPrimitiveNumber(rightAsObject, Long.class).longValue();
      return operator.apply(left, right);
    }
  }

  // -------------------------------------
  /**
   * Performs all of the necessary type conversions, then calls on the appropriate operator.
   **/
  public Object applyRelationalOperator(final Object leftAsObject,
                                        final Object rightAsObject,
                                        final RelationalOperator operator)
    throws CommandException
  {
    if (isBigDecimal(leftAsObject) || isBigDecimal(rightAsObject))
    {
      final BigDecimal left = (BigDecimal) coerceToPrimitiveNumber(leftAsObject, BigDecimal.class);
      final BigDecimal right = (BigDecimal) coerceToPrimitiveNumber(rightAsObject, BigDecimal.class);
      return operator.apply(left, right);
    }

    else if (isFloatingPointType(leftAsObject)
      || isFloatingPointType(rightAsObject))
    {
      final double left = coerceToPrimitiveNumber(leftAsObject, Double.class).doubleValue();
      final double right = coerceToPrimitiveNumber(rightAsObject, Double.class).doubleValue();
      return operator.apply(left, right);
    }

    else if (isBigInteger(leftAsObject) || isBigInteger(rightAsObject))
    {
      final BigInteger left = (BigInteger) coerceToPrimitiveNumber(leftAsObject, BigInteger.class);
      final BigInteger right = (BigInteger) coerceToPrimitiveNumber(rightAsObject, BigInteger.class);
      return operator.apply(left, right);
    }

    else if (isIntegerType(leftAsObject)
      || isIntegerType(rightAsObject))
    {
      final long left = coerceToPrimitiveNumber(leftAsObject, Long.class).longValue();
      final long right = coerceToPrimitiveNumber(rightAsObject, Long.class).longValue();
      return operator.apply(left, right);
    }

    else if (leftAsObject instanceof String
      || rightAsObject instanceof String)
    {
      final String left = coerceToString(leftAsObject);
      final String right = coerceToString(rightAsObject);
      return operator.apply(left, right);
    }

    else if (leftAsObject instanceof Comparable)
    {
      try
      {
        @SuppressWarnings("unchecked")
        final Comparable<Object> comparable = ((Comparable<Object>) leftAsObject);
        final int result = comparable.compareTo(rightAsObject);
        return operator.apply(result, -result);
      }
      catch (final Exception exception)
      {
        if (_logger.isLoggingError())
        {
          _logger.logError("An exception occurred while trying to compare a value of Comparable type ["
                             + leftAsObject.getClass().getName() + "] with a value of type ["
                             + (rightAsObject == null ? "null" : rightAsObject.getClass().getName())
                             + "] for operator [" + operator.getOperatorSymbol() + "]",
                           exception);
        }
        return Boolean.FALSE;
      }
    }

    else if (rightAsObject instanceof Comparable)
    {
      try
      {
        @SuppressWarnings("unchecked")
        final Comparable<Object> comparable = ((Comparable<Object>) rightAsObject);
        final int result = comparable.compareTo(leftAsObject);
        return operator.apply(-result, result);
      }
      catch (final Exception exception)
      {
        if (_logger.isLoggingError())
        {
          _logger.logError("An exception occurred while trying to compare a value of Comparable type ["
                             + rightAsObject.getClass().getName() + "] with a value of type ["
                             + (leftAsObject == null ? "null" : leftAsObject.getClass().getName())
                             + "] for operator [" + operator.getOperatorSymbol() + "]",
                           exception);
        }
        return Boolean.FALSE;
      }
    }

    else
    {
      if (_logger.isLoggingError())
      {
        _logger.logError("Attempt to apply operator [" + operator.getOperatorSymbol() + "] to arguments of type ["
                           + leftAsObject.getClass().getName() + "] and [" + rightAsObject.getClass().getName() + "]");
      }
      return Boolean.FALSE;
    }
  }

  // -------------------------------------
  /**
   * Performs all of the necessary type conversions, then calls on the appropriate operator.
   **/
  public Object applyEqualityOperator(final Object leftAsObject,
                                      final Object rightAsObject,
                                      final EqualityOperator operator)
    throws CommandException
  {
    if (leftAsObject == rightAsObject)
    {
      return operator.apply(true);
    }

    else if (leftAsObject == null
      || rightAsObject == null)
    {
      return operator.apply(false);
    }

    else if (isBigDecimal(leftAsObject) || isBigDecimal(rightAsObject))
    {
      final BigDecimal left = (BigDecimal) coerceToPrimitiveNumber(leftAsObject, BigDecimal.class);
      final BigDecimal right = (BigDecimal) coerceToPrimitiveNumber(rightAsObject, BigDecimal.class);
      return operator.apply(left.equals(right));
    }

    else if (isFloatingPointType(leftAsObject)
      || isFloatingPointType(rightAsObject))
    {
      final double left = coerceToPrimitiveNumber(leftAsObject, Double.class).doubleValue();
      final double right = coerceToPrimitiveNumber(rightAsObject, Double.class).doubleValue();
      return operator.apply(left == right);
    }

    else if (isBigInteger(leftAsObject) || isBigInteger(rightAsObject))
    {
      final BigInteger left = (BigInteger) coerceToPrimitiveNumber(leftAsObject, BigInteger.class);
      final BigInteger right = (BigInteger) coerceToPrimitiveNumber(rightAsObject, BigInteger.class);
      return operator.apply(left.equals(right));
    }

    else if (isIntegerType(leftAsObject)
      || isIntegerType(rightAsObject))
    {
      final long left = coerceToPrimitiveNumber(leftAsObject, Long.class).longValue();
      final long right = coerceToPrimitiveNumber(rightAsObject, Long.class).longValue();
      return operator.apply(left == right);
    }

    else if (leftAsObject instanceof Boolean
      || rightAsObject instanceof Boolean)
    {
      final boolean left = coerceToBoolean(leftAsObject).booleanValue();
      final boolean right = coerceToBoolean(rightAsObject).booleanValue();
      return operator.apply(left == right);
    }

    else if (leftAsObject instanceof String
      || rightAsObject instanceof String)
    {
      final String left = coerceToString(leftAsObject);
      final String right = coerceToString(rightAsObject);
      return operator.apply(left.equals(right));
    }

    else
    {
      try
      {
        return operator.apply(leftAsObject.equals(rightAsObject));
      }
      catch (final Exception exception)
      {
        if (_logger.isLoggingError())
        {
          _logger.logError("An error occurred calling equals() on an object of type ["
                             + leftAsObject.getClass().getName() + "] when comparing with an object of type ["
                             + rightAsObject.getClass().getName() + "] for operator ["
                             + operator.getOperatorSymbol() + "]",
                           exception);
        }
        return Boolean.FALSE;
      }
    }
  }

  // -------------------------------------
  /**
   * Returns true if the given Object is of a floating point type
   **/
  public boolean isFloatingPointType(final Object object)
  {
    return object != null &&
           isFloatingPointType(object.getClass());
  }

  // -------------------------------------
  /**
   * Returns true if the given class is of a floating point type
   **/
  public boolean isFloatingPointType(final Class<?> clazz)
  {
    return clazz == Float.class ||
           clazz == Float.TYPE ||
           clazz == Double.class ||
           clazz == Double.TYPE;
  }

  // -------------------------------------
  /**
   * Returns true if the given string might contain a floating point number - i.e., it contains ".", "e", or "E"
   **/
  public boolean isFloatingPointString(final Object object)
  {
    if (object instanceof String)
    {
      final String str = (String) object;
      final int len = str.length();
      for (int i = 0 ; i < len ; i++)
      {
        final char ch = str.charAt(i);
        if (ch == '.' ||
            ch == 'e' ||
            ch == 'E')
        {
          return true;
        }
      }
      return false;
    }
    else
    {
      return false;
    }
  }

  // -------------------------------------
  /**
   * Returns true if the given Object is of an integer type
   **/
  public boolean isIntegerType(final Object object)
  {
    return object != null
      && isIntegerType(object.getClass());
  }

  // -------------------------------------

  /**
   * Returns true if the given object is BigInteger.
   * @param object - Object to evaluate
   * @return - true if the given object is BigInteger
   */
  public boolean isBigInteger(final Object object)
  {
    return object != null && object instanceof BigInteger;
  }

  /**
   * Returns true if the given object is BigDecimal.
   * @param object - Object to evaluate
   * @return - true if the given object is BigDecimal
   */
  public boolean isBigDecimal(final Object object)
  {
    return object != null && object instanceof BigDecimal;
  }
}
