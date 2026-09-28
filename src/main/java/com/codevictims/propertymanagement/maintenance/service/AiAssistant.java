package com.codevictims.propertymanagement.maintenance.service;

import com.fasterxml.jackson.databind.*;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiAssistant implements AutoCloseable {
  public record Suggestion(String summary, String category) {}

  public record Result(String status, Suggestion suggestion) {}

  private static final String systemPrompt = loadSystemPrompt();

  private static String loadSystemPrompt() {
    try {
      return new org.springframework.core.io.ClassPathResource("prompts/maintenance-assistant.txt")
          .getContentAsString(java.nio.charset.StandardCharsets.UTF_8)
          .trim();
    } catch (java.io.IOException e) {
      throw new IllegalStateException("Maintenance assistant prompt is missing", e);
    }
  }

  private final ChatClient client;
  private final int timeout;
  private final ThreadPoolExecutor executor =
      new ThreadPoolExecutor(
          2,
          2,
          0,
          TimeUnit.SECONDS,
          new ArrayBlockingQueue<>(8),
          new ThreadPoolExecutor.AbortPolicy());
  private final ObjectMapper json =
      new ObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  @org.springframework.beans.factory.annotation.Autowired
  public AiAssistant(
      @Value("${app.ai.key:}") String key,
      @Value("${app.ai.model}") String model,
      @Value("${app.ai.timeout-seconds:12}") int timeout) {
    this.timeout = timeout;
    if (key.isBlank()) {
      client = null;
      return;
    }
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(4000);
    factory.setReadTimeout(10000);
    var api =
        OpenAiApi.builder()
            .apiKey(key)
            .restClientBuilder(RestClient.builder().requestFactory(factory))
            .build();
    var chat =
        OpenAiChatModel.builder()
            .openAiApi(api)
            .defaultOptions(OpenAiChatOptions.builder().model(model).temperature(0.0).build())
            .build();
    client = ChatClient.create(chat);
  }

  // Test constructor uses a mocked Spring AI ChatClient. No provider is called in tests.
  AiAssistant(ChatClient client, int timeout) {
    this.client = client;
    this.timeout = timeout;
  }

  public boolean available() {
    return client != null;
  }

  public Result suggest(String description, String language) {
    if (client == null) return new Result("UNAVAILABLE", null);
    if (description == null
        || description.isBlank()
        || description.length() > 2000
        || !Set.of("ar", "en").contains(language)) return new Result("INVALID_INPUT", null);
    Future<String> pending = null;
    try {
      pending =
          executor.submit(
              () ->
                  client
                      .prompt()
                      .system(
                          systemPrompt
                              + " Summarize in "
                              + (language.equals("ar") ? "Arabic" : "English")
                              + ".")
                      .user(description)
                      .call()
                      .content());
      return new Result("PROPOSED", parse(pending.get(timeout, TimeUnit.SECONDS)));
    } catch (TimeoutException e) {
      if (pending != null) pending.cancel(true);
      return new Result("TIMEOUT", null);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      if (pending != null) pending.cancel(true);
      return new Result("UNAVAILABLE", null);
    } catch (Exception e) {
      return new Result("ERROR", null);
    }
  }

  public Suggestion parse(String response) throws Exception {
    if (response == null || response.length() > 2000)
      throw new IllegalArgumentException("Malformed response");
    Suggestion s = json.readValue(response, Suggestion.class);
    if (s.summary() == null
        || s.summary().isBlank()
        || s.summary().length() > 255
        || s.category() == null
        || !Arrays.asList(MaintenanceService.CATEGORIES).contains(s.category()))
      throw new IllegalArgumentException("Malformed response");
    return s;
  }

  @PreDestroy
  public void close() {
    executor.shutdownNow();
  }
}
