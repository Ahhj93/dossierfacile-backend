package fr.dossierfacile.common.entity;

import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.FileAuthenticationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static fr.dossierfacile.common.entity.AuthenticityStatus.UNKNOWN;
import static fr.dossierfacile.common.entity.AuthenticityStatus.getAuthenticityStatus;
import static fr.dossierfacile.common.enums.DocumentCategory.TAX;
import static org.assertj.core.api.Assertions.assertThat;

class AuthenticityStatusTest {

    @ParameterizedTest
    @EnumSource(value = DocumentCategory.class, names = "TAX", mode = EnumSource.Mode.EXCLUDE)
    void only_check_tax_category(DocumentCategory documentCategory) {
        Document document = Document.builder()
                .documentCategory(documentCategory)
                .build();
        assertThat(getAuthenticityStatus(document)).isEqualTo(UNKNOWN);
    }

    @Test
    void only_check_tax_documents_with_files() {
        Document document = Document.builder()
                .documentCategory(TAX)
                .build();
        assertThat(getAuthenticityStatus(document)).isEqualTo(UNKNOWN);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            TWO_D_DOC, TAX_ASSESSMENT, VALID, AUTHENTIC
            TWO_D_DOC, TAX_ASSESSMENT, INVALID, UNKNOWN
            TWO_D_DOC, UNKNOWN, VALID, UNKNOWN
            QR_CODE, TAX_ASSESSMENT, VALID, UNKNOWN
            """)
    void verify_bar_code_type_and_issuer_and_status(BarCodeType barCodeType, BarCodeDocumentType documentType, FileAuthenticationStatus status, AuthenticityStatus expected) {
        Document document = Document.builder()
                .documentCategory(TAX)
                .files(List.of(File.builder()
                        .fileAnalysis(fileAnalysis(barCodeType, status, documentType))
                        .build()))
                .build();
        assertThat(getAuthenticityStatus(document)).isEqualTo(expected);
    }

    private static BarCodeFileAnalysis fileAnalysis(BarCodeType barCodeType, FileAuthenticationStatus fileAuthenticationStatus, BarCodeDocumentType documentType) {
        return BarCodeFileAnalysis.builder()
                .barCodeType(barCodeType)
                .authenticationStatus(fileAuthenticationStatus)
                .documentType(documentType)
                .build();
    }

}