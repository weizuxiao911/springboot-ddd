package io.github.weizuxiao911.springboot.ddd.common.pbac.service;

import io.github.weizuxiao911.springboot.ddd.common.pbac.domain.AccessContext;
import io.github.weizuxiao911.springboot.ddd.common.pbac.domain.EvaluationResult;
import io.github.weizuxiao911.springboot.ddd.common.pbac.domain.UserPermissionContext;

import java.util.Map;

public interface PBACService {

    EvaluationResult evaluate(AccessContext context);

    UserPermissionContext parseUserContext(Map<String, String> headers);
}
