package chen.genealogy.module.genealogy.service.region;

import chen.genealogy.framework.ip.core.utils.AreaUtils;
import chen.genealogy.module.genealogy.controller.admin.region.vo.AdminRegionRespVO;
import chen.genealogy.module.genealogy.controller.admin.region.vo.AdminRegionSaveReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;
import chen.genealogy.module.genealogy.dal.dataobject.region.AdminRegionDO;
import chen.genealogy.module.genealogy.dal.mysql.region.AdminRegionMapper;
import chen.genealogy.module.genealogy.enums.ScholarshipAuditLevelEnum;
import chen.genealogy.module.genealogy.util.RegionAreaUtils;
import chen.genealogy.module.system.api.permission.PermissionApi;
import chen.genealogy.module.system.api.permission.RoleApi;
import chen.genealogy.module.system.api.permission.dto.RoleRespDTO;
import chen.genealogy.module.system.api.user.AdminUserApi;
import chen.genealogy.module.system.api.user.dto.AdminUserRespDTO;
import chen.genealogy.module.system.enums.permission.RoleCodeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static chen.genealogy.framework.common.exception.util.ServiceExceptionUtil.exception;
import static chen.genealogy.module.genealogy.enums.ErrorCodeConstants.ADMIN_REGION_ROLE_MISSING;
import static chen.genealogy.module.genealogy.enums.ScholarshipAuditLevelEnum.*;

@Service("genealogyAdminRegionService")
@Validated
public class AdminRegionService {

    @Resource
    private AdminRegionMapper adminRegionMapper;
    @Resource
    private PermissionApi permissionApi;
    @Resource
    private RoleApi roleApi;
    @Resource
    private AdminUserApi adminUserApi;

    public boolean hasAdmin(ScholarshipAuditLevelEnum level, Integer areaId) {
        return level != null && adminRegionMapper.existsAdmin(level.getCode(), areaId);
    }

    public List<String> buildPipeline(Integer provinceId, Integer cityId, Integer countyId) {
        List<String> pipeline = new ArrayList<>();
        if (hasAdmin(COUNTY, countyId)) {
            pipeline.add(COUNTY.getCode());
        }
        if (hasAdmin(CITY, cityId)) {
            pipeline.add(CITY.getCode());
        }
        if (hasAdmin(PROVINCE, provinceId)) {
            pipeline.add(PROVINCE.getCode());
        }
        if (pipeline.isEmpty()) {
            pipeline.add(FAMILY.getCode());
        }
        return pipeline;
    }

    public boolean isFamilyWide(Long userId) {
        if (userId == null) {
            return false;
        }
        return Boolean.TRUE.equals(permissionApi.hasAnyRoles(userId,
                RoleCodeEnum.SUPER_ADMIN.getCode(),
                RoleCodeEnum.TENANT_ADMIN.getCode(),
                RoleCodeEnum.PATRIARCH.getCode(),
                RoleCodeEnum.GENEALOGY_ADMIN.getCode()).getCheckedData());
    }

    public AdminRegionDO getBinding(Long userId, ScholarshipAuditLevelEnum level) {
        if (userId == null || level == null || level == FAMILY) {
            return null;
        }
        return adminRegionMapper.selectByUserAndLevel(userId, level.getCode());
    }

    public ScholarshipAuditLevelEnum resolveRegionalLevel(Long userId) {
        if (Boolean.TRUE.equals(permissionApi.hasAnyRoles(userId, RoleCodeEnum.COUNTY_ADMIN.getCode()).getCheckedData())) {
            return COUNTY;
        }
        if (Boolean.TRUE.equals(permissionApi.hasAnyRoles(userId, RoleCodeEnum.CITY_ADMIN.getCode()).getCheckedData())) {
            return CITY;
        }
        if (Boolean.TRUE.equals(permissionApi.hasAnyRoles(userId, RoleCodeEnum.PROVINCE_ADMIN.getCode()).getCheckedData())) {
            return PROVINCE;
        }
        return null;
    }

