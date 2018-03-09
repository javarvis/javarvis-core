package com.itineric.javarvis.core.stt;

import java.io.InputStream;

import javax.sound.sampled.AudioFormat;

import org.apache.commons.configuration2.Configuration;

public interface SpeechToTextProvider
{
  void init(AudioFormat audioFormat,
            Configuration configuration);

  void destroy();

  SpeechToTextResult speechToText(InputStream inputStream) throws Exception;
}
