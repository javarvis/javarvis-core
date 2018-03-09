package com.itineric.javarvis.core.audio.in;

public class SelfGrowingByteBuffer
{
  private byte[] _buffer = new byte[0];

  public void add(final byte[] b)
  {
    add(b, 0, b.length);
  }

  public void add(final byte[] b, final int offset, final int length)
  {
    final byte[] tmp = _buffer;
    _buffer = new byte[_buffer.length + length];
    System.arraycopy(tmp, 0, _buffer, 0, tmp.length);
    System.arraycopy(b, offset, _buffer, tmp.length, length);
  }

  public int length()
  {
    return _buffer.length;
  }

  public int get(final int i)
  {
    return _buffer[i];
  }

  public void fill(final int fromIndex, final byte[] to, final int toIndex, final int length)
  {
    System.arraycopy(_buffer, fromIndex, to, toIndex, length);
  }

  public byte[] getBytes()
  {
    return _buffer;
  }
}
