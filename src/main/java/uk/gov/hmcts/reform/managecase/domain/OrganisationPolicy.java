package uk.gov.hmcts.reform.managecase.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrganisationPolicy {

    @JsonProperty("Organisation")
    private Organisation organisation;
    @JsonProperty("OrgPolicyReference")
    private String orgPolicyReference;
    @JsonProperty("OrgPolicyCaseAssignedRole")
    private String orgPolicyCaseAssignedRole;
    @JsonProperty("PreviousOrganisations")
    private List<PreviousOrganisation> previousOrganisations;

}
