package chen.genealogy.module.genealogy.controller.admin.dashboard;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.module.genealogy.dal.dataobject.feed.FeedDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.ArchiveApplyDO;
import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipApplicationDO;
import chen.genealogy.module.genealogy.dal.mysql.activity.ActivityMapper;
import chen.genealogy.module.genealogy.dal.mysql.feed.FeedMapper;
import chen.genealogy.module.genealogy.dal.mysql.member.ArchiveApplyMapper;
import chen.genealogy.module.genealogy.dal.mysql.member.MemberMapper;
import chen.genealogy.module.genealogy.dal.mysql.scholarship.ScholarshipApplicationMapper;
import chen.genealogy.module.genealogy.enums.ScholarshipStatusEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 族谱工作台")
@RestController
@RequestMapping("/genealogy/dashboard")
@Validated
public class DashboardController {

    @Resource
    private MemberMapper memberMapper;
    @Resource
    private ScholarshipApplicationMapper applicationMapper;
    @Resource
    private ActivityMapper activityMapper;
    @Resource
    private FeedMapper feedMapper;
    @Resource
    private ArchiveApplyMapper archiveApplyMapper;

    @GetMapping("/summary")
    @Operation(summary = "工作台统计")
    @PreAuthorize("@ss.hasPermission('genealogy:dashboard:query')")
    public CommonResult<Map<String, Object>> summary() {
        Map<String, Object> map = new HashMap<>();
        map.put("memberCount", memberMapper.selectCount());
        map.put("pendingFirst", applicationMapper.selectCount(ScholarshipApplicationDO::getStatus, ScholarshipStatusEnum.PENDING_FIRST.getStatus()));
        map.put("pendingFinal", applicationMapper.selectCount(ScholarshipApplicationDO::getStatus, ScholarshipStatusEnum.FIRST_PASSED.getStatus()));
        map.put("pendingDisburse", applicationMapper.selectCount(ScholarshipApplicationDO::getStatus, ScholarshipStatusEnum.PENDING_DISBURSE.getStatus()));
        map.put("pendingFeed", feedMapper.selectCount(FeedDO::getStatus, 0));
        map.put("pendingArchive", archiveApplyMapper.selectCount(ArchiveApplyDO::getStatus, 0));
        map.put("activityCount", activityMapper.selectCount());
        return success(map);
    }
}
