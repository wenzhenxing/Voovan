package org.voovan.test.network;


import org.voovan.network.ConnectModel;
import org.voovan.network.HeartBeat;
import org.voovan.network.IoHandler;
import org.voovan.network.IoSession;
import org.voovan.network.udp.UdpServerSocket;
import org.voovan.tools.log.Logger;

public class ServerHandlerTest implements IoHandler {

	@Override
	public Object onConnect(IoSession session) {
		Logger.simple("onConnect");
		return null;
	}

	@Override
	public void onDisconnect(IoSession session) {
		Logger.simple("onDisconnect");
	}

	@Override
	public Object onReceive(IoSession session, Object obj) {
		Logger.simple(session.remoteAddress()+":"+session.remotePort());
        Logger.simple("Server onRecive: "+obj.toString());
        return "==== "+obj.toString().trim()+" ===== \r\n";
    }

    @Override
    public void onException(IoSession session, Exception e) {
		e.printStackTrace();
        Logger.error("Server Exception",e);
        session.close();
	}

	@Override
	public void onIdle(IoSession session) {
		//心跳依赖于 idle 时间,这个参数在构造 socket 的时候设置具体查看org.voovan.network.aio.AioServerSocket

		//服务端和客户端使用了两种不同的心跳绑定方式,这是其中一种
		//心跳绑定到 Session, 绑定过一次以后每次返回的都是第一次绑定的对象
		HeartBeat heartBeat = HeartBeat.attachSession(session, ConnectModel.SERVER, "PINGq", "PONGq");

		//心跳一次, 返回 true:本次心跳成功, false: 本次心跳失败
		System.out.println("==>"+heartBeat.beat(session));

		//UdpSocket是 Server 模式关闭可能导致关闭异常
		//如果是 UdpServer 模式这里只能作为心跳响应
		if(session.socketContext().getConnectModel() != ConnectModel.SERVER) {
			if (heartBeat.getFieldCount() > 5) {
				session.close();
			}
		}
	}

	@Override
	public void onSent(IoSession session, Object obj) {
		String sad = (String)obj;
		Logger.simple("Server onSent: " + sad);
		//jmeter 测试是需要打开,和客户端测试时关闭
//		session.close();
	}

}
