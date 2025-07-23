package org.ebndrnk.orderservice.model.entity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class BasicEntity {

    @Access(AccessType.PROPERTY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identifier")
    @Column(nullable = false, name = "id", unique = true, columnDefinition = "BIGINT")
    private Long id;

    @Version
    @NotNull
    @Min(value = 1, message = "version must be grater or equals 1")
    @Comment("large range number from -9223372036854775808 to +9223372036854775807")
    @Column(nullable = false, name = "version", columnDefinition = "BIGINT DEFAULT 1 CHECK(version > 0)")
    private Long version = 1L;


    @NotNull
    @Comment("Format ISO 8601: YYYY-MM-DD hh:mm:ss.000000")
    @Column(nullable = false, name = "created_dttm", columnDefinition = "TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();


    @LastModifiedDate
    @NotNull
    @Comment("Format ISO 8601: YYYY-MM-DD hh:mm:ss.000000")
    @Column(nullable = false, name = "updated_at", columnDefinition = "TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt = LocalDateTime.now();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }
        BasicEntity that = (BasicEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
