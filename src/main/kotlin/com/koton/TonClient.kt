package com.koton

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.ton.api.liteclient.config.LiteClientConfigGlobal
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.CellBuilder
import org.ton.lite.api.exception.LiteServerException
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.client.LiteClient
import java.lang.invoke.MethodHandles
import java.net.URL
import java.util.*

object TonClient {

	private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
	val liteClient: LiteClient

	init {
		log.info("initializing lite client configuration")
		val json = Json { ignoreUnknownKeys = true }
		val config = json.decodeFromString<LiteClientConfigGlobal>(
			URL("https://ton.org/global-config.json").readText()
		)
		log.info("Initializing lite client")
		liteClient = LiteClient(CoroutineName("tonClient"), config)
	}


	fun bouildTonkeeperLink(addr: String, lock: Boolean, amount: Int) : String {
		val base = internalMessageAsBock(lock)
		val ss = "{\n" +
				"  \"version\": \"0\",\n" +
				"  \"body\": {\n" +
				"    \"type\": \"sign-raw-payload\",\n" +
				"    \"params\": {\n" +
				"      \"messages\": [\n" +
				"        {\n" +
				"          \"address\": \""+addr+"\",\n" +
				"          \"amount\": \""+amount+"\",\n" +
				"          \"payload\": \""+base+"\"\n" +
				"        }\n" +
				"      ]\n" +
				"    }\n" +
				"  }\n" +
				"}";
		return "https://app.tonkeeper.com/v1/txrequest-inline/" + Base64.getUrlEncoder().encodeToString(ss.toByteArray())
	}

	fun internalMessageAsBock(lock: Boolean) : String {
		val queryId = 0
		val intMsg = CellBuilder()
			.storeUInt(0x7af0035f, 32)
			.storeUInt(queryId, 64)
			.storeBit(lock)
			.endCell()
		val boc = BagOfCells(intMsg)
		return Base64.getUrlEncoder().encodeToString(boc.toByteArray())
	}


	fun getNftFromCollection(addr: String, index: Int, lastBlockIdExt: TonNodeBlockIdExt?): MsgAddress {
		try {
			val param = VmStackValue(index)
			val params = VmStackList(param)
			val result = runGetMethod(addr, "get_nft_address_by_index", VmStack(params), lastBlockIdExt)
			val slice = result.get(0) as VmCellSlice
			return MsgAddress.Companion.loadTlb(slice.toCellSlice())
		} catch (e: Exception) {
			log.info("Failed to get nft from collection: {}", e.message)
			return getNftFromCollection(addr, index, lastBlockIdExt)
		}
	}

	fun getCollectionDataNextItemIndex(addr: String, lastBlockIdExt: TonNodeBlockIdExt?): Int{
		val res = runGetMethod(addr, "get_collection_data", null, lastBlockIdExt)
		val nextIndex = res.get(2) as VmStackTinyInt
		val ownerAddr = res.get(0) as VmCellSlice
		var ownerMsgAddress = MsgAddress.loadTlb(ownerAddr.toCellSlice())
		return nextIndex.toInt()
	}

	fun checkLockStatus(addr: String, lastBlockIdExt: TonNodeBlockIdExt?): Boolean{
		try {
			val res: VmStack
			if (lastBlockIdExt == null) {
				res = runGetMethod(addr, "get_lock_status", null, null)
			} else {
				res = runGetMethod(addr, "get_lock_status", null, lastBlockIdExt)
			}
			val adminLock = res[0] as VmStackTinyInt
			val userLock = res[1] as VmStackTinyInt

			return adminLock.toInt() != 0 || userLock.toInt() != 0
		} catch (e: Exception) {
			log.info("Failed to check lock status: {}", e.message)
			return checkLockStatus(addr, lastBlockIdExt)
		}
	}

	fun getLastBlock() : TonNodeBlockIdExt = runBlocking {
		liteClient.getLastBlockId()
	}

	fun runGetMethod(addr: String, method: String): VmStack {
		return runGetMethod(addr, method, null, null)
	}

	fun runGetMethod(addr: String, method: String, vmStack: VmStack?, lastBlockIdExt: TonNodeBlockIdExt?): VmStack {
		try {
			return runBlocking {
				val address = LiteServerAccountId(addr)
				if (vmStack != null && lastBlockIdExt != null) {
					liteClient.runSmcMethod(address, lastBlockIdExt, method, vmStack)
				} else if (vmStack == null && lastBlockIdExt != null) {
					liteClient.runSmcMethod(address, lastBlockIdExt, method)
				} else if (lastBlockIdExt == null && vmStack != null) {
					liteClient.runSmcMethod(address, method, vmStack)
				} else {
					liteClient.runSmcMethod(address, method, Collections.emptyList())
				}
			}
		} catch (e:LiteServerException) {
			log.info("LiteServer exception: {}", e.message)
			throw RuntimeException(e);
		}
	}
}
