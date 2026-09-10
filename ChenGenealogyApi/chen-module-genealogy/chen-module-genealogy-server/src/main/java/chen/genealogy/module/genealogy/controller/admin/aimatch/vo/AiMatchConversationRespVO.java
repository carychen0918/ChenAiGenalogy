package chen.genealogy.module.genealogy.controller.admin.aimatch.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiMatchConversationRespVO {

    private Long id;
    private String sessionId;
    private String title;
    private Integer lastScore;
    private LocalDateTime updateTime;
}
