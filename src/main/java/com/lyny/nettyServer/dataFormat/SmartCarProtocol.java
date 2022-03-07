package com.lyny.nettyServer.dataFormat;

public class SmartCarProtocol {
    private byte head_data;//消息的开头信息标志
    /**
     * 消息的内容
     */
    private byte[] content;

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public SmartCarProtocol() {
    }

    public byte getHead_data() {
        return head_data;
    }

    public void setHead_data(byte head_data) {
        this.head_data = head_data;
    }

    public SmartCarProtocol(byte head_data, byte[] content) {
        this.head_data = head_data;
        this.content = content;
    }
}
