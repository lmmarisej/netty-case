package org.lmmarise.netty.chat.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.concurrent.EventExecutorGroup;
import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义IM协议的编码器
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/30 6:42 下午
 */
public class IMDecoder extends ByteToMessageDecoder {

    private final Pattern pattern = Pattern.compile("^\\[(.*)\\](\\s\\-\\s(.*))?");     // 解析IM协议下请求内容的正则

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            final int length = in.readableBytes();      // 获取可读字节数
            final byte[] array = new byte[length];
            String content = new String(array, in.readerIndex(), length);
            if (!"".equals(content.trim())) {           // 空消息不解析
                if (!IMP.isIMP(content)) {
                    ctx.channel().pipeline().remove(this);
                    return;
                }
            }
            in.getBytes(in.readerIndex(), array, 0, length);
            out.add(new MessagePack().read(array, IMMessage.class));    // 将msgPack格式的数据反序列化为Java实体
            in.clear();
        } catch (MessageTypeException e) {
            ctx.channel().pipeline().remove(this);
        }
    }

    /**
     * 字符串解析成自定义即时通信协议
     */
    public IMMessage decode(String msg) {
        if (null == msg || "".equals(msg.trim())) {
            return null;
        }
        try {
            Matcher m = pattern.matcher(msg);
            String header = "";
            String content = "";
            if (m.matches()) {
                header = m.group(1);
                content = m.group(3);
            }
            String[] headers = header.split("\\]\\[");
            long time = 0;
            try {
                time = Long.parseLong(headers[1]);
            } catch (Exception ignored) {
            }
            String nickName = headers[2];
            nickName = nickName.length() < 10 ? nickName : nickName.substring(0, 9);        // 昵称最多十个字
            if (msg.startsWith("[" + IMP.LOGIN.getName() + "]")) {
                return new IMMessage(headers[0], headers[3], time, nickName);
            } else if (msg.startsWith("[" + IMP.CHAT.getName() + "]")) {
                return new IMMessage(headers[0], time, nickName, content);
            } else if (msg.startsWith("[" + IMP.FLOWER.getName() + "]")) {
                return new IMMessage(headers[0], headers[3], time, nickName);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
