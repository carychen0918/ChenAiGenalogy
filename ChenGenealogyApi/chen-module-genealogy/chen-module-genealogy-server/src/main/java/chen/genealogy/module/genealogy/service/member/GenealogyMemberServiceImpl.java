package chen.genealogy.module.genealogy.service.member;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.common.util.object.BeanUtils;
import chen.genealogy.module.genealogy.controller.admin.member.vo.*;
import chen.genealogy.module.genealogy.dal.dataobject.generation.GenerationDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.ArchiveApplyDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDeedDO;
import chen.genealogy.module.genealogy.dal.mysql.generation.GenerationMapper;
import chen.genealogy.module.genealogy.dal.mysql.member.ArchiveApplyMapper;
import chen.genealogy.module.genealogy.dal.mysql.member.MemberDeedMapper;
import chen.genealogy.module.genealogy.dal.mysql.member.MemberMapper;
import chen.genealogy.module.genealogy.service.region.AdminRegionService;
import chen.genealogy.module.genealogy.util.RegionAreaUtils;
import chen.genealogy.module.infra.api.config.ConfigApi;
import chen.genealogy.module.system.api.permission.PermissionApi;
import chen.genealogy.module.system.api.permission.RoleApi;
import chen.genealogy.module.system.api.permission.dto.PermissionAssignUserRoleReqDTO;
import chen.genealogy.module.system.api.permission.dto.RoleRespDTO;
import chen.genealogy.module.system.api.user.AdminUserApi;
import chen.genealogy.module.system.api.user.dto.AdminUserCreateReqDTO;
import chen.genealogy.module.system.api.user.dto.AdminUserRespDTO;
import cn.hutool.extra.pinyin.PinyinUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static chen.genealogy.framework.common.exception.util.ServiceExceptionUtil.exception;
import static chen.genealogy.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static chen.genealogy.module.genealogy.enums.ErrorCodeConstants.*;

@Service("genealogyMemberService")
@Validated
public class GenealogyMemberServiceImpl implements MemberService {

    private static final Long DEFAULT_FAMILY_ID = 1L;
    private static final String MEMBER_ROLE_CODE = "genealogy_member";
    private static final String DEFAULT_LOGIN_PASSWORD = "Chen123456";
    private static final String USER_INIT_PASSWORD_KEY = "system.user.init-password";

