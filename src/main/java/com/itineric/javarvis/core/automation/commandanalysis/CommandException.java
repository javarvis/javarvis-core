package com.itineric.javarvis.core.automation.commandanalysis;

public class CommandException extends Exception
{
  private static final long serialVersionUID = 1L;

  public CommandException(final String message)
  {
    super(message);
  }

  public CommandException(final Throwable rootCause)
  {
    super(rootCause);
  }

  public CommandException(final String message, final Throwable rootCause)
  {
    super(message, rootCause);
  }
}
