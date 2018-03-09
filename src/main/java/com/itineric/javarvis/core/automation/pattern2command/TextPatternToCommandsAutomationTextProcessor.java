package com.itineric.javarvis.core.automation.pattern2command;

import static com.itineric.javarvis.core.automation.pattern2command.ConfigurationConstants.KEY__TEXT_CONFIGURATION_FILE_PATHS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.itineric.javarvis.core.automation.AutomationTextProcessor;
import com.itineric.javarvis.core.automation.AutomationTextProcessorInitContext;
import com.itineric.javarvis.core.automation.commandanalysis.CommandAnalyser;
import com.itineric.javarvis.core.automation.commandanalysis.Context;
import com.itineric.javarvis.core.automation.patternanalysis.TextAnalyser;
import com.itineric.javarvis.core.automation.provider.AutomationProvider;
import com.itineric.javarvis.core.stt.SpeechToTextResult;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextPatternToCommandsAutomationTextProcessor
  implements AutomationTextProcessor
{
  private static class Line
  {
    private final String _value;
    private final int _number;

    public Line(final String value, final int number)
    {
      _value = value;
      _number = number;
    }

    public String getValue()
    {
      return _value;
    }

    public int getNumber()
    {
      return _number;
    }
  }

  private static final Logger _logger = LogManager.getLogger(TextPatternToCommandsAutomationTextProcessor.class);

  private final List<ConfigurationLine> _configurationLines = new ArrayList<ConfigurationLine>();
  private List<AutomationProvider> _automationProviders;

  @Override
  public void init(final AutomationTextProcessorInitContext context)
    throws Exception
  {
    final Configuration configuration = context.getConfiguration();
    _automationProviders = context.getAutomationProviders();

    final TextAnalyser textAnalyser = new TextAnalyser();
    final CommandAnalyser commandAnalyser = new CommandAnalyser();

    final String[] filePaths = configuration.getStringArray(KEY__TEXT_CONFIGURATION_FILE_PATHS);
    for (final String filePath : filePaths)
    {
      final File file = new File(filePath);
      final List<Line> lines = new ArrayList<Line>();
      boolean addToPreviousLine = false;

      final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
      try
      {
        int lineNumber = 0;
        String lineValue;
        while ((lineValue = bufferedReader.readLine()) != null)
        {
          lineNumber++;
          if (addToPreviousLine)
          {
            final Line previousLine = lines.remove(lines.size() - 1);
            lineValue = previousLine.getValue() + lineValue;
          }
          addToPreviousLine = lineValue.endsWith("\\");
          if (addToPreviousLine)
          {
            lineValue = lineValue.substring(0, lineValue.length() - 1);
          }
          lines.add(new Line(lineValue, lineNumber));
        }
      }
      finally
      {
        bufferedReader.close();
      }

      for (final Line line : lines)
      {
        final String lineValue = line.getValue();
        final int lineNumber = line.getNumber();
        final String trimmedLine = lineValue.trim();
        if ("".equals(trimmedLine)
          || trimmedLine.startsWith("#"))
        {
          continue;
        }

        final String[] tab = trimmedLine.split("->");
        if (tab.length != 2)
        {
          if (_logger.isErrorEnabled())
          {
            _logger.error("A configuration error was found in configuration file [" + filePath
                            + "] at line [" + lineNumber + "], cannot find two parts or expression as: "
                            + "[pattern -> commands]");
          }
          continue;
        }

        final String patternAsString = tab[0].trim().toLowerCase();
        final String commandAsString = tab[1].trim();

        final Pattern pattern;
        final List<Command> commands;
        try
        {
          pattern = textAnalyser.parsePattern(patternAsString);
        }
        catch (final Throwable throwable)
        {
          if (_logger.isErrorEnabled())
          {
            _logger.error("Pattern [" + patternAsString + "] parsing failed at line ["
                            + lineNumber + "] of file [" + filePath + "]",
                          throwable);
          }
          continue;
        }

        try
        {
          commands = commandAnalyser.parseCommands(commandAsString);
        }
        catch (final Throwable throwable)
        {
          if (_logger.isErrorEnabled())
          {
            _logger.error("Command [" + commandAsString + "] parsing failed at line ["
                            + lineNumber + "] of file [" + filePath + "]",
                          throwable);
          }
          continue;
        }

        final ConfigurationLine configurationLine = new ConfigurationLine(pattern, commands);
        _configurationLines.add(configurationLine);
      }
    }

    if (_logger.isDebugEnabled())
    {
      _logger.debug("Pattern to command text processor ready");
    }
  }

  @Override
  public void destroy()
  {
  }

  @Override
  public boolean isParallel()
  {
    return false;
  }

  @Override
  public boolean process(final SpeechToTextResult speechToTextResult)
    throws Exception
  {
    final String text = speechToTextResult.getText();

    PatternMatchingContext matchingContext = null;
    List<Command> commands = null;
    for (final ConfigurationLine configurationLine : _configurationLines)
    {
      final PatternMatchingContext context = new DefaultPatternMatchingContext(text);

      final Pattern pattern = configurationLine.getPattern();
      final boolean matches = pattern.matches(context);
      if (matches)
      {
        if (_logger.isDebugEnabled())
        {
          _logger.debug("Pattern [" + pattern.getRawRepresentation() + "] matches");
        }

        matchingContext = context;
        commands = configurationLine.getCommands();
      }
    }

    if (commands == null)
    {
      return false;
    }

    final Context context = new Context(null,
                                        _automationProviders,
                                        matchingContext.getParameters());
    for (final Command command : commands)
    {
      command.evaluate(context);
    }

    return true;
  }
}
