# Maintenance assistant

`maintenance/service/AiAssistant` uses Spring AI and the classpath prompt `prompts/maintenance-assistant.txt`. Configure `AI_API_KEY` and `AI_MODEL` on the server to enable it. User descriptions are untrusted data. Output is strictly limited to a summary and an allowed category; managers review the proposal before approval. Original reports remain available.

Missing credentials, timeout, invalid output or provider errors leave the manual maintenance workflow available. The assistant cannot set urgency, spend money or perform maintenance actions. Unit tests mock the client; no provider call is included in automated verification. See [integration activation](INTEGRATIONS.md).
