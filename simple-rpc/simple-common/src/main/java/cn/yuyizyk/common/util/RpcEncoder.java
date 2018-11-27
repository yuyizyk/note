package cn.yuyizyk.common.util;

import cn.yuyizyk.common.util.seriailize.ProtostuffSerializeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<Object> {
	private Class<?> genericClass;
	private final ProtostuffSerializeUtil protostuffSerializeUtil = new ProtostuffSerializeUtil();

	public RpcEncoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		if (genericClass.isInstance(msg)) {
			byte[] data = protostuffSerializeUtil.serialize(msg);
			out.writeInt(data.length);
			out.writeBytes(data);
		}
	}

}
