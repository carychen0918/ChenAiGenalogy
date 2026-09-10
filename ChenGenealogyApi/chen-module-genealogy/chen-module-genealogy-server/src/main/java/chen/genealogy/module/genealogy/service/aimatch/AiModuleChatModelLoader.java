package chen.genealogy.module.genealogy.service.aimatch;

import cn.hutool.core.util.StrUtil;
import chen.genealogy.framework.common.enums.CommonStatusEnum;
import chen.genealogy.module.genealogy.framework.aimatch.GenealogyAiMatchProperties;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 读取芋道 AI 模块已配置的默认聊天模型与角色提示词。
 */
@Slf4j
@Component
public class AiModuleChatModelLoader {

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private GenealogyAiMatchProperties properties;

    public ChatModelConfig load() {
        ChatModelConfig fromDb = loadFromAiTables();
        if (fromDb != null && StrUtil.isNotBlank(fromDb.getApiKey())) {
            return fromDb;
        }
        ChatModelConfig fallback = new ChatModelConfig();
        fallback.setApiKey(properties.getApiKey());
        fallback.setModel(StrUtil.blankToDefault(properties.getModel(), "deepseek-chat"));
        fallback.setBaseUrl(StrUtil.blankToDefault(properties.getBaseUrl(), "https://api.deepseek.com"));
        fallback.setPlatform(StrUtil.blankToDefault(properties.getPlatform(), "DeepSeek"));
        fallback.setSystemMessage(loadRolePrompt());
        if (StrUtil.isBlank(fallback.getApiKey())) {
            return null;
        }
        return fallback;
    }

    private ChatModelConfig loadFromAiTables() {
        try {
            String sql = """
                    SELECT m.model, m.temperature, m.max_tokens, k.api_key, k.url, k.platform
                    FROM ai_model m
                    INNER JOIN ai_api_key k ON k.id = m.key_id AND k.deleted = 0 AND k.status = ?
                    WHERE m.deleted = 0 AND m.status = ? AND m.type = ?
                      AND (m.platform = 'DeepSeek' OR k.platform = 'DeepSeek')
                    ORDER BY m.sort ASC, m.id ASC
                    LIMIT 1
                    """;
            return jdbcTemplate.query(sql, rs -> {
                if (!rs.next()) {
                    return null;
                }
                ChatModelConfig cfg = new ChatModelConfig();
                cfg.setModel(rs.getString("model"));
                cfg.setTemperature(rs.getObject("temperature") == null ? 0.3D : rs.getDouble("temperature"));
                cfg.setMaxTokens(rs.getObject("max_tokens") == null ? 1024 : rs.getInt("max_tokens"));
                cfg.setApiKey(rs.getString("api_key"));
                cfg.setBaseUrl(rs.getString("url"));
                cfg.setPlatform(rs.getString("platform"));
                cfg.setSystemMessage(loadRolePrompt());
                return cfg;
            }, CommonStatusEnum.ENABLE.getStatus(), CommonStatusEnum.ENABLE.getStatus(), 1);
        } catch (DataAccessException ex) {
            log.debug("[loadFromAiTables] skip: {}", ex.getMessage());
            return null;
        }
    }

    private String loadRolePrompt() {
        try {
            String sql = """
                    SELECT system_message FROM ai_chat_role
                    WHERE deleted = 0 AND status = ? AND public_status = 1 AND name = ?
                    ORDER BY sort ASC, id ASC LIMIT 1
                    """;
            return jdbcTemplate.query(sql, rs -> rs.next() ? rs.getString(1) : null,
                    CommonStatusEnum.ENABLE.getStatus(), properties.getRoleName());
        } catch (DataAccessException ex) {
            return null;
        }
    }

    @Data
    public static class ChatModelConfig {
        private String apiKey;
        private String model;
        private String baseUrl;
        private String platform;
        private Double temperature = 0.3D;
        private Integer maxTokens = 1024;
        private String systemMessage;
    }
}
