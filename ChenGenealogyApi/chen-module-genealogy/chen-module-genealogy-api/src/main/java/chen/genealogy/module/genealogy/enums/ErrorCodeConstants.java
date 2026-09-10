package chen.genealogy.module.genealogy.enums;

import chen.genealogy.framework.common.exception.ErrorCode;

/**
 * 族谱模块错误码
 * <p>
 * genealogy 系统，使用 1-040-000-000 段
 */
public interface ErrorCodeConstants {

    ErrorCode FAMILY_NOT_EXISTS = new ErrorCode(1_040_000_000, "家族信息不存在");

    ErrorCode MEMBER_NOT_EXISTS = new ErrorCode(1_040_001_000, "成员不存在");
    ErrorCode MEMBER_NAME_DUPLICATE = new ErrorCode(1_040_001_001, "同一父亲下已存在同名同性别成员");
    ErrorCode MEMBER_DATE_INVALID = new ErrorCode(1_040_001_002, "出生日期不得晚于逝世日期");
    ErrorCode MEMBER_RELATION_INVALID = new ErrorCode(1_040_001_003, "亲属关系不合法，不得选择本人或后代");
    ErrorCode MEMBER_HAS_DESCENDANTS = new ErrorCode(1_040_001_004, "该成员存在{}位后代，删除后后代将变为未挂载状态");
    ErrorCode MEMBER_SPOUSE_CONFLICT = new ErrorCode(1_040_001_005, "所选配偶已有其他配偶记录，请确认是否继续");
    ErrorCode MEMBER_NOT_CERTIFIED = new ErrorCode(1_040_001_006, "您的成员身份未认证，请联系管理员");
    ErrorCode MEMBER_IMPORT_LIST_EMPTY = new ErrorCode(1_040_001_007, "导入数据不能为空");
    ErrorCode MEMBER_LOGIN_ROLE_MISSING = new ErrorCode(1_040_001_008, "未找到「普通族人」角色，无法开通登录账号");
    ErrorCode MEMBER_LOGIN_USERNAME_INVALID = new ErrorCode(1_040_001_009, "登录账号需为 4-30 位字母或数字");
    ErrorCode MEMBER_DEED_NOT_EXISTS = new ErrorCode(1_040_001_010, "事迹记录不存在");

    ErrorCode GENERATION_NOT_EXISTS = new ErrorCode(1_040_002_000, "字辈不存在");
    ErrorCode GENERATION_DUPLICATE = new ErrorCode(1_040_002_001, "第{}世已存在「{}」字辈");
    ErrorCode GENERATION_IN_USE = new ErrorCode(1_040_002_002, "该字辈下仍有{}位成员，无法删除");

    ErrorCode ARCHIVE_APPLY_NOT_EXISTS = new ErrorCode(1_040_003_000, "档案补充申请不存在");
    ErrorCode ARCHIVE_APPLY_AUDITED = new ErrorCode(1_040_003_001, "该申请已审核，不可重复操作");

    ErrorCode MIGRATION_NOT_EXISTS = new ErrorCode(1_040_004_000, "迁徙节点不存在");
    ErrorCode ANCESTOR_DEED_NOT_EXISTS = new ErrorCode(1_040_004_001, "祖先事迹不存在");
    ErrorCode CULTURE_GUIDE_NOT_EXISTS = new ErrorCode(1_040_004_002, "文化指南不存在");

    ErrorCode FEED_NOT_EXISTS = new ErrorCode(1_040_005_000, "动态/公告不存在");
    ErrorCode FEED_COMMENT_NOT_EXISTS = new ErrorCode(1_040_005_001, "评论不存在");

