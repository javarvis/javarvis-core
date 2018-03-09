package com.itineric.javarvis.core.tts;

import javax.sound.sampled.AudioInputStream;

import org.apache.commons.configuration2.Configuration;

public interface TextToSpeechProvider
{
  void init(Configuration configuration) throws Exception;

  void destroy();

  AudioInputStream textToSpeech(String text)
    throws Exception;
}
