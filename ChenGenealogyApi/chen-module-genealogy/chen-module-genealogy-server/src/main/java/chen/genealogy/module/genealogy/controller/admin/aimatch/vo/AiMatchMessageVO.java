package chen.genealogy.module.genealogy.controller.admin.aimatch.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiMatchMessageVO {

    private String role;
    private String content;
    private Integer score;
    private List<AiMatchContactVO> contacts = new ArrayList<>();
}
