package com.koton.tonapi;

import com.koton.TonClient;
import com.koton.tonapi.Block.BlockState;
import com.koton.tonapi.Block.LastBlock.LastBlock;
import com.koton.tonapi.GetMethod.GetMethodRequest;
import com.koton.tonapi.GetMethod.GetMethodResult;
import com.koton.tonapi.Nft.NftItems;
import com.koton.tonapi.Transaction.TransactionDetails;
import io.javalin.plugin.json.JavalinJackson;
import org.eclipse.jetty.util.UrlEncoded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class TonApi {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String tonHubEndpoint = "https://mainnet-v4.tonhubapi.com";
	private static final String tonCenterIndexEndpoint = "https://toncenter.com/api/index";
	private static final String tonCenterV2Endpoint = "https://toncenter.com/api/v2";
	private static final String toncenterApiKey = "a4b4faa419fc1ed5cea4305a197da5479a1171bc90221d6c9692887f2381e71c";
	private static final String tonApiEndpoint = "https://tonapi.io/v1";
	private static final String tonApiKey = "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsidnV4ZXMiXSwiZXhwIjoxODMxODI0MjU3LCJpc3MiOiJAdG9uYXBpX2JvdCIsImp0aSI6IkVJSElVRkpZNkVMUEtHUzVQVVo0UkdFRiIsInNjb3BlIjoic2VydmVyIiwic3ViIjoidG9uYXBpIn0.o7GZcpqwxqf8firazakaTTkJzZJg2SKkpBrk9QQHL89C-U8xJDA8mAhDv6FWPyzeLxtKdSNZ79bdGmupQGapAA";
	private static HttpClient httpClient;
	private static JavalinJackson javalinJackson;

	private static TonClient tonUtils;

	public TonApi() {
		httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(30))
				.build();
		javalinJackson = new JavalinJackson();
	}

	public LastBlock getLastBlock() {
		try {
			HttpRequest httpRequest = get(tonHubEndpoint + "/block/latest");
			HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				throw new RuntimeException("Failed to receive block info. Status code: " + response.statusCode() + response.body());
			}
			return javalinJackson.fromJsonString(response.body(), LastBlock.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public BlockState getBlock(Long blockId) {
		try {
			HttpRequest get = get(tonHubEndpoint + "/block/" + blockId);
			HttpResponse<String> response = httpClient.send(get, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				throw new RuntimeException("Failed to receive block " + blockId +" info. Status code  " + response.statusCode() + response.body());
			}
			return javalinJackson.fromJsonString(response.body(), BlockState.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public TransactionDetails getTransactionDetails(String txHash) {
		txHash = UrlEncoded.encodeString(txHash);
		try {
			HttpRequest get = get(tonCenterIndexEndpoint +"/getTransactionByHash?tx_hash="+txHash+"&include_msg_body=true");
			HttpResponse<String> response = httpClient.send(get, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				throw new RuntimeException("Failed to receive transaction details. Status code: " + response.statusCode()+ " " + response.body());
			}
			TransactionDetails[] transactionDetails = javalinJackson.fromJsonString(response.body(), TransactionDetails[].class);
			return transactionDetails[0];
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public NftItems getNftListFromCollection(String account, String collection, int limit, int offset) {
		try {
			HttpRequest get = get(tonApiEndpoint+"/nft/searchItems" +
					"?owner=" + account +
					"&collection=" + collection +
					"&include_on_sale=true" +
					"&limit=" + limit +
					"&offset=" + offset,
					tonApiKey
			);

			HttpResponse<String> response = httpClient.send(get, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				throw new RuntimeException("Failed to receive nft list. Status code: " + response.statusCode()+ " " + response.body());
			}
			return javalinJackson.fromJsonString(response.body(), NftItems.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public GetMethodResult runGetMethod(String account, String method, List<Object> stack) {
		try {
			GetMethodRequest request = new GetMethodRequest();
			request.address = account;
			request.method = method;
			request.stack = stack;
			HttpRequest post = post(tonCenterV2Endpoint + "/runGetMethod?api_key="+toncenterApiKey, javalinJackson.toJsonString(request));
			HttpResponse<String> response = httpClient.send(post, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				throw new RuntimeException("Failed to execute get method . Status code: " + response.statusCode()+ " " + response.body());
			}
			return javalinJackson.fromJsonString(response.body(), GetMethodResult.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isNftLocked(String address) {
		GetMethodResult lockedNftResult = runGetMethod(address, "get_lock_status", Collections.emptyList());
		String adminLock = (String)lockedNftResult.result.stack.get(0).get(1);
		String userLock = (String)lockedNftResult.result.stack.get(1).get(1);
		return Integer.decode(adminLock) == 1 || Integer.decode(userLock) == 1;
	}

	private HttpRequest get(String url) throws URISyntaxException {
		return get(url, null);
	}


	private HttpRequest get(String url, String bearer) throws URISyntaxException {
		if (bearer == null) {
			return HttpRequest.newBuilder()
					.uri(new URI(url))
					.GET()
					.build();
		} else {
			return HttpRequest.newBuilder()
					.uri(new URI(url))
					.setHeader("Authorization", "Bearer " + bearer)
					.GET()
					.build();
		}
	}


	private HttpRequest post(String url, String body) throws URISyntaxException {
		return HttpRequest.newBuilder()
				.uri(new URI(url))
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
	}
}
