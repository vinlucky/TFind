package com.tfind.toilet.grpc.client;

import com.tfind.user.grpc.UserServiceGrpc;
import com.tfind.user.grpc.UserServiceProto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class UserGrpcClient {

    @Value("${grpc.client.user-service.address:static://127.0.0.1:9091}")
    private String userServiceAddress;

    private ManagedChannel channel;
    private UserServiceGrpc.UserServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        String address = userServiceAddress.replace("static://", "");
        String[] parts = address.split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9091;

        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = UserServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    public UserResponse getUser(String userId) {
        UserRequest request = UserRequest.newBuilder()
                .setUserId(userId)
                .build();
        return blockingStub.getUser(request);
    }

    public String getUserIdByOpenid(String openid) {
        OpenidRequest request = OpenidRequest.newBuilder()
                .setOpenid(openid)
                .build();
        UserIdResponse response = blockingStub.getUserIdByOpenid(request);
        return response.getUserId();
    }
}
