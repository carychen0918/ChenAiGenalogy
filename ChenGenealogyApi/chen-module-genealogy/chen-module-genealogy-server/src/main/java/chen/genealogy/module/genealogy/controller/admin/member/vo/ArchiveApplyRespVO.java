package chen.genealogy.module.genealogy.controller.admin.member.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArchiveApplyRespVO {

    private Long id;
    private Long memberId;
    private String memberName;
    private Long applicantUserId;
    private String content;
    private List<String> attachments;
    private Integer status;
    private String auditReason;
    private Long auditUserId;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
}
