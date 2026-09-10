package chen.genealogy.module.genealogy.controller.app.feed;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.module.genealogy.controller.admin.feed.vo.FeedPageReqVO;
import chen.genealogy.module.genealogy.controller.admin.feed.vo.FeedSaveReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.feed.FeedCommentDO;
import chen.genealogy.module.genealogy.dal.dataobject.feed.FeedDO;
import chen.genealogy.module.genealogy.service.feed.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 动态公告")
@RestController
@RequestMapping("/genealogy/feed")
@Validated
public class AppFeedController {

    @Resource
    private FeedService feedService;

    @PostMapping("/create")
    @Operation(summary = "发布动态")
    public CommonResult<Long> create(@RequestBody FeedSaveReqVO reqVO) {
        return success(feedService.create(reqVO));
    }

    @GetMapping("/page")
    @PermitAll
    @Operation(summary = "动态分页")
    public CommonResult<PageResult<FeedDO>> page(@Valid FeedPageReqVO reqVO) {
        return success(feedService.getPage(reqVO));
    }

    @PostMapping("/like")
    @Operation(summary = "点赞")
    public CommonResult<Boolean> like(@RequestParam("id") Long id) {
        feedService.like(id);
        return success(true);
    }

    @PostMapping("/comment/create")
    @Operation(summary = "评论")
    public CommonResult<Long> comment(@RequestParam("feedId") Long feedId, @RequestParam("content") String content) {
        return success(feedService.comment(feedId, content));
    }

    @GetMapping("/comment/list")
    @Operation(summary = "已审核评论")
    public CommonResult<List<FeedCommentDO>> comments(@RequestParam("feedId") Long feedId) {
        return success(feedService.approvedComments(feedId));
    }
}
