package com.koton;

import com.koton.tonapi.Nft.NftItem;
import com.koton.tonapi.Nft.NftItems;
import com.koton.tonapi.TonApi;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class Controller {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static final String collection = getCollectionAddress();

	private final UpdateReceiver updateReceiver;
	private final TonApi tonApi;

	public Controller(UpdateReceiver updateReceiver) {
		this.updateReceiver = updateReceiver;
		this.tonApi = new TonApi();
	}

	public void getTonKeeperLink(Context ctx, boolean lock) {
		try {
			String address = ctx.queryParam("address");
			String amount = ctx.queryParam("amount");
			if (address == null) {
				ctx.status(HttpCode.BAD_REQUEST);
				return;
			}
			int coins = amount == null ? 50000000 : Integer.parseInt(amount);
			String encoded = TonClient.INSTANCE.bouildTonkeeperLink(address, lock, coins);
			String res = "{\n" +
					"  \"tonkeeperLink\": \"" + encoded + "\"\n" +
					"}";
			ctx.json(res);
		} catch (Exception e) {
			log.error("Failed to generate boc: ", e);
			ctx.status(HttpCode.BAD_REQUEST);
			ctx.json(e.getMessage());
		}
	}

	public void getLockedNftList(Context ctx) {
		try {
			String address = ctx.queryParam("address");
			if (StringUtils.isBlank(address)) {
				ctx.status(404);
				return;
			}
			if (!updateReceiver.ready) {
				ctx.status(HttpCode.FORBIDDEN);
				String res = "{\n" +
						"  \"error\": \"Please wait, server is not ready" + "\"\n" +
						"}";
				ctx.json(res);
				return;
			}
			NftItems items = tonApi.getNftsFromCollection(address, collection, 2222, 0);
			log.info("Found {} nft items from collection {}", items.nft_items.size(), collection);
			List<NftItem> lockedNfts = items.nft_items.parallelStream()
					.filter(nftItem -> updateReceiver.nftLockState.containsKey(nftItem.userFriendlyAddress()))
					.filter(nftItem -> updateReceiver.nftLockState.get(nftItem.userFriendlyAddress()))
					.collect(Collectors.toList());
			ctx.json(lockedNfts);
		} catch (Exception e) {
			log.error("Failed to get locked nft list: ", e);
			ctx.status(HttpCode.INTERNAL_SERVER_ERROR);
			ctx.json(e.getMessage());
		}
	}

	public void getBoc(Context ctx, boolean lock) {
		try {
			TonClient tonUtils = TonClient.INSTANCE;
			String encoded = tonUtils.internalMessageAsBock(lock);
			String res = "{\n" +
					"  \"boc\": \"" + encoded + "\"\n" +
					"}";
			ctx.json(res);
		} catch (Exception e) {
			log.error("Failed to generate boc: ", e);
			ctx.status(HttpCode.BAD_REQUEST);
			ctx.json(e.getMessage());
		}
	}

	public static String getCollectionAddress() {
		Properties properties = new Properties();
		try (var resourceStream = Controller.class.getResourceAsStream("/app.properties")){
			properties.load(resourceStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return properties.getProperty("collection");
	}
}
