package chen.genealogy.module.promotion.api.combination;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.framework.common.util.object.BeanUtils;
import chen.genealogy.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import chen.genealogy.module.promotion.api.combination.dto.CombinationRecordCreateRespDTO;
import chen.genealogy.module.promotion.api.combination.dto.CombinationRecordRespDTO;
import chen.genealogy.module.promotion.api.combination.dto.CombinationValidateJoinRespDTO;
import chen.genealogy.module.promotion.convert.combination.CombinationActivityConvert;
import chen.genealogy.module.promotion.dal.dataobject.combination.CombinationRecordDO;
import chen.genealogy.module.promotion.service.combination.CombinationRecordService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

/**
 * 拼团活动 API 实现类
 *
 * @author HUIHUI
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class CombinationRecordApiImpl implements CombinationRecordApi {

    @Resource
    private CombinationRecordService combinationRecordService;

    @Override
    public CommonResult<Boolean> validateCombinationRecord(Long userId, Long activityId, Long headId, Long skuId, Integer count) {
        combinationRecordService.validateCombinationRecord(userId, activityId, headId, skuId, count);
        return success(true);
    }

    @Override
    public CommonResult<CombinationRecordCreateRespDTO> createCombinationRecord(CombinationRecordCreateReqDTO reqDTO) {
        return success(CombinationActivityConvert.INSTANCE.convert4(combinationRecordService.createCombinationRecord(reqDTO)));
    }

    @Override
    public CommonResult<CombinationRecordRespDTO> getCombinationRecordByOrderId(Long userId, Long orderId) {
        CombinationRecordDO record = combinationRecordService.getCombinationRecord(userId, orderId);
        return success(BeanUtils.toBean(record, CombinationRecordRespDTO.class));
    }

    @Override
    public CommonResult<CombinationValidateJoinRespDTO> validateJoinCombination(
            Long userId, Long activityId, Long headId, Long skuId, Integer count) {
        return success(combinationRecordService.validateJoinCombination(userId, activityId, headId, skuId, count));
    }

}
