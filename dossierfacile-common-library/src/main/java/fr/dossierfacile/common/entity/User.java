package fr.dossierfacile.common.entity;

import fr.dossierfacile.common.enums.AuthProvider;
import fr.dossierfacile.common.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Entity
@Table(name = "user_account")
@Inheritance(
        strategy = InheritanceType.JOINED
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class User implements Serializable {

    private static final long serialVersionUID = -3603815439883206021L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String preferredName;

    @Column
    private String email;

    @Column
    private String password;

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<UserRole> userRoles = new HashSet<>();

    @Builder.Default
    @Column(name = "creation_date")
    private LocalDateTime creationDateTime = LocalDateTime.now();

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date updateDateTime;

    @Builder.Default
    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate = LocalDateTime.now();

    /** use Keycloak service */
    @Builder.Default
    @Deprecated
    @Column(columnDefinition = "boolean default true")
    private boolean enabled = false;

    @Builder.Default
    @OneToMany(mappedBy = "fromUser", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<Message> sentMessages = new HashSet<>();
    @Builder.Default
    @OneToMany(mappedBy = "toUser", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<Message> receivedMessages = new HashSet<>();

    @Builder.Default
    @Column(length = 20, columnDefinition = "varchar(20) default 'local'")
    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.local;
    private String providerId;
    private String imageUrl;

    private String keycloakId;
    @Builder.Default
    private Boolean franceConnect = false;
    private String franceConnectSub;
    private String franceConnectBirthDate;
    private String franceConnectBirthPlace;
    private String franceConnectBirthCountry;

    @Column(name = "user_type")
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private ConfirmationToken confirmationToken;

    public User(UserType userType){
        this.userType = userType;
    }

    public User(UserType userType, String email) {
        this.userType = userType;
        this.email = email;
    }

    public User(UserType userType, String firstName, String lastName, String preferredName, String email) {
        this.userType = userType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.preferredName = preferredName;
        this.email = email;
    }

    public String getFullName() {
        String displayName = isBlank(preferredName) ? lastName : preferredName;
        return isNotBlank(firstName) && isNotBlank(displayName) ? String.join(" ", firstName, displayName) : "";
    }
}
