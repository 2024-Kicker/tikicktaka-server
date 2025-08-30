package com.example.tikicktaka.domain.mapping.blocked;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.enums.TargetType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "blocked",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_block_member_type_target",
                columnNames = {"member_id", "target_type", "target_id"}
        )
)
public class Blocked extends BaseDateTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 32, nullable = false)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    public static Blocked of(Long memberId, TargetType type, Long targetId) {
        return Blocked.builder()
                .memberId(memberId)
                .targetType(type)
                .targetId(targetId)
                .build();
    }
}
