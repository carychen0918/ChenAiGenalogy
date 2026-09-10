package chen.genealogy.module.genealogy.util;

import chen.genealogy.framework.ip.core.enums.AreaTypeEnum;
import chen.genealogy.framework.ip.core.utils.AreaUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 把地区编号拆成省 / 市 / 县
 */
public class RegionAreaUtils {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Region {
        private Integer provinceId;
        private Integer cityId;
        private Integer countyId;
        private String regionName;
    }

    public static Region of(Integer areaId) {
        if (areaId == null) {
            return new Region();
        }
        Region region = new Region();
        region.setProvinceId(AreaUtils.getParentIdByType(areaId, AreaTypeEnum.PROVINCE));
        region.setCityId(AreaUtils.getParentIdByType(areaId, AreaTypeEnum.CITY));
        region.setCountyId(AreaUtils.getParentIdByType(areaId, AreaTypeEnum.DISTRICT));
        region.setRegionName(AreaUtils.format(areaId, " / "));
        return region;
    }

    public static Region of(Integer provinceId, Integer cityId, Integer countyId) {
        Integer leaf = countyId != null ? countyId : (cityId != null ? cityId : provinceId);
        Region region = of(leaf);
        if (provinceId != null) {
            region.setProvinceId(provinceId);
        }
        if (cityId != null) {
            region.setCityId(cityId);
        }
        if (countyId != null) {
            region.setCountyId(countyId);
        }
        return region;
    }
}
