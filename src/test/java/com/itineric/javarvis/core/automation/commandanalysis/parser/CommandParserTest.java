package com.itineric.javarvis.core.automation.commandanalysis.parser;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itineric.javarvis.core.automation.pattern2command.Command;
import com.itineric.javarvis.core.automation.provider.AutomationProvider;
import org.junit.Test;

public class CommandParserTest
{
  @Test
  public void parseCommand1() throws Exception
  {
    final CommandParser commandParser =
      createCommandParser("knx:write(\"light\", true, ^1)");
    commandParser.commands();
  }

  @Test
  public void parseCommand2() throws Exception
  {
    final CommandParser commandParser =
      createCommandParser("knx:write('light', ^1, true) ? message = 'C\\'est fait' : message = 'Je n\\'y arrive pas'");
    final List<Command> commands = commandParser.commands();
    final AutomationProvider knxAutomationProviderMock = new KnxAutomationProviderMock();
    final TestContext context = createDefaultContext(Arrays.asList(knxAutomationProviderMock),
                                                     Arrays.asList("test"));
    assertEquals(1, commands.size());
    final Command command = commands.get(0);
    final Object result = command.evaluate(context);
    assertEquals("C'est fait", result);
    assertEquals("C'est fait", context.getValue("message"));
  }

  @Test
  public void parseCommand3() throws Exception
  {
    final CommandParser commandParser =
      createCommandParser("bean.string");
    final List<Command> commands = commandParser.commands();
    assertEquals(1, commands.size());
    final Command command = commands.get(0);
    final Object result = command.evaluate(createDefaultContext(null, null));
    assertEquals("stringValue", result);
  }

  private CommandParser createCommandParser(final String command)
  {
    final CommandParser commandParser = new CommandParser(new StringReader(command));
    return commandParser;
  }

  private TestContext createDefaultContext(final List<AutomationProvider> automationProviders,
                                           final List<String> parameters)
    throws Exception
  {
    final Map<String, Object> defaultVariables = new HashMap<String, Object>();
    defaultVariables.put("bean", new Bean());
    return new TestContext(defaultVariables,
                           automationProviders == null ? new ArrayList<AutomationProvider>() : automationProviders,
                           parameters);
  }
}
