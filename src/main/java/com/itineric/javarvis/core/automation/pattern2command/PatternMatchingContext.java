package com.itineric.javarvis.core.automation.pattern2command;

import java.util.List;

public interface PatternMatchingContext
{
  String getText();

  List<String> getParameters();

  void setParameters(List<String> parameters);
}
