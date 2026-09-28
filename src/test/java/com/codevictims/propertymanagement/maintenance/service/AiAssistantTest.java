package com.codevictims.propertymanagement.maintenance.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

class AiAssistantTest {
  @Test
  void missingCredentialsReturnUnavailable() {
    try (var ai = new AiAssistant(null, 1)) {
      assertEquals("UNAVAILABLE", ai.suggest("المكيف لا يبرد", "ar").status());
    }
  }

  @Test
  void rejectsMalformedAndUnapprovedCategories() throws Exception {
    var ai = new AiAssistant(null, 1);
    assertThrows(Exception.class, () -> ai.parse("not json"));
    assertThrows(Exception.class, () -> ai.parse("{\"summary\":\"Issue\",\"category\":\"FIRE\"}"));
    assertThrows(
        Exception.class, () -> ai.parse("{\"summary\":\"Issue\",\"category\":\"AC\",\"cost\":5}"));
    assertEquals("AC", ai.parse("{\"summary\":\"تبريد ضعيف\",\"category\":\"AC\"}").category());
    ai.close();
  }

  @Test
  void serviceErrorsBecomeManualFallback() {
    ChatClient client = mock(ChatClient.class);
    when(client.prompt()).thenThrow(new IllegalStateException("Provider unavailable"));
    var ai = new AiAssistant(client, 1);
    assertEquals("ERROR", ai.suggest("The AC is not cooling", "en").status());
    ai.close();
  }

  @Test
  void timeoutIsBounded() {
    ChatClient client = mock(ChatClient.class);
    when(client.prompt())
        .thenAnswer(
            invocation -> {
              Thread.sleep(3000);
              throw new IllegalStateException();
            });
    var ai = new AiAssistant(client, 1);
    assertEquals("TIMEOUT", ai.suggest("لا يوجد تبريد", "ar").status());
    ai.close();
  }
}
