package fr.dossierfacile.common.repository;

import fr.dossierfacile.common.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentCommonRepository extends JpaRepository<Document, Long> {

    @Query(value = """
        select * from document
        where tenant_id = :tenantId
        and document_category = 'TAX'
        """, nativeQuery = true)
    Document findTaxDocumentOfTenant(@Param("tenantId") Long tenantId);

}