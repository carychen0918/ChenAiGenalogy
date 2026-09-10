package chen.genealogy.module.genealogy.dal.mysql.feed;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.dal.dataobject.feed.FeedLikeDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FeedLikeMapper extends BaseMapperX<FeedLikeDO> {

    default FeedLikeDO selectByFeedAndUser(Long feedId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<FeedLikeDO>()
                .eq(FeedLikeDO::getFeedId, feedId)
                .eq(FeedLikeDO::getUserId, userId)
                .orderByDesc(FeedLikeDO::getId)
                .last("LIMIT 1"));
    }
}
