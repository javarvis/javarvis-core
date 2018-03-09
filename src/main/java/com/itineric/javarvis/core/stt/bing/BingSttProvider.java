package com.itineric.javarvis.core.stt.bing;

import static com.itineric.javarvis.core.bing.Constants.HEADER_SUBSCRIPTION_KEY;
import static com.itineric.javarvis.core.stt.bing.ConfigurationConstants.KEY__HTTP_CHUNKED;
import static com.itineric.javarvis.core.stt.bing.ConfigurationConstants.KEY__HTTP_LANGUAGE;
import static com.itineric.javarvis.core.stt.bing.ConfigurationConstants.KEY__SUBSCRIPTION_KEY;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
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

public class BingSttProvider implements SpeechToTextProvider
{
  private static final String JSON_RESULT_RECOGNITION_STATUS_KEY = "RecognitionStatus";
  private static final String JSON_RESULT_RECOGNITION_STATUS_SUCCESS_VALUE = "Success";
  private static final String JSON_RESULT_EXTRACTED_VALUE = "NBest";
  private static final String JSON_RESULT_EXTRACTED_SUB_VALUE = "Lexical";

  private static final Logger _logger = LogManager.getLogger(BingSttProvider.class);

  private AudioFormat _capturedAudioFormat;
  private AudioFormat _bingWaveAudioFormat;
  private CloseableHttpClient _httpClient;
  private String _subscriptionKey;
  private boolean _chunked;
  private String _language;

  @Override
  public void init(final AudioFormat audioFormat,
                   final Configuration configuration)
  {
    _capturedAudioFormat = audioFormat;
    _httpClient = HttpClients.createDefault();

    _chunked = configuration.getBoolean(KEY__HTTP_CHUNKED);
    _subscriptionKey = configuration.getString(KEY__SUBSCRIPTION_KEY);
    _language = configuration.getString(KEY__HTTP_LANGUAGE);
    _bingWaveAudioFormat = new AudioFormat(16000,
                                           16,
                                           1,
                                           true,
                                           false);

    if (_logger.isDebugEnabled())
    {
      _logger.debug("Bing speech to text provider ready");
    }
  }

  @Override
  public void destroy()
  {
  }

  @Override
  public SpeechToTextResult speechToText(final InputStream inputStream) throws Exception
  {
    final ByteArrayOutputStream rawAudio = new ByteArrayOutputStream();
    int readBytes;
    final byte[] data = new byte[4096];
    while ((readBytes = inputStream.read(data, 0, data.length)) != -1)
    {
      rawAudio.write(data, 0, readBytes);
    }
    final int frameLength = rawAudio.size() / _capturedAudioFormat.getFrameSize();

    final ByteArrayOutputStream wavAudio = new ByteArrayOutputStream();
    final AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(rawAudio.toByteArray()),
                                                                   _bingWaveAudioFormat,
                                                                   frameLength);
    try
    {
      AudioSystem.write(audioInputStream,
                        AudioFileFormat.Type.WAVE,
                        wavAudio);
    }
    finally
    {
      audioInputStream.close();
    }

    final JsonObject result = callBingRecognitionService(wavAudio.toByteArray());
    if (_logger.isDebugEnabled())
    {
      _logger.debug("Get result: " + result);
    }

    if (result == null)
    {
      return new DefaultSpeechToTextResult(false, null);
    }

    final String recognitionStatus = result.getString(JSON_RESULT_RECOGNITION_STATUS_KEY, null);
    if (JSON_RESULT_RECOGNITION_STATUS_SUCCESS_VALUE.equals(recognitionStatus))
    {
      final JsonValue values = result.get(JSON_RESULT_EXTRACTED_VALUE);
      final JsonArray array = values.asArray();
      final JsonValue firstValue = array.get(0);
      final JsonObject firstObject = firstValue.asObject();
      final String text = firstObject.getString(JSON_RESULT_EXTRACTED_SUB_VALUE, null);
      return new DefaultSpeechToTextResult(true, text);
    }
    else
    {
      return null;
    }
  }

  private JsonObject callBingRecognitionService(final byte[] wavAudio)
  {
    try
    {
      final HttpPost httpPost = createBingSpeechRecognitionPost();
      final InputStreamEntity requestEntity = new InputStreamEntity(new ByteArrayInputStream(wavAudio),
                                                                    -1);
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
        final int statusCode = statusLine.getStatusCode();
        if (statusCode == HttpStatus.SC_OK)
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

          return result;
        }
        else
        {
          if (_logger.isWarnEnabled())
          {
            _logger.warn("Called failed (error code ["
                           + statusCode + "], reason: [" + statusLine.getReasonPhrase() + "]): "
                           + EntityUtils.toString(responseEntity));
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
        _logger.error("An error occurred while calling Bing online service", exception);
      }
    }

    return null;
  }

  private HttpPost createBingSpeechRecognitionPost()
  {
    final String uri =
      "https://speech.platform.bing.com/speech/recognition/interactive/cognitiveservices/v1?format=detailed&language="
        + _language;
    final HttpPost httpPost = new HttpPost(uri);
    httpPost.addHeader(HEADER_SUBSCRIPTION_KEY, _subscriptionKey);
    httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "audio/wav; codec=audio/pcm; samplerate=16000");
    return httpPost;
  }
}
