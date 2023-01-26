package com.koton.tonapi.Nft;

import org.ton.block.AddrStd;

import java.util.ArrayList;

public class NftItem {
	public String address;
	public Object approved_by;
	public Collection collection;
	public String collection_address;
	public Long index;
	public Metadata metadata;
	public Owner owner;
	public ArrayList<Preview> previews;
	public boolean verified;

	public String userFriendlyAddress() {
		AddrStd addr = AddrStd.parse(address);
		return addr.toString(true, true, false, true);
	}
}
