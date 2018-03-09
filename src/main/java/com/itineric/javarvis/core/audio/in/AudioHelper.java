package com.itineric.javarvis.core.audio.in;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;

public class AudioHelper
{
  private static final float SHORT_MAX_VALUE = 32768f;

  public static double rms(final byte[] data)
  {
    final short[] shorts = new short[data.length / 2];
    ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
    double rms = 0;
    for (int i = 0 ; i < shorts.length ; i++)
    {
      final double normal = shorts[i] / SHORT_MAX_VALUE;
      rms += normal * normal;
    }
    return Math.sqrt(rms / shorts.length);
  }

  public static int getZeroDotOneSecondBufferSize(final AudioFormat audioFormat)
  {
    return (int)audioFormat.getSampleRate() * (audioFormat.getSampleSizeInBits() / 8) / 10;
  }
}
