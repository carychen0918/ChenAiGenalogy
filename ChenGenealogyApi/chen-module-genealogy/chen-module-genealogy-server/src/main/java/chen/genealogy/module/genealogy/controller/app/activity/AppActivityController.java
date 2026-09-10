package chen.genealogy.module.genealogy.controller.app.activity;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.module.genealogy.controller.admin.activity.vo.ActivityPageReqVO;
import chen.genealogy.module.genealogy.controller.admin.activity.vo.ActivityRegisterReqVO;
import chen.genealogy.module.genealogy.controller.admin.activity.vo.WorshipSaveReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.activity.ActivityDO;
import chen.genealogy.module.genealogy.dal.dataobject.activity.ActivityRegistrationDO;
import chen.genealogy.module.genealogy.dal.dataobject.activity.WorshipRecordDO;
import chen.genealogy.module.genealogy.service.activity.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 祭祖活动")
@RestController
@RequestMapping("/genealogy/activity")
@Validated
public class AppActivityController {

    @Resource
    private ActivityService activityService;

    @GetMapping("/get")
    @Operation(summary = "活动详情")
    public CommonResult<ActivityDO> get(@RequestParam("id") Long id) {
        return success(activityService.get(id));
    }

    @GetMapping("/page")
    @Operation(summary = "活动分页")
    public CommonResult<PageResult<ActivityDO>> page(@Valid ActivityPageReqVO reqVO) {
        return success(activityService.getPage(reqVO));
    }

    @PostMapping("/register")
    @Operation(summary = "报名活动")
    public CommonResult<Long> register(@RequestBody ActivityRegisterReqVO reqVO) {
        return success(activityService.register(reqVO));
    }

    @GetMapping("/registration/mine")
    @Operation(summary = "我的报名")
    public CommonResult<List<ActivityRegistrationDO>> myRegistrations() {
        return success(activityService.listMyRegistrations());
    }

    @PostMapping("/worship/create")
    @Operation(summary = "提交祭扫记录")
    public CommonResult<Long> worship(@RequestBody WorshipSaveReqVO reqVO) {
        return success(activityService.createWorship(reqVO));
    }

    @GetMapping("/worship/list")
    @Operation(summary = "祭扫记录")
    public CommonResult<List<WorshipRecordDO>> worshipList(@RequestParam(value = "activityId", required = false) Long activityId) {
        return success(activityService.listWorship(activityId));
    }
}
