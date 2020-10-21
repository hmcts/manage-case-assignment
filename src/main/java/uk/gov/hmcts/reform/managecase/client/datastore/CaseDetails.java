package uk.gov.hmcts.reform.managecase.client.datastore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.ValidationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseDetails {

    public static final String ORG_POLICY_CASE_ASSIGNED_ROLE = "OrgPolicyCaseAssignedRole";
    public static final String ORGANISATION = "Organisation";
    public static final String APPROVAL_STATUS = "ApprovalStatus";
    public static final String CASE_ROLE_ID = "CaseRoleId";
    public static final String ORGANISATION_TO_ADD = "OrganisationToAdd";
    public static final String ORGANISATION_TO_REMOVE = "OrganisationToRemove";
    public static final String ORG_POLICY_REFERENCE = "OrgPolicyReference";
    public static final String ORG_ID = "OrganisationID";
    public static final String ORG_NAME = "OrganisationName";

    private String reference;
    private String jurisdiction;
    private String state;
    @JsonProperty("case_type_id")
    private String caseTypeId;
    @JsonProperty("case_data")
    private Map<String, JsonNode> data;

    public List<JsonNode> findOrganisationPolicyNodes() {
        return getData().values().stream()
            .map(node -> node.findParents(ORG_POLICY_CASE_ASSIGNED_ROLE))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    public Optional<JsonNode> findOrganisationPolicyNodeForCaseRole(String caseRoleId) {
        return findOrganisationPolicyNodes().stream()
                .filter(node -> node.get(ORG_POLICY_CASE_ASSIGNED_ROLE).asText().equals(caseRoleId))
                .reduce((a, b) -> {
                    throw new ValidationException(String.format("More than one Organisation Policy with "
                            + "case role ID '%s' exists on case", caseRoleId));
                });
    }
}
