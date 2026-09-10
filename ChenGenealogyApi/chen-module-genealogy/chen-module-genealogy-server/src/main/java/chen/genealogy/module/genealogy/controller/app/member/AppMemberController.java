package chen.genealogy.module.genealogy.controller.app.member;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.module.genealogy.controller.admin.member.vo.*;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;
import chen.genealogy.module.genealogy.service.member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 家族成员")
@RestController
@RequestMapping("/genealogy/member")
@Validated
public class AppMemberController {

    @Resource
    private MemberService memberService;

    @GetMapping("/get")
    @Operation(summary = "成员详情")
    public CommonResult<MemberRespVO> getMember(@RequestParam("id") Long id) {
        return success(memberService.getMember(id));
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

    @PostMapping("/archive-apply/create")
    @Operation(summary = "提交档案补充申请")
    public CommonResult<Long> createArchiveApply(@Valid @RequestBody ArchiveApplySaveReqVO reqVO) {
        return success(memberService.createArchiveApply(reqVO));
    }
}
