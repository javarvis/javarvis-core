package com.itineric.javarvis.core.automation;

import com.itineric.javarvis.core.stt.SpeechToTextResult;

public interface AutomationTextProcessor
{
  void init(AutomationTextProcessorInitContext context)
    throws Exception;

  void destroy();

  boolean isParallel();

  boolean process(SpeechToTextResult speechToTextResult)
    throws Exception;
}
