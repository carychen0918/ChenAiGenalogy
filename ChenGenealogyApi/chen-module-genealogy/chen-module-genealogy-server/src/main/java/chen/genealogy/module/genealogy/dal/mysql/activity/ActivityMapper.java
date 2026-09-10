package chen.genealogy.module.genealogy.dal.mysql.activity;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.controller.admin.activity.vo.ActivityPageReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.activity.ActivityDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivityMapper extends BaseMapperX<ActivityDO> {

    default PageResult<ActivityDO> selectPage(ActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ActivityDO>()
                .likeIfPresent(ActivityDO::getTitle, reqVO.getTitle())
                .eqIfPresent(ActivityDO::getStatus, reqVO.getStatus())
                .orderByDesc(ActivityDO::getId));
    }
}
