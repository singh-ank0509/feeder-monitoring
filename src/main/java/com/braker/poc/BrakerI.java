package com.braker.poc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"substationCode", "substation", "feederCode", "feeder", "status",
					"lastChangeTimeDt", "duration", "lastChangeReasonDesc", "tagStatus", "rtuErrFlag"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface BrakerI {

	public String getSubstationCode();
	public String getSubstation();
	public String getFeederCode();
	public String getFeeder();
	public String getStatus();
	public String getLastChangeTimeDt();
	public String getDuration();
	public String getLastChangeReasonDesc();
	public String getTagStatus();
	public String getRtuErrFlag();
}
