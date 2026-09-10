package chen.genealogy.module.genealogy.service.activity;

import cn.hutool.core.collection.CollUtil;
import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.common.util.object.BeanUtils;
import chen.genealogy.module.genealogy.controller.admin.activity.vo.ActivityPageReqVO;
import chen.genealogy.module.genealogy.controller.admin.activity.vo.ActivityRegisterReqVO;
import chen.genealogy.module.genealogy.controller.admin.activity.vo.ActivitySaveReqVO;
import chen.genealogy.module.genealogy.controller.admin.activity.vo.WorshipSaveReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.activity.ActivityDO;
import chen.genealogy.module.genealogy.dal.dataobject.activity.ActivityRegistrationDO;
import chen.genealogy.module.genealogy.dal.dataobject.activity.WorshipRecordDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;
import chen.genealogy.module.genealogy.dal.mysql.activity.ActivityMapper;
import chen.genealogy.module.genealogy.dal.mysql.activity.ActivityRegistrationMapper;
import chen.genealogy.module.genealogy.dal.mysql.activity.WorshipRecordMapper;
import chen.genealogy.module.genealogy.service.member.MemberService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static chen.genealogy.framework.common.exception.util.ServiceExceptionUtil.exception;
import static chen.genealogy.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static chen.genealogy.module.genealogy.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ActivityService {

    private static final Long FAMILY_ID = 1L;

    @Resource
    private ActivityMapper activityMapper;
    @Resource
    private ActivityRegistrationMapper registrationMapper;
    @Resource
    private WorshipRecordMapper worshipRecordMapper;
    @Resource
    private MemberService memberService;

    public Long create(ActivitySaveReqVO reqVO) {
        validateTime(reqVO);
        ActivityDO data = BeanUtils.toBean(reqVO, ActivityDO.class);
        data.setFamilyId(FAMILY_ID);
        if (data.getStatus() == null) {
            data.setStatus(1);
        }
        activityMapper.insert(data);
        return data.getId();
    }

    public void update(ActivitySaveReqVO reqVO) {
        validateTime(reqVO);
        if (activityMapper.selectById(reqVO.getId()) == null) {
            throw exception(ACTIVITY_NOT_EXISTS);
        }
        activityMapper.updateById(BeanUtils.toBean(reqVO, ActivityDO.class));
    }

    public void delete(Long id) {
        activityMapper.deleteById(id);
    }

    public void cancel(Long id) {
        ActivityDO activity = get(id);
        activity.setStatus(3);
        activityMapper.updateById(activity);
    }

    public ActivityDO get(Long id) {
        ActivityDO activity = activityMapper.selectById(id);
        if (activity == null) {
            throw exception(ACTIVITY_NOT_EXISTS);
        }
        activity.setJoinedCount(registrationMapper.selectJoinedCount(id));
        Long userId = getLoginUserId();
        if (userId != null) {
            ActivityRegistrationDO mine = registrationMapper.selectByActivityAndUser(id, userId);
            activity.setMyRegStatus(mine == null ? null : mine.getStatus());
        }
        return activity;
    }

    public List<ActivityRegistrationDO> listMyRegistrations() {
        return registrationMapper.selectByUserId(getLoginUserId());
    }

    public PageResult<ActivityDO> getPage(ActivityPageReqVO reqVO) {
        return activityMapper.selectPage(reqVO);
    }

    public Long register(ActivityRegisterReqVO reqVO) {
        ActivityDO activity = get(reqVO.getActivityId());
        if (activity.getStatus() != null && activity.getStatus() == 3) {
            throw exception(ACTIVITY_CANCELLED);
        }
        if (activity.getDeadline() != null && LocalDateTime.now().isAfter(activity.getDeadline())) {
            throw exception(ACTIVITY_DEADLINE);
        }
        Long userId = getLoginUserId();
        if (registrationMapper.selectByActivityAndUser(activity.getId(), userId) != null) {
            throw exception(ACTIVITY_ALREADY_JOINED);
        }
        long joined = registrationMapper.selectJoinedCount(activity.getId());
        int status = 1;
        if (activity.getMaxCount() != null && joined >= activity.getMaxCount()) {
            status = 2;
        }
        MemberDO me = memberService.getCurrentMember();
        ActivityRegistrationDO reg = new ActivityRegistrationDO();
        reg.setActivityId(activity.getId());
        reg.setUserId(userId);
        reg.setMemberId(me == null ? null : me.getId());
        reg.setUserName(me == null ? null : me.getName());
        reg.setMobile(reqVO.getMobile());
        reg.setPeopleCount(reqVO.getPeopleCount() == null ? 1 : reqVO.getPeopleCount());
        reg.setNeedBus(Boolean.TRUE.equals(reqVO.getNeedBus()));
        reg.setStatus(status);
        registrationMapper.insert(reg);
        return reg.getId();
    }

    public List<ActivityRegistrationDO> listRegistrations(Long activityId) {
        return registrationMapper.selectListByActivityId(activityId);
    }

    public Long createWorship(WorshipSaveReqVO reqVO) {
        Long userId = getLoginUserId();
        if (reqVO.getActivityId() != null && !Boolean.TRUE.equals(reqVO.getOnline())) {
            ActivityRegistrationDO reg = registrationMapper.selectByActivityAndUser(reqVO.getActivityId(), userId);
            if (reg == null || reg.getStatus() != 1) {
                throw exception(WORSHIP_NOT_JOINED);
            }
        }
        if (CollUtil.isNotEmpty(reqVO.getImages()) && reqVO.getImages().size() > 9) {
            reqVO.setImages(reqVO.getImages().subList(0, 9));
        }
        MemberDO me = memberService.getCurrentMember();
        WorshipRecordDO rec = BeanUtils.toBean(reqVO, WorshipRecordDO.class);
        rec.setUserId(userId);
        rec.setUserName(me == null ? null : me.getName());
        rec.setPinned(false);
        worshipRecordMapper.insert(rec);
        return rec.getId();
    }

    public void deleteWorship(Long id) {
        worshipRecordMapper.deleteById(id);
    }

    public void pinWorship(Long id, Boolean pinned) {
        WorshipRecordDO rec = worshipRecordMapper.selectById(id);
        if (rec == null) {
            throw exception(WORSHIP_NOT_EXISTS);
        }
        rec.setPinned(Boolean.TRUE.equals(pinned));
        worshipRecordMapper.updateById(rec);
    }

    public List<WorshipRecordDO> listWorship(Long activityId) {
        return worshipRecordMapper.selectListByActivityId(activityId);
    }

    public void remindTomorrow() {
        LocalDateTime from = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime to = from.plusDays(1);
        List<ActivityDO> list = activityMapper.selectList();
        for (ActivityDO activity : list) {
            if (activity.getStartTime() == null || Boolean.TRUE.equals(activity.getReminded())) {
                continue;
            }
            if (!activity.getStartTime().isBefore(from) && activity.getStartTime().isBefore(to)) {
                activity.setReminded(true);
                activityMapper.updateById(activity);
            }
        }
    }

    private void validateTime(ActivitySaveReqVO reqVO) {
        if (reqVO.getEndTime() != null && reqVO.getStartTime() != null && !reqVO.getEndTime().isAfter(reqVO.getStartTime())) {
            throw exception(ACTIVITY_TIME_INVALID);
        }
        if (reqVO.getDeadline() != null && reqVO.getStartTime() != null && !reqVO.getDeadline().isBefore(reqVO.getStartTime())) {
            throw exception(ACTIVITY_TIME_INVALID);
        }
    }
}
