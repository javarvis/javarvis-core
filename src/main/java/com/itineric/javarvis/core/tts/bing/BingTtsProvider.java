package com.itineric.javarvis.core.tts.bing;

import static com.itineric.javarvis.core.bing.Constants.HEADER_SUBSCRIPTION_KEY;
import static com.itineric.javarvis.core.tts.bing.ConfigurationConstants.KEY__SUBSCRIPTION_KEY;
import static com.itineric.javarvis.core.tts.bing.ConfigurationConstants.KEY__LANGUAGE;
import static com.itineric.javarvis.core.tts.bing.ConfigurationConstants.KEY__VOICE_NAME;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.UUID;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.itineric.javarvis.core.tts.TextToSpeechProvider;
import org.apache.commons.configuration2.Configuration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class BingTtsProvider implements TextToSpeechProvider
{
  private static final String OUTPUT_FORMAT_HEADER_KEY = "X-Microsoft-OutputFormat";
  private static final String OUTPUT_FORMAT_HEADER_VALUE = "raw-16khz-16bit-mono-pcm";
  private static final String APP_ID_HEADER_KEY = "X-Search-AppId";
  private static final String APP_ID_HEADER_VALUE = UUID.randomUUID().toString().replace("-", "");
  private static final String CLIENT_ID_HEADER_KEY = "X-Search-ClientID";
  private static final String CLIENT_ID_HEADER_VALUE = UUID.randomUUID().toString().replace("-", "");
  private static final String USER_AGENT_HEADER_VALUE = "Javarvis";
  private static final String CONTENT_TYPE_HEADER_VALUE = "application/ssml+xml";

  private static final String SSML_SPEAK_NODE = "speak";
  private static final String SSML_SPEAK_VERSION_ATTRIBUTE = "version";
  private static final String SSML_VOICE_NODE = "voice";
  private static final String SSML_VOICE_NAME_ATTRIBUTE = "name";

  private static final Logger _logger = LogManager.getLogger(BingTtsProvider.class);

  private String _subscriptionKey;
  private String _language;
  private String _voiceName;
  private DocumentBuilder _documentBuilder;
  private Transformer _transformer;
  private CloseableHttpClient _httpClient;
  private AudioFormat _audioFormat;

  @Override
  public void init(final Configuration configuration) throws Exception
  {
    _subscriptionKey = configuration.getString(KEY__SUBSCRIPTION_KEY);
    _language = configuration.getString(KEY__LANGUAGE);
    _voiceName = configuration.getString(KEY__VOICE_NAME);

    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    _documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    _transformer = transformerFactory.newTransformer();
    _transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    _httpClient = HttpClients.createDefault();

    _audioFormat = new AudioFormat(16000,
                                   16,
                                   1,
                                   true,
                                   false);

    if (_logger.isDebugEnabled())
    {
      _logger.debug("Bing text to speech provider started");
    }
  }

  @Override
  public void destroy()
  {
  }

  @Override
  public AudioInputStream textToSpeech(final String text) throws Exception
  {
    final String authenticationToken = getAuthenticationToken();
    if (authenticationToken == null)
    {
      if (_logger.isErrorEnabled())
      {
        _logger.error("Authentication token was null");
      }
      return null;
    }

    final String ssml = textToSsml(text);

    if (_logger.isTraceEnabled())
    {
      _logger.trace("SSML: " + ssml);
    }

    final HttpPost httpPost = createTtsPost(authenticationToken);
    final HttpEntity httpEntity = new StringEntity(ssml);
    httpPost.setEntity(httpEntity);
    final CloseableHttpResponse response = _httpClient.execute(httpPost);
    try
    {
      final HttpEntity responseEntity = response.getEntity();
      final StatusLine statusLine = response.getStatusLine();
      final int statusCode = statusLine.getStatusCode();
      if (statusCode == HttpStatus.SC_OK)
      {
        final byte[] bytes = EntityUtils.toByteArray(responseEntity);
        final long frameLength = bytes.length / _audioFormat.getFrameSize();
        return new AudioInputStream(new ByteArrayInputStream(bytes),
                                    _audioFormat,
                                    frameLength);
      }
      else
      {
        if (_logger.isErrorEnabled())
        {
          _logger.warn("Called failed (error code ["
                         + statusCode + "], reason: [" + statusLine.getReasonPhrase() + "]): "
                         + EntityUtils.toString(responseEntity));
        }
        return null;
      }
    }
    finally
    {
      response.close();
    }
  }

  private String getAuthenticationToken() throws Exception
  {
    final String uri = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";
    final HttpPost httpPost = new HttpPost(uri);
    httpPost.addHeader(HEADER_SUBSCRIPTION_KEY, _subscriptionKey);
    final HttpEntity requestEntity = new StringEntity("");
    httpPost.setEntity(requestEntity);
    final CloseableHttpResponse response = _httpClient.execute(httpPost);
    try
    {
      final HttpEntity responseEntity = response.getEntity();
      final StatusLine statusLine = response.getStatusLine();
      final int statusCode = statusLine.getStatusCode();
      if (statusCode == HttpStatus.SC_OK)
      {
        return EntityUtils.toString(response.getEntity());
      }
      else
      {
        if (_logger.isErrorEnabled())
        {
          _logger.error("Authentication on bing failed (error code [" + statusCode
                          + "], reason: [" + statusLine.getReasonPhrase() + "]): "
                          + EntityUtils.toString(responseEntity));
        }
        return null;
      }
    }
    finally
    {
      response.close();
    }
  }

  private HttpPost createTtsPost(final String jwt)
    throws Exception
  {
    final String authorizationHeader = "Bearer " + jwt;

    final String uri = "https://speech.platform.bing.com/synthesize";
    final HttpPost httpPost = new HttpPost(uri);
    httpPost.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
    httpPost.addHeader(OUTPUT_FORMAT_HEADER_KEY, OUTPUT_FORMAT_HEADER_VALUE);
    httpPost.addHeader(APP_ID_HEADER_KEY, APP_ID_HEADER_VALUE);
    httpPost.addHeader(CLIENT_ID_HEADER_KEY, CLIENT_ID_HEADER_VALUE);
    httpPost.addHeader(HttpHeaders.USER_AGENT, USER_AGENT_HEADER_VALUE);
    httpPost.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_HEADER_VALUE);
    return httpPost;
  }

  private String textToSsml(final String text) throws Exception
  {
    final Document document = _documentBuilder.newDocument();
    document.setXmlStandalone(true);

    final Element speakElement = document.createElement(SSML_SPEAK_NODE);
    speakElement.setAttribute(SSML_SPEAK_VERSION_ATTRIBUTE, "1.0");
    speakElement.setAttribute(XMLConstants.XML_NS_PREFIX + ":lang", _language);
    document.appendChild(speakElement);

    final Element voiceElement = document.createElement(SSML_VOICE_NODE);
    voiceElement.setAttribute(SSML_VOICE_NAME_ATTRIBUTE, _voiceName);
    speakElement.appendChild(voiceElement);

    final Text textNode = document.createTextNode(text);
    voiceElement.appendChild(textNode);

    final StringWriter stringWriter = new StringWriter();
    _transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
    return stringWriter.toString();
  }
}
