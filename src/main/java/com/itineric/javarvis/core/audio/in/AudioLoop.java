package com.itineric.javarvis.core.audio.in;

import static com.itineric.javarvis.core.audio.in.ConfigurationConstants.KEY__AUDIO_IN_VOICE_COMMAND_STOP_KEYWORD;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;

import com.itineric.javarvis.core.automation.AutomationTextProcessor;
import com.itineric.javarvis.core.hotword.HotwordDetector;
import com.itineric.javarvis.core.stt.SpeechToTextProvider;
import com.itineric.javarvis.core.stt.SpeechToTextResult;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AudioLoop extends Thread
{
  private static final int NUMBER_OF_THREADS_IN_PARALLEL_EXECUTOR = 2;
  private static final int NUMBER_OF_BUFFERED_FRAMES_FOR_STT = 3;

  private static final Logger _logger = LogManager.getLogger(AudioLoop.class);

  private final ExecutorService _executorService;
  private final List<byte[]> _previousBuffers = new ArrayList<byte[]>();
  private final byte[] _buffer;
  private final Configuration _configuration;
  private final TargetDataLine _line;
  private final HotwordDetector _hotwordDetector;
  private final SpeechToTextProvider _sttProvider;
  private final List<AutomationTextProcessor> _parallelAutomationTextProcessors =
    new ArrayList<AutomationTextProcessor>();
  private final List<AutomationTextProcessor> _sequentialAutomationTextProcessors =
    new ArrayList<AutomationTextProcessor>();
  private final String _stopKeyword;

  public AudioLoop(final Configuration configuration,
                   final TargetDataLine line,
                   final HotwordDetector hotwordDetector,
                   final SpeechToTextProvider sttProvider,
                   final List<AutomationTextProcessor> automationTextProcessors)
  {
    final AudioFormat audioFormat = line.getFormat();
    final int bufferSize = AudioHelper.getZeroDotOneSecondBufferSize(audioFormat);
    _buffer = new byte[bufferSize];
    _configuration = configuration;
    _line = line;
    _hotwordDetector = hotwordDetector;
    _sttProvider = sttProvider;

    for (final AutomationTextProcessor automationTextProcessor : automationTextProcessors)
    {
      if (automationTextProcessor.isParallel())
      {
        _parallelAutomationTextProcessors.add(automationTextProcessor);
      }
      else
      {
        _sequentialAutomationTextProcessors.add(automationTextProcessor);
      }
    }

    _stopKeyword = configuration.getString(KEY__AUDIO_IN_VOICE_COMMAND_STOP_KEYWORD, null);

    _executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS_IN_PARALLEL_EXECUTOR);
  }

  @Override
  public void run()
  {
    while (true)
    {
      final int readBytes = _line.read(_buffer, 0, _buffer.length);

      final byte[] actualBuffer;
      if (readBytes == _buffer.length)
      {
        actualBuffer = _buffer;
      }
      else
      {
        actualBuffer = ByteBuffer.wrap(_buffer, 0, readBytes).array();
      }

      final boolean hotwordDetected = _hotwordDetector.detect(actualBuffer);
      if (!hotwordDetected)
      {
        continue;
      }

      if (_previousBuffers.size() >= NUMBER_OF_BUFFERED_FRAMES_FOR_STT)
      {
        _previousBuffers.remove(0);
      }
      _previousBuffers.add(actualBuffer);

      final InputStream inputStream = new InterruptibleAudioInputStream(_configuration, _line, _previousBuffers);
      final SpeechToTextResult speechToTextResult;
      try
      {
        speechToTextResult = _sttProvider.speechToText(inputStream);
      }
      catch (final Exception exception)
      {
        if (_logger.isErrorEnabled())
        {
          _logger.error("Speech to text failed", exception);
        }
        continue;
      }

      if (speechToTextResult == null || !speechToTextResult.isSuccess())
      {
        if (_logger.isWarnEnabled())
        {
          _logger.warn("STT provider failed");
        }
        continue;
      }

      final String text = speechToTextResult.getText();
      if (_stopKeyword.equals(text))
      {
        break;
      }

      for (final AutomationTextProcessor automationTextProcessor : _parallelAutomationTextProcessors)
      {
        _executorService.submit(new Callable<Object>()
        {
          @Override
          public Object call() throws Exception
          {
            callAutomationTextProcessor(automationTextProcessor,
                                        speechToTextResult);
            return null;
          }
        });
      }

      for (final AutomationTextProcessor automationTextProcessor : _sequentialAutomationTextProcessors)
      {
        final boolean processed = callAutomationTextProcessor(automationTextProcessor,
                                                              speechToTextResult);
        if (processed)
        {
          break;
        }
      }
    }

    _executorService.shutdown();
  }

  private boolean callAutomationTextProcessor(final AutomationTextProcessor automationTextProcessor,
                                              final SpeechToTextResult speechToTextResult)
  {
    try
    {
      return automationTextProcessor.process(speechToTextResult);
    }
    catch (final Throwable throwable)
    {
      if (_logger.isErrorEnabled())
      {
        _logger.error(throwable.getMessage(), throwable);
      }
    }
    return false;
  }
}
