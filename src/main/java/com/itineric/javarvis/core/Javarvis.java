package com.itineric.javarvis.core;

import static com.itineric.javarvis.core.ConfigurationConstants.KEY__AUTOMATION_PROVIDER_CLASS_NAMES;
import static com.itineric.javarvis.core.ConfigurationConstants.KEY__AUTOMATION_TEXT_PROCESSOR_CLASS_NAMES;
import static com.itineric.javarvis.core.ConfigurationConstants.KEY__HOTWORD_DETECTOR_CLASS_NAME;
import static com.itineric.javarvis.core.ConfigurationConstants.KEY__STT_PROVIDER_CLASS_NAME;
import static com.itineric.javarvis.core.ConfigurationConstants.KEY__TTS_PROVIDER_CLASS_NAME;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import com.itineric.javarvis.core.audio.in.AudioLoop;
import com.itineric.javarvis.core.automation.AutomationTextProcessor;
import com.itineric.javarvis.core.automation.AutomationTextProcessorInitContext;
import com.itineric.javarvis.core.automation.DefaultAutomationTextProcessorInitContext;
import com.itineric.javarvis.core.automation.provider.AutomationProvider;
import com.itineric.javarvis.core.automation.provider.AutomationProviderInitContext;
import com.itineric.javarvis.core.automation.provider.DefaultAutomationProviderInitContext;
import com.itineric.javarvis.core.hotword.HotwordDetector;
import com.itineric.javarvis.core.stt.SpeechToTextProvider;
import com.itineric.javarvis.core.tts.TextToSpeechProvider;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Javarvis
{
  public static final String DEFAULT_CONFIG_FILE_NAME = "javarvis.properties";

  private static final Logger _logger = LogManager.getLogger(Javarvis.class);

  public static void main(final String[] args) throws Exception
  {
    final String configurationFilePath;
    if (args.length == 0)
    {
      configurationFilePath = DEFAULT_CONFIG_FILE_NAME;
    }
    else if (args.length == 1)
    {
      configurationFilePath = args[0];
    }
    else
    {
      System.out.println("Expecting zero or one parameter, "
                           + "if a parameter is given it must be the path of the configuration file");
      System.exit(-1);
      return;
    }

    if (_logger.isDebugEnabled())
    {
      _logger.debug("Loading configuration from [" + configurationFilePath + "]");
    }

    final Configurations configs = new Configurations();
    final Configuration config = configs.properties(new File(configurationFilePath));

    if (_logger.isDebugEnabled())
    {
      _logger.debug("Configuration loaded");
    }

    final String hotwordDetectorClassName = config.getString(KEY__HOTWORD_DETECTOR_CLASS_NAME);
    final String sttProviderClassName = config.getString(KEY__STT_PROVIDER_CLASS_NAME);
    final String ttsProviderClassName = config.getString(KEY__TTS_PROVIDER_CLASS_NAME);
    final String[] automationProviderClassNames =
      config.getStringArray(KEY__AUTOMATION_PROVIDER_CLASS_NAMES);
    final String[] automationTextProcessorsClassNames =
      config.getStringArray(KEY__AUTOMATION_TEXT_PROCESSOR_CLASS_NAMES);

    final Map<Class<?>, Object> providers = new HashMap<Class<?>, Object>();
    final HotwordDetector hotwordDetector = createInstance(HotwordDetector.class,
                                                           hotwordDetectorClassName,
                                                           providers);
    final SpeechToTextProvider sttProvider = createInstance(SpeechToTextProvider.class,
                                                            sttProviderClassName,
                                                            providers);
    final TextToSpeechProvider ttsProvider = createInstance(TextToSpeechProvider.class,
                                                            ttsProviderClassName,
                                                            providers);
    final List<AutomationProvider> automationProviders = createInstances(automationProviderClassNames);
    final List<AutomationTextProcessor> automationTextProcessors = createInstances(automationTextProcessorsClassNames);

    if (_logger.isDebugEnabled())
    {
      _logger.debug("Dynamic providers and processors instantiations done");
    }

    final AudioFormat audioFormat = new AudioFormat(16000,
                                                    16,
                                                    1,
                                                    true,
                                                    false);

    hotwordDetector.init(audioFormat, config);
    sttProvider.init(audioFormat, config);
    ttsProvider.init(config);

    final AutomationProviderInitContext automationProviderInitContext =
      new DefaultAutomationProviderInitContext(config,
                                               providers);
    for (final AutomationProvider automationProvider : automationProviders)
    {
      automationProvider.init(automationProviderInitContext);
    }

    if (_logger.isDebugEnabled())
    {
      _logger.debug("Providers and processors initialized");
    }

    final AutomationTextProcessorInitContext automationTextProcessorInitContext =
      new DefaultAutomationTextProcessorInitContext(config,
                                                    automationProviders);
    for (final AutomationTextProcessor automationTextProcessor : automationTextProcessors)
    {
      automationTextProcessor.init(automationTextProcessorInitContext);
    }

    final DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
    final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
    line.open(audioFormat);
    line.start();

    if (_logger.isDebugEnabled())
    {
      _logger.debug("Audio input line created");
    }

    final AudioLoop audioLoop = new AudioLoop(config,
                                              line,
                                              hotwordDetector,
                                              sttProvider,
                                              automationTextProcessors);
    audioLoop.start();

    _logger.info("Ready !");

    audioLoop.join();

    for (final AutomationTextProcessor automationTextProcessor : automationTextProcessors)
    {
      automationTextProcessor.destroy();
    }
    for (final AutomationProvider automationProvider : automationProviders)
    {
      automationProvider.destroy();
    }
    ttsProvider.destroy();
    sttProvider.destroy();
    hotwordDetector.destroy();
  }

  private static <T> T createInstance(final Class<T> interfaceClass,
                                      final String className,
                                      final Map<Class<?>, Object> providers)
    throws Exception
  {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    final Class<T> clazz = (Class)Class.forName(className, true, Thread.currentThread().getContextClassLoader());
    final T result = (T)clazz.newInstance();

    if (providers != null)
    {
      providers.put(interfaceClass, result);
    }

    return result;
  }

  private static <T> List<T> createInstances(final String[] classNames) throws Exception
  {
    final List<T> instances = new ArrayList<T>();
    for (final String className : classNames)
    {
      final T instance = createInstance(null, className, null);
      instances.add(instance);
    }
    return instances;
  }
}
