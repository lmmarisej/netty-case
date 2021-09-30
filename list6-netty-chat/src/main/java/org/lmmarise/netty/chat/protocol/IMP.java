package org.lmmarise.netty.chat.protocol;

/**
 * 自定义IM协议，Instant Messaging Protocol即时通信协议
 *
 * @author lmmarise.j@gmail.com
 * @since 2021/9/30 6:44 下午
 */
public enum IMP {

    /**
     * 系统消息
     */
    SYSTEM("SYSTEM"),
    /**
     * 登录指令
     */
    LOGIN("LOGIN"),
    /**
     * 登出指令
     */
    LOGOUT("LOGOUT"),
    /**
     * 聊天消息
     */
    CHAT("CHAT"),
    /**
     * 送鲜花
     */
    FLOWER("FLOWER");

    public static boolean isIMP(String content) {
        return content.matches("^\\[(SYSTEM|LOGIN|LOGIN|CHAT)\\]");
    }

    private final String name;

    IMP(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }
}
