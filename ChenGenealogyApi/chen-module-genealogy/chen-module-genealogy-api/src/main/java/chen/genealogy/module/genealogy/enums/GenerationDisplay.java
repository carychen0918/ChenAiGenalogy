package chen.genealogy.module.genealogy.enums;

/**
 * 字辈展示：所属房、字源与字辈文字的组合文案。
 * 所属房：1长房 2二房 3三房 4三房织金 5四五房
 */
public final class GenerationDisplay {

    private GenerationDisplay() {
    }

    private static final String[] CN = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};

    public static String chituOrder(Integer generationNo) {
        if (generationNo == null || generationNo <= 0) {
            return "";
        }
        if (generationNo <= 10) {
            return CN[generationNo] + "世";
        }
        return generationNo + "世";
    }

    public static String houseLabel(String house) {
        if (house == null || house.isBlank()) {
            return "";
        }
        return switch (house) {
            case "1" -> "长房";
            case "2" -> "二房";
            case "3" -> "三房";
            case "4" -> "三房织金";
            case "5" -> "四五房";
            default -> house;
        };
    }

    public static String wordWithHouse(String word, String house) {
        if (word == null || word.isBlank()) {
            return "";
        }
        String label = houseLabel(house);
        return label.isBlank() ? word : word + "（" + label + "）";
    }
}
