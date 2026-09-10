package chen.genealogy.module.genealogy.service.scholarship;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.common.util.object.BeanUtils;
import chen.genealogy.framework.ip.core.utils.AreaUtils;
import chen.genealogy.module.genealogy.controller.admin.scholarship.vo.*;
import chen.genealogy.module.genealogy.dal.dataobject.generation.GenerationDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;
import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipApplicationDO;
import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipAuditLogDO;
import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipConfigDO;
import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipDisbursementDO;
import chen.genealogy.module.genealogy.dal.mysql.generation.GenerationMapper;
import chen.genealogy.module.genealogy.dal.mysql.member.MemberMapper;
import chen.genealogy.module.genealogy.dal.mysql.scholarship.ScholarshipApplicationMapper;
import chen.genealogy.module.genealogy.dal.mysql.scholarship.ScholarshipAuditLogMapper;
import chen.genealogy.module.genealogy.dal.mysql.scholarship.ScholarshipConfigMapper;
import chen.genealogy.module.genealogy.dal.mysql.scholarship.ScholarshipDisbursementMapper;
import chen.genealogy.module.genealogy.enums.ScholarshipAuditLevelEnum;
import chen.genealogy.module.genealogy.service.member.MemberService;
import chen.genealogy.module.genealogy.service.region.AdminRegionService;
import chen.genealogy.module.genealogy.util.RegionAreaUtils;
import chen.genealogy.module.system.api.permission.PermissionApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static chen.genealogy.framework.common.exception.util.ServiceExceptionUtil.exception;
import static chen.genealogy.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static chen.genealogy.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;
import static chen.genealogy.module.genealogy.enums.ErrorCodeConstants.*;
import static chen.genealogy.module.genealogy.enums.ScholarshipAuditLevelEnum.*;
import static chen.genealogy.module.genealogy.enums.ScholarshipStatusEnum.*;

@Service
@Validated
public class ScholarshipService {

    private static final Long FAMILY_ID = 1L;

    @Resource
    private ScholarshipApplicationMapper applicationMapper;
    @Resource
    private ScholarshipConfigMapper scholarshipConfigMapper;
    @Resource
    private ScholarshipDisbursementMapper disbursementMapper;
    @Resource
    private ScholarshipAuditLogMapper auditLogMapper;
    @Resource
    private MemberService memberService;
    @Resource
    private MemberMapper memberMapper;
    @Resource
    private GenerationMapper generationMapper;
    @Resource
    private PermissionApi permissionApi;
    @Resource
    private AdminRegionService adminRegionService;

    public ScholarshipConfigRespVO getConfig(Integer year) {
        int y = year == null ? Year.now().getValue() : year;
        ScholarshipConfigDO config = scholarshipConfigMapper.selectByYear(FAMILY_ID, y);
        if (config == null) {
            config = scholarshipConfigMapper.selectLatest(FAMILY_ID);
        }
        if (config == null) {
            return null;
        }
        ScholarshipConfigRespVO vo = BeanUtils.toBean(config, ScholarshipConfigRespVO.class);
        LocalDateTime now = LocalDateTime.now();
        vo.setOpen(now.isAfter(config.getWindowStart()) && now.isBefore(config.getWindowEnd()));
        return vo;
    }

    public void saveConfig(ScholarshipConfigSaveReqVO reqVO) {
        ScholarshipConfigDO exists = reqVO.getYear() == null ? null : scholarshipConfigMapper.selectByYear(FAMILY_ID, reqVO.getYear());
        ScholarshipConfigDO data = BeanUtils.toBean(reqVO, ScholarshipConfigDO.class);
        data.setFamilyId(FAMILY_ID);
        if (exists == null && reqVO.getId() == null) {
            scholarshipConfigMapper.insert(data);
        } else {
            data.setId(exists != null ? exists.getId() : reqVO.getId());
            scholarshipConfigMapper.updateById(data);
        }
    }

