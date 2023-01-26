package com.koton;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ConcurrentHashMap;

public class App  {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static Javalin javalin;
	private static UpdateReceiver updateReceiver;

	public static void main(String[] args) {
		updateReceiver = new UpdateReceiver(null, new ConcurrentHashMap<>());
		Controller controller = new Controller(updateReceiver);
		int port;
		try {
			port = Integer.parseInt(System.getenv("TONAPI_PORT"));
		} catch (Exception e) {
			log.warn("Failed to PORT env variable, setting to 8099");
			port = 8099;
		}
		javalin = Javalin.create()
				.get("/getTonkeeperLink/lock", ctx -> controller.getTonKeeperLink(ctx, true))
				.get("/getTonkeeperLink/unlock", ctx -> controller.getTonKeeperLink(ctx, false))
				.get("/getBoc/lock", ctx-> controller.getBoc(ctx, true))
				.get("/getBoc/unlock", ctx-> controller.getBoc(ctx, false))
				.get("/getLockedItems", controller::getLockedNftList)
				.start(port);

		javalin.exception(Exception.class, (e, ctx) -> ctx.json(e.getMessage()));

		log.info("Starting update receiver");
		updateReceiver.start();
	}
}
