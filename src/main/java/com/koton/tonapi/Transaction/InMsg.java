package com.koton.tonapi.Transaction;

public class InMsg {
	public String source;
	public String destination;
	public Long value;
	public Long fwd_fee;
	public Long ihr_fee;
	public Long created_lt;
	public Long op;
	public Object comment;
	public String hash;
	public String body_hash;
	public String body;

	@Override
	public String toString() {
		return "InMsg{" +
				"source='" + source + '\'' +
				", destination='" + destination + '\'' +
				", value=" + value +
				", fwd_fee=" + fwd_fee +
				", ihr_fee=" + ihr_fee +
				", created_lt=" + created_lt +
				", op=" + op +
				", comment=" + comment +
				", hash='" + hash + '\'' +
				", body_hash='" + body_hash + '\'' +
				", body='" + body + '\'' +
				'}';
	}
}
