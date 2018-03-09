package com.itineric.javarvis.core.audio.in;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.sound.sampled.TargetDataLine;

import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InterruptibleAudioInputStream extends InputStream
{
  private static final Logger _logger = LogManager.getLogger(InterruptibleAudioInputStream.class);

  private final SelfGrowingByteBuffer _byteBuffer = new SelfGrowingByteBuffer();
  private int _currentIndex = 0;
  private final ThreadedLineReader _threadedLineReader;

  public InterruptibleAudioInputStream(final Configuration configuration,
                                       final TargetDataLine line,
                                       final List<byte[]> initialData)
  {
    for (final byte[] b : initialData)
    {
      _byteBuffer.add(b);
    }
    _threadedLineReader = new ThreadedLineReader(configuration, line, _byteBuffer);
    _threadedLineReader.start();
  }

  @Override
  public int read() throws IOException
  {
    synchronized (_byteBuffer)
    {
      if (!_threadedLineReader.isAlive() && _currentIndex >= _byteBuffer.length())
      {
        return -1;
      }
      return _byteBuffer.get(_currentIndex++);
    }
  }

  @Override
  public int read(final byte[] b) throws IOException
  {
    return read(b, 0, b.length);
  }

  @Override
  public int read(final byte[] b, final int off, final int len) throws IOException
  {
    synchronized (_byteBuffer)
    {
      final int byteBufferLength = _byteBuffer.length();
      if (!_threadedLineReader.isAlive() && _currentIndex >= byteBufferLength)
      {
        return -1;
      }
      final int readBytes =
        _currentIndex + len <= byteBufferLength ? len : byteBufferLength - _currentIndex;
      _byteBuffer.fill(_currentIndex, b, off, readBytes);
      _currentIndex += readBytes;
      return readBytes;
    }
  }

  @Override
  public void close() throws IOException
  {
    if (_logger.isTraceEnabled())
    {
      _logger.trace("Stream closed");
    }
    super.close();
  }
}
