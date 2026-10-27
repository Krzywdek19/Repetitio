package pl.exceptionhandled.repetitio.subjects.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.exceptionhandled.repetitio.users.domain.User;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "subjects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String name;

    @Column
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Subject(User user, String name, String description) {
        this.user = Objects.requireNonNull(user);
        this.name = requireValidName(name);
        this.description = description;
    }

    public void rename(String newName) {
        this.name = requireValidName(newName);
    }

    public void changeDescription(String newDescription) {
        this.description = newDescription;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    private static String requireValidName(String name) {
        Objects.requireNonNull(name, "Subject name cannot be null");

        String normalized = name.strip();

        if (normalized.isBlank() || normalized.length() > 120) {
            throw new IllegalArgumentException(
                    "Subject name must contain between 1 and 120 characters"
            );
        }

        return normalized;
    }
}