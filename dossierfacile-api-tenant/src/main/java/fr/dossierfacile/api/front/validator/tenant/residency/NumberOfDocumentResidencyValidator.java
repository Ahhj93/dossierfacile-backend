package fr.dossierfacile.api.front.validator.tenant.residency;

import fr.dossierfacile.api.front.register.form.tenant.DocumentResidencyForm;
import fr.dossierfacile.api.front.repository.FileRepository;
import fr.dossierfacile.api.front.validator.TenantConstraintValidator;
import fr.dossierfacile.api.front.validator.anotation.tenant.residency.NumberOfDocumentResidency;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.enums.DocumentCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidatorContext;

@Component
@RequiredArgsConstructor
public class NumberOfDocumentResidencyValidator extends TenantConstraintValidator<NumberOfDocumentResidency, DocumentResidencyForm> {
    private final FileRepository fileRepository;

    @Override
    public boolean isValid(DocumentResidencyForm documentResidencyForm, ConstraintValidatorContext constraintValidatorContext) {
        Tenant tenant = getTenant(documentResidencyForm);
        long sizeOldDoc = 0;
        long countOld = fileRepository.countFileByDocumentCategoryTenant(DocumentCategory.RESIDENCY, tenant);
        long countNew = documentResidencyForm.getDocuments()
                .stream()
                .filter(f -> !f.isEmpty())
                .count();
        if (countOld > 0) {
            sizeOldDoc = fileRepository.sumSizeOfAllFilesForDocument(DocumentCategory.RESIDENCY, tenant);
        }
        long sizeNewDoc = documentResidencyForm.getDocuments().stream().filter(o -> o.getSize() >= 0).mapToLong(MultipartFile::getSize).sum();

        var isValid = 1 <= countNew + countOld && countNew + countOld <= 10 && sizeNewDoc + sizeOldDoc <= 52428800;
        if (!isValid) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("documents").addConstraintViolation();
        }
        return isValid;
    }
}
