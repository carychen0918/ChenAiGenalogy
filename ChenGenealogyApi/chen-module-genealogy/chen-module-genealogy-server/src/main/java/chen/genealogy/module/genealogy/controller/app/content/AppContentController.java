package chen.genealogy.module.genealogy.controller.app.content;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.common.util.object.BeanUtils;
import chen.genealogy.module.genealogy.controller.admin.content.vo.AncestorDeedPageReqVO;
import chen.genealogy.module.genealogy.controller.admin.content.vo.GenerationRespVO;
import chen.genealogy.module.genealogy.dal.dataobject.activity.TombSiteDO;
import chen.genealogy.module.genealogy.dal.dataobject.content.AncestorDeedDO;
import chen.genealogy.module.genealogy.dal.dataobject.content.CultureGuideDO;
import chen.genealogy.module.genealogy.dal.dataobject.content.MigrationNodeDO;
import chen.genealogy.module.genealogy.dal.dataobject.family.FamilyDO;
import chen.genealogy.module.genealogy.service.content.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 族谱内容")
@RestController
@RequestMapping("/genealogy/content")
@Validated
public class AppContentController {

    @Resource
    private ContentService contentService;

    @GetMapping("/family/get")
    @PermitAll
    @Operation(summary = "家族信息")
    public CommonResult<FamilyDO> getFamily() {
        return success(contentService.getFamily());
    }

    @GetMapping("/generation/list")
    @PermitAll
    public CommonResult<List<GenerationRespVO>> generationList() {
        return success(BeanUtils.toBean(contentService.getGenerationList(), GenerationRespVO.class));
    }

    @GetMapping("/generation/recommend")
    @PermitAll
    @Operation(summary = "根据父亲世代推荐下一辈字辈")
    public CommonResult<String> recommend(@RequestParam("fatherGenerationNo") Integer fatherGenerationNo) {
        return success(contentService.recommendName(fatherGenerationNo));
    }

    @GetMapping("/migration/list")
    @PermitAll
    public CommonResult<List<MigrationNodeDO>> migrationList() {
        return success(contentService.getMigrationList());
    }

    @GetMapping("/deed/page")
    @PermitAll
    public CommonResult<PageResult<AncestorDeedDO>> deedPage(@Valid AncestorDeedPageReqVO reqVO) {
        return success(contentService.getAncestorDeedPage(reqVO));
    }

    @GetMapping("/deed/get")
    @PermitAll
    public CommonResult<AncestorDeedDO> getDeed(@RequestParam("id") Long id) {
        return success(contentService.getAncestorDeed(id));
    }

    @GetMapping("/culture/list")
    @PermitAll
    public CommonResult<List<CultureGuideDO>> cultureList() {
        return success(contentService.getCultureList());
    }

    @GetMapping("/tomb/get")
    @Operation(summary = "坟地导航")
    public CommonResult<TombSiteDO> getTomb() {
        return success(contentService.getTomb());
    }
}
