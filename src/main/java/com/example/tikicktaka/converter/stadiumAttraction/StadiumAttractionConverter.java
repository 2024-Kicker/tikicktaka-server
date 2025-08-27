package com.example.tikicktaka.converter.stadiumAttraction;

import com.example.tikicktaka.domain.travel.TravelRegion;
import com.example.tikicktaka.web.dto.stadiumAttraction.StadiumAttractionResponseDTO;

public class StadiumAttractionConverter {

    /**
     * @param region  TravelRegion 엔티티
     * @param baseLat 기준 위도(구장/사용자 위치 등). null이면 거리 계산 안 함
     * @param baseLng 기준 경도(구장/사용자 위치 등). null이면 거리 계산 안 함
     */
    public static StadiumAttractionResponseDTO.Item toItemDTO(TravelRegion region,
                                                              Double baseLat, Double baseLng) {
        // 거리 계산 (기준 좌표와 장소 좌표가 모두 있을 때만)
        Double distanceKm = null;
        if (baseLat != null && baseLng != null
                && region.getMapY() != null && region.getMapX() != null) {
            distanceKm = haversineKm(baseLat, baseLng, region.getMapY(), region.getMapX());
        }

        // description: overview가 없으므로 addr1을 간단 요약으로 사용(없으면 null)
        String description = (region.getAddr1() != null && !region.getAddr1().isBlank())
                ? region.getAddr1().trim()
                : null;

        return StadiumAttractionResponseDTO.Item.builder()
                .id(region.getId())
                .name(region.getTitle())
                //.description(description)
                .imageUrl(region.getFirstImage())
                .distanceKm(distanceKm)
                .build();
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0088; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}