    public boolean canAuditLevel(Long userId, String auditLevel, Integer provinceId, Integer cityId, Integer countyId) {
        if (userId == null || auditLevel == null) {
            return false;
        }
        if (isFamilyWide(userId)) {
            return true;
        }
        ScholarshipAuditLevelEnum level = ScholarshipAuditLevelEnum.of(auditLevel);
        if (level == null || level == FAMILY) {
            return false;
        }
        AdminRegionDO binding = getBinding(userId, level);
        if (binding == null) {
            return false;
        }
        return switch (level) {
            case COUNTY -> binding.getAreaId() != null && binding.getAreaId().equals(countyId);
            case CITY -> binding.getAreaId() != null && binding.getAreaId().equals(cityId);
            case PROVINCE -> binding.getAreaId() != null && binding.getAreaId().equals(provinceId);
            default -> false;
        };
    }

    public void applyPageScope(Long userId, ScholarshipPageScope scope) {
        if (isFamilyWide(userId)) {
            return;
        }
        ScholarshipAuditLevelEnum level = resolveRegionalLevel(userId);
        AdminRegionDO binding = getBinding(userId, level);
        if (level == null || binding == null) {
            scope.setEmpty(true);
            return;
        }
        switch (level) {
            case COUNTY -> scope.setCountyId(binding.getAreaId());
            case CITY -> scope.setCityId(binding.getAreaId());
            case PROVINCE -> scope.setProvinceId(binding.getAreaId());
            default -> scope.setEmpty(true);
        }
    }

    public void syncFromMember(MemberDO member) {
        if (member == null || member.getUserId() == null) {
            return;
        }
        Long userId = member.getUserId();
        if (Boolean.TRUE.equals(permissionApi.hasAnyRoles(userId, RoleCodeEnum.COUNTY_ADMIN.getCode()).getCheckedData())
                && member.getCountyId() != null) {
            upsert(userId, COUNTY, member.getCountyId());
        }
        if (Boolean.TRUE.equals(permissionApi.hasAnyRoles(userId, RoleCodeEnum.CITY_ADMIN.getCode()).getCheckedData())
                && member.getCityId() != null) {
            upsert(userId, CITY, member.getCityId());
        }
        if (Boolean.TRUE.equals(permissionApi.hasAnyRoles(userId, RoleCodeEnum.PROVINCE_ADMIN.getCode()).getCheckedData())
                && member.getProvinceId() != null) {
            upsert(userId, PROVINCE, member.getProvinceId());
        }
    }

    public void save(AdminRegionSaveReqVO reqVO) {
        ScholarshipAuditLevelEnum level = resolveRegionalLevel(reqVO.getUserId());
        if (level == null) {
            throw exception(ADMIN_REGION_ROLE_MISSING);
        }
        upsert(reqVO.getUserId(), level, reqVO.getAreaId());
    }

    public List<AdminRegionContact> listContacts(Integer provinceId, Integer cityId, Integer countyId) {
        List<AdminRegionContact> contacts = new ArrayList<>();
        addAreaContacts(contacts, COUNTY, countyId, "县管理员");
        addAreaContacts(contacts, CITY, cityId, "市管理员");
        addAreaContacts(contacts, PROVINCE, provinceId, "省管理员");
        if (contacts.isEmpty()) {
            addRoleContacts(contacts, RoleCodeEnum.PATRIARCH, "族长");
            addRoleContacts(contacts, RoleCodeEnum.GENEALOGY_ADMIN, "家族管理员");
        }
        return contacts;
    }

    private void addAreaContacts(List<AdminRegionContact> contacts, ScholarshipAuditLevelEnum level,
                                 Integer areaId, String levelName) {
        if (areaId == null) {
            return;
        }
        List<AdminRegionDO> bindings = adminRegionMapper.selectListByLevelAndArea(level.getCode(), areaId);
        for (AdminRegionDO binding : bindings) {
            AdminUserRespDTO user;
            try {
                user = adminUserApi.getUser(binding.getUserId()).getCheckedData();
            } catch (Exception ex) {
                continue;
            }
            if (user == null) {
                continue;
            }
            contacts.add(new AdminRegionContact(levelName, AreaUtils.format(areaId, " / "),
                    user.getNickname(), user.getMobile()));
        }
    }

