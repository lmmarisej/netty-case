package org.lmmarise.netty.chat.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.EventExecutorGroup;
import org.lmmarise.netty.chat.protocol.IMMessage;
import org.msgpack.MessagePack;

/**
 * @author lmmarise.j@gmail.com
 * @since 2021/9/30 6:59 下午
 */
public class IMEncoder extends MessageToByteEncoder<IMMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, IMMessage msg, ByteBuf out) throws Exception {
        out.writeBytes(new MessagePack().write(msg));
    }

    public String encode(IMMessage msg) {
        if (null == msg) {
            return "";
        }
        String prex = "[" + msg.getCmd() + "][" + msg.getTime() + "]";
        if (IMP.LOGIN.getName().equals(msg.getCmd()) || IMP.FLOWER.getName().equals(msg.getCmd())) {
            prex += ("[" + msg.getSender() + "][" + msg.getTerminal() + "]");
        } else if (IMP.CHAT.getName().equals(msg.getCmd())) {
            prex += ("[" + msg.getSender() + "]");
        } else if (IMP.SYSTEM.getName().equals(msg.getCmd())) {
            prex += ("[" + msg.getOnline() + "]");
        }
        if (!(null == msg.getContent() || "".equals(msg.getContent()))) {
            prex += (" - " + msg.getContent());
        }
        return prex;
    }

}
