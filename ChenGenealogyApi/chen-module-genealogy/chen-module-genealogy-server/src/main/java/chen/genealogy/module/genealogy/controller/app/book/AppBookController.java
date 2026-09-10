package chen.genealogy.module.genealogy.controller.app.book;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.framework.security.core.util.SecurityFrameworkUtils;
import chen.genealogy.module.genealogy.controller.admin.book.vo.BookMetaRespVO;
import chen.genealogy.module.genealogy.controller.admin.book.vo.BookPageRespVO;
import chen.genealogy.module.genealogy.controller.admin.book.vo.BookSearchRespVO;
import chen.genealogy.module.genealogy.service.book.BookComposeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 谱书")
@RestController
@RequestMapping("/genealogy/book")
@Validated
public class AppBookController {

    @Resource
    private BookComposeService bookComposeService;

    @GetMapping("/meta")
    @PermitAll
    @Operation(summary = "谱书目录与封面摘要")
    public CommonResult<BookMetaRespVO> meta() {
        return success(bookComposeService.getMeta());
    }

    @GetMapping("/page")
    @PermitAll
    @Operation(summary = "谱书单页")
    public CommonResult<BookPageRespVO> page(@RequestParam("pageNo") Integer pageNo) {
        return success(bookComposeService.getPage(pageNo, SecurityFrameworkUtils.getLoginUserId() != null));
    }

    @GetMapping("/search")
    @PermitAll
    @Operation(summary = "定位人、字辈或世代")
    public CommonResult<BookSearchRespVO> search(@RequestParam("keyword") String keyword) {
        return success(bookComposeService.search(keyword, SecurityFrameworkUtils.getLoginUserId() != null));
    }
}
