package com.koton.tonapi.Nft;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Owner {
	public String address;
	public String icon;
	public boolean is_scam;
	public String name;
}
