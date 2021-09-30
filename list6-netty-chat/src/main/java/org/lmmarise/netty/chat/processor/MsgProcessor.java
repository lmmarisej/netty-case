package org.lmmarise.netty.chat.processor;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.json.simple.JSONObject;
import org.lmmarise.netty.chat.protocol.IMDecoder;
import org.lmmarise.netty.chat.protocol.IMEncoder;
import org.lmmarise.netty.chat.protocol.IMMessage;
import org.lmmarise.netty.chat.protocol.IMP;

/**
 * 自定义协议内容逻辑处理
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/30 7:02 下午
 */
public class MsgProcessor {
    private static final ChannelGroup onlineUsers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);  // 在线用户
    public static final AttributeKey<String> NICK_NAME = AttributeKey.valueOf("nickName");  // 扩展属性
    public static final AttributeKey<String> IP_ADDR = AttributeKey.valueOf("ipAddr");
    public static final AttributeKey<JSONObject> ATTRS = AttributeKey.valueOf("attrs");
    public static final AttributeKey<String> FROM = AttributeKey.valueOf("from");
    private final IMDecoder decoder = new IMDecoder();    // 自定义解码器
    private final IMEncoder encoder = new IMEncoder();    // 自定义编码器

    /**
     * 获取用户昵称
     */
    public String getNickName(Channel client) {
        return client.attr(NICK_NAME).get();
    }

    /**
     * 获取用户远程IP地址
     */
    public String getAddress(Channel client) {
        return client.remoteAddress().toString().replaceFirst("/", "");
    }

    /**
     * 获取扩展属性
     */
    public JSONObject getAttrs(Channel client) {
        try {
            return client.attr(ATTRS).get();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取扩展属性
     */
    private void setAttrs(Channel client, String key, Object value) {
        try {
            JSONObject json = client.attr(ATTRS).get();
            json.put(key, value);
            client.attr(ATTRS).set(json);
        } catch (Exception e) {
            JSONObject json = new JSONObject();
            json.put(key, value);
            client.attr(ATTRS).set(json);
        }
    }

    /**
     * 登出通知
     */
    public void logout(Channel client) {
        // 如果nickName为null，没有遵从聊天协议的连接，表示未非法登录
        if (getNickName(client) == null) {
            return;
        }
        for (Channel channel : onlineUsers) {
            IMMessage request = new IMMessage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(),
                    getNickName(client) + "离开");
            String content = encoder.encode(request);
            channel.writeAndFlush(new TextWebSocketFrame(content));
        }
        onlineUsers.remove(client);
    }

    /**
     * 发送消息
     */
    public void sendMsg(Channel client, IMMessage msg) {
        sendMsg(client, encoder.encode(msg));
    }

    /**
     * 发送消息
     */
    public void sendMsg(Channel client, String msg) {
        IMMessage request = decoder.decode(msg);
        if (null == request) {
            return;
        }
        String addr = getAddress(client);
        if (request.getCmd().equals(IMP.LOGIN.getName())) {
            client.attr(NICK_NAME).getAndSet(request.getSender());
            client.attr(IP_ADDR).getAndSet(addr);
            client.attr(FROM).getAndSet(request.getTerminal());
//			System.out.println(client.attr(FROM).get());
            onlineUsers.add(client);
            for (Channel channel : onlineUsers) {
                boolean isSelf = (channel == client);
                if (!isSelf) {
                    request = new IMMessage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(),
                            getNickName(client) + "加入");
                } else {
                    request = new IMMessage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(),
                            "已与服务器建立连接！");
                }
                if ("Console".equals(channel.attr(FROM).get())) {
                    channel.writeAndFlush(request);
                    continue;
                }
                String content = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(content));
            }
        } else if (request.getCmd().equals(IMP.CHAT.getName())) {
            for (Channel channel : onlineUsers) {
                boolean isSelf = (channel == client);
                if (isSelf) {
                    request.setSender("you");
                } else {
                    request.setSender(getNickName(client));
                }
                request.setTime(sysTime());
                if ("Console".equals(channel.attr(FROM).get()) & !isSelf) {
                    channel.writeAndFlush(request);
                    continue;
                }
                String content = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(content));
            }
        } else if (request.getCmd().equals(IMP.FLOWER.getName())) {
            JSONObject attrs = getAttrs(client);
            if (null != attrs) {
                long lastTime = (long) attrs.get("lastFlowerTime");
                // 60秒之内不允许重复刷鲜花
                int seconds = 1;
                long sub = sysTime() - lastTime;
                if (sub < 1000 * seconds) {
                    request.setSender("you");
                    request.setCmd(IMP.SYSTEM.getName());
                    request.setContent("您送鲜花太频繁," + (seconds - Math.round(sub / 1000f)) + "秒后再试");
                    String content = encoder.encode(request);
                    client.writeAndFlush(new TextWebSocketFrame(content));
                    return;
                }
            }
            sendFlower(client, request);
        }
    }

    private void sendFlower(Channel client, IMMessage request) {
        for (Channel channel : onlineUsers) {
            if (channel == client) {
                request.setSender("you");
                request.setContent("你给大家送了一波鲜花雨");
                setAttrs(client, "lastFlowerTime", sysTime());
            } else {
                request.setSender(getNickName(client));
                request.setContent(getNickName(client) + "送来一波鲜花雨");
            }
            request.setTime(sysTime());
            String content = encoder.encode(request);
            channel.writeAndFlush(new TextWebSocketFrame(content));
        }
    }

    /**
     * 获取系统时间
     */
    private Long sysTime() {
        return System.currentTimeMillis();
    }
}
