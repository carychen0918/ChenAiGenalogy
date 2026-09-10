package chen.genealogy.module.genealogy.dal.mysql.feed;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.controller.admin.feed.vo.FeedPageReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.feed.FeedDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FeedMapper extends BaseMapperX<FeedDO> {

    default PageResult<FeedDO> selectPage(FeedPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FeedDO>()
                .eqIfPresent(FeedDO::getType, reqVO.getType())
                .eqIfPresent(FeedDO::getStatus, reqVO.getStatus())
                .eqIfPresent(FeedDO::getAuthorUserId, reqVO.getAuthorUserId())
                .likeIfPresent(FeedDO::getTitle, reqVO.getTitle())
                .orderByDesc(FeedDO::getPinned)
                .orderByDesc(FeedDO::getId));
    }
}
