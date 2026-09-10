package chen.genealogy.module.genealogy.dal.mysql.activity;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.dal.dataobject.activity.ActivityRegistrationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ActivityRegistrationMapper extends BaseMapperX<ActivityRegistrationDO> {

    default List<ActivityRegistrationDO> selectListByActivityId(Long activityId) {
        return selectList(new LambdaQueryWrapperX<ActivityRegistrationDO>()
                .eq(ActivityRegistrationDO::getActivityId, activityId)
                .orderByAsc(ActivityRegistrationDO::getId));
    }

    default ActivityRegistrationDO selectByActivityAndUser(Long activityId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ActivityRegistrationDO>()
                .eq(ActivityRegistrationDO::getActivityId, activityId)
                .eq(ActivityRegistrationDO::getUserId, userId)
                .in(ActivityRegistrationDO::getStatus, 1, 2)
                .orderByDesc(ActivityRegistrationDO::getId)
                .last("LIMIT 1"));
    }

    default Long selectJoinedCount(Long activityId) {
        return selectCount(new LambdaQueryWrapperX<ActivityRegistrationDO>()
                .eq(ActivityRegistrationDO::getActivityId, activityId)
                .eq(ActivityRegistrationDO::getStatus, 1));
    }

    default List<ActivityRegistrationDO> selectByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<ActivityRegistrationDO>()
                .eq(ActivityRegistrationDO::getUserId, userId)
                .orderByDesc(ActivityRegistrationDO::getId));
    }
}
