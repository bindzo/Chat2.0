package com.muc;

public interface TopicUsageListener {
    public void join(String topic);
    public void leave(String topic);
}
