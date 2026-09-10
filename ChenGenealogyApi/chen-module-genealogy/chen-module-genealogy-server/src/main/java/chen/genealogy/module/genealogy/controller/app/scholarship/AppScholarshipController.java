package chen.genealogy.module.genealogy.controller.app.scholarship;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.security.core.util.SecurityFrameworkUtils;
import chen.genealogy.module.genealogy.controller.admin.scholarship.vo.ScholarshipConfigRespVO;
import chen.genealogy.module.genealogy.controller.admin.scholarship.vo.ScholarshipPageReqVO;
import chen.genealogy.module.genealogy.controller.admin.scholarship.vo.ScholarshipRespVO;
import chen.genealogy.module.genealogy.controller.admin.scholarship.vo.ScholarshipSaveReqVO;
import chen.genealogy.module.genealogy.service.scholarship.ScholarshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 学海无涯")
@RestController
@RequestMapping("/genealogy/scholarship")
@Validated
public class AppScholarshipController {

    @Resource
    private ScholarshipService scholarshipService;

    @GetMapping("/config/get")
    @Operation(summary = "资助窗口配置")
    public CommonResult<ScholarshipConfigRespVO> getConfig(@RequestParam(value = "year", required = false) Integer year) {
        return success(scholarshipService.getConfig(year));
    }

    @PostMapping("/create")
    @Operation(summary = "暂存草稿")
    public CommonResult<Long> create(@Valid @RequestBody ScholarshipSaveReqVO reqVO) {
        return success(scholarshipService.saveDraftOrSubmit(reqVO, false));
    }

    @PostMapping("/submit")
    @Operation(summary = "提交申请")
    public CommonResult<Long> submit(@Valid @RequestBody ScholarshipSaveReqVO reqVO) {
        return success(scholarshipService.saveDraftOrSubmit(reqVO, true));
    }

    @PutMapping("/withdraw")
    @Operation(summary = "撤回申请")
    public CommonResult<Boolean> withdraw(@RequestParam("id") Long id) {
        scholarshipService.withdraw(id);
        return success(true);
    }

    @GetMapping("/my-page")
    @Operation(summary = "我的申请分页")
    public CommonResult<PageResult<ScholarshipRespVO>> myPage(@Valid ScholarshipPageReqVO reqVO) {
        reqVO.setUserId(SecurityFrameworkUtils.getLoginUserId());
        return success(scholarshipService.getPage(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "申请详情")
    public CommonResult<ScholarshipRespVO> get(@RequestParam("id") Long id) {
        return success(scholarshipService.getDetail(id));
    }
}
