package chen.genealogy.module.genealogy.controller.app.showcase;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.module.genealogy.controller.admin.member.vo.MemberSimpleVO;
import chen.genealogy.module.genealogy.controller.admin.showcase.vo.MiniFamilyVO;
import chen.genealogy.module.genealogy.controller.admin.showcase.vo.RelationHopVO;
import chen.genealogy.module.genealogy.controller.admin.showcase.vo.ShowcaseHomeRespVO;
import chen.genealogy.module.genealogy.service.showcase.ShowcaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 族谱客厅")
@RestController
@RequestMapping("/genealogy/showcase")
@Validated
public class AppShowcaseController {

    @Resource
    private ShowcaseService showcaseService;

    @GetMapping("/home")
    @PermitAll
    @Operation(summary = "客厅首页")
    public CommonResult<ShowcaseHomeRespVO> home() {
        return success(showcaseService.home());
    }

    @GetMapping("/gallery")
    @PermitAll
    public CommonResult<List<ShowcaseHomeRespVO.GalleryItem>> gallery(@RequestParam(value = "limit", required = false) Integer limit) {
        return success(showcaseService.gallery(limit == null ? 40 : limit));
    }

    @GetMapping("/calendar")
    @PermitAll
    public CommonResult<List<ShowcaseHomeRespVO.CalendarItem>> calendar(@RequestParam(value = "year", required = false) Integer year,
                                                                      @RequestParam(value = "month", required = false) Integer month) {
        return success(showcaseService.calendar(year, month));
    }

    @GetMapping("/deeds")
    @PermitAll
    public CommonResult<List<ShowcaseHomeRespVO.DeedCard>> deeds() {
        return success(showcaseService.deeds());
    }

    @GetMapping("/scholarship-wall")
    @PermitAll
    public CommonResult<List<ShowcaseHomeRespVO.WallItem>> scholarshipWall() {
        return success(showcaseService.scholarshipWall());
    }

    @GetMapping("/search")
    @PermitAll
    public CommonResult<List<MemberSimpleVO>> search(@RequestParam("keyword") String keyword) {
        return success(showcaseService.search(keyword));
    }

    @GetMapping("/relation")
    public CommonResult<List<RelationHopVO>> relation(@RequestParam("fromId") Long fromId,
                                                     @RequestParam("toId") Long toId) {
        return success(showcaseService.relation(fromId, toId));
    }

    @GetMapping("/mini-family")
    @PermitAll
    public CommonResult<MiniFamilyVO> miniFamily(@RequestParam("memberId") Long memberId) {
        return success(showcaseService.miniFamily(memberId));
    }
}
