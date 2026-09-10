package chen.genealogy.module.genealogy.service.aimatch;

import cn.hutool.core.util.StrUtil;
import chen.genealogy.framework.ip.core.utils.AreaUtils;
import chen.genealogy.module.genealogy.dal.dataobject.content.MigrationNodeDO;
import chen.genealogy.module.genealogy.dal.dataobject.family.FamilyDO;
import chen.genealogy.module.genealogy.dal.dataobject.generation.GenerationDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;
import chen.genealogy.module.genealogy.dal.mysql.member.MemberMapper;
import chen.genealogy.module.genealogy.service.content.ContentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class AiMatchKnowledgeBuilder {

    private static final Pattern CN_NAME = Pattern.compile("[\\u4e00-\\u9fa5]{2,4}");

    @Resource
    private ContentService contentService;
    @Resource
    private MemberMapper memberMapper;

    public String build(String userCorpus) {
        FamilyDO family = contentService.getFamily();
        List<GenerationDO> generations = contentService.getGenerationList();
        List<MigrationNodeDO> migrations = contentService.getMigrationList();
        List<MemberDO> members = memberMapper.selectListByFamilyId(family.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("【本系统族谱资料，只允许据此判断是否同族，禁止编造】\n");
        sb.append("家族名称：").append(nvl(family.getName())).append('\n');
        sb.append("姓氏：").append(nvl(family.getSurname())).append('\n');
        sb.append("始祖：").append(nvl(family.getAncestorName())).append('\n');
        sb.append("聚居地：").append(nvl(family.getRegion())).append('\n');
        if (StrUtil.isNotBlank(family.getIntro())) {
            sb.append("简介：").append(StrUtil.sub(strip(family.getIntro()), 0, 400)).append('\n');
        }
        if (StrUtil.isNotBlank(family.getOriginContent())) {
            sb.append("源流：").append(StrUtil.sub(strip(family.getOriginContent()), 0, 400)).append('\n');
        }
        sb.append("字辈（世代-字）：");
        sb.append(generations.stream()
                .map(g -> g.getGenerationNo() + "世" + StrUtil.blankToDefault(g.getWord(), ""))
                .collect(Collectors.joining("、")));
        sb.append('\n');
        if (!migrations.isEmpty()) {
            sb.append("迁徙节点：");
            sb.append(migrations.stream().limit(20)
                    .map(m -> StrUtil.blankToDefault(m.getNodeTime(), "") + m.getPlace()
                            + StrUtil.blankToDefault(m.getEventTitle(), ""))
                    .collect(Collectors.joining("；")));
            sb.append('\n');
        }
        Set<String> regions = new LinkedHashSet<>();
        for (MemberDO member : members) {
            String region = AreaUtils.format(member.getCountyId() != null ? member.getCountyId()
                    : member.getCityId() != null ? member.getCityId() : member.getProvinceId(), " ");
            if (StrUtil.isNotBlank(region)) {
                regions.add(region);
            }
        }
        sb.append("在谱人数：").append(members.size()).append('\n');
        sb.append("成员分布地区：").append(String.join("、", regions)).append('\n');
        appendNameHits(sb, userCorpus);
        return sb.toString();
    }

    public int heuristicScore(String userCorpus) {
        FamilyDO family = contentService.getFamily();
        List<GenerationDO> generations = contentService.getGenerationList();
        String text = StrUtil.blankToDefault(userCorpus, "");
        int score = 0;
        if (StrUtil.isNotBlank(family.getSurname()) && text.contains(family.getSurname())) {
            score += 20;
        }
        if (StrUtil.isNotBlank(family.getAncestorName()) && text.contains(family.getAncestorName())) {
            score += 20;
        }
        if (StrUtil.isNotBlank(family.getRegion()) && text.contains(family.getRegion())) {
            score += 15;
        }
        for (GenerationDO gen : generations) {
            if (StrUtil.isNotBlank(gen.getWord()) && gen.getWord().length() <= 4 && text.contains(gen.getWord())) {
                score += 25;
                break;
            }
        }
        if (hasNameHit(text)) {
            score += 30;
        }
        return Math.min(score, 100);
    }

    private void appendNameHits(StringBuilder sb, String userCorpus) {
        if (StrUtil.isBlank(userCorpus)) {
            return;
        }
        Matcher matcher = CN_NAME.matcher(userCorpus);
        int used = 0;
        Set<String> seen = new LinkedHashSet<>();
        while (matcher.find() && used < 3) {
            String name = matcher.group();
            if (!seen.add(name)) {
                continue;
            }
            MemberDO member = memberMapper.selectByName(name);
            if (member == null) {
                continue;
            }
            String region = AreaUtils.format(member.getCountyId() != null ? member.getCountyId()
                    : member.getCityId() != null ? member.getCityId() : member.getProvinceId(), " ");
            sb.append("谱中同名线索：存在「").append(member.getName()).append("」，")
                    .append(member.getGenerationNo() == null ? "" : member.getGenerationNo() + "世，")
                    .append(StrUtil.blankToDefault(region, "地区未填")).append('\n');
            used++;
        }
    }

    private boolean hasNameHit(String text) {
        Matcher matcher = CN_NAME.matcher(text);
        int checked = 0;
        while (matcher.find() && checked < 5) {
            checked++;
            if (memberMapper.selectByName(matcher.group()) != null) {
                return true;
            }
        }
        return false;
    }

    private static String nvl(String v) {
        return StrUtil.blankToDefault(v, "未记载");
    }

    private static String strip(String html) {
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }
}
