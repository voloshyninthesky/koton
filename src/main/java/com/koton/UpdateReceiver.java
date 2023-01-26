package com.koton;

import com.koton.tonapi.Block.BlockState;
import com.koton.tonapi.Block.LastBlock.LastBlock;
import com.koton.tonapi.Block.Transaction;
import com.koton.tonapi.TonApi;
import com.koton.tonapi.Transaction.TransactionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ton.api.tonnode.TonNodeBlockId;
import org.ton.api.tonnode.TonNodeBlockIdExt;
import org.ton.block.AddrStd;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class UpdateReceiver {
	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final TonApi tonApi = new TonApi();
	private static final TonClient tonUtils = TonClient.INSTANCE;
	private static final int waitUntilExistsMillis = 6200;
	public final Map<String, Boolean> nftLockState;
	private TonNodeBlockIdExt lastCheckedBlock;
	private final ExecutorService initExecutor = Executors.newFixedThreadPool(500);
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private static final String collectionAddr = Controller.getCollectionAddress();
	public boolean ready = false;

	public UpdateReceiver(TonNodeBlockIdExt lastBlock, Map<String, Boolean> nftLockState) {
		this.lastCheckedBlock = lastBlock;
		this.nftLockState = nftLockState;
    }

    public void start() {
		init();
		ready = true;
        Supplier<ScheduledFuture> supplier = () -> executorService.scheduleWithFixedDelay(this::checkNewBlocksAndUpdateState,
            0,
            2,
            TimeUnit.SECONDS);
        supplier.get();
    }

	public void init() {
		logger.info("Initializing nft locked map");
		TonNodeBlockIdExt lastBlock = tonUtils.getLastBlock();
		while (!lastBlock.isValid()) {
			logger.info("Waiting until last block {} is ready", lastBlock.getSeqno());
			lastBlock = tonUtils.getLastBlock();
		}
		logger.info("Last block = {}", lastBlock.getSeqno());
		TonNodeBlockIdExt finalLastBlock = lastBlock;
		lastCheckedBlock = lastBlock;
		int index = tonUtils.getCollectionDataNextItemIndex(collectionAddr, lastBlock);
		for (int i = 0; i < index; i++) {
			int finalI = i;
			initExecutor.execute(() -> {
				AddrStd address = (AddrStd) tonUtils.getNftFromCollection(collectionAddr, finalI, finalLastBlock);
				String nftAddr = address.toString(true, true, false, true);
				boolean isLocked = tonUtils.checkLockStatus(nftAddr, finalLastBlock);
				logger.info("Initializing nft {} with locked {}", nftAddr, isLocked);
				nftLockState.put(nftAddr, isLocked);
			});
		}
	}

    private void checkNewBlocksAndUpdateState() {
		if (!ready) {
			init();
			ready = true;
		}
        logger.info("Receiving updates");
		try {
			Long lastCheckedBlockId1 = (long) lastCheckedBlock.getSeqno();
			LastBlock lastBlock = tonApi.getLastBlock();
			long lastBlockSeqno = lastBlock.getLast().getSeqno();
			int fails = 0;
			while (lastCheckedBlockId1 < lastBlockSeqno) {
				boolean success = findAndProcessRelatedTransactionsInBlock(lastCheckedBlockId1 + 1, nftLockState);
				if (success) {
					lastCheckedBlockId1 += 1;
					var lastCheckedBlockId = TonNodeBlockId.of(lastCheckedBlock.getWorkchain(), lastCheckedBlock.getShard(), lastCheckedBlock.getSeqno() + 1);
					lastCheckedBlock = new TonNodeBlockIdExt(lastCheckedBlockId);
				} else {
					logger.warn("Failed to check block {}. Waiting {} milliseconds", lastCheckedBlockId1 + 1, waitUntilExistsMillis);
					Thread.sleep(waitUntilExistsMillis);
					fails++;
				}
				if (fails > 10) {
					logger.error("Too much errors. Restarting service");
					ready = false;
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Failed to check new blocks: ", e);
		}
	}

	private boolean findAndProcessRelatedTransactionsInBlock(Long blockSeqno, Map<String, Boolean> nftLockState) {
		try {
			BlockState block = tonApi.getBlock(blockSeqno);
			if (!block.exist) {
				return false;
			}
			logger.info("Checking block with seqno {}", blockSeqno);
			block.block.getShards().parallelStream()
					.flatMap(shard -> shard.getTransactions().parallelStream()
							.filter(transaction -> nftLockState.containsKey(transaction.getAccount()) || transaction.getAccount().equalsIgnoreCase(collectionAddr)))
					.forEach(this::processTransaction);
		} catch (Exception e){
			logger.error("Failed to check block #{}", blockSeqno, e);
			return false;
		}
		return true;
	}

	private void processTransaction(Transaction transaction) {
		if (transaction.getAccount().equalsIgnoreCase(collectionAddr)) {
			logger.info("Found transaction on the collection");
			TransactionDetails transactionDetails = tonApi.getTransactionDetails(transaction.getHash());
			transactionDetails.out_msgs.stream()
					.filter(outMsg -> outMsg.op == 0x54753766)
					.findAny()
					.ifPresent(outMsg -> {
						logger.info("NFT {} was minted. Added to state as not locked", outMsg.destination);
						nftLockState.put(outMsg.destination, false);
					});
		} else {
			logger.info("found transaction on the nft {}", transaction.getAccount());
			boolean lockStatus = TonClient.INSTANCE.checkLockStatus(transaction.getAccount(), null);
			logger.info("account {} has lock = {}", transaction.getAccount(), lockStatus);
			nftLockState.put(transaction.getAccount(), lockStatus);
		}
	}
}
