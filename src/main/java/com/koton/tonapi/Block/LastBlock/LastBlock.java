package com.koton.tonapi.Block.LastBlock;

public class LastBlock {
	private Last last;
	private Init init;
	private String stateRootHash;
	private Long now;
	public Last getLast() {
		return last;
	}
	public void setLast(Last last) {
		this.last = last;
	}
	public Init getInit() {
		return init;
	}
	public void setInit(Init init) {
		this.init = init;
	}
	public String getStateRootHash() {
		return stateRootHash;
	}
	public void setStateRootHash(String stateRootHash) {
		this.stateRootHash = stateRootHash;
	}
	public Long getNow() {
		return now;
	}
	public void setNow(Long now) {
		this.now = now;
	}
}