    ErrorCode SCHOLARSHIP_WINDOW_CLOSED = new ErrorCode(1_040_006_000, "当前不在申请期内，申请开放时间为{}至{}");
    ErrorCode SCHOLARSHIP_DUPLICATE = new ErrorCode(1_040_006_001, "您本学年已提交申请，申请编号{}");
    ErrorCode SCHOLARSHIP_NOT_EXISTS = new ErrorCode(1_040_006_002, "资助申请不存在");
    ErrorCode SCHOLARSHIP_STATUS_ERROR = new ErrorCode(1_040_006_003, "当前状态不允许该操作");
    ErrorCode SCHOLARSHIP_REJECT_REASON_REQUIRED = new ErrorCode(1_040_006_004, "驳回时必须填写原因");
    ErrorCode SCHOLARSHIP_AMOUNT_REQUIRED = new ErrorCode(1_040_006_005, "发放金额不能为空");
    ErrorCode SCHOLARSHIP_MATERIAL_REQUIRED = new ErrorCode(1_040_006_006, "证明材料不完整");
    ErrorCode SCHOLARSHIP_CONFIG_NOT_EXISTS = new ErrorCode(1_040_006_007, "资助窗口配置不存在");
    ErrorCode SCHOLARSHIP_CANNOT_EDIT = new ErrorCode(1_040_006_008, "当前申请不可编辑");
    ErrorCode SCHOLARSHIP_REGION_REQUIRED = new ErrorCode(1_040_006_009, "请先完善成员的省市区地址后再提交资助申请");
    ErrorCode SCHOLARSHIP_AUDIT_NO_PERMISSION = new ErrorCode(1_040_006_010, "当前级别不由您审核，或您尚未绑定管辖地区");
    ErrorCode ADMIN_REGION_ROLE_MISSING = new ErrorCode(1_040_006_011, "该用户不是省/市/县管理员，无法绑定管辖地区");

    ErrorCode ACTIVITY_NOT_EXISTS = new ErrorCode(1_040_007_000, "祭祖活动不存在");
    ErrorCode ACTIVITY_TIME_INVALID = new ErrorCode(1_040_007_001, "活动时间不合法：结束须晚于开始，报名截止须早于开始");
    ErrorCode ACTIVITY_DEADLINE = new ErrorCode(1_040_007_002, "报名已截止");
    ErrorCode ACTIVITY_CANCELLED = new ErrorCode(1_040_007_003, "活动已取消");
    ErrorCode ACTIVITY_ALREADY_JOINED = new ErrorCode(1_040_007_004, "您已报名该活动");
    ErrorCode REGISTRATION_NOT_EXISTS = new ErrorCode(1_040_007_005, "报名记录不存在");
    ErrorCode TOMB_NOT_EXISTS = new ErrorCode(1_040_007_006, "温蒂坟地尚未配置");
    ErrorCode TOMB_COORD_MISSING = new ErrorCode(1_040_007_007, "地点信息待管理员配置");
    ErrorCode WORSHIP_NOT_EXISTS = new ErrorCode(1_040_007_008, "祭扫记录不存在");
    ErrorCode WORSHIP_NOT_JOINED = new ErrorCode(1_040_007_009, "仅已报名族人可上传祭扫记录");

    ErrorCode AI_MATCH_DISABLED = new ErrorCode(1_040_008_000, "AI 寻宗功能暂未开放");
    ErrorCode AI_MATCH_MODEL_MISSING = new ErrorCode(1_040_008_001, "尚未配置可用的 AI 聊天模型，请在管理端「AI 大模型」中启用模型，或配置 chen.genealogy.ai-match");
    ErrorCode AI_MATCH_INPUT_INVALID = new ErrorCode(1_040_008_002, "请用自己的话描述家族线索，不要包含指令或特殊标记");
    ErrorCode AI_MATCH_RATE_LIMIT = new ErrorCode(1_040_008_003, "提问过于频繁，请稍后再试");
    ErrorCode AI_MATCH_SESSION_INVALID = new ErrorCode(1_040_008_004, "会话无效，请刷新页面后重试");
    ErrorCode AI_MATCH_CONVERSATION_NOT_EXISTS = new ErrorCode(1_040_008_005, "寻宗会话不存在");
    ErrorCode AI_MATCH_GUEST_DAILY_LIMIT = new ErrorCode(1_040_008_006, "使用次数已达上限，请明天再试");

    ErrorCode BOOK_PAGE_NOT_EXISTS = new ErrorCode(1_040_009_000, "谱书页码不存在");
}
