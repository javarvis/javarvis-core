package com.itineric.javarvis.core.audio.in;

import static com.itineric.javarvis.core.audio.in.ConfigurationConstants.KEY__AUDIO_IN_VOICE_COMMAND_SILENCE_IGNORE_TIME;
import static com.itineric.javarvis.core.audio.in.ConfigurationConstants.KEY__AUDIO_IN_VOICE_COMMAND_SILENCE_THRESHOLD;
import static com.itineric.javarvis.core.audio.in.ConfigurationConstants.KEY__AUDIO_IN_VOICE_COMMAND_TIME_TO_SPEAK;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;

import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadedLineReader extends Thread
{
  private static final Logger _logger = LogManager.getLogger(ThreadedLineReader.class);

  private static Double _silenceThreshold;
  private static long _silenceIgnoreTime;
  private static long _timeToSpeak;

  private final byte[] _readBuffer;
  private final TargetDataLine _line;
  private final SelfGrowingByteBuffer _byteBuffer;

  public ThreadedLineReader(final Configuration configuration,
                            final TargetDataLine line,
                            final SelfGrowingByteBuffer byteBuffer)
  {
    if (_silenceThreshold == null)
    {
      _silenceThreshold = configuration.getDouble(KEY__AUDIO_IN_VOICE_COMMAND_SILENCE_THRESHOLD);
      _silenceIgnoreTime = configuration.getLong(KEY__AUDIO_IN_VOICE_COMMAND_SILENCE_IGNORE_TIME);
      _timeToSpeak = configuration.getLong(KEY__AUDIO_IN_VOICE_COMMAND_TIME_TO_SPEAK);
    }

    _line = line;
    _byteBuffer = byteBuffer;

    final AudioFormat audioFormat = line.getFormat();
    final int bufferSize = AudioHelper.getZeroDotOneSecondBufferSize(audioFormat);
    _readBuffer = new byte[bufferSize];
  }

  @Override
  public void run()
  {
    long lastSound = 0;
    boolean somethingWasSaid = false;
    boolean silence = false;
    boolean nothingWasSaid = false;
    final long startToListenTime = System.currentTimeMillis();
    while (!silence && !nothingWasSaid)
    {
      final int readBytes = _line.read(_readBuffer, 0, _readBuffer.length);

      final double rms = AudioHelper.rms(_readBuffer);
      if (_logger.isTraceEnabled())
      {
        _logger.trace("RMS: " + rms);
      }
      if (somethingWasSaid && rms < _silenceThreshold)
      {
        final long now = System.currentTimeMillis();
        if (lastSound > 0 && now - lastSound > _silenceIgnoreTime)
        {
          if (_logger.isDebugEnabled())
          {
            _logger.debug("Silence detected");
          }
          silence = true;
        }
      }

      if (!silence)
      {
        if (rms >= _silenceThreshold)
        {
          somethingWasSaid = true;
          lastSound = System.currentTimeMillis();
        }

        synchronized (_byteBuffer)
        {
          _byteBuffer.add(_readBuffer, 0, readBytes);
        }
      }
      else if (startToListenTime + _timeToSpeak < System.currentTimeMillis())
      {
        nothingWasSaid = true;
      }
    }
  }
}
