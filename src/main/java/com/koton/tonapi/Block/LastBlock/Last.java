package com.koton.tonapi.Block.LastBlock;

public class Last {
	private Long seqno;
	private String shard;
	private Long workchain;
	private String fileHash;
	private String rootHash;

	public Long getSeqno() {
		return seqno;
	}

	public void setSeqno(Long seqno) {
		this.seqno = seqno;
	}

	public String getShard() {
		return shard;
	}

	public void setShard(String shard) {
		this.shard = shard;
	}

	public Long getWorkchain() {
		return workchain;
	}

	public void setWorkchain(Long workchain) {
		this.workchain = workchain;
	}

	public String getFileHash() {
		return fileHash;
	}

	public void setFileHash(String fileHash) {
		this.fileHash = fileHash;
	}

	public String getRootHash() {
		return rootHash;
	}

	public void setRootHash(String rootHash) {
		this.rootHash = rootHash;
	}
}
