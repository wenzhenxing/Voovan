package org.voovan.test.http;

import org.voovan.http.message.HttpStatic;
import org.voovan.http.server.HttpRequest;
import org.voovan.http.server.HttpResponse;
import org.voovan.http.server.HttpRouter;
import org.voovan.http.server.WebServer;
import org.voovan.http.server.context.WebContext;
import org.voovan.http.server.context.WebServerConfig;
import org.voovan.tools.TObject;
import org.voovan.tools.json.JSON;
import org.voovan.tools.log.Logger;

import java.util.Map;


public class VoovanTFB {
	public static final byte[] HELLO_WORLD = "Hello, World!".getBytes();
	public static final Map MAP = TObject.asMap("message", "Hello, World!");


	public static void main(String[] args) {

		WebServerConfig webServerConfig = WebContext.getWebServerConfig();
		webServerConfig.setGzip(false);
		webServerConfig.setAccessLog(false);
		webServerConfig.setKeepAliveTimeout(1000);
		webServerConfig.setHost("0.0.0.0");
		webServerConfig.setPort(28080);
		webServerConfig.setHotSwapInterval(0);
		webServerConfig.setCache(true);
		webServerConfig.getModuleonfigs().clear();
		webServerConfig.getRouterConfigs().clear();
		WebServer webServer = WebServer.newInstance(webServerConfig);
		Logger.setEnable(true);

		//性能测试请求;
		webServer.get("/plaintext", new HttpRouter() {
			public void process(HttpRequest req, HttpResponse resp) throws Exception {
				resp.header().remove(HttpStatic.CONNECTION_STRING);
				resp.write(HELLO_WORLD);
			}
		});
		//性能测试请求
		webServer.get("/json", new HttpRouter() {
			public void process(HttpRequest req, HttpResponse resp) throws Exception {
				resp.header().put("Content-Type", "application/json");
				resp.write(JSON.toJSON(MAP, false, false));
			}
		});


		webServer.syncServe();
	}
}
