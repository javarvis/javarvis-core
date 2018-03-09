package com.itineric.javarvis.core.automation.provider;

import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import com.itineric.javarvis.core.tts.TextToSpeechProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextToSpeechAutomationProvider implements AutomationProvider
{
  private static final String TTS_KEY = "tts";

  private static final Logger _logger = LogManager.getLogger();

  private Map<Class<?>, ?> _providers;
  private Clip _clip;

  @Override
  public void init(final AutomationProviderInitContext context)
    throws Exception
  {
    _providers = context.getProviders();

    final Line.Info info = new Line.Info(Clip.class);
    final Line line = AudioSystem.getLine(info);
    _clip = (Clip) line;

    _clip.addLineListener(new LineListener()
    {
      @Override
      public void update(final LineEvent event)
      {
        if (event.getType() == LineEvent.Type.START)
        {
          synchronized (_clip)
          {
            _clip.notify();
          }
        }
      }
    });

    if (_logger.isDebugEnabled())
    {
      _logger.debug("Text to speech automation provider ready");
    }
  }

  @Override
  public void destroy()
  {
  }

  @Override
  public String getName()
  {
    return TTS_KEY;
  }

  public void say(final String message)
    throws Exception
  {
    final TextToSpeechProvider textToSpeechProvider =
      (TextToSpeechProvider)_providers.get(TextToSpeechProvider.class);
    if (textToSpeechProvider == null)
    {
      if (_logger.isErrorEnabled())
      {
        _logger.error("Could not find text to speech provider");
      }
      return;
    }

    final AudioInputStream audioInputStream = textToSpeechProvider.textToSpeech(message);
    if (audioInputStream == null)
    {
      return;
    }

    try
    {
      _clip.open(audioInputStream);

      try
      {
        synchronized (_clip)
        {
          _clip.start();
          _clip.wait();
        }

        _clip.drain();
        _clip.stop();
      }
      finally
      {
        _clip.close();
      }
    }
    finally
    {
      audioInputStream.close();
    }
  }
}
