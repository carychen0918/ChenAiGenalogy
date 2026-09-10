package chen.genealogy.module.genealogy.dal.mysql.activity;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.dal.dataobject.activity.WorshipRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WorshipRecordMapper extends BaseMapperX<WorshipRecordDO> {

    default List<WorshipRecordDO> selectListByActivityId(Long activityId) {
        return selectList(new LambdaQueryWrapperX<WorshipRecordDO>()
                .eqIfPresent(WorshipRecordDO::getActivityId, activityId)
                .orderByDesc(WorshipRecordDO::getPinned)
                .orderByDesc(WorshipRecordDO::getId));
    }
}
