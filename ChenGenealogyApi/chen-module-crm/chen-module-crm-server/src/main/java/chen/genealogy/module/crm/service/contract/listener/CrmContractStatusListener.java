package chen.genealogy.module.crm.service.contract.listener;

import chen.genealogy.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import chen.genealogy.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import chen.genealogy.module.crm.enums.ApiConstants;
import chen.genealogy.module.crm.service.contract.CrmContractService;
import chen.genealogy.module.crm.service.contract.CrmContractServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 合同审批的结果的监听器实现类
 *
 * @author HUIHUI
 */
@RestController
@Validated
public class CrmContractStatusListener extends BpmProcessInstanceStatusEventListener {

    private static final String PREFIX = ApiConstants.PREFIX + "/contract";

    @Resource
    private CrmContractService contractService;

    @Override
    public String getProcessDefinitionKey() {
        return CrmContractServiceImpl.BPM_PROCESS_DEFINITION_KEY;
    }

    @Override
    @PostMapping(PREFIX + "/update-audit-status") // 目的：提供给 bpm-server rpc 调用
    protected void onEvent(@RequestBody BpmProcessInstanceStatusEvent event) {
        contractService.updateContractAuditStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }

}
