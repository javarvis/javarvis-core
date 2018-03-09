package com.itineric.javarvis.core.hotword;

import javax.sound.sampled.AudioFormat;

import org.apache.commons.configuration2.Configuration;

public interface HotwordDetector
{
  void init(AudioFormat audioFormat,
            Configuration configuration);

  void destroy();

  boolean detect(byte[] buffer);
}
