package top.archaiharness.framework.common.pbac.service;

import top.archaiharness.framework.common.pbac.domain.AccessContext;
import top.archaiharness.framework.common.pbac.domain.EvaluationResult;
import top.archaiharness.framework.common.pbac.domain.UserPermissionContext;

import java.util.Map;

public interface PBACService {

    EvaluationResult evaluate(AccessContext context);

    UserPermissionContext parseUserContext(Map<String, String> headers);
}
