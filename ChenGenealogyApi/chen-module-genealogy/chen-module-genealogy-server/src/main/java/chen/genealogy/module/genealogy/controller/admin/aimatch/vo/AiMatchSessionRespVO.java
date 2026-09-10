package chen.genealogy.module.genealogy.controller.admin.aimatch.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiMatchSessionRespVO {

    private String sessionId;
    private Long conversationId;
    private String title;
    private List<AiMatchMessageVO> messages = new ArrayList<>();
}
