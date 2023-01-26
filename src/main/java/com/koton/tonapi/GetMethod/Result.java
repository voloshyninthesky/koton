package com.koton.tonapi.GetMethod;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Result {
	@JsonProperty("@type")
	public String type;
	public Long gas_used;
	public ArrayList<ArrayList<Object>> stack;
	public Long exit_code;
	@JsonProperty("@extra")
	public String extra;
}
