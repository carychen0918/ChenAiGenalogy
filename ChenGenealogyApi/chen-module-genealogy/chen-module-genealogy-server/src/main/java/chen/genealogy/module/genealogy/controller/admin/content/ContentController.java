package chen.genealogy.module.genealogy.controller.admin.content;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.common.util.object.BeanUtils;
import chen.genealogy.module.genealogy.controller.admin.activity.vo.TombSiteSaveReqVO;
import chen.genealogy.module.genealogy.controller.admin.content.vo.*;
import chen.genealogy.module.genealogy.dal.dataobject.activity.TombSiteDO;
import chen.genealogy.module.genealogy.dal.dataobject.content.AncestorDeedDO;
import chen.genealogy.module.genealogy.dal.dataobject.content.CultureGuideDO;
import chen.genealogy.module.genealogy.dal.dataobject.content.MigrationNodeDO;
import chen.genealogy.module.genealogy.dal.dataobject.family.FamilyDO;
import chen.genealogy.module.genealogy.dal.dataobject.generation.GenerationDO;
import chen.genealogy.module.genealogy.service.content.ContentService;
import jakarta.annotation.security.PermitAll;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 族谱内容")
@RestController
@RequestMapping("/genealogy/content")
@Validated
public class ContentController {

    @Resource
    private ContentService contentService;

    @GetMapping("/family/get")
    @PermitAll
    @Operation(summary = "家族信息")
    public CommonResult<FamilyDO> getFamily() {
        return success(contentService.getFamily());
    }

    @PutMapping("/family/update")
    @PreAuthorize("@ss.hasPermission('genealogy:config:update')")
    public CommonResult<Boolean> updateFamily(@Valid @RequestBody FamilySaveReqVO reqVO) {
        contentService.updateFamily(reqVO);
        return success(true);
    }

    @GetMapping("/generation/list")
    @PermitAll
    public CommonResult<List<GenerationRespVO>> generationList() {
        return success(BeanUtils.toBean(contentService.getGenerationList(), GenerationRespVO.class));
    }

    @GetMapping("/generation/poem-table")
    @PermitAll
    @Operation(summary = "字辈派语对照表")
    public CommonResult<List<GenerationPoemRowVO>> generationPoemTable() {
        return success(contentService.getPoemTable());
    }

    @GetMapping("/generation/recommend")
    @PermitAll
    @Operation(summary = "根据父亲世代推荐下一辈字辈")
    public CommonResult<String> recommend(@RequestParam("fatherGenerationNo") Integer fatherGenerationNo) {
        return success(contentService.recommendName(fatherGenerationNo));
    }

    @PostMapping("/generation/create")
    @PreAuthorize("@ss.hasPermission('genealogy:content:update')")
    public CommonResult<Long> createGeneration(@Valid @RequestBody GenerationSaveReqVO reqVO) {
        return success(contentService.createGeneration(reqVO));
    }

    @PutMapping("/generation/update")
    @PreAuthorize("@ss.hasPermission('genealogy:content:update')")
    public CommonResult<Boolean> updateGeneration(@Valid @RequestBody GenerationSaveReqVO reqVO) {
        contentService.updateGeneration(reqVO);
        return success(true);
    }

    @DeleteMapping("/generation/delete")
    @PreAuthorize("@ss.hasPermission('genealogy:content:update')")
    public CommonResult<Boolean> deleteGeneration(@RequestParam("id") Long id) {
        contentService.deleteGeneration(id);
        return success(true);
    }

    @GetMapping("/migration/list")
    @PermitAll
    public CommonResult<List<MigrationNodeDO>> migrationList() {
        return success(contentService.getMigrationList());
    }

    @PostMapping("/migration/create")
    @PreAuthorize("@ss.hasPermission('genealogy:content:update')")
    public CommonResult<Long> createMigration(@RequestBody MigrationNodeSaveReqVO reqVO) {
        return success(contentService.createMigration(reqVO));
    }

    @PutMapping("/migration/update")
    @PreAuthorize("@ss.hasPermission('genealogy:content:update')")
    public CommonResult<Boolean> updateMigration(@RequestBody MigrationNodeSaveReqVO reqVO) {
        contentService.updateMigration(reqVO);
        return success(true);
    }

    @DeleteMapping("/migration/delete")
    @PreAuthorize("@ss.hasPermission('genealogy:content:update')")
    public CommonResult<Boolean> deleteMigration(@RequestParam("id") Long id) {
        contentService.deleteMigration(id);
        return success(true);
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

    @PostMapping("/deed/create")
    @PreAuthorize("@ss.hasPermission('genealogy:content:update')")
    public CommonResult<Long> createDeed(@RequestBody AncestorDeedSaveReqVO reqVO) {
        return success(contentService.createAncestorDeed(reqVO));
    }

    @PutMapping("/deed/update")
    @PreAuthorize("@ss.hasPermission('genealogy:content:update')")
    public CommonResult<Boolean> updateDeed(@RequestBody AncestorDeedSaveReqVO reqVO) {
        contentService.updateAncestorDeed(reqVO);
        return success(true);
    }

    @DeleteMapping("/deed/delete")
    @PreAuthorize("@ss.hasPermission('genealogy:content:update')")
    public CommonResult<Boolean> deleteDeed(@RequestParam("id") Long id) {
        contentService.deleteAncestorDeed(id);
        return success(true);
    }

    @GetMapping("/culture/list")
    @PermitAll
    public CommonResult<List<CultureGuideDO>> cultureList() {
        return success(contentService.getCultureList());
    }

    @PostMapping("/culture/create")
    @PreAuthorize("@ss.hasPermission('genealogy:content:update')")
    public CommonResult<Long> createCulture(@RequestBody CultureGuideSaveReqVO reqVO) {
        return success(contentService.createCulture(reqVO));
    }

    @PutMapping("/culture/update")
    @PreAuthorize("@ss.hasPermission('genealogy:content:update')")
    public CommonResult<Boolean> updateCulture(@RequestBody CultureGuideSaveReqVO reqVO) {
        contentService.updateCulture(reqVO);
        return success(true);
    }

    @DeleteMapping("/culture/delete")
    @PreAuthorize("@ss.hasPermission('genealogy:content:update')")
    public CommonResult<Boolean> deleteCulture(@RequestParam("id") Long id) {
        contentService.deleteCulture(id);
        return success(true);
    }

    @GetMapping("/tomb/get")
    public CommonResult<TombSiteDO> getTomb() {
        return success(contentService.getTomb());
    }

    @PutMapping("/tomb/save")
    @PreAuthorize("@ss.hasPermission('genealogy:config:update')")
    public CommonResult<Boolean> saveTomb(@RequestBody TombSiteSaveReqVO reqVO) {
        contentService.saveTomb(reqVO);
        return success(true);
    }
}
