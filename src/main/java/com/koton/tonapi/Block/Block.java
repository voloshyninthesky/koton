package com.koton.tonapi.Block;
import java.util.List;

public class Block {

   List<Shard> shards;

    public void setShards(List<Shard> shards) {
        this.shards = shards;
    }
    public List<Shard> getShards() {
        return shards;
    }

}
