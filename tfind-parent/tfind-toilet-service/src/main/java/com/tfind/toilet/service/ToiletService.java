package com.tfind.toilet.service;

import com.tfind.toilet.entity.Toilet;

import java.util.List;

public interface ToiletService {

    List<Toilet> getNearbyToilets(double lat, double lng, String mode, double distance);

    Toilet getToiletById(String id);

    Toilet createToilet(Toilet toilet);

    Toilet updateToilet(String id, Toilet toilet);

    void softDeleteToilet(String id);

    void restoreToilet(String id);

    int physicalDeleteToilets();

    void deleteToiletByAdmin(String id);

    List<Toilet> getAllDeletedToilets();

    void approveToilet(String id, String userId);

    void rejectToilet(String id);

    List<Toilet> getAllToilets();

    List<Toilet> getPendingToilets();

    void physicalDeleteSingleToilet(String id);

    void updateToiletStatus(String id, String status);
}
