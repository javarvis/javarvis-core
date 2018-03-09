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

import java.text.MessageFormat;

import org.apache.logging.log4j.Logger;

/**
 *
 * <p>The evaluator may pass an instance of this class to operators
 * and expressions during evaluation.  They should use this to log any
 * warning or error messages that might come up.  This allows all of
 * our logging policies to be concentrated in one class.
 *
 * <p>Errors are conditions that are severe enough to abort operation.
 * Warnings are conditions through which the operation may continue,
 * but which should be reported to the developer.
 *
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: luehe $
 **/

public class CommandLogger
{
  private final Logger _logger;

  public CommandLogger(final Logger logger)
  {
    _logger = logger;
  }

  public boolean isLoggingWarning()
  {
    return false;
  }

  public void logWarning(final String message,
                         final Throwable rootCause)
    throws CommandException
  {
    if (isLoggingWarning())
    {
      if (message == null)
      {
        if (_logger.isWarnEnabled())
        {
          _logger.warn(rootCause);
        }
      }
      else if (rootCause == null)
      {
        if (_logger.isWarnEnabled())
        {
          _logger.warn(message);
        }
      }
      else
      {
        if (_logger.isWarnEnabled())
        {
          _logger.warn(message, rootCause);
        }
      }
    }
  }

  public void logWarning(final String template)
    throws CommandException
  {
    if (isLoggingWarning())
    {
      logWarning(template);
    }
  }

  public void logWarning(final Throwable rootCause)
    throws CommandException
  {
    if (isLoggingWarning())
    {
      logWarning(null, rootCause);
    }
  }

  public void logWarning(final String template,
                         final Object... args)
    throws CommandException
  {
    if (isLoggingWarning())
    {
      logWarning(MessageFormat.format(template,
                                      args));
    }
  }

  public void logWarning(final String template,
                         final Throwable rootCause,
                         final Object pArg0)
    throws CommandException
  {
    if (isLoggingWarning())
    {
      logWarning(MessageFormat.format(template,
                                      new Object[] {
                                                     "" + pArg0,
                                      }),
                 rootCause);
    }
  }

  public boolean isLoggingError()
  {
    return true;
  }

  public void logError(final String message,
                       final Throwable rootCause)
    throws CommandException
  {
    if (isLoggingError())
    {
      if (message == null)
      {
        throw new CommandException(rootCause);
      }
      else if (rootCause == null)
      {
        throw new CommandException(message);
      }
      else
      {
        throw new CommandException(message, rootCause);
      }
    }
  }

  public void logError(final String template)
    throws CommandException
  {
    if (isLoggingError())
    {
      logError(template);
    }
  }

  public void logError(final Throwable rootCause)
    throws CommandException
  {
    if (isLoggingError())
    {
      logError(null, rootCause);
    }
  }

  public void logError(final String template,
                       final Object... args)
    throws CommandException
  {
    if (isLoggingError())
    {
      logError(MessageFormat.format(template,
                                    args ));
    }
  }

  public void logError(final String template,
                       final Throwable rootCause,
                       final Object... args)
    throws CommandException
  {
    if (isLoggingError())
    {
      logError(MessageFormat.format(template,
                                    args),
               rootCause);
    }
  }
}
