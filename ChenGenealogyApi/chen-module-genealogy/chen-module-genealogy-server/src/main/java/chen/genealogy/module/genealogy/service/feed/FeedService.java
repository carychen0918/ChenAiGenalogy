package chen.genealogy.module.genealogy.service.feed;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.common.util.object.BeanUtils;
import chen.genealogy.module.genealogy.controller.admin.feed.vo.FeedCommentPageReqVO;
import chen.genealogy.module.genealogy.controller.admin.feed.vo.FeedPageReqVO;
import chen.genealogy.module.genealogy.controller.admin.feed.vo.FeedSaveReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.feed.FeedCommentDO;
import chen.genealogy.module.genealogy.dal.dataobject.feed.FeedDO;
import chen.genealogy.module.genealogy.dal.dataobject.feed.FeedLikeDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;
import chen.genealogy.module.genealogy.dal.mysql.feed.FeedCommentMapper;
import chen.genealogy.module.genealogy.dal.mysql.feed.FeedLikeMapper;
import chen.genealogy.module.genealogy.dal.mysql.feed.FeedMapper;
import chen.genealogy.module.genealogy.service.member.ChenMemberService;
import chen.genealogy.module.system.api.permission.PermissionApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static chen.genealogy.framework.common.exception.util.ServiceExceptionUtil.exception;
import static chen.genealogy.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static chen.genealogy.module.genealogy.enums.ErrorCodeConstants.FEED_NOT_EXISTS;

@Service
@Validated
public class FeedService {

    private static final Long FAMILY_ID = 1L;

    @Resource
    private FeedMapper feedMapper;
    @Resource
    private FeedCommentMapper commentMapper;
    @Resource
    private FeedLikeMapper likeMapper;
    @Resource
    private ChenMemberService memberService;
    @Resource
    private PermissionApi permissionApi;

    public Long create(FeedSaveReqVO reqVO) {
        Long userId = getLoginUserId();
        boolean admin = Boolean.TRUE.equals(permissionApi.hasAnyPermissions(userId, "genealogy:feed:create").getCheckedData());
        FeedDO feed = BeanUtils.toBean(reqVO, FeedDO.class);
        feed.setFamilyId(FAMILY_ID);
        feed.setAuthorUserId(userId);
        MemberDO me = memberService.getCurrentMember();
        feed.setAuthorName(me == null ? "族人" : me.getName());
        feed.setLikeCount(0);
        feed.setCommentCount(0);
        if (reqVO.getType() != null && reqVO.getType() == 1) {
            if (!admin) {
                feed.setType(2);
                feed.setStatus(0);
            } else {
                feed.setStatus(1);
            }
        } else {
            feed.setStatus(admin ? 1 : 0);
        }
        if (feed.getPinned() == null) {
            feed.setPinned(false);
        }
        feedMapper.insert(feed);
        return feed.getId();
    }

    public void update(FeedSaveReqVO reqVO) {
        feedMapper.updateById(BeanUtils.toBean(reqVO, FeedDO.class));
    }

    public void delete(Long id) {
        feedMapper.deleteById(id);
    }

    public void audit(Long id, Integer status) {
        FeedDO feed = feedMapper.selectById(id);
        if (feed == null) {
            throw exception(FEED_NOT_EXISTS);
        }
        feed.setStatus(status);
        feedMapper.updateById(feed);
    }

    public void pin(Long id, Boolean pinned) {
        FeedDO feed = feedMapper.selectById(id);
        if (feed == null) {
            throw exception(FEED_NOT_EXISTS);
        }
        feed.setPinned(Boolean.TRUE.equals(pinned));
        feedMapper.updateById(feed);
    }

    public PageResult<FeedDO> getPage(FeedPageReqVO reqVO) {
        Long userId = getLoginUserId();
        if (userId == null) {
            reqVO.setStatus(1);
            reqVO.setAuthorUserId(null);
            return feedMapper.selectPage(reqVO);
        }
        boolean admin = Boolean.TRUE.equals(permissionApi.hasAnyPermissions(userId, "genealogy:feed:query").getCheckedData());
        if (!admin) {
            if (reqVO.getAuthorUserId() == null) {
                reqVO.setStatus(1);
            } else {
                reqVO.setAuthorUserId(userId);
            }
        }
        return feedMapper.selectPage(reqVO);
    }

    public void like(Long feedId) {
        Long userId = getLoginUserId();
        FeedLikeDO exists = likeMapper.selectByFeedAndUser(feedId, userId);
        FeedDO feed = feedMapper.selectById(feedId);
        if (feed == null) {
            throw exception(FEED_NOT_EXISTS);
        }
        if (exists != null) {
            likeMapper.deleteById(exists.getId());
            feed.setLikeCount(Math.max(0, feed.getLikeCount() - 1));
        } else {
            FeedLikeDO like = new FeedLikeDO();
            like.setFeedId(feedId);
            like.setUserId(userId);
            likeMapper.insert(like);
            feed.setLikeCount(feed.getLikeCount() == null ? 1 : feed.getLikeCount() + 1);
        }
        feedMapper.updateById(feed);
    }

    public Long comment(Long feedId, String content) {
        Long userId = getLoginUserId();
        boolean admin = Boolean.TRUE.equals(permissionApi.hasAnyPermissions(userId, "genealogy:feed:audit").getCheckedData());
        MemberDO me = memberService.getCurrentMember();
        FeedCommentDO c = new FeedCommentDO();
        c.setFeedId(feedId);
        c.setUserId(userId);
        c.setUserName(me == null ? "族人" : me.getName());
        c.setContent(content);
        c.setStatus(admin ? 1 : 0);
        commentMapper.insert(c);
        if (admin) {
            FeedDO feed = feedMapper.selectById(feedId);
            if (feed != null) {
                feed.setCommentCount(feed.getCommentCount() == null ? 1 : feed.getCommentCount() + 1);
                feedMapper.updateById(feed);
            }
        }
        return c.getId();
    }

    public void auditComment(Long id, Integer status) {
        FeedCommentDO c = commentMapper.selectById(id);
        if (c == null) {
            return;
        }
        c.setStatus(status);
        commentMapper.updateById(c);
        if (status != null && status == 1) {
            FeedDO feed = feedMapper.selectById(c.getFeedId());
            if (feed != null) {
                feed.setCommentCount(feed.getCommentCount() == null ? 1 : feed.getCommentCount() + 1);
                feedMapper.updateById(feed);
            }
        }
    }

    public PageResult<FeedCommentDO> commentPage(FeedCommentPageReqVO reqVO) {
        return commentMapper.selectPage(reqVO);
    }

    public List<FeedCommentDO> approvedComments(Long feedId) {
        return commentMapper.selectApprovedByFeedId(feedId);
    }
}
