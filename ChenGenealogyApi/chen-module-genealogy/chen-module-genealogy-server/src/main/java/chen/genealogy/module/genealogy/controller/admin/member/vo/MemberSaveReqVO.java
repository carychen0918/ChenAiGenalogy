package chen.genealogy.module.genealogy.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 成员新增/修改")
@Data
public class MemberSaveReqVO {

    private Long id;

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "性别不能为空")
    @Schema(description = "性别 1男 2女", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer gender;

    @NotNull(message = "辈分不能为空")
    @Schema(description = "字辈编号")
    private Long generationId;

    private LocalDateTime birthDate;
    private LocalDateTime deathDate;
    private Long fatherId;
    private Long motherId;
    private List<Long> spouseIds;
    /** 多配偶冲突时二次确认 */
    private Boolean confirmSpouseConflict;
    private String intro;
    private String avatar;
    private String mobile;
    private String idCard;
    private String tags;
    private List<String> photoUrls;
    private List<String> documentUrls;
    private Long userId;
    @Schema(description = "登录账号，不填则按姓名拼音生成")
    private String loginUsername;
    @Schema(description = "是否同步创建登录账号，默认在世成员为 true")
    private Boolean createLoginAccount;
    private Integer provinceId;
    private Integer cityId;
    private Integer countyId;
    private String address;
}
