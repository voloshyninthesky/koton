package com.koton;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ConcurrentHashMap;

public class App  {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static void main(String[] args) {
		UpdateReceiver updateReceiver = new UpdateReceiver(new ConcurrentHashMap<>());
		Controller controller = new Controller(updateReceiver);
		int port;
		try {
			port = Integer.parseInt(System.getenv("TONAPI_PORT"));
		} catch (Exception e) {
			log.warn("Failed to PORT env variable, setting to 8099");
			port = 8099;
		}
		Javalin javalin = Javalin.create()
				.get("/getTonkeeperLink/lock", ctx -> controller.getTonKeeperLink(ctx, true))
				.get("/getTonkeeperLink/unlock", ctx -> controller.getTonKeeperLink(ctx, false))
				.get("/getBoc/lock", ctx -> controller.getBoc(ctx, true))
				.get("/getBoc/unlock", ctx -> controller.getBoc(ctx, false))
				.get("/getLockedItems", controller::getLockedNftList)
				.start(port);

		javalin.exception(Exception.class, (e, ctx) -> ctx.json(e.getMessage()));

		log.info("Starting update receiver");
		updateReceiver.start();
	}
}
