package chen.genealogy.module.genealogy.controller.admin.dashboard;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.module.genealogy.controller.admin.dashboard.vo.DashboardSummaryRespVO;
import chen.genealogy.module.genealogy.service.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 族谱工作台")
@RestController
@RequestMapping("/genealogy/dashboard")
@Validated
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "工作台总览")
    @PreAuthorize("@ss.hasPermission('genealogy:dashboard:query')")
    public CommonResult<DashboardSummaryRespVO> summary() {
        return success(dashboardService.summary());
    }
}
