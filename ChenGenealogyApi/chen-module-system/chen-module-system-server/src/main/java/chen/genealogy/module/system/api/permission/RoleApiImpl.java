package chen.genealogy.module.system.api.permission;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.framework.common.util.object.BeanUtils;
import chen.genealogy.module.system.api.permission.dto.RoleRespDTO;
import chen.genealogy.module.system.dal.dataobject.permission.RoleDO;
import chen.genealogy.module.system.service.permission.RoleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class RoleApiImpl implements RoleApi {

    @Resource
    private RoleService roleService;

    @Override
    public CommonResult<Boolean> validRoleList(Collection<Long> ids) {
        roleService.validateRoleList(ids);
        return success(true);
    }

    @Override
    public CommonResult<RoleRespDTO> getRole(Long id) {
        RoleDO role = roleService.getRole(id);
        return success(BeanUtils.toBean(role, RoleRespDTO.class));
    }

    @Override
    public CommonResult<List<RoleRespDTO>> getRoleList(Collection<Long> ids) {
        List<RoleDO> list = roleService.getRoleList(ids);
        return success(BeanUtils.toBean(list, RoleRespDTO.class));
    }

    @Override
    public CommonResult<RoleRespDTO> getRoleByCode(String code) {
        RoleDO role = roleService.getRoleList().stream()
                .filter(item -> code.equals(item.getCode()))
                .findFirst()
                .orElse(null);
        return success(BeanUtils.toBean(role, RoleRespDTO.class));
    }

}
