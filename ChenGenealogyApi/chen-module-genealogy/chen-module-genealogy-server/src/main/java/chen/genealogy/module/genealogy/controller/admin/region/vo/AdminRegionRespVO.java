package chen.genealogy.module.genealogy.controller.admin.region.vo;

import lombok.Data;

@Data
public class AdminRegionRespVO {
    private Long id;
    private Long userId;
    private String nickname;
    private String username;
    private String roleCode;
    private String roleName;
    private String auditLevel;
    private Integer areaId;
    private Integer provinceId;
    private Integer cityId;
    private Integer countyId;
    private String regionName;
    private boolean bound;
}
