package com.itineric.javarvis.core.hotword.snowboy;

import static com.itineric.javarvis.core.hotword.snowboy.ConfigurationConstants.KEY__SNOWBOY_COMMON_RESOURCE;
import static com.itineric.javarvis.core.hotword.snowboy.ConfigurationConstants.KEY__SNOWBOY_DETECTOR_AUDIO_GAIN;
import static com.itineric.javarvis.core.hotword.snowboy.ConfigurationConstants.KEY__SNOWBOY_DETECTOR_SENSITIVITY;
import static com.itineric.javarvis.core.hotword.snowboy.ConfigurationConstants.KEY__SNOWBOY_HOTWORD_RESOURCE;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;

import ai.kitt.snowboy.SnowboyDetect;
import com.itineric.javarvis.core.audio.in.AudioHelper;
import com.itineric.javarvis.core.hotword.HotwordDetector;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SnowboyDetector implements HotwordDetector
{
  static
  {
    System.loadLibrary("snowboy-detect-java");
  }

  private static final Logger _logger = LogManager.getLogger(SnowboyDetector.class);

  private short[] _snowboyData;
  private SnowboyDetect _detector;

  @Override
  public void init(final AudioFormat audioFormat,
                   final Configuration configuration)
  {
    final String snowboyCommonResource = configuration.getString(KEY__SNOWBOY_COMMON_RESOURCE);
    final String snowboyHotwordResource = configuration.getString(KEY__SNOWBOY_HOTWORD_RESOURCE);
    final String sensitivity = configuration.getString(KEY__SNOWBOY_DETECTOR_SENSITIVITY);
    final float audioGain = configuration.getFloat(KEY__SNOWBOY_DETECTOR_AUDIO_GAIN);

    final int bufferSize = AudioHelper.getZeroDotOneSecondBufferSize(audioFormat);
    _snowboyData = new short[bufferSize / 2];
    _detector = new SnowboyDetect(snowboyCommonResource, snowboyHotwordResource);
    _detector.SetSensitivity(sensitivity);
    _detector.SetAudioGain(audioGain);

    if (_logger.isDebugEnabled())
    {
      _logger.debug("Snowboy detector ready");
    }
  }

  @Override
  public void destroy()
  {
  }

  @Override
  public boolean detect(final byte[] buffer)
  {
    ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(_snowboyData);
    final int result = _detector.RunDetection(_snowboyData, _snowboyData.length);
    if (result > 0)
    {
      if (_logger.isDebugEnabled())
      {
        _logger.debug("Hotword detected!");
      }
      return true;
    }
    return false;
  }
}
