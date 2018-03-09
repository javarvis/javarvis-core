package com.itineric.javarvis.core.stt.wit;

import static com.itineric.javarvis.core.stt.wit.ConfigurationConstants.KEY__AUTHORIZATION_TOKEN;
import static com.itineric.javarvis.core.stt.wit.ConfigurationConstants.KEY__HTTP_CHUNKED;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.itineric.javarvis.core.stt.DefaultSpeechToTextResult;
import com.itineric.javarvis.core.stt.SpeechToTextProvider;
import com.itineric.javarvis.core.stt.SpeechToTextResult;
import org.apache.commons.configuration2.Configuration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WitSttProvider implements SpeechToTextProvider
{
  private static final String JSON_RESULT_EXTRACTED_VALUE = "_text";

  private static final Logger _logger = LogManager.getLogger(WitSttProvider.class);

  private CloseableHttpClient _httpClient;
  private String _authorizationHeader;
  private String _contentTypeHeader;
  private boolean _chunked;

  @Override
  public void init(final AudioFormat audioFormat,
                   final Configuration configuration)
  {
    _httpClient = HttpClients.createDefault();

    _chunked = configuration.getBoolean(KEY__HTTP_CHUNKED);
    final String token = configuration.getString(KEY__AUTHORIZATION_TOKEN);
    _authorizationHeader = "Bearer " + token;
    final Encoding encoding = audioFormat.getEncoding();
    _contentTypeHeader = "audio/raw;"
                           + "encoding=" + (encoding == Encoding.PCM_UNSIGNED ? "un" : "") + "signed-integer;"
                           + "bits=" + audioFormat.getSampleSizeInBits() + ";"
                           + "rate=" + (int)audioFormat.getSampleRate() + ";"
                           + "endian=" + (audioFormat.isBigEndian() ? "big" : "little");

    if (_logger.isDebugEnabled())
    {
      _logger.debug("Wit speech to text provider ready");
    }
  }

  @Override
  public void destroy()
  {
  }

  @Override
  public SpeechToTextResult speechToText(final InputStream inputStream)
  {
    String text = null;
    try
    {
      final HttpPost httpPost = createWitSpeechRecognitionPost();
      final InputStreamEntity requestEntity = new InputStreamEntity(inputStream, -1);
      requestEntity.setChunked(_chunked);
      httpPost.setEntity(requestEntity);

      if (_logger.isDebugEnabled())
      {
        _logger.debug("Executing request: " + httpPost.getRequestLine());
      }

      final CloseableHttpResponse response = _httpClient.execute(httpPost);

      if (_logger.isTraceEnabled())
      {
        _logger.trace("Response arrived");
      }

      try
      {
        final HttpEntity responseEntity = response.getEntity();
        final StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() == HttpStatus.SC_OK)
        {
          final JsonObject result;
          final InputStreamReader reader = new InputStreamReader(responseEntity.getContent());
          try
          {
            result = Json.parse(reader).asObject();
          }
          finally
          {
            reader.close();
          }

          if (_logger.isDebugEnabled())
          {
            _logger.debug("Get result: " + result);
          }
          text = result.getString(JSON_RESULT_EXTRACTED_VALUE, null);
        }
        else
        {
          if (_logger.isWarnEnabled())
          {
            _logger.warn("Called failed: " + EntityUtils.toString(responseEntity));
          }
        }
      }
      finally
      {
        response.close();
      }
    }
    catch (final IOException exception)
    {
      if (_logger.isErrorEnabled())
      {
        _logger.error("An error occurred while calling Wit online service", exception);
      }
    }

    if (text == null)
    {
      return new DefaultSpeechToTextResult(false, null);
    }
    else
    {
      return new DefaultSpeechToTextResult(true, text);
    }
  }

  private HttpPost createWitSpeechRecognitionPost()
  {
    final HttpPost httpPost = new HttpPost("https://api.wit.ai/speech?v=20170307");
    httpPost.addHeader(HttpHeaders.AUTHORIZATION, _authorizationHeader);
    httpPost.addHeader(HttpHeaders.CONTENT_TYPE, _contentTypeHeader);
    return httpPost;
  }
}
