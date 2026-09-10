package chen.genealogy.module.genealogy.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MemberDeedRespVO {

    private Long id;
    private Long memberId;
    private String title;
    private String content;
    private String source;
    private String occurYear;
}
