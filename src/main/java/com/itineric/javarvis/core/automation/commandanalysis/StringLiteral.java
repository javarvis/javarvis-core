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

public class StringLiteral extends Literal
{
  StringLiteral(final Object value)
  {
    super(value);
  }

  public static StringLiteral fromToken(final String pToken)
  {
    return new StringLiteral(getValueFromToken(pToken));
  }

  public static StringLiteral fromLiteralValue(final String value)
  {
    return new StringLiteral(value);
  }

  public static String getValueFromToken(final String pToken)
  {
    final StringBuffer buf = new StringBuffer();
    final int len = pToken.length() - 1;
    boolean escaping = false;
    for (int i = 1 ; i < len ; i++)
    {
      final char ch = pToken.charAt(i);
      if (escaping)
      {
        buf.append(ch);
        escaping = false;
      }
      else if (ch == '\\')
      {
        escaping = true;
      }
      else
      {
        buf.append(ch);
      }
    }
    return buf.toString();
  }

  public static String toStringToken(final String value)
  {
    if (value.indexOf('\"') < 0 &&
        value.indexOf('\\') < 0)
    {
      return "\"" + value + "\"";
    }
    else
    {
      final StringBuffer buf = new StringBuffer();
      buf.append('\"');
      final int len = value.length();
      for (int i = 0 ; i < len ; i++)
      {
        final char ch = value.charAt(i);
        if (ch == '\\')
        {
          buf.append('\\');
          buf.append('\\');
        }
        else if (ch == '\"')
        {
          buf.append('\\');
          buf.append('\"');
        }
        else
        {
          buf.append(ch);
        }
      }
      buf.append('\"');
      return buf.toString();
    }
  }

  public static String toIdentifierToken(final String value)
  {
    if (isJavaIdentifier(value))
    {
      return value;
    }

    else
    {
      return toStringToken(value);
    }
  }

  static boolean isJavaIdentifier(final String value)
  {
    final int len = value.length();
    if (len == 0)
    {
      return false;
    }
    else
    {
      if (!Character.isJavaIdentifierStart(value.charAt(0)))
      {
        return false;
      }
      else
      {
        for (int i = 1 ; i < len ; i++)
        {
          if (!Character.isJavaIdentifierPart(value.charAt(i)))
          {
            return false;
          }
        }
        return true;
      }
    }
  }

  @Override
  public String getExpressionString()
  {
    return toStringToken((String) getValue());
  }
}
