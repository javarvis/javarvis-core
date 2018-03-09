package com.itineric.javarvis.core.stt;

public class DefaultSpeechToTextResult implements SpeechToTextResult
{
  private final boolean _success;
  private final String _text;

  public DefaultSpeechToTextResult(final boolean success,
                                   final String text)
  {
    _success = success;
    _text = text;
  }

  @Override
  public boolean isSuccess()
  {
    return _success;
  }

  @Override
  public String getText()
  {
    return _text;
  }
}
