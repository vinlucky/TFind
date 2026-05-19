package com.tfind.user.grpc.client;

import com.tfind.toilet.grpc.ToiletServiceGrpc;
import com.tfind.toilet.grpc.ToiletServiceProto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Component
public class ToiletGrpcClient {

    @Value("${grpc.client.toilet-service.address:static://127.0.0.1:9092}")
    private String toiletServiceAddress;

    private ManagedChannel channel;
    private ToiletServiceGrpc.ToiletServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        String address = toiletServiceAddress.replace("static://", "");
        String[] parts = address.split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9092;

        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = ToiletServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    public List<ToiletResponse> getNearbyToilets(double lat, double lng, String mode, double distance) {
        NearbyToiletsRequest request = NearbyToiletsRequest.newBuilder()
                .setLat(lat)
                .setLng(lng)
                .setMode(mode)
                .setDistance(distance)
                .build();
        ToiletListResponse response = blockingStub.getNearbyToilets(request);
        return response.getToiletsList();
    }

    public ToiletResponse getToiletById(String id) {
        ToiletIdRequest request = ToiletIdRequest.newBuilder()
                .setId(id)
                .build();
        return blockingStub.getToiletById(request);
    }

    public ToiletResponse createToilet(ToiletRequest request) {
        return blockingStub.createToilet(request);
    }

    public ToiletResponse updateToilet(String id, ToiletRequest request) {
        ToiletUpdateRequest updateRequest = ToiletUpdateRequest.newBuilder()
                .setId(id)
                .setToilet(request)
                .build();
        return blockingStub.updateToilet(updateRequest);
    }

    public OperationResponse softDeleteToilet(String id) {
        ToiletIdRequest request = ToiletIdRequest.newBuilder()
                .setId(id)
                .build();
        return blockingStub.softDeleteToilet(request);
    }

    public OperationResponse restoreToilet(String id) {
        ToiletIdRequest request = ToiletIdRequest.newBuilder()
                .setId(id)
                .build();
        return blockingStub.restoreToilet(request);
    }

    public OperationResponse physicalDeleteToilet(String id) {
        ToiletIdRequest request = ToiletIdRequest.newBuilder()
                .setId(id)
                .build();
        return blockingStub.physicalDeleteToilet(request);
    }

    public List<ToiletResponse> getAllDeletedToilets() {
        EmptyRequest request = EmptyRequest.getDefaultInstance();
        ToiletListResponse response = blockingStub.getAllDeletedToilets(request);
        return response.getToiletsList();
    }

    public List<ToiletResponse> getAllToilets() {
        EmptyRequest request = EmptyRequest.getDefaultInstance();
        ToiletListResponse response = blockingStub.getAllToilets(request);
        return response.getToiletsList();
    }

    public List<ToiletResponse> getPendingToilets() {
        EmptyRequest request = EmptyRequest.getDefaultInstance();
        ToiletListResponse response = blockingStub.getPendingToilets(request);
        return response.getToiletsList();
    }

    public OperationResponse approveToilet(String id) {
        ToiletIdRequest request = ToiletIdRequest.newBuilder()
                .setId(id)
                .build();
        return blockingStub.approveToilet(request);
    }

    public OperationResponse rejectToilet(String id) {
        ToiletIdRequest request = ToiletIdRequest.newBuilder()
                .setId(id)
                .build();
        return blockingStub.rejectToilet(request);
    }

    public OperationResponse updateToiletStatus(String id, String status) {
        ToiletStatusRequest request = ToiletStatusRequest.newBuilder()
                .setId(id)
                .setStatus(status)
                .build();
        return blockingStub.updateToiletStatus(request);
    }

    public List<ReviewResponse> getReviewsByToiletId(String toiletId) {
        ToiletIdRequest request = ToiletIdRequest.newBuilder()
                .setId(toiletId)
                .build();
        ReviewListResponse response = blockingStub.getReviewsByToiletId(request);
        return response.getReviewsList();
    }

    public List<ReportResponse> getReports() {
        EmptyRequest request = EmptyRequest.getDefaultInstance();
        ReportListResponse response = blockingStub.getReports(request);
        return response.getReportsList();
    }

    public OperationResponse updateReportStatus(String reportId, String status) {
        ReportStatusRequest request = ReportStatusRequest.newBuilder()
                .setReportId(reportId)
                .setStatus(status)
                .build();
        return blockingStub.updateReportStatus(request);
    }
}
