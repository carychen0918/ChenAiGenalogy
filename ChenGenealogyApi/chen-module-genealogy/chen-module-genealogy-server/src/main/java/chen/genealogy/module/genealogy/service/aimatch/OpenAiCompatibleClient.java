package chen.genealogy.module.genealogy.service.aimatch;

import cn.hutool.core.util.StrUtil;
import chen.genealogy.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * OpenAI 兼容协议流式对话，对接芋道 AI 模块配置的各类兼容网关。
 */
@Slf4j
public class OpenAiCompatibleClient {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public String stream(AiModuleChatModelLoader.ChatModelConfig config,
                         List<Map<String, String>> messages,
                         Consumer<String> onDelta) throws Exception {
        String url = completionsUrl(config.getBaseUrl(), config.getPlatform());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", config.getModel());
        body.put("messages", messages);
        body.put("stream", true);
        body.put("temperature", config.getTemperature() == null ? 0.3 : config.getTemperature());
        if (config.getMaxTokens() != null && config.getMaxTokens() > 0) {
            body.put("max_tokens", config.getMaxTokens());
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(body), StandardCharsets.UTF_8))
                .build();
        HttpResponse<java.util.stream.Stream<String>> response = CLIENT.send(request,
                HttpResponse.BodyHandlers.ofLines());
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("AI 接口返回 " + response.statusCode());
        }
        StringBuilder full = new StringBuilder();
        response.body().forEach(line -> {
            String payload = parseSseLine(line);
            if (payload == null) {
                return;
            }
            String delta = readDelta(payload);
            if (StrUtil.isNotEmpty(delta)) {
                full.append(delta);
                if (onDelta != null) {
                    onDelta.accept(delta);
                }
            }
        });
        return full.toString();
    }

    static String completionsUrl(String baseUrl, String platform) {
        String base = StrUtil.blankToDefault(baseUrl, defaultBase(platform)).trim();
        if (base.contains("/chat/completions")) {
            return base;
        }
        return StrUtil.removeSuffix(base, "/") + "/v1/chat/completions";
    }

    private static String defaultBase(String platform) {
        if (platform == null) {
            return "https://api.deepseek.com";
        }
        return switch (platform) {
            case "OpenAI" -> "https://api.openai.com";
            case "DeepSeek" -> "https://api.deepseek.com";
            case "TongYi" -> "https://dashscope.aliyuncs.com/compatible-mode";
            case "Moonshot" -> "https://api.moonshot.cn";
            case "ZhiPu" -> "https://open.bigmodel.cn/api/paas/v4";
            default -> "https://api.deepseek.com";
        };
    }

    private static String parseSseLine(String line) {
        if (StrUtil.isBlank(line)) {
            return null;
        }
        String trimmed = line.trim();
        if (!trimmed.startsWith("data:")) {
            return null;
        }
        String data = trimmed.substring(5).trim();
        if (data.isEmpty() || "[DONE]".equals(data)) {
            return null;
        }
        return data;
    }

    private static String readDelta(String json) {
        try {
            JsonNode root = JsonUtils.parseTree(json);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return null;
            }
            JsonNode delta = choices.get(0).path("delta").path("content");
            if (delta.isMissingNode() || delta.isNull()) {
                delta = choices.get(0).path("message").path("content");
            }
            return delta.isMissingNode() || delta.isNull() ? null : delta.asText();
        } catch (Exception ex) {
            log.debug("[readDelta] skip: {}", ex.getMessage());
            return null;
        }
    }

    public static List<Map<String, String>> messages(String system, List<Map<String, String>> history, String user) {
        List<Map<String, String>> list = new ArrayList<>();
        list.add(Map.of("role", "system", "content", system));
        if (history != null) {
            list.addAll(history);
        }
        list.add(Map.of("role", "user", "content", user));
        return list;
    }
}
