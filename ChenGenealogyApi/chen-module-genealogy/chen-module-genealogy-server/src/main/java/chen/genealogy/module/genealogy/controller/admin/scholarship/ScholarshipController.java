package chen.genealogy.module.genealogy.controller.admin.scholarship;

import chen.genealogy.framework.apilog.core.annotation.ApiAccessLog;
import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.framework.common.pojo.PageParam;
import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.excel.core.util.ExcelUtils;
import chen.genealogy.module.genealogy.controller.admin.scholarship.vo.*;
import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipDisbursementDO;
import chen.genealogy.module.genealogy.service.scholarship.ScholarshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static chen.genealogy.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 学海无涯")
@RestController
@RequestMapping("/genealogy/scholarship")
@Validated
public class ScholarshipController {

    @Resource
    private ScholarshipService scholarshipService;

    @GetMapping("/config/get")
    public CommonResult<ScholarshipConfigRespVO> getConfig(@RequestParam(value = "year", required = false) Integer year) {
        return success(scholarshipService.getConfig(year));
    }

    @PutMapping("/config/save")
    @PreAuthorize("@ss.hasPermission('genealogy:config:update')")
    public CommonResult<Boolean> saveConfig(@RequestBody ScholarshipConfigSaveReqVO reqVO) {
        scholarshipService.saveConfig(reqVO);
        return success(true);
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
    public CommonResult<Boolean> withdraw(@RequestParam("id") Long id) {
        scholarshipService.withdraw(id);
        return success(true);
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('genealogy:scholarship:query')")
    public CommonResult<PageResult<ScholarshipRespVO>> page(@Valid ScholarshipPageReqVO reqVO) {
        return success(scholarshipService.getPage(reqVO));
    }

    @GetMapping("/my-page")
    public CommonResult<PageResult<ScholarshipRespVO>> myPage(@Valid ScholarshipPageReqVO reqVO) {
        reqVO.setUserId(chen.genealogy.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId());
        return success(scholarshipService.getPage(reqVO));
    }

    @GetMapping("/get")
    public CommonResult<ScholarshipRespVO> get(@RequestParam("id") Long id) {
        return success(scholarshipService.getDetail(id));
    }

    @PutMapping("/audit")
    @Operation(summary = "逐级审核")
    @PreAuthorize("@ss.hasPermission('genealogy:scholarship:first-audit')")
    public CommonResult<Boolean> audit(@RequestParam("id") Long id,
                                       @RequestParam("result") Integer result,
                                       @RequestParam(value = "opinion", required = false) String opinion) {
        scholarshipService.audit(id, result, opinion);
        return success(true);
    }

    @PutMapping("/first-audit")
    @PreAuthorize("@ss.hasPermission('genealogy:scholarship:first-audit')")
    public CommonResult<Boolean> firstAudit(@RequestParam("id") Long id,
                                            @RequestParam("result") Integer result,
                                            @RequestParam(value = "opinion", required = false) String opinion) {
        scholarshipService.firstAudit(id, result, opinion);
        return success(true);
    }

    @PutMapping("/first-audit-batch")
    @PreAuthorize("@ss.hasPermission('genealogy:scholarship:first-audit')")
    public CommonResult<Boolean> batchFirst(@RequestParam("ids") List<Long> ids,
                                            @RequestParam("result") Integer result,
                                            @RequestParam(value = "opinion", required = false) String opinion) {
        scholarshipService.batchFirstAudit(ids, result, opinion);
        return success(true);
    }

    @PutMapping("/final-audit")
    @PreAuthorize("@ss.hasPermission('genealogy:scholarship:final-audit')")
    public CommonResult<Boolean> finalAudit(@RequestParam("id") Long id,
                                            @RequestParam("result") Integer result,
                                            @RequestParam(value = "opinion", required = false) String opinion) {
        scholarshipService.finalAudit(id, result, opinion);
        return success(true);
    }

    @PostMapping("/disburse")
    @PreAuthorize("@ss.hasPermission('genealogy:disburse:create')")
    public CommonResult<Long> disburse(@Valid @RequestBody DisbursementSaveReqVO reqVO) {
        return success(scholarshipService.disburse(reqVO));
    }

    @GetMapping("/disburse/page")
    @PreAuthorize("@ss.hasPermission('genealogy:disburse:query')")
    public CommonResult<PageResult<ScholarshipDisbursementDO>> disbursePage(@Valid DisbursementPageReqVO reqVO) {
        return success(scholarshipService.getDisbursePage(reqVO));
    }

    @GetMapping("/disburse/export-excel")
    @PreAuthorize("@ss.hasPermission('genealogy:disburse:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void export(DisbursementPageReqVO reqVO, HttpServletResponse response) throws IOException {
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        ExcelUtils.write(response, "资助发放记录.xls", "数据", ScholarshipDisbursementDO.class,
                scholarshipService.getDisbursePage(reqVO).getList());
    }

    @GetMapping("/stats")
    @PreAuthorize("@ss.hasPermission('genealogy:scholarship:stats')")
    public CommonResult<Map<String, Object>> stats(@RequestParam(value = "year", required = false) Integer year) {
        return success(scholarshipService.stats(year));
    }
}
