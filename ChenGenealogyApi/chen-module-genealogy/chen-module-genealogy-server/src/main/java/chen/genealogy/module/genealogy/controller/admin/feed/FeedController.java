package chen.genealogy.module.genealogy.controller.admin.feed;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.module.genealogy.controller.admin.feed.vo.FeedCommentPageReqVO;
import chen.genealogy.module.genealogy.controller.admin.feed.vo.FeedPageReqVO;
import chen.genealogy.module.genealogy.controller.admin.feed.vo.FeedSaveReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.feed.FeedCommentDO;
import chen.genealogy.module.genealogy.dal.dataobject.feed.FeedDO;
import chen.genealogy.module.genealogy.service.feed.FeedService;
import jakarta.annotation.security.PermitAll;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 动态公告")
@RestController
@RequestMapping("/genealogy/feed")
@Validated
public class FeedController {

    @Resource
    private FeedService feedService;

    @PostMapping("/create")
    public CommonResult<Long> create(@RequestBody FeedSaveReqVO reqVO) {
        return success(feedService.create(reqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('genealogy:feed:create')")
    public CommonResult<Boolean> update(@RequestBody FeedSaveReqVO reqVO) {
        feedService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('genealogy:feed:create')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        feedService.delete(id);
        return success(true);
    }

    @PutMapping("/audit")
    @PreAuthorize("@ss.hasPermission('genealogy:feed:audit')")
    public CommonResult<Boolean> audit(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        feedService.audit(id, status);
        return success(true);
    }

    @PutMapping("/pin")
    @PreAuthorize("@ss.hasPermission('genealogy:feed:create')")
    public CommonResult<Boolean> pin(@RequestParam("id") Long id, @RequestParam("pinned") Boolean pinned) {
        feedService.pin(id, pinned);
        return success(true);
    }

    @GetMapping("/page")
    @PermitAll
    public CommonResult<PageResult<FeedDO>> page(@Valid FeedPageReqVO reqVO) {
        return success(feedService.getPage(reqVO));
    }

    @PostMapping("/like")
    public CommonResult<Boolean> like(@RequestParam("id") Long id) {
        feedService.like(id);
        return success(true);
    }

    @PostMapping("/comment/create")
    public CommonResult<Long> comment(@RequestParam("feedId") Long feedId, @RequestParam("content") String content) {
        return success(feedService.comment(feedId, content));
    }

    @PutMapping("/comment/audit")
    @PreAuthorize("@ss.hasPermission('genealogy:feed:audit')")
    public CommonResult<Boolean> auditComment(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        feedService.auditComment(id, status);
        return success(true);
    }

    @GetMapping("/comment/page")
    @PreAuthorize("@ss.hasPermission('genealogy:feed:query')")
    public CommonResult<PageResult<FeedCommentDO>> commentPage(@Valid FeedCommentPageReqVO reqVO) {
        return success(feedService.commentPage(reqVO));
    }

    @GetMapping("/comment/list")
    public CommonResult<List<FeedCommentDO>> comments(@RequestParam("feedId") Long feedId) {
        return success(feedService.approvedComments(feedId));
    }
}
