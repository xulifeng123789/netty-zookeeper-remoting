package com.beidahuang.handler;

import com.beidahuang.api.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcServerHandler extends ChannelInboundHandlerAdapter {

    private Map map = new ConcurrentHashMap();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        RpcRequest request = (RpcRequest) msg;
        //获取类名
        String className = request.getClassName();
        if(map.containsKey(className)) {
            Object service = map.get(className);
            Method method = service.getClass().getMethod(request.getMethodName(), request.getMethodTypeParam());
            result = method.invoke(service,request.getParams());//反射调用
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();

    }

    public RpcServerHandler(Map map) {
        this.map = map;
    }
}
