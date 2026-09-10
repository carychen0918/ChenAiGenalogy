package chen.genealogy.module.genealogy.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ArchiveApplySaveReqVO {

    @NotNull(message = "成员不能为空")
    private Long memberId;
    @NotBlank(message = "补充说明不能为空")
    private String content;
    private List<String> attachments;
}
