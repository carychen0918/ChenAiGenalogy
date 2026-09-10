package chen.genealogy.module.genealogy.framework.aimatch;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "chen.genealogy.ai-match")
public class GenealogyAiMatchProperties {

    private boolean enabled = true;
    /** 优先使用 AI 模块 ai_model + ai_api_key；为空再使用下列备用配置 */
    private String apiKey;
    private String model;
    private String baseUrl;
    private String platform = "DeepSeek";
    private String roleName = "寻宗问祖";
    private int maxInputLength = 1500;
    private int maxMessages = 40;
    /** Redis 会话保留天数；登录用户的历史以数据库为准 */
    private int sessionTtlDays = 7;
    private int rateLimitPerHour = 30;
    /** 未登录：同一 IP 或同一浏览器身份，每天最多咨询次数 */
    private int guestDailyLimit = 10;
    private int matchThreshold = 70;
}