    @Resource
    private MemberMapper memberMapper;
    @Resource
    private MemberDeedMapper memberDeedMapper;
    @Resource
    private ArchiveApplyMapper archiveApplyMapper;
    @Resource
    private GenerationMapper generationMapper;
    @Resource
    private PermissionApi permissionApi;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private RoleApi roleApi;
    @Resource
    private ConfigApi configApi;
    @Resource
    private AdminRegionService adminRegionService;
    @Resource
    private PedigreeCacheService pedigreeCacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberCreateRespVO createMember(MemberSaveReqVO reqVO) {
        validateMember(reqVO, null);
        MemberDO member = BeanUtils.toBean(reqVO, MemberDO.class);
        fillGeneration(member, reqVO.getGenerationId());
        fillRegion(member);
        member.setFamilyId(DEFAULT_FAMILY_ID);
        member.setAlive(resolveAlive(reqVO));
        memberMapper.insert(member);
        syncSpouse(member.getId(), reqVO.getSpouseIds(), reqVO.getConfirmSpouseConflict());
        AccountBind account = ensureLoginAccount(member, reqVO);
        adminRegionService.syncFromMember(memberMapper.selectById(member.getId()));
        MemberCreateRespVO resp = new MemberCreateRespVO();
        resp.setId(member.getId());
        if (account != null) {
            resp.setUsername(account.username());
            resp.setDefaultPassword(account.password());
        }
        pedigreeCacheService.evictFamilyMembers();
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberCreateRespVO updateMember(MemberSaveReqVO reqVO) {
        validateMemberExists(reqVO.getId());
        validateMember(reqVO, reqVO.getId());
        MemberDO update = BeanUtils.toBean(reqVO, MemberDO.class);
        fillGeneration(update, reqVO.getGenerationId());
        fillRegion(update);
        update.setAlive(resolveAlive(reqVO));
        memberMapper.updateById(update);
        memberMapper.updateLineage(update);
        syncSpouse(reqVO.getId(), reqVO.getSpouseIds(), reqVO.getConfirmSpouseConflict());
        MemberDO latest = memberMapper.selectById(reqVO.getId());
        AccountBind account = ensureLoginAccount(latest, reqVO);
        adminRegionService.syncFromMember(memberMapper.selectById(reqVO.getId()));
        MemberCreateRespVO resp = new MemberCreateRespVO();
        resp.setId(reqVO.getId());
        if (account != null) {
            resp.setUsername(account.username());
            resp.setDefaultPassword(account.password());
        }
        pedigreeCacheService.evictFamilyMembers();
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMember(Long id, Boolean confirm) {
        validateMemberExists(id);
        long count = memberMapper.selectDescendantCount(id);
        if (count > 0 && !Boolean.TRUE.equals(confirm)) {
            throw exception(MEMBER_HAS_DESCENDANTS, count);
        }
        if (count > 0) {
            List<MemberDO> children = memberMapper.selectByFatherId(id);
            for (MemberDO child : children) {
                MemberDO patch = new MemberDO();
                patch.setId(child.getId());
                patch.setFatherId(null);
                memberMapper.updateById(patch);
            }
        }
        MemberDO patch = new MemberDO();
        patch.setId(id);
        patch.setDeletedTime(LocalDateTime.now());
        memberMapper.updateById(patch);
        memberMapper.deleteById(id);
        pedigreeCacheService.evictFamilyMembers();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreMember(Long id) {
        memberMapper.restoreById(id);
        pedigreeCacheService.evictFamilyMembers();
    }

    @Override
    public List<MemberDO> getRecycleList() {
        return memberMapper.selectRecycleList();
    }

    @Override
    public MemberRespVO getMember(Long id) {
        MemberDO member = memberMapper.selectById(id);
        if (member == null) {
            throw exception(MEMBER_NOT_EXISTS);
        }
        return toResp(member, true);
    }

    @Override
    public PageResult<MemberRespVO> getMemberPage(MemberPageReqVO pageReqVO) {
        PageResult<MemberDO> page = memberMapper.selectPage(pageReqVO);
        return new PageResult<>(page.getList().stream().map(m -> toResp(m, false)).collect(Collectors.toList()), page.getTotal());
    }

    @Override
    public List<MemberSimpleVO> getSimpleList() {
        return pedigreeCacheService.getFamilyMembers(DEFAULT_FAMILY_ID).stream()
                .map(this::toSimple)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberRespVO> getTree(Long rootId, Integer up, Integer down) {
        List<MemberRespVO> all = pedigreeCacheService.getFamilyMembers(DEFAULT_FAMILY_ID);
        if (CollUtil.isEmpty(all)) {
            return Collections.emptyList();
        }
        Map<Long, MemberRespVO> map = all.stream().collect(Collectors.toMap(MemberRespVO::getId, m -> m));
        MemberRespVO center = resolveTreeCenter(rootId, map, all);
        if (center == null) {
            return all.stream().map(this::copyAndMask).collect(Collectors.toList());
        }
        int upLevel = up == null ? 30 : up;
        int downLevel = down == null ? 30 : down;
        Set<Long> keep = new HashSet<>();
        keep.add(center.getId());
        MemberRespVO cursor = center;
        for (int i = 0; i < upLevel && cursor != null && cursor.getFatherId() != null; i++) {
            cursor = map.get(cursor.getFatherId());
            if (cursor != null && !isSpouseOnlyMember(cursor)) {
                keep.add(cursor.getId());
            }
        }
        collectDescendants(center.getId(), all, keep, downLevel);
        for (MemberRespVO m : all) {
            if (keep.contains(m.getId()) && CollUtil.isNotEmpty(m.getSpouseIds())) {
                keep.addAll(m.getSpouseIds());
            }
        }
        boolean fold = all.size() > 400 && rootId != null;
        final MemberRespVO root = center;
        return all.stream()
                .filter(m -> !fold || keep.contains(m.getId()) || Objects.equals(m.getFatherId(), root.getId())
                        || Objects.equals(m.getFatherId(), root.getFatherId()))
                .map(this::copyAndMask)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberImportRespVO importMemberList(List<MemberImportExcelVO> list, Boolean updateSupport) {
        if (CollUtil.isEmpty(list)) {
            throw exception(MEMBER_IMPORT_LIST_EMPTY);
        }
        int create = 0, update = 0;
        List<String> failures = new ArrayList<>();
        int row = 1;
        for (MemberImportExcelVO excel : list) {
            row++;
            try {
                if (StrUtil.isBlank(excel.getName())) {
                    failures.add("第" + row + "行：姓名不能为空");
                    continue;
                }
                MemberSaveReqVO req = new MemberSaveReqVO();
                req.setName(excel.getName().trim());
                req.setGender("女".equals(excel.getGender()) ? 2 : 1);
                req.setIntro(excel.getIntro());
                if (excel.getGenerationNo() != null) {
                    GenerationDO gen = generationMapper.selectByFamilyAndNo(DEFAULT_FAMILY_ID, excel.getGenerationNo());
                    if (gen != null) {
                        req.setGenerationId(gen.getId());
                    }
                }
                req.setBirthDate(parseDate(excel.getBirthDate()));
                req.setDeathDate(parseDate(excel.getDeathDate()));
                if (StrUtil.isNotBlank(excel.getFatherName())) {
                    MemberDO father = findByName(excel.getFatherName());
                    if (father != null) {
                        req.setFatherId(father.getId());
                    }
                }
                if (StrUtil.isNotBlank(excel.getMotherName())) {
                    MemberDO mother = findByName(excel.getMotherName());
                    if (mother != null) {
                        req.setMotherId(mother.getId());
                    }
                }
                MemberDO exists = findByName(req.getName());
                if (exists != null && Boolean.TRUE.equals(updateSupport)) {
                    req.setId(exists.getId());
                    updateMember(req);
                    update++;
                } else if (exists == null) {
                    createMember(req);
                    create++;
                } else {
                    failures.add("第" + row + "行：成员已存在");
                }
            } catch (Exception ex) {
                failures.add("第" + row + "行：" + ex.getMessage());
            }
        }
        return MemberImportRespVO.builder().createCount(create).updateCount(update)
                .failureCount(failures.size()).failureMessages(failures).build();
    }

    @Override
    public Long createArchiveApply(ArchiveApplySaveReqVO reqVO) {
        validateMemberExists(reqVO.getMemberId());
        ArchiveApplyDO apply = BeanUtils.toBean(reqVO, ArchiveApplyDO.class);
        apply.setApplicantUserId(getLoginUserId());
        apply.setStatus(0);
        archiveApplyMapper.insert(apply);
        return apply.getId();
    }

    @Override
    public void auditArchiveApply(Long id, Integer status, String reason) {
        ArchiveApplyDO apply = archiveApplyMapper.selectById(id);
        if (apply == null) {
            throw exception(ARCHIVE_APPLY_NOT_EXISTS);
        }
        if (apply.getStatus() != 0) {
            throw exception(ARCHIVE_APPLY_AUDITED);
        }
        if (Objects.equals(status, 2) && StrUtil.isBlank(reason)) {
            throw exception(SCHOLARSHIP_REJECT_REASON_REQUIRED);
        }
        apply.setStatus(status);
        apply.setAuditReason(reason);
        apply.setAuditUserId(getLoginUserId());
        apply.setAuditTime(LocalDateTime.now());
        archiveApplyMapper.updateById(apply);
        if (Objects.equals(status, 1) && StrUtil.isNotBlank(apply.getContent())) {
            MemberDO member = memberMapper.selectById(apply.getMemberId());
            if (member != null) {
                String intro = StrUtil.blankToDefault(member.getIntro(), "") + "\n" + apply.getContent();
                MemberDO patch = new MemberDO();
                patch.setId(member.getId());
                patch.setIntro(intro.trim());
                memberMapper.updateById(patch);
                pedigreeCacheService.evictFamilyMembers();
            }
        }
    }

    @Override
    public PageResult<ArchiveApplyRespVO> getArchiveApplyPage(ArchiveApplyPageReqVO reqVO) {
        PageResult<ArchiveApplyDO> page = archiveApplyMapper.selectPage(reqVO);
        List<ArchiveApplyRespVO> list = page.getList().stream().map(a -> {
            ArchiveApplyRespVO vo = BeanUtils.toBean(a, ArchiveApplyRespVO.class);
            MemberDO member = memberMapper.selectById(a.getMemberId());
            if (member != null) {
                vo.setMemberName(member.getName());
            }
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public MemberDO getCurrentMember() {
        Long userId = getLoginUserId();
        if (userId == null) {
            return null;
        }
        return memberMapper.selectByUserId(userId);
    }

    @Override
    public void updateMyPhotos(List<String> photoUrls) {
        MemberDO me = requireCurrentMember();
        MemberDO patch = new MemberDO();
        patch.setId(me.getId());
        patch.setPhotoUrls(photoUrls == null ? new ArrayList<>() : photoUrls);
        memberMapper.updateById(patch);
        pedigreeCacheService.evictFamilyMembers();
    }

    @Override
    public Long saveMyDeed(MemberDeedSaveReqVO reqVO) {
        reqVO.setMemberId(requireCurrentMember().getId());
        return saveDeed(reqVO);
    }

    @Override
    public void deleteMyDeed(Long id) {
        MemberDO me = requireCurrentMember();
        MemberDeedDO old = memberDeedMapper.selectById(id);
        if (old == null || !Objects.equals(old.getMemberId(), me.getId())) {
            throw exception(MEMBER_DEED_NOT_EXISTS);
        }
        memberDeedMapper.deleteById(id);
    }

    @Override
    public Long saveDeed(MemberDeedSaveReqVO reqVO) {
        if (reqVO.getMemberId() == null) {
            throw exception(MEMBER_NOT_EXISTS);
        }
        validateMemberExists(reqVO.getMemberId());
        if (reqVO.getId() == null) {
            MemberDeedDO deed = BeanUtils.toBean(reqVO, MemberDeedDO.class);
            deed.setMemberId(reqVO.getMemberId());
            memberDeedMapper.insert(deed);
            return deed.getId();
        }
        MemberDeedDO old = memberDeedMapper.selectById(reqVO.getId());
        if (old == null || !Objects.equals(old.getMemberId(), reqVO.getMemberId())) {
            throw exception(MEMBER_DEED_NOT_EXISTS);
        }
        MemberDeedDO update = BeanUtils.toBean(reqVO, MemberDeedDO.class);
        update.setMemberId(reqVO.getMemberId());
        memberDeedMapper.updateById(update);
        return old.getId();
    }

    @Override
    public void updateArchive(MemberArchiveSaveReqVO reqVO) {
        validateMemberExists(reqVO.getId());
        MemberDO patch = new MemberDO();
        patch.setId(reqVO.getId());
        patch.setIntro(reqVO.getIntro());
        patch.setPhotoUrls(reqVO.getPhotoUrls() == null ? new ArrayList<>() : reqVO.getPhotoUrls());
        memberMapper.updateById(patch);
        pedigreeCacheService.evictFamilyMembers();
    }

    @Override
    public void deleteDeed(Long id) {
        MemberDeedDO old = memberDeedMapper.selectById(id);
        if (old == null) {
            throw exception(MEMBER_DEED_NOT_EXISTS);
        }
        memberDeedMapper.deleteById(id);
    }

    private MemberDO requireCurrentMember() {
        MemberDO me = getCurrentMember();
        if (me == null) {
            throw exception(MEMBER_NOT_CERTIFIED);
        }
        return me;
    }

    private void validateMember(MemberSaveReqVO reqVO, Long selfId) {
        if (isSpouseOnlyArchive(reqVO)) {
            reqVO.setGenerationId(null);
            reqVO.setFatherId(null);
        } else if (reqVO.getGenerationId() == null) {
            throw exception(MEMBER_GENERATION_REQUIRED);
        }
        if (reqVO.getBirthDate() != null && reqVO.getDeathDate() != null
                && reqVO.getBirthDate().isAfter(reqVO.getDeathDate())) {
            throw exception(MEMBER_DATE_INVALID);
        }
        if (reqVO.getFatherId() != null) {
            List<MemberDO> siblings = memberMapper.selectByFatherId(reqVO.getFatherId());
            for (MemberDO s : siblings) {
                if (!Objects.equals(s.getId(), selfId) && s.getName().equals(reqVO.getName())
                        && Objects.equals(s.getGender(), reqVO.getGender())) {
                    throw exception(MEMBER_NAME_DUPLICATE);
                }
            }
            if (Objects.equals(reqVO.getFatherId(), selfId) || isDescendant(selfId, reqVO.getFatherId())) {
                throw exception(MEMBER_RELATION_INVALID);
            }
        }
        if (reqVO.getMotherId() != null && (Objects.equals(reqVO.getMotherId(), selfId)
                || isDescendant(selfId, reqVO.getMotherId()))) {
            throw exception(MEMBER_RELATION_INVALID);
        }
    }

    private boolean isDescendant(Long ancestorId, Long maybeDescendantId) {
        if (ancestorId == null || maybeDescendantId == null) {
            return false;
        }
        MemberDO cursor = memberMapper.selectById(maybeDescendantId);
        int guard = 0;
        while (cursor != null && cursor.getFatherId() != null && guard++ < 32) {
            if (Objects.equals(cursor.getFatherId(), ancestorId)) {
                return true;
            }
            cursor = memberMapper.selectById(cursor.getFatherId());
        }
        return false;
    }

    private void syncSpouse(Long id, List<Long> spouseIds, Boolean confirm) {
        if (CollUtil.isEmpty(spouseIds)) {
            return;
        }
        for (Long spouseId : spouseIds) {
            MemberDO spouse = memberMapper.selectById(spouseId);
            if (spouse == null) {
                continue;
            }
            List<Long> other = spouse.getSpouseIds() == null ? new ArrayList<>() : new ArrayList<>(spouse.getSpouseIds());
            boolean conflict = other.stream().anyMatch(s -> !Objects.equals(s, id));
            if (conflict && !Boolean.TRUE.equals(confirm)) {
                throw exception(MEMBER_SPOUSE_CONFLICT);
            }
            if (!other.contains(id)) {
                other.add(id);
                MemberDO patch = new MemberDO();
                patch.setId(spouseId);
                patch.setSpouseIds(other);
                memberMapper.updateById(patch);
            }
        }
    }

    private AccountBind ensureLoginAccount(MemberDO member, MemberSaveReqVO reqVO) {
        if (member == null || member.getUserId() != null) {
            return null;
        }
        if (member.getDeathDate() != null || Boolean.FALSE.equals(member.getAlive())) {
            return null;
        }
        if (Boolean.FALSE.equals(reqVO.getCreateLoginAccount())) {
            return null;
        }
        String password = resolveDefaultPassword();
        String username = resolveUsername(reqVO.getLoginUsername(), member.getName(), member.getId());
        AdminUserCreateReqDTO userReq = new AdminUserCreateReqDTO();
        userReq.setUsername(username);
        userReq.setNickname(member.getName());
        userReq.setPassword(password);
        userReq.setSex(member.getGender());
        userReq.setAvatar(member.getAvatar());
        Long userId = adminUserApi.createUser(userReq).getCheckedData();
        RoleRespDTO role = roleApi.getRoleByCode(MEMBER_ROLE_CODE).getCheckedData();
        if (role == null || role.getId() == null) {
            throw exception(MEMBER_LOGIN_ROLE_MISSING);
        }
        PermissionAssignUserRoleReqDTO assign = new PermissionAssignUserRoleReqDTO();
        assign.setUserId(userId);
        assign.setRoleIds(Collections.singleton(role.getId()));
        permissionApi.assignUserRole(assign).checkError();
        MemberDO patch = new MemberDO();
        patch.setId(member.getId());
        patch.setUserId(userId);
        memberMapper.updateById(patch);
        member.setUserId(userId);
        return new AccountBind(username, password, userId);
    }

    private String resolveDefaultPassword() {
        try {
            String configured = configApi.getConfigValueByKey(USER_INIT_PASSWORD_KEY).getCheckedData();
            if (StrUtil.isNotBlank(configured) && configured.length() >= 4 && configured.length() <= 16) {
                return configured;
            }
        } catch (Exception ignored) {
            // 使用族谱默认密码
        }
        return DEFAULT_LOGIN_PASSWORD;
    }

    private String resolveUsername(String preferred, String name, Long memberId) {
        String base = StrUtil.trim(preferred);
        if (StrUtil.isBlank(base)) {
            base = toPinyinUsername(name);
        }
        base = base.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (base.length() < 4) {
            base = ("chen" + (memberId == null ? "user" : memberId)).toLowerCase();
        }
        if (base.length() > 24) {
            base = base.substring(0, 24);
        }
        if (!base.matches("^[a-z0-9]{4,30}$")) {
            throw exception(MEMBER_LOGIN_USERNAME_INVALID);
        }
        String candidate = base;
        int i = 1;
        while (adminUserApi.getUserByUsername(candidate).getCheckedData() != null) {
            String suffix = String.valueOf(i++);
            candidate = base.substring(0, Math.min(base.length(), 30 - suffix.length())) + suffix;
            if (i > 9999) {
                throw exception(MEMBER_LOGIN_USERNAME_INVALID);
            }
        }
        return candidate;
    }

    private String toPinyinUsername(String name) {
        if (StrUtil.isBlank(name)) {
            return "chenuser";
        }
        try {
            return PinyinUtil.getPinyin(name, "").replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        } catch (Exception ignored) {
            return "chenuser";
        }
    }

    private record AccountBind(String username, String password, Long userId) {
    }

    private void fillRegion(MemberDO member) {
        Integer leaf = member.getCountyId() != null ? member.getCountyId()
                : (member.getCityId() != null ? member.getCityId() : member.getProvinceId());
        RegionAreaUtils.Region region = RegionAreaUtils.of(leaf);
        if (member.getProvinceId() == null) {
            member.setProvinceId(region.getProvinceId());
        }
        if (member.getCityId() == null) {
            member.setCityId(region.getCityId());
        }
        if (member.getCountyId() == null) {
            member.setCountyId(region.getCountyId());
        }
    }

    private Boolean resolveAlive(MemberSaveReqVO reqVO) {
        if (reqVO.getAlive() != null) {
            return reqVO.getAlive();
        }
        return true;
    }

    private boolean isSpouseOnlyArchive(MemberSaveReqVO reqVO) {
        if (Boolean.TRUE.equals(reqVO.getSpouseOnly())) {
            return true;
        }
        return Objects.equals(reqVO.getGender(), 2) && reqVO.getFatherId() == null;
    }

    private void fillGeneration(MemberDO member, Long generationId) {
        if (generationId == null) {
            member.setGenerationId(null);
            member.setGenerationNo(null);
            return;
        }
        GenerationDO gen = generationMapper.selectById(generationId);
        if (gen != null) {
            member.setGenerationId(gen.getId());
            member.setGenerationNo(gen.getGenerationNo());
        }
    }

    private MemberDO validateMemberExists(Long id) {
        MemberDO member = memberMapper.selectById(id);
        if (member == null) {
            throw exception(MEMBER_NOT_EXISTS);
        }
        return member;
    }

    private MemberRespVO toResp(MemberDO member, boolean detail) {
        MemberRespVO vo = BeanUtils.toBean(member, MemberRespVO.class);
        if (member.getGenerationId() != null) {
            GenerationDO gen = generationMapper.selectById(member.getGenerationId());
            if (gen != null) {
                vo.setGenerationWord(gen.getWord());
                vo.setGenerationHouse(gen.getHouse());
                vo.setGenerationNationalSource(gen.getNationalSource());
            }
        }
        if (member.getFatherId() != null) {
            MemberDO father = memberMapper.selectById(member.getFatherId());
            if (father != null) {
                vo.setFatherName(father.getName());
            }
        }
        if (member.getMotherId() != null) {
            MemberDO mother = memberMapper.selectById(member.getMotherId());
            if (mother != null) {
                vo.setMotherName(mother.getName());
            }
        }
        if (CollUtil.isNotEmpty(member.getSpouseIds())) {
            vo.setSpouseNames(member.getSpouseIds().stream().map(sid -> {
                MemberDO s = memberMapper.selectById(sid);
                return s == null ? null : s.getName();
            }).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        if (detail) {
            vo.setDeeds(BeanUtils.toBean(memberDeedMapper.selectListByMemberId(member.getId()), MemberDeedRespVO.class));
            vo.setChildren(memberMapper.selectByFatherId(member.getId()).stream().map(this::toSimple).collect(Collectors.toList()));
            if (member.getFatherId() != null) {
                vo.setSiblings(memberMapper.selectByFatherId(member.getFatherId()).stream()
                        .filter(s -> !s.getId().equals(member.getId()))
                        .map(this::toSimple)
                        .collect(Collectors.toList()));
            }
        }
        vo.setDescendantCount((int) memberMapper.selectDescendantCount(member.getId()));
        if (member.getUserId() != null) {
            AdminUserRespDTO user = adminUserApi.getUser(member.getUserId()).getCheckedData();
            if (user != null) {
                vo.setLoginUsername(user.getUsername());
            }
        }
        RegionAreaUtils.Region region = RegionAreaUtils.of(member.getProvinceId(), member.getCityId(), member.getCountyId());
        vo.setRegionName(region.getRegionName());
        maskSensitive(vo);
        return vo;
    }

    private void maskSensitive(MemberRespVO vo) {
        Long userId = getLoginUserId();
        boolean admin = userId != null && Boolean.TRUE.equals(
                permissionApi.hasAnyPermissions(userId, "genealogy:member:update").getCheckedData());
        if (!admin) {
            vo.setMobile(null);
            vo.setIdCard(null);
        }
    }

    private MemberSimpleVO toSimple(MemberDO m) {
        MemberSimpleVO vo = new MemberSimpleVO();
        vo.setId(m.getId());
        vo.setName(m.getName());
        vo.setGender(m.getGender());
        vo.setGenerationNo(m.getGenerationNo());
        vo.setAvatar(m.getAvatar());
        vo.setAlive(m.getAlive());
        vo.setFatherId(m.getFatherId());
        vo.setBirthDate(m.getBirthDate());
        vo.setDeathDate(m.getDeathDate());
        if (m.getGenerationId() != null) {
            GenerationDO gen = generationMapper.selectById(m.getGenerationId());
            if (gen != null) {
                vo.setGenerationWord(gen.getWord());
                vo.setGenerationHouse(gen.getHouse());
                vo.setGenerationNationalSource(gen.getNationalSource());
            }
        }
        return vo;
    }

    private MemberSimpleVO toSimple(MemberRespVO m) {
        MemberSimpleVO vo = new MemberSimpleVO();
        vo.setId(m.getId());
        vo.setName(m.getName());
        vo.setGender(m.getGender());
        vo.setGenerationNo(m.getGenerationNo());
        vo.setGenerationWord(m.getGenerationWord());
        vo.setGenerationHouse(m.getGenerationHouse());
        vo.setGenerationNationalSource(m.getGenerationNationalSource());
        vo.setAvatar(m.getAvatar());
        vo.setAlive(m.getAlive());
        vo.setFatherId(m.getFatherId());
        vo.setFatherName(m.getFatherName());
        vo.setBirthDate(m.getBirthDate());
        vo.setDeathDate(m.getDeathDate());
        return vo;
    }

    private MemberRespVO copyAndMask(MemberRespVO src) {
        MemberRespVO vo = BeanUtils.toBean(src, MemberRespVO.class);
        maskSensitive(vo);
        return vo;
    }

    private boolean isSpouseOnlyMember(MemberRespVO m) {
        return m != null
                && Objects.equals(m.getGender(), 2)
                && m.getFatherId() == null
                && m.getGenerationId() == null
                && m.getGenerationNo() == null;
    }

    private MemberRespVO resolveTreeCenter(Long rootId, Map<Long, MemberRespVO> map, List<MemberRespVO> all) {
        MemberRespVO center = rootId != null ? map.get(rootId) : null;
        if (isSpouseOnlyMember(center)) {
            MemberRespVO partner = firstSpousePartner(center, map, all);
            if (partner != null) {
                center = partner;
            }
        }
        if (center == null) {
            center = pickProgenitor(all);
        }
        return center;
    }

    private MemberRespVO firstSpousePartner(MemberRespVO member, Map<Long, MemberRespVO> map, List<MemberRespVO> all) {
        if (member == null) {
            return null;
        }
        if (CollUtil.isNotEmpty(member.getSpouseIds())) {
            for (Long spouseId : member.getSpouseIds()) {
                MemberRespVO partner = map.get(spouseId);
                if (partner != null && !isSpouseOnlyMember(partner)) {
                    return partner;
                }
            }
        }
        for (MemberRespVO other : all) {
            if (CollUtil.isNotEmpty(other.getSpouseIds()) && other.getSpouseIds().contains(member.getId())
                    && !isSpouseOnlyMember(other)) {
                return other;
            }
        }
        return null;
    }

    private MemberRespVO pickProgenitor(List<MemberRespVO> all) {
        return all.stream()
                .filter(m -> !isSpouseOnlyMember(m))
                .min(Comparator
                        .comparing((MemberRespVO m) -> m.getFatherId() != null)
                        .thenComparing(m -> m.getGenerationNo() == null ? Integer.MAX_VALUE : m.getGenerationNo())
                        .thenComparing(MemberRespVO::getId))
                .orElse(all.get(0));
    }

    private void collectDescendants(Long id, List<MemberRespVO> all, Set<Long> keep, int level) {
        if (level <= 0) {
            return;
        }
        for (MemberRespVO m : all) {
            if (isSpouseOnlyMember(m)) {
                continue;
            }
            if (Objects.equals(m.getFatherId(), id) || Objects.equals(m.getMotherId(), id)) {
                keep.add(m.getId());
                collectDescendants(m.getId(), all, keep, level - 1);
            }
        }
    }

    private MemberDO findByName(String name) {
        return memberMapper.selectOne(MemberDO::getName, name);
    }

    private LocalDateTime parseDate(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        try {
            if (text.length() == 4) {
                return LocalDate.parse(text + "-01-01").atStartOfDay();
            }
            return LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }
}
