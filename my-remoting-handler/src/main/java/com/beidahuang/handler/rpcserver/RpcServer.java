package com.beidahuang.handler.rpcserver;

import com.beidahuang.annotation.RpcService;
import com.beidahuang.handler.RpcServerHandler;
import com.beidahuang.register.IRegisterCenter;
import com.beidahuang.util.NetUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.zookeeper.KeeperException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcServer {

    private static Map<String,Object>  map = new ConcurrentHashMap<String,Object>();//存放需要发布的对象

    private IRegisterCenter registerCenter;
    private String url;

    public RpcServer(IRegisterCenter registerCenter, String url) {

        this.registerCenter = registerCenter;
        this.url = url;
    }

    /**
     * 绑定服务
     * @param services
     */
    public void bind(Object ... services ) {

        for(Object service : services) {

            RpcService annotation = service.getClass().getAnnotation(RpcService.class);
            String serviceName = annotation.value().getName();
            map.put(serviceName,service);
        }
    }

    //注册服务并且监听
    public void registetrAndListener() throws KeeperException, InterruptedException {

        //注册服务
        for(String serviceName : map.keySet()) {
            registerCenter.register(serviceName,url);
        }
        //监听端口，与客户端通信
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup,workGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {

                ChannelPipeline channelPipeline = ch.pipeline();
                channelPipeline.addLast("encoder",new ObjectEncoder());
                channelPipeline.addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
                channelPipeline.addLast(new RpcServerHandler(map));
            }
        });
        String[] split = url.split(":");
        String ip = split[0];
        String port = split[1];

        ChannelFuture f = null;
        f = serverBootstrap.bind(ip,Integer.parseInt(port)).sync();


        //ChannelFuture f = serverBootstrap.bind(ip,Integer.parseInt(port)).sync();
//        System.out.println("netty服务器端启动成功，等待客户端连接....");
//        f.channel().closeFuture().sync();

    }

}
