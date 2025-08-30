package com.example.tikicktaka.domain.mapping.scrap;

import com.example.tikicktaka.domain.enums.TargetType;
import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "scrap",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_scrap_user_type_target",
                columnNames = {"member_id", "target_type", "target_id"}
        ),
        indexes = {
                @Index(name = "idx_scrap_user_type_created", columnList = "member_id, target_type, created_at"),
                @Index(name = "idx_scrap_user_created", columnList = "member_id, created_at"),
                @Index(name = "idx_scrap_type_target", columnList = "target_type, target_id")
        }
)
public class Scrap extends BaseDateTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scrap_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 32)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    public static Scrap of(Long memberId, TargetType type, Long targetId) {
        return Scrap.builder()
                .memberId(memberId)
                .targetType(type)
                .targetId(targetId)
                .build();
    }
}