    public Long saveDraftOrSubmit(ScholarshipSaveReqVO reqVO, boolean submit) {
        MemberDO member = memberService.getCurrentMember();
        if (member == null) {
            throw exception(MEMBER_NOT_CERTIFIED);
        }
        int year = Year.now().getValue();
        ScholarshipConfigDO config = scholarshipConfigMapper.selectByYear(FAMILY_ID, year);
        if (config == null) {
            config = scholarshipConfigMapper.selectLatest(FAMILY_ID);
        }
        if (submit) {
            if (config == null) {
                throw exception(SCHOLARSHIP_CONFIG_NOT_EXISTS);
            }
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(config.getWindowStart()) || now.isAfter(config.getWindowEnd())) {
                throw exception(SCHOLARSHIP_WINDOW_CLOSED,
                        LocalDateTimeUtil.format(config.getWindowStart(), "yyyy-MM-dd"),
                        LocalDateTimeUtil.format(config.getWindowEnd(), "yyyy-MM-dd"));
            }
            validateMaterials(reqVO);
        }
        ScholarshipApplicationDO exists = applicationMapper.selectByMemberAndYear(member.getId(), year);
        if (exists != null && reqVO.getId() == null && !canEdit(exists.getStatus())) {
            throw exception(SCHOLARSHIP_DUPLICATE, exists.getApplyNo());
        }
        ScholarshipApplicationDO data = BeanUtils.toBean(reqVO, ScholarshipApplicationDO.class);
        data.setFamilyId(FAMILY_ID);
        data.setMemberId(member.getId());
        data.setUserId(getLoginUserId());
        data.setYear(year);
        snapshotRegion(data, member);
        if (submit) {
            startPipeline(data, member);
            data.setSubmitTime(LocalDateTime.now());
        } else {
            data.setStatus(DRAFT.getStatus());
        }
        if (reqVO.getId() == null) {
            data.setApplyNo(nextApplyNo(year));
            applicationMapper.insert(data);
            return data.getId();
        }
        ScholarshipApplicationDO old = applicationMapper.selectById(reqVO.getId());
        if (old == null) {
            throw exception(SCHOLARSHIP_NOT_EXISTS);
        }
        if (!canEdit(old.getStatus())) {
            throw exception(SCHOLARSHIP_CANNOT_EDIT);
        }
        data.setApplyNo(old.getApplyNo());
        if (submit && Objects.equals(old.getStatus(), NEED_MATERIAL.getStatus())
                && StrUtil.isNotBlank(old.getCurrentAuditLevel())) {
            data.setAuditPipeline(old.getAuditPipeline());
            data.setCurrentAuditLevel(old.getCurrentAuditLevel());
            ScholarshipAuditLevelEnum level = ScholarshipAuditLevelEnum.of(old.getCurrentAuditLevel());
            data.setStatus(level != null ? level.getPendingStatus() : PENDING_FAMILY.getStatus());
        }
        applicationMapper.updateById(data);
        return data.getId();
    }

    public void withdraw(Long id) {
        ScholarshipApplicationDO app = get(id);
        assertOwner(app);
        if (!isPendingAudit(app.getStatus()) && app.getStatus() != NEED_MATERIAL.getStatus()) {
            throw exception(SCHOLARSHIP_STATUS_ERROR);
        }
        app.setStatus(WITHDRAWN.getStatus());
        applicationMapper.updateById(app);
    }

    /** 逐级审核：1通过 2驳回 3补材料 */
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, Integer result, String opinion) {
        ScholarshipApplicationDO app = get(id);
        if (!isPendingAudit(app.getStatus())) {
            throw exception(SCHOLARSHIP_STATUS_ERROR);
        }
        Long userId = getLoginUserId();
        if (!adminRegionService.canAuditLevel(userId, app.getCurrentAuditLevel(),
                app.getProvinceId(), app.getCityId(), app.getCountyId())) {
            throw exception(SCHOLARSHIP_AUDIT_NO_PERMISSION);
        }
        if (Objects.equals(result, 2) && StrUtil.isBlank(opinion)) {
            throw exception(SCHOLARSHIP_REJECT_REASON_REQUIRED);
        }
        String level = StrUtil.blankToDefault(app.getCurrentAuditLevel(), FAMILY.getCode());
        writeAuditLog(app.getId(), level, result, opinion, userId);
        if (Objects.equals(result, 1)) {
            List<String> pipeline = app.getAuditPipeline() == null ? List.of(FAMILY.getCode()) : app.getAuditPipeline();
            int idx = pipeline.indexOf(level);
            if (idx >= 0 && idx < pipeline.size() - 1) {
                String next = pipeline.get(idx + 1);
                app.setCurrentAuditLevel(next);
                ScholarshipAuditLevelEnum nextLevel = ScholarshipAuditLevelEnum.of(next);
                app.setStatus(nextLevel != null ? nextLevel.getPendingStatus() : PENDING_FAMILY.getStatus());
            } else {
                app.setStatus(PENDING_DISBURSE.getStatus());
                app.setCurrentAuditLevel(null);
            }
        } else if (Objects.equals(result, 3)) {
            app.setStatus(NEED_MATERIAL.getStatus());
            app.setSupplementRemark(opinion);
        } else {
            app.setStatus(REJECTED.getStatus());
            app.setRejectReason(opinion);
        }
        applicationMapper.updateById(app);
    }

    public void firstAudit(Long id, Integer result, String opinion) {
        audit(id, result, opinion);
    }

    public void batchFirstAudit(List<Long> ids, Integer result, String opinion) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        ids.forEach(id -> audit(id, result, opinion));
    }

    public void finalAudit(Long id, Integer result, String opinion) {
        audit(id, result, opinion);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long disburse(DisbursementSaveReqVO reqVO) {
        if (reqVO.getAmount() == null) {
            throw exception(SCHOLARSHIP_AMOUNT_REQUIRED);
        }
        ScholarshipApplicationDO app = get(reqVO.getApplicationId());
        if (app.getStatus() != PENDING_DISBURSE.getStatus()) {
            throw exception(SCHOLARSHIP_STATUS_ERROR);
        }
        ScholarshipDisbursementDO rec = BeanUtils.toBean(reqVO, ScholarshipDisbursementDO.class);
        disbursementMapper.insert(rec);
        app.setStatus(DISBURSED.getStatus());
        applicationMapper.updateById(app);
        return rec.getId();
    }

    public PageResult<ScholarshipRespVO> getPage(ScholarshipPageReqVO reqVO) {
        Long userId = getLoginUserId();
        if (reqVO.getUserId() == null) {
            AdminRegionService.ScholarshipPageScope scope = new AdminRegionService.ScholarshipPageScope();
            adminRegionService.applyPageScope(userId, scope);
            if (scope.isEmpty()) {
                return PageResult.empty();
            }
            if (reqVO.getProvinceId() == null) {
                reqVO.setProvinceId(scope.getProvinceId());
            }
            if (reqVO.getCityId() == null) {
                reqVO.setCityId(scope.getCityId());
            }
            if (reqVO.getCountyId() == null) {
                reqVO.setCountyId(scope.getCountyId());
            }
        }
        PageResult<ScholarshipApplicationDO> page = applicationMapper.selectPage(reqVO);
        List<ScholarshipRespVO> list = page.getList().stream().map(this::toResp).collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    public ScholarshipRespVO getDetail(Long id) {
        ScholarshipApplicationDO app = get(id);
        Long userId = getLoginUserId();
        boolean auditor = userId != null && Boolean.TRUE.equals(
                permissionApi.hasAnyPermissions(userId, "genealogy:scholarship:query").getCheckedData());
        if (!auditor) {
            assertOwner(app);
        }
        return toResp(app);
    }

    public PageResult<ScholarshipDisbursementDO> getDisbursePage(DisbursementPageReqVO reqVO) {
        return disbursementMapper.selectPage(reqVO);
    }

    public Map<String, Object> stats(Integer year) {
        int y = year == null ? Year.now().getValue() : year;
        List<ScholarshipApplicationDO> list = applicationMapper.selectList(ScholarshipApplicationDO::getYear, y);
        long apply = list.size();
        long passed = list.stream().filter(a -> Objects.equals(a.getStatus(), PENDING_DISBURSE.getStatus())
                || Objects.equals(a.getStatus(), DISBURSED.getStatus())).count();
        List<ScholarshipDisbursementDO> disburses = disbursementMapper.selectList();
        BigDecimal amount = disburses.stream().map(ScholarshipDisbursementDO::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<Integer, Long> typeDist = list.stream().filter(a -> a.getType() != null)
                .collect(Collectors.groupingBy(ScholarshipApplicationDO::getType, Collectors.counting()));
        Map<String, Object> map = new HashMap<>();
        map.put("year", y);
        map.put("applyCount", apply);
        map.put("passedCount", passed);
        map.put("disburseAmount", amount);
        map.put("typeDist", typeDist);
        return map;
    }

    private void startPipeline(ScholarshipApplicationDO data, MemberDO member) {
        snapshotRegion(data, member);
        if (data.getProvinceId() == null && data.getCityId() == null && data.getCountyId() == null) {
            throw exception(SCHOLARSHIP_REGION_REQUIRED);
        }
        List<String> pipeline = adminRegionService.buildPipeline(data.getProvinceId(), data.getCityId(), data.getCountyId());
        data.setAuditPipeline(pipeline);
        String first = pipeline.get(0);
        data.setCurrentAuditLevel(first);
        ScholarshipAuditLevelEnum level = ScholarshipAuditLevelEnum.of(first);
        data.setStatus(level != null ? level.getPendingStatus() : PENDING_FAMILY.getStatus());
    }

    private void snapshotRegion(ScholarshipApplicationDO data, MemberDO member) {
        RegionAreaUtils.Region region = RegionAreaUtils.of(member.getProvinceId(), member.getCityId(), member.getCountyId());
        data.setProvinceId(member.getProvinceId() != null ? member.getProvinceId() : region.getProvinceId());
        data.setCityId(member.getCityId() != null ? member.getCityId() : region.getCityId());
        data.setCountyId(member.getCountyId() != null ? member.getCountyId() : region.getCountyId());
        Integer leaf = data.getCountyId() != null ? data.getCountyId()
                : (data.getCityId() != null ? data.getCityId() : data.getProvinceId());
        data.setRegionName(StrUtil.blankToDefault(region.getRegionName(), AreaUtils.format(leaf, " / ")));
    }

    private void writeAuditLog(Long applicationId, String level, Integer result, String opinion, Long userId) {
        ScholarshipAuditLogDO log = new ScholarshipAuditLogDO();
        log.setApplicationId(applicationId);
        log.setAuditLevel(level);
        log.setResult(result);
        log.setOpinion(opinion);
        log.setAuditorUserId(userId);
        log.setAuditorName(getLoginUserNickname());
        auditLogMapper.insert(log);
    }

    private ScholarshipRespVO toResp(ScholarshipApplicationDO app) {
        ScholarshipRespVO vo = BeanUtils.toBean(app, ScholarshipRespVO.class);
        MemberDO member = memberMapper.selectById(app.getMemberId());
        if (member != null) {
            vo.setMemberName(member.getName());
            vo.setGender(member.getGender());
            vo.setMemberAvatar(member.getAvatar());
            vo.setMemberMobile(member.getMobile());
            vo.setGenerationNo(member.getGenerationNo());
            if (member.getGenerationId() != null) {
                GenerationDO gen = generationMapper.selectById(member.getGenerationId());
                if (gen != null) {
                    vo.setGenerationWord(gen.getWord());
                }
            }
            if (StrUtil.isBlank(vo.getRegionName())) {
                vo.setRegionName(RegionAreaUtils.of(member.getProvinceId(), member.getCityId(), member.getCountyId()).getRegionName());
            }
        }
        List<ScholarshipAuditLogDO> logs = auditLogMapper.selectListByApplicationId(app.getId());
        vo.setAuditLogs(logs.stream().map(item -> {
            ScholarshipRespVO.AuditLogVO logVo = new ScholarshipRespVO.AuditLogVO();
            logVo.setAuditLevel(item.getAuditLevel());
            logVo.setResult(item.getResult());
            logVo.setOpinion(item.getOpinion());
            logVo.setAuditorName(item.getAuditorName());
            logVo.setCreateTime(item.getCreateTime());
            return logVo;
        }).collect(Collectors.toList()));
        vo.setAuditSteps(buildSteps(app, logs));
        vo.setCanAudit(isPendingAudit(app.getStatus())
                && adminRegionService.canAuditLevel(getLoginUserId(), app.getCurrentAuditLevel(),
                app.getProvinceId(), app.getCityId(), app.getCountyId()));
        return vo;
    }

    private List<ScholarshipRespVO.AuditStepVO> buildSteps(ScholarshipApplicationDO app, List<ScholarshipAuditLogDO> logs) {
        List<String> pipeline = app.getAuditPipeline() == null ? List.of() : app.getAuditPipeline();
        List<ScholarshipRespVO.AuditStepVO> steps = new ArrayList<>();
        for (ScholarshipAuditLevelEnum level : List.of(COUNTY, CITY, PROVINCE, FAMILY)) {
            if (level == FAMILY && !pipeline.contains(FAMILY.getCode())) {
                continue;
            }
            ScholarshipRespVO.AuditStepVO step = new ScholarshipRespVO.AuditStepVO();
            step.setLevel(level.getCode());
            step.setName(level.getName());
            ScholarshipAuditLogDO log = logs.stream()
                    .filter(item -> level.getCode().equals(item.getAuditLevel()))
                    .reduce((first, second) -> second)
                    .orElse(null);
            boolean inPipeline = pipeline.contains(level.getCode());
            if (!inPipeline) {
                step.setState("skipped");
                step.setRemark("该级暂无管理员，已自动跳过");
            } else if (log != null) {
                if (Objects.equals(log.getResult(), 1)) {
                    step.setState("passed");
                } else if (Objects.equals(log.getResult(), 3)) {
                    step.setState("supplement");
                } else {
                    step.setState("rejected");
                }
                step.setOpinion(log.getOpinion());
                step.setAuditorName(log.getAuditorName());
                step.setAuditTime(log.getCreateTime());
            } else if (level.getCode().equals(app.getCurrentAuditLevel()) && isPendingAudit(app.getStatus())) {
                step.setState("pending");
                step.setRemark("等待审核");
            } else if (Objects.equals(app.getStatus(), NEED_MATERIAL.getStatus())
                    && level.getCode().equals(app.getCurrentAuditLevel())) {
                step.setState("supplement");
                step.setRemark("待补充材料");
            } else {
                step.setState("waiting");
                step.setRemark("尚未到达该级");
            }
            steps.add(step);
        }
        return steps;
    }

    private boolean isPendingAudit(Integer status) {
        return Objects.equals(status, PENDING_FIRST.getStatus())
                || Objects.equals(status, PENDING_COUNTY.getStatus())
                || Objects.equals(status, PENDING_CITY.getStatus())
                || Objects.equals(status, PENDING_PROVINCE.getStatus())
                || Objects.equals(status, PENDING_FAMILY.getStatus());
    }

    private ScholarshipApplicationDO get(Long id) {
        ScholarshipApplicationDO app = applicationMapper.selectById(id);
        if (app == null) {
            throw exception(SCHOLARSHIP_NOT_EXISTS);
        }
        return app;
    }

    private void assertOwner(ScholarshipApplicationDO app) {
        if (!Objects.equals(app.getUserId(), getLoginUserId())) {
            throw exception(SCHOLARSHIP_STATUS_ERROR);
        }
    }

    private boolean canEdit(Integer status) {
        return Objects.equals(status, DRAFT.getStatus()) || Objects.equals(status, WITHDRAWN.getStatus())
                || Objects.equals(status, NEED_MATERIAL.getStatus()) || Objects.equals(status, REJECTED.getStatus());
    }

    private void validateMaterials(ScholarshipSaveReqVO reqVO) {
        if (CollUtil.isEmpty(reqVO.getMaterials())) {
            throw exception(SCHOLARSHIP_MATERIAL_REQUIRED);
        }
        boolean enroll = reqVO.getMaterials().stream().anyMatch(m -> "ENROLL".equals(m.getType()) && StrUtil.isNotBlank(m.getUrl()));
        boolean transcript = reqVO.getMaterials().stream().anyMatch(m -> "TRANSCRIPT".equals(m.getType()) && StrUtil.isNotBlank(m.getUrl()));
        if (!enroll || !transcript) {
            throw exception(SCHOLARSHIP_MATERIAL_REQUIRED);
        }
        if (("大一".equals(reqVO.getGrade()) || "研一".equals(reqVO.getGrade()))
                && reqVO.getMaterials().stream().noneMatch(m -> "ADMISSION".equals(m.getType()) && StrUtil.isNotBlank(m.getUrl()))) {
            throw exception(SCHOLARSHIP_MATERIAL_REQUIRED);
        }
    }

    private String nextApplyNo(int year) {
        long count = applicationMapper.selectCount(ScholarshipApplicationDO::getYear, year) + 1;
        return String.format("JD%d-%03d", year, count);
    }
}
