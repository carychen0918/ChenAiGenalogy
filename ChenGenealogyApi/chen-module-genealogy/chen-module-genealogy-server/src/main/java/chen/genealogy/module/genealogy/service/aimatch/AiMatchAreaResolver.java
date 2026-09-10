package chen.genealogy.module.genealogy.service.aimatch;

import chen.genealogy.framework.ip.core.Area;
import chen.genealogy.framework.ip.core.enums.AreaTypeEnum;
import chen.genealogy.framework.ip.core.utils.AreaUtils;
import chen.genealogy.module.genealogy.util.RegionAreaUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 从用户自然语言中解析省/市/县，只用内存地区树，不拼接 SQL。
 */
public final class AiMatchAreaResolver {

    private AiMatchAreaResolver() {
    }

    @Data
    public static class ResolvedArea {
        private Integer provinceId;
        private Integer cityId;
        private Integer countyId;
        private String regionName;
        private boolean present;
    }

    public static ResolvedArea resolve(String text) {
        ResolvedArea result = new ResolvedArea();
        if (text == null || text.isBlank()) {
            return result;
        }
        Area province = longestMatch(text, AreaTypeEnum.PROVINCE, null);
        Area city = longestMatch(text, AreaTypeEnum.CITY, province);
        Area county = longestMatch(text, AreaTypeEnum.DISTRICT, city != null ? city : province);
        Integer leaf = county != null ? county.getId() : (city != null ? city.getId() : (province != null ? province.getId() : null));
        if (leaf == null) {
            return result;
        }
        RegionAreaUtils.Region region = RegionAreaUtils.of(leaf);
        result.setProvinceId(region.getProvinceId());
        result.setCityId(region.getCityId());
        result.setCountyId(region.getCountyId());
        result.setRegionName(region.getRegionName());
        result.setPresent(true);
        return result;
    }

    private static Area longestMatch(String text, AreaTypeEnum type, Area parent) {
        List<Area> candidates = new ArrayList<>(AreaUtils.getByType(type, a -> a));
        candidates.sort(Comparator.comparingInt((Area a) -> a.getName() == null ? 0 : a.getName().length()).reversed());
        for (Area area : candidates) {
            String name = area.getName();
            if (name == null || name.length() < 2) {
                continue;
            }
            if (parent != null && !isDescendant(area, parent.getId())) {
                continue;
            }
            if (text.contains(name) || text.contains(stripSuffix(name))) {
                return area;
            }
        }
        return null;
    }

    private static boolean isDescendant(Area area, Integer ancestorId) {
        Area cur = area;
        for (int i = 0; i < 8 && cur != null; i++) {
            if (ancestorId.equals(cur.getId())) {
                return true;
            }
            cur = cur.getParent();
        }
        return false;
    }

    private static String stripSuffix(String name) {
        return name.replaceAll("(特别行政区|维吾尔自治区|壮族自治区|回族自治区|自治区|省|市|地区|盟|县|区)$", "");
    }
}
