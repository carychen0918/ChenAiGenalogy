package chen.genealogy.module.product.convert.spu;

import chen.genealogy.framework.common.util.collection.CollectionUtils;
import chen.genealogy.framework.common.util.object.BeanUtils;
import chen.genealogy.module.product.controller.admin.spu.vo.ProductSkuRespVO;
import chen.genealogy.module.product.controller.admin.spu.vo.ProductSpuPageReqVO;
import chen.genealogy.module.product.controller.admin.spu.vo.ProductSpuRespVO;
import chen.genealogy.module.product.controller.app.spu.vo.AppProductSpuPageReqVO;
import chen.genealogy.module.product.dal.dataobject.sku.ProductSkuDO;
import chen.genealogy.module.product.dal.dataobject.spu.ProductSpuDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static chen.genealogy.framework.common.util.collection.CollectionUtils.convertMultiMap;

/**
 * 商品 SPU Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductSpuConvert {

    ProductSpuConvert INSTANCE = Mappers.getMapper(ProductSpuConvert.class);

    ProductSpuPageReqVO convert(AppProductSpuPageReqVO bean);

    default ProductSpuRespVO convert(ProductSpuDO spu, List<ProductSkuDO> skus) {
        ProductSpuRespVO spuVO = BeanUtils.toBean(spu, ProductSpuRespVO.class);
        spuVO.setSkus(BeanUtils.toBean(skus, ProductSkuRespVO.class));
        return spuVO;
    }

    default List<ProductSpuRespVO> convertForSpuDetailRespListVO(List<ProductSpuDO> spus, List<ProductSkuDO> skus) {
        Map<Long, List<ProductSkuDO>> skuMultiMap = convertMultiMap(skus, ProductSkuDO::getSpuId);
        return CollectionUtils.convertList(spus, spu -> convert(spu, skuMultiMap.get(spu.getId())));
    }

}
