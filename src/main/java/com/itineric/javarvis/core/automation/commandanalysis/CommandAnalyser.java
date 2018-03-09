package com.itineric.javarvis.core.automation.commandanalysis;

import java.io.StringReader;
import java.util.List;

import com.itineric.javarvis.core.automation.commandanalysis.parser.CommandParser;
import com.itineric.javarvis.core.automation.pattern2command.Command;

public class CommandAnalyser
{
  public List<Command> parseCommands(final String commandsAsString) throws Exception
  {
    final CommandParser commandParser = new CommandParser(new StringReader(commandsAsString));
    final List<Command> commands = commandParser.commands();
    return commands;
  }

}
