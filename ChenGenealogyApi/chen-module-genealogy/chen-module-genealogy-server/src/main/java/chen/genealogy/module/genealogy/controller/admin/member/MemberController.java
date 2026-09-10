package chen.genealogy.module.genealogy.controller.admin.member;

import chen.genealogy.framework.apilog.core.annotation.ApiAccessLog;
import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.framework.common.pojo.PageParam;
import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.common.util.object.BeanUtils;
import chen.genealogy.framework.excel.core.util.ExcelUtils;
import chen.genealogy.module.genealogy.controller.admin.member.vo.*;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;
import chen.genealogy.module.genealogy.service.member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static chen.genealogy.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 家族成员")
@RestController
@RequestMapping("/genealogy/member")
@Validated
public class MemberController {

    @Resource
    private MemberService memberService;

    @PostMapping("/create")
    @Operation(summary = "新增成员")
    @PreAuthorize("@ss.hasPermission('genealogy:member:create')")
    public CommonResult<MemberCreateRespVO> createMember(@Valid @RequestBody MemberSaveReqVO reqVO) {
        return success(memberService.createMember(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新成员")
    @PreAuthorize("@ss.hasPermission('genealogy:member:update')")
    public CommonResult<MemberCreateRespVO> updateMember(@Valid @RequestBody MemberSaveReqVO reqVO) {
        return success(memberService.updateMember(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除成员（进入回收站）")
    @PreAuthorize("@ss.hasPermission('genealogy:member:delete')")
    public CommonResult<Boolean> deleteMember(@RequestParam("id") Long id,
                                              @RequestParam(value = "confirm", required = false) Boolean confirm) {
        memberService.deleteMember(id, confirm);
        return success(true);
    }

    @PutMapping("/restore")
    @Operation(summary = "从回收站恢复")
    @PreAuthorize("@ss.hasPermission('genealogy:member:update')")
    public CommonResult<Boolean> restore(@RequestParam("id") Long id) {
        memberService.restoreMember(id);
        return success(true);
    }

    @GetMapping("/recycle-list")
    @Operation(summary = "回收站列表")
    @PreAuthorize("@ss.hasPermission('genealogy:member:query')")
    public CommonResult<List<MemberRespVO>> recycleList() {
        return success(BeanUtils.toBean(memberService.getRecycleList(), MemberRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "成员详情")
    public CommonResult<MemberRespVO> getMember(@RequestParam("id") Long id) {
        return success(memberService.getMember(id));
    }

    @GetMapping("/page")
    @Operation(summary = "成员分页")
    @PreAuthorize("@ss.hasPermission('genealogy:member:query')")
    public CommonResult<PageResult<MemberRespVO>> getMemberPage(@Valid MemberPageReqVO pageReqVO) {
        return success(memberService.getMemberPage(pageReqVO));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "成员精简列表")
    public CommonResult<List<MemberSimpleVO>> simpleList() {
        return success(memberService.getSimpleList());
    }

    @GetMapping("/tree")
    @Operation(summary = "谱系树")
    public CommonResult<List<MemberRespVO>> tree(@RequestParam(value = "rootId", required = false) Long rootId,
                                                 @RequestParam(value = "up", required = false) Integer up,
                                                 @RequestParam(value = "down", required = false) Integer down) {
        return success(memberService.getTree(rootId, up, down));
    }

    @GetMapping("/me")
    @Operation(summary = "当前登录用户对应成员")
    public CommonResult<MemberRespVO> me() {
        MemberDO me = memberService.getCurrentMember();
        return success(me == null ? null : memberService.getMember(me.getId()));
    }

    @PutMapping("/me/photos")
    @Operation(summary = "更新当前登录人照片集")
    public CommonResult<Boolean> updateMyPhotos(@RequestBody MemberPhotoSaveReqVO reqVO) {
        memberService.updateMyPhotos(reqVO.getPhotoUrls());
        return success(true);
    }

    @PostMapping("/me/deed")
    @Operation(summary = "新增当前登录人事迹荣誉")
    public CommonResult<Long> createMyDeed(@Valid @RequestBody MemberDeedSaveReqVO reqVO) {
        reqVO.setId(null);
        return success(memberService.saveMyDeed(reqVO));
    }

    @PutMapping("/me/deed")
    @Operation(summary = "修改当前登录人事迹荣誉")
    public CommonResult<Boolean> updateMyDeed(@Valid @RequestBody MemberDeedSaveReqVO reqVO) {
        memberService.saveMyDeed(reqVO);
        return success(true);
    }

    @DeleteMapping("/me/deed")
    @Operation(summary = "删除当前登录人事迹荣誉")
    public CommonResult<Boolean> deleteMyDeed(@RequestParam("id") Long id) {
        memberService.deleteMyDeed(id);
        return success(true);
    }

    @PostMapping("/deed")
    @Operation(summary = "新增成员事迹荣誉")
    @PreAuthorize("@ss.hasPermission('genealogy:member:update')")
    public CommonResult<Long> createDeed(@Valid @RequestBody MemberDeedSaveReqVO reqVO) {
        reqVO.setId(null);
        return success(memberService.saveDeed(reqVO));
    }

    @PutMapping("/deed")
    @Operation(summary = "修改成员事迹荣誉")
    @PreAuthorize("@ss.hasPermission('genealogy:member:update')")
    public CommonResult<Boolean> updateDeed(@Valid @RequestBody MemberDeedSaveReqVO reqVO) {
        memberService.saveDeed(reqVO);
        return success(true);
    }

    @DeleteMapping("/deed")
    @Operation(summary = "删除成员事迹荣誉")
    @PreAuthorize("@ss.hasPermission('genealogy:member:update')")
    public CommonResult<Boolean> deleteDeed(@RequestParam("id") Long id) {
        memberService.deleteDeed(id);
        return success(true);
    }

    @PutMapping("/archive")
    @Operation(summary = "维护成员档案（生平简介、照片集）")
    @PreAuthorize("@ss.hasPermission('genealogy:member:update')")
    public CommonResult<Boolean> updateArchive(@Valid @RequestBody MemberArchiveSaveReqVO reqVO) {
        memberService.updateArchive(reqVO);
        return success(true);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "下载导入模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "成员导入模板.xls", "成员", MemberImportExcelVO.class, List.of(
                MemberImportExcelVO.builder().name("陈示例").gender("男").generationNo(6)
                        .generationWord("明").birthDate("1994-01-01").fatherName("陈德厚").intro("示例").build()));
    }

    @PostMapping("/import")
    @Operation(summary = "导入成员")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true),
            @Parameter(name = "updateSupport", description = "是否支持更新")
    })
    @PreAuthorize("@ss.hasPermission('genealogy:member:import')")
    public CommonResult<MemberImportRespVO> importExcel(@RequestParam("file") MultipartFile file,
                                                        @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception {
        List<MemberImportExcelVO> list = ExcelUtils.read(file, MemberImportExcelVO.class);
        return success(memberService.importMemberList(list, updateSupport));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出成员")
    @PreAuthorize("@ss.hasPermission('genealogy:member:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void export(MemberPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MemberRespVO> list = memberService.getMemberPage(pageReqVO).getList();
        ExcelUtils.write(response, "家族成员.xls", "数据", MemberRespVO.class, list);
    }

    @PostMapping("/archive-apply/create")
    @Operation(summary = "提交档案补充申请")
    public CommonResult<Long> createArchiveApply(@Valid @RequestBody ArchiveApplySaveReqVO reqVO) {
        return success(memberService.createArchiveApply(reqVO));
    }

    @PutMapping("/archive-apply/audit")
    @Operation(summary = "审核档案补充")
    @PreAuthorize("@ss.hasPermission('genealogy:archive:audit')")
    public CommonResult<Boolean> auditArchive(@RequestParam("id") Long id,
                                              @RequestParam("status") Integer status,
                                              @RequestParam(value = "reason", required = false) String reason) {
        memberService.auditArchiveApply(id, status, reason);
        return success(true);
    }

    @GetMapping("/archive-apply/page")
    @Operation(summary = "档案补充申请分页")
    @PreAuthorize("@ss.hasPermission('genealogy:archive:query')")
    public CommonResult<PageResult<ArchiveApplyRespVO>> archivePage(@Valid ArchiveApplyPageReqVO reqVO) {
        return success(memberService.getArchiveApplyPage(reqVO));
    }
}
