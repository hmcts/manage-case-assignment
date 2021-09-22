package uk.gov.hmcts.reform.managecase.service.ras;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.domain.model.casedataaccesscontrol.enums.RoleType;
import uk.gov.hmcts.reform.managecase.api.payload.CaseAssignedUserRole;
import uk.gov.hmcts.reform.managecase.api.payload.RoleAssignment;
import uk.gov.hmcts.reform.managecase.api.payload.RoleAssignmentAttributes;
import uk.gov.hmcts.reform.managecase.api.payload.RoleAssignmentQuery;
import uk.gov.hmcts.reform.managecase.api.payload.RoleAssignments;
import uk.gov.hmcts.reform.managecase.api.payload.RoleAssignmentsDeleteRequest;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleAssignmentService {

    private final RoleAssignmentServiceHelper roleAssignmentServiceHelper;
    private final RoleAssignmentsMapper roleAssignmentsMapper;

    @Autowired
    public RoleAssignmentService(RoleAssignmentServiceHelper roleAssignmentServiceHelper,
                                 RoleAssignmentsMapper roleAssignmentsMapper) {
        this.roleAssignmentServiceHelper = roleAssignmentServiceHelper;
        this.roleAssignmentsMapper = roleAssignmentsMapper;
    }

    public void deleteRoleAssignments(List<RoleAssignmentsDeleteRequest> deleteRequests) {
        if (deleteRequests != null && !deleteRequests.isEmpty()) {
            List<RoleAssignmentQuery> queryRequests = deleteRequests.stream()
                .map(request -> new RoleAssignmentQuery(
                    request.getCaseId(),
                    request.getUserId(),
                    request.getRoleNames())
                )
                .collect(Collectors.toList());

            roleAssignmentServiceHelper.deleteRoleAssignmentsByQuery(queryRequests);
        }
    }

    public RoleAssignments createCaseRoleAssignments(List<RoleAssignmentsDeleteRequest> addRequest) {

        if (addRequest != null && !addRequest.isEmpty()) {
            List<RoleAssignmentQuery> queryRequests = addRequest.stream()
                .map(request -> new RoleAssignmentQuery(
                         request.getCaseId(),
                         request.getUserId(),
                         request.getRoleNames()
                     )
                )
                .collect(Collectors.toList());

            roleAssignmentServiceHelper.addRoleAssignmentsByQuery(queryRequests);
        }
        return null;
    }


    public List<CaseAssignedUserRole> findRoleAssignmentsByCasesAndUsers(List<String> caseIds, List<String> userIds) {
        final var roleAssignmentResponse =
            roleAssignmentServiceHelper.findRoleAssignmentsByCasesAndUsers(caseIds, userIds);

        final var roleAssignments = roleAssignmentsMapper.toRoleAssignments(roleAssignmentResponse);
        var caseIdError = new RuntimeException(RoleAssignmentAttributes.ATTRIBUTE_NOT_DEFINED);
        return roleAssignments.getRoleAssignmentsList().stream()
            .filter(roleAssignment -> isValidRoleAssignment(roleAssignment))
            .map(roleAssignment ->
                     new CaseAssignedUserRole(
                         roleAssignment.getAttributes().getCaseId().orElseThrow(() -> caseIdError),
                         roleAssignment.getActorId(),
                         roleAssignment.getRoleName()
                     )
            )
            .collect(Collectors.toList());
    }

    private boolean isValidRoleAssignment(RoleAssignment roleAssignment) {
        final boolean isCaseRoleType = roleAssignment.getRoleType().equals(RoleType.CASE.name());
        return roleAssignment.isNotExpiredRoleAssignment() && isCaseRoleType;
    }
}
