package fr.dossierfacile.common.service.interfaces;

import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.model.apartment_sharing.TenantModel;

public interface TenantCommonService {
    void recordAndDeleteTenantData(Long tenantId);

    Tenant findByKeycloakId(String keycloakId);

    void addDeleteLogIfMissing(Long tenantId);
}
