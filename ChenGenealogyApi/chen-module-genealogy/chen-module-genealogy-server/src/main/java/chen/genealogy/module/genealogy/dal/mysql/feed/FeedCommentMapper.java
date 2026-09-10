package chen.genealogy.module.genealogy.dal.mysql.feed;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.controller.admin.feed.vo.FeedCommentPageReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.feed.FeedCommentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FeedCommentMapper extends BaseMapperX<FeedCommentDO> {

    default PageResult<FeedCommentDO> selectPage(FeedCommentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FeedCommentDO>()
                .eqIfPresent(FeedCommentDO::getFeedId, reqVO.getFeedId())
                .eqIfPresent(FeedCommentDO::getStatus, reqVO.getStatus())
                .orderByDesc(FeedCommentDO::getId));
    }

    default List<FeedCommentDO> selectApprovedByFeedId(Long feedId) {
        return selectList(new LambdaQueryWrapperX<FeedCommentDO>()
                .eq(FeedCommentDO::getFeedId, feedId)
                .eq(FeedCommentDO::getStatus, 1)
                .orderByDesc(FeedCommentDO::getId));
    }
}
