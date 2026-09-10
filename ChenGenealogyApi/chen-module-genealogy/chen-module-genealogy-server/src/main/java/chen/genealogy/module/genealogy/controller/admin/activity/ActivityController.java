package chen.genealogy.module.genealogy.controller.admin.activity;

import chen.genealogy.framework.apilog.core.annotation.ApiAccessLog;
import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.framework.common.pojo.PageParam;
import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.excel.core.util.ExcelUtils;
import chen.genealogy.module.genealogy.controller.admin.activity.vo.*;
import chen.genealogy.module.genealogy.dal.dataobject.activity.ActivityDO;
import chen.genealogy.module.genealogy.dal.dataobject.activity.ActivityRegistrationDO;
import chen.genealogy.module.genealogy.dal.dataobject.activity.WorshipRecordDO;
import chen.genealogy.module.genealogy.service.activity.ActivityService;
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

import static chen.genealogy.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 祭祖活动")
@RestController
@RequestMapping("/genealogy/activity")
@Validated
public class ActivityController {

    @Resource
    private ActivityService activityService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('genealogy:activity:create')")
    public CommonResult<Long> create(@Valid @RequestBody ActivitySaveReqVO reqVO) {
        return success(activityService.create(reqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('genealogy:activity:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody ActivitySaveReqVO reqVO) {
        activityService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('genealogy:activity:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        activityService.delete(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @PreAuthorize("@ss.hasPermission('genealogy:activity:update')")
    public CommonResult<Boolean> cancel(@RequestParam("id") Long id) {
        activityService.cancel(id);
        return success(true);
    }

    @GetMapping("/get")
    public CommonResult<ActivityDO> get(@RequestParam("id") Long id) {
        return success(activityService.get(id));
    }

    @GetMapping("/page")
    public CommonResult<PageResult<ActivityDO>> page(@Valid ActivityPageReqVO reqVO) {
        return success(activityService.getPage(reqVO));
    }

    @PostMapping("/register")
    public CommonResult<Long> register(@RequestBody ActivityRegisterReqVO reqVO) {
        return success(activityService.register(reqVO));
    }

    @GetMapping("/registration/mine")
    public CommonResult<List<ActivityRegistrationDO>> myRegistrations() {
        return success(activityService.listMyRegistrations());
    }

    @GetMapping("/registration/list")
    @PreAuthorize("@ss.hasPermission('genealogy:activity:query')")
    public CommonResult<List<ActivityRegistrationDO>> registrations(@RequestParam("activityId") Long activityId) {
        return success(activityService.listRegistrations(activityId));
    }

    @GetMapping("/registration/export-excel")
    @PreAuthorize("@ss.hasPermission('genealogy:activity:query')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportReg(@RequestParam("activityId") Long activityId, HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "报名名单.xls", "数据", ActivityRegistrationDO.class,
                activityService.listRegistrations(activityId));
    }

    @PostMapping("/worship/create")
    public CommonResult<Long> worship(@RequestBody WorshipSaveReqVO reqVO) {
        return success(activityService.createWorship(reqVO));
    }

    @DeleteMapping("/worship/delete")
    @PreAuthorize("@ss.hasPermission('genealogy:activity:update')")
    public CommonResult<Boolean> deleteWorship(@RequestParam("id") Long id) {
        activityService.deleteWorship(id);
        return success(true);
    }

    @PutMapping("/worship/pin")
    @PreAuthorize("@ss.hasPermission('genealogy:activity:update')")
    public CommonResult<Boolean> pin(@RequestParam("id") Long id, @RequestParam("pinned") Boolean pinned) {
        activityService.pinWorship(id, pinned);
        return success(true);
    }

    @GetMapping("/worship/list")
    public CommonResult<List<WorshipRecordDO>> worshipList(@RequestParam(value = "activityId", required = false) Long activityId) {
        return success(activityService.listWorship(activityId));
    }
}
