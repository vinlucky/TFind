package com.tfind.toilet.grpc;

import com.tfind.toilet.entity.Report;
import com.tfind.toilet.entity.Review;
import com.tfind.toilet.entity.Toilet;
import com.tfind.toilet.grpc.ToiletServiceProto.*;
import com.tfind.toilet.service.ReportService;
import com.tfind.toilet.service.ReviewService;
import com.tfind.toilet.service.ToiletService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService
public class ToiletGrpcService extends ToiletServiceGrpc.ToiletServiceImplBase {

    @Autowired
    private ToiletService toiletService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReportService reportService;

    @Override
    public void getNearbyToilets(NearbyToiletsRequest request, StreamObserver<ToiletListResponse> responseObserver) {
        List<Toilet> toilets = toiletService.getNearbyToilets(
                request.getLat(),
                request.getLng(),
                request.getMode(),
                request.getDistance()
        );
        ToiletListResponse.Builder builder = ToiletListResponse.newBuilder();
        for (Toilet toilet : toilets) {
            builder.addToilets(toiletToProto(toilet));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getToiletById(ToiletIdRequest request, StreamObserver<ToiletResponse> responseObserver) {
        Toilet toilet = toiletService.getToiletById(request.getId());
        if (toilet != null) {
            responseObserver.onNext(toiletToProto(toilet));
        } else {
            responseObserver.onNext(ToiletResponse.getDefaultInstance());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void createToilet(ToiletRequest request, StreamObserver<ToiletResponse> responseObserver) {
        Toilet toilet = protoToToilet(request);
        Toilet created = toiletService.createToilet(toilet);
        responseObserver.onNext(toiletToProto(created));
        responseObserver.onCompleted();
    }

    @Override
    public void updateToilet(ToiletUpdateRequest request, StreamObserver<ToiletResponse> responseObserver) {
        Toilet toilet = protoToToilet(request.getToilet());
        Toilet updated = toiletService.updateToilet(request.getId(), toilet);
        responseObserver.onNext(toiletToProto(updated));
        responseObserver.onCompleted();
    }

    @Override
    public void softDeleteToilet(ToiletIdRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            toiletService.softDeleteToilet(request.getId());
            responseObserver.onNext(OperationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Toilet soft deleted successfully")
                    .build());
        } catch (Exception e) {
            responseObserver.onNext(OperationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to soft delete toilet: " + e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void restoreToilet(ToiletIdRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            toiletService.restoreToilet(request.getId());
            responseObserver.onNext(OperationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Toilet restored successfully")
                    .build());
        } catch (Exception e) {
            responseObserver.onNext(OperationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to restore toilet: " + e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void physicalDeleteToilet(ToiletIdRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            toiletService.physicalDeleteSingleToilet(request.getId());
            responseObserver.onNext(OperationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Toilet physically deleted successfully")
                    .build());
        } catch (Exception e) {
            responseObserver.onNext(OperationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to physically delete toilet: " + e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getAllDeletedToilets(EmptyRequest request, StreamObserver<ToiletListResponse> responseObserver) {
        List<Toilet> toilets = toiletService.getAllDeletedToilets();
        ToiletListResponse.Builder builder = ToiletListResponse.newBuilder();
        for (Toilet toilet : toilets) {
            builder.addToilets(toiletToProto(toilet));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllToilets(EmptyRequest request, StreamObserver<ToiletListResponse> responseObserver) {
        List<Toilet> toilets = toiletService.getAllToilets();
        ToiletListResponse.Builder builder = ToiletListResponse.newBuilder();
        for (Toilet toilet : toilets) {
            builder.addToilets(toiletToProto(toilet));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getPendingToilets(EmptyRequest request, StreamObserver<ToiletListResponse> responseObserver) {
        List<Toilet> toilets = toiletService.getPendingToilets();
        ToiletListResponse.Builder builder = ToiletListResponse.newBuilder();
        for (Toilet toilet : toilets) {
            builder.addToilets(toiletToProto(toilet));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void approveToilet(ToiletIdRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            toiletService.approveToilet(request.getId(), "ADMIN");
            responseObserver.onNext(OperationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Toilet approved successfully")
                    .build());
        } catch (Exception e) {
            responseObserver.onNext(OperationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to approve toilet: " + e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void rejectToilet(ToiletIdRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            toiletService.rejectToilet(request.getId());
            responseObserver.onNext(OperationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Toilet rejected successfully")
                    .build());
        } catch (Exception e) {
            responseObserver.onNext(OperationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to reject toilet: " + e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void updateToiletStatus(ToiletStatusRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            toiletService.updateToiletStatus(request.getId(), request.getStatus());
            responseObserver.onNext(OperationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Toilet status updated successfully")
                    .build());
        } catch (Exception e) {
            responseObserver.onNext(OperationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to update toilet status: " + e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getReviewsByToiletId(ToiletIdRequest request, StreamObserver<ReviewListResponse> responseObserver) {
        List<Review> reviews = reviewService.getReviewsByToiletId(request.getId());
        ReviewListResponse.Builder builder = ReviewListResponse.newBuilder();
        for (Review review : reviews) {
            builder.addReviews(reviewToProto(review));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getReports(EmptyRequest request, StreamObserver<ReportListResponse> responseObserver) {
        List<Report> reports = reportService.getAllReports();
        ReportListResponse.Builder builder = ReportListResponse.newBuilder();
        for (Report report : reports) {
            builder.addReports(reportToProto(report));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateReportStatus(ReportStatusRequest request, StreamObserver<OperationResponse> responseObserver) {
        try {
            reportService.updateReportStatus(request.getReportId(), request.getStatus());
            responseObserver.onNext(OperationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Report status updated successfully")
                    .build());
        } catch (Exception e) {
            responseObserver.onNext(OperationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to update report status: " + e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    private ToiletResponse.Builder toiletToProtoBuilder(Toilet toilet) {
        ToiletResponse.Builder builder = ToiletResponse.newBuilder();
        if (toilet.getId() != null) builder.setId(toilet.getId());
        if (toilet.getName() != null) builder.setName(toilet.getName());
        builder.setLatitude(toilet.getLatitude() != null ? toilet.getLatitude() : 0.0);
        builder.setLongitude(toilet.getLongitude() != null ? toilet.getLongitude() : 0.0);
        if (toilet.getFloor() != null) builder.setFloor(toilet.getFloor());
        builder.setPositions(toilet.getPositions() != null ? toilet.getPositions() : 0);
        builder.setCleanScore(toilet.getCleanScore() != null ? toilet.getCleanScore() : 0.0);
        builder.setScore(toilet.getScore() != null ? toilet.getScore() : 0.0);
        builder.setAiScore(toilet.getAiScore() != null ? toilet.getAiScore() : 0.0);
        if (toilet.getTags() != null) builder.addAllTags(toilet.getTags());
        if (toilet.getAiTags() != null) builder.addAllAiTags(toilet.getAiTags());
        builder.setHasMotherRoom(toilet.getHasMotherRoom() != null ? toilet.getHasMotherRoom() : false);
        builder.setHasAccessibility(toilet.getHasAccessibility() != null ? toilet.getHasAccessibility() : false);
        builder.setIsFree(toilet.getIsFree() != null ? toilet.getIsFree() : false);
        builder.setIs24Hours(toilet.getIs24Hours() != null ? toilet.getIs24Hours() : false);
        builder.setIsQueuing(toilet.getIsQueuing() != null ? toilet.getIsQueuing() : false);
        builder.setQueueTime(toilet.getQueueTime() != null ? toilet.getQueueTime() : 0);
        if (toilet.getPhotoUrl() != null) builder.setPhotoUrl(toilet.getPhotoUrl());
        if (toilet.getAddress() != null) builder.setAddress(toilet.getAddress());
        if (toilet.getOpenid() != null) builder.setOpenid(toilet.getOpenid());
        if (toilet.getStatus() != null) builder.setStatus(toilet.getStatus());
        builder.setDeltoilet(toilet.getDeltoilet() != null ? toilet.getDeltoilet() : false);
        builder.setDeltime(toilet.getDeltime() != null ? toilet.getDeltime() : 0L);
        if (toilet.getAiAnalysis() != null) builder.setAiAnalysis(toilet.getAiAnalysis());
        builder.setCreateTime(toilet.getCreateTime() != null ? toilet.getCreateTime() : 0L);
        builder.setUpdateTime(toilet.getUpdateTime() != null ? toilet.getUpdateTime() : 0L);
        return builder;
    }

    private ToiletResponse toiletToProto(Toilet toilet) {
        return toiletToProtoBuilder(toilet).build();
    }

    private Toilet protoToToilet(ToiletRequest proto) {
        Toilet toilet = new Toilet();
        toilet.setId(proto.getId());
        toilet.setName(proto.getName());
        toilet.setLatitude(proto.getLatitude());
        toilet.setLongitude(proto.getLongitude());
        toilet.setFloor(proto.getFloor());
        toilet.setPositions(proto.getPositions());
        toilet.setCleanScore(proto.getCleanScore());
        toilet.setScore(proto.getScore());
        toilet.setAiScore(proto.getAiScore());
        toilet.setTags(proto.getTagsList());
        toilet.setAiTags(proto.getAiTagsList());
        toilet.setHasMotherRoom(proto.getHasMotherRoom());
        toilet.setHasAccessibility(proto.getHasAccessibility());
        toilet.setIsFree(proto.getIsFree());
        toilet.setIs24Hours(proto.getIs24Hours());
        toilet.setIsQueuing(proto.getIsQueuing());
        toilet.setQueueTime(proto.getQueueTime());
        toilet.setPhotoUrl(proto.getPhotoUrl());
        toilet.setAddress(proto.getAddress());
        toilet.setOpenid(proto.getOpenid());
        toilet.setStatus(proto.getStatus());
        toilet.setDeltoilet(proto.getDeltoilet());
        toilet.setDeltime(proto.getDeltime());
        toilet.setAiAnalysis(proto.getAiAnalysis());
        toilet.setCreateTime(proto.getCreateTime());
        toilet.setUpdateTime(proto.getUpdateTime());
        return toilet;
    }

    private ReviewResponse reviewToProto(Review review) {
        ReviewResponse.Builder builder = ReviewResponse.newBuilder();
        if (review.getId() != null) builder.setId(review.getId());
        if (review.getToiletId() != null) builder.setToiletId(review.getToiletId());
        if (review.getOpenid() != null) builder.setOpenid(review.getOpenid());
        if (review.getUserName() != null) builder.setUserName(review.getUserName());
        if (review.getContent() != null) builder.setContent(review.getContent());
        builder.setScore(review.getScore() != null ? review.getScore() : 0.0);
        if (review.getTags() != null) builder.addAllTags(review.getTags());
        if (review.getStatus() != null) builder.setStatus(review.getStatus());
        builder.setCreateTime(review.getCreateTime() != null ? review.getCreateTime() : 0L);
        return builder.build();
    }

    private ReportResponse reportToProto(Report report) {
        ReportResponse.Builder builder = ReportResponse.newBuilder();
        if (report.getId() != null) builder.setId(report.getId());
        if (report.getToiletId() != null) builder.setToiletId(report.getToiletId());
        if (report.getToiletName() != null) builder.setToiletName(report.getToiletName());
        if (report.getOpenid() != null) builder.setOpenid(report.getOpenid());
        if (report.getReportType() != null) builder.setReportType(report.getReportType());
        if (report.getReportTypeName() != null) builder.setReportTypeName(report.getReportTypeName());
        if (report.getContent() != null) builder.setContent(report.getContent());
        if (report.getStatus() != null) builder.setStatus(report.getStatus());
        builder.setCreateTime(report.getCreateTime() != null ? report.getCreateTime() : 0L);
        return builder.build();
    }
}
