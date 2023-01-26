package com.koton.tonapi.Block;
import java.util.List;


public class Shard {

	Long workchain;

	Long seqno;

   String shard;

   String rootHash;

   String fileHash;

   List<Transaction> transactions;


    public void setWorkchain(Long workchain) {
        this.workchain = workchain;
    }
    public Long getWorkchain() {
        return workchain;
    }

    public void setSeqno(Long seqno) {
        this.seqno = seqno;
    }
    public Long getSeqno() {
        return seqno;
    }

    public void setShard(String shard) {
        this.shard = shard;
    }
    public String getShard() {
        return shard;
    }

    public void setRootHash(String rootHash) {
        this.rootHash = rootHash;
    }
    public String getRootHash() {
        return rootHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }
    public String getFileHash() {
        return fileHash;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    public List<Transaction> getTransactions() {
        return transactions;
    }

}
