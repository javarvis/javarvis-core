package com.itineric.javarvis.core.util;

import java.text.Normalizer;

public abstract class TextHelper
{
  private TextHelper()
  {
  }

  public static String normalizeText(final String text)
  {
    String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD);
    normalizedText = normalizedText.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    normalizedText = normalizedText.replaceAll("'", " ");
    normalizedText = normalizedText.replaceAll("-", " ");
    return normalizedText;
  }
}
