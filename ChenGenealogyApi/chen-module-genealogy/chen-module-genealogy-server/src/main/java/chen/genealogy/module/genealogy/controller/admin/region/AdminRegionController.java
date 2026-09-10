package chen.genealogy.module.genealogy.controller.admin.region;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.module.genealogy.controller.admin.region.vo.AdminRegionRespVO;
import chen.genealogy.module.genealogy.controller.admin.region.vo.AdminRegionSaveReqVO;
import chen.genealogy.module.genealogy.service.region.AdminRegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 地区管理员")
@RestController
@RequestMapping("/genealogy/admin-region")
@Validated
public class AdminRegionController {

    @Resource
    private AdminRegionService adminRegionService;

    @GetMapping("/list")
    @Operation(summary = "地区管理员列表")
    @PreAuthorize("@ss.hasPermission('genealogy:admin-region:query')")
    public CommonResult<List<AdminRegionRespVO>> list() {
        return success(adminRegionService.listBindings());
    }

    @PutMapping("/save")
    @Operation(summary = "绑定管辖地区")
    @PreAuthorize("@ss.hasPermission('genealogy:admin-region:update')")
    public CommonResult<Boolean> save(@Valid @RequestBody AdminRegionSaveReqVO reqVO) {
        adminRegionService.save(reqVO);
        return success(true);
    }
}
