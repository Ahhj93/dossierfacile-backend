package fr.dossierfacile.common.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.dossierfacile.common.enums.PropertyFurniture;
import fr.dossierfacile.common.enums.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.lang.Nullable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static fr.dossierfacile.common.enums.TenantType.CREATE;

@Entity
@Table(name = "property")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Property implements Serializable {

    private static final long serialVersionUID = 1489742852598125157L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creation_date")
    @JsonIgnore
    @Builder.Default
    private LocalDateTime creationDateTime = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private Owner owner;

    @Column
    @Builder.Default
    private String token = RandomStringUtils.randomAlphanumeric(20);

    @Column
    @NotBlank
    private String name;

    @Column
    private String propertyId;

    @Column
    private Integer countVisit;

    @Column(name = "rent_cost", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double rentCost;

    @Column(name = "charges_cost", columnDefinition = "Decimal(10,2) default '0.00'")
    private Double chargesCost;

    @Column(name = "living_space", columnDefinition = "Decimal(10,1) default '0.0'")
    private Double livingSpace;

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<PropertyApartmentSharing> propertiesApartmentSharing;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Prospect> prospects;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<PropertyId> mergedPropertyId;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean notification = true;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean displayed = true;

    @Builder.Default
    @Column(columnDefinition = "integer default 0")
    private Integer cantEmailSentProspect = 0;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean validated = false;

    @Nullable
    private LocalDateTime validatedDate;

    @Column
    @Enumerated(EnumType.STRING)
    private PropertyType type;

    @Column
    @Enumerated(EnumType.STRING)
    private PropertyFurniture furniture;

    @Column
    private Integer energyConsumption;

    @Column
    private Integer co2Emission;

    @Column
    private String address;

    @Override
    public String toString() {
        return this.name;
    }

    public int size() {
        int count = 0;
        for (Prospect prospect : prospects) {
            if (prospect.getProspectType() != null && prospect.getProspectType() == CREATE) {
                count++;
            }
        }
        return count;
    }

}
