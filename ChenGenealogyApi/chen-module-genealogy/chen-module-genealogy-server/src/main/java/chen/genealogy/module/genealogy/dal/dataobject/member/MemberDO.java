package chen.genealogy.module.genealogy.dal.dataobject.member;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@TableName(value = "tb_member", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long familyId;
    private Long userId;
    private String name;
    /** 1男 2女 */
    private Integer gender;
    private Long generationId;
    private Integer generationNo;
    private LocalDateTime birthDate;
    private LocalDateTime deathDate;
    private Long fatherId;
    private Long motherId;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> spouseIds;
    private String intro;
    private String avatar;
    private String mobile;
    private String idCard;
    private String tags;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> photoUrls;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> documentUrls;
    private Boolean alive;
    private Integer provinceId;
    private Integer cityId;
    private Integer countyId;
    private String address;
    private LocalDateTime deletedTime;
}
