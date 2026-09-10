package chen.genealogy.module.genealogy.controller.admin.aimatch.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiMatchChatRespVO {

    private String content = "";
    private boolean done;
    private Integer score;
    private List<AiMatchContactVO> contacts = new ArrayList<>();
}