    private void addRoleContacts(List<AdminRegionContact> contacts, RoleCodeEnum roleCode, String levelName) {
        RoleRespDTO role = roleApi.getRoleByCode(roleCode.getCode()).getCheckedData();
        if (role == null || role.getId() == null) {
            return;
        }
        Set<Long> userIds = permissionApi.getUserRoleIdListByRoleIds(Collections.singleton(role.getId())).getCheckedData();
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<AdminUserRespDTO> users = adminUserApi.getUserList(userIds).getCheckedData();
        if (users == null) {
            return;
        }
        for (AdminUserRespDTO user : users) {
            contacts.add(new AdminRegionContact(levelName, "全族", user.getNickname(), user.getMobile()));
        }
    }

    public List<AdminRegionRespVO> listBindings() {
        List<AdminRegionRespVO> result = new ArrayList<>();
        addRoleUsers(result, RoleCodeEnum.PROVINCE_ADMIN, PROVINCE);
        addRoleUsers(result, RoleCodeEnum.CITY_ADMIN, CITY);
        addRoleUsers(result, RoleCodeEnum.COUNTY_ADMIN, COUNTY);
        return result;
    }

    private void addRoleUsers(List<AdminRegionRespVO> result, RoleCodeEnum roleCode, ScholarshipAuditLevelEnum level) {
        RoleRespDTO role = roleApi.getRoleByCode(roleCode.getCode()).getCheckedData();
        if (role == null || role.getId() == null) {
            return;
        }
        Set<Long> userIds = permissionApi.getUserRoleIdListByRoleIds(Collections.singleton(role.getId())).getCheckedData();
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<AdminUserRespDTO> users = adminUserApi.getUserList(userIds).getCheckedData();
        if (users == null) {
            return;
        }
        for (AdminUserRespDTO user : users) {
            AdminRegionRespVO vo = new AdminRegionRespVO();
            vo.setUserId(user.getId());
            vo.setNickname(user.getNickname());
            vo.setUsername(user.getUsername());
            vo.setRoleCode(roleCode.getCode());
            vo.setRoleName(roleCode.getName());
            vo.setAuditLevel(level.getCode());
            AdminRegionDO binding = adminRegionMapper.selectByUserAndLevel(user.getId(), level.getCode());
            if (binding != null) {
                vo.setId(binding.getId());
                vo.setAreaId(binding.getAreaId());
                vo.setProvinceId(binding.getProvinceId());
                vo.setCityId(binding.getCityId());
                vo.setCountyId(binding.getCountyId());
                vo.setRegionName(AreaUtils.format(binding.getAreaId(), " / "));
                vo.setBound(true);
            }
            result.add(vo);
        }
    }

    private void upsert(Long userId, ScholarshipAuditLevelEnum level, Integer areaId) {
        RegionAreaUtils.Region region = RegionAreaUtils.of(areaId);
        AdminRegionDO exists = adminRegionMapper.selectByUserAndLevel(userId, level.getCode());
        AdminRegionDO data = exists != null ? exists : new AdminRegionDO();
        data.setUserId(userId);
        data.setAuditLevel(level.getCode());
        data.setAreaId(areaId);
        data.setProvinceId(region.getProvinceId());
        data.setCityId(region.getCityId());
        data.setCountyId(region.getCountyId());
        if (exists == null) {
            adminRegionMapper.insert(data);
        } else {
            adminRegionMapper.updateById(data);
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class AdminRegionContact {
        private String levelName;
        private String regionName;
        private String nickname;
        private String mobile;
    }

    @lombok.Data
    public static class ScholarshipPageScope {
        private boolean empty;
        private Integer provinceId;
        private Integer cityId;
        private Integer countyId;
    }
}
