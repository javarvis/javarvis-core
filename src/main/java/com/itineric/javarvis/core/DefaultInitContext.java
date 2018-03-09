package com.itineric.javarvis.core;

import org.apache.commons.configuration2.Configuration;

public class DefaultInitContext implements InitContext
{
  private final Configuration _configuration;

  public DefaultInitContext(final Configuration configuration)
  {
    _configuration = configuration;
  }

  @Override
  public Configuration getConfiguration()
  {
    return _configuration;
  }

}
