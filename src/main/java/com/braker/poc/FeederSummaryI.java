package com.braker.poc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"totalFeeder", "openFeeder", "closeFeeder"})
public interface FeederSummaryI {

	public Integer getTotalFeeder();
	public Integer getOpenFeeder();
	public Integer getCloseFeeder();
	public Integer getErrorFeeder();
}
