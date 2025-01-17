package fr.dossierfacile.process.file.service.qrcodeanalysis.monfranceconnect.client;

import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonFranceConnectClient {

    @Value("${monfranceconnect.api.url}")
    private String monFranceConnectBaseUrl;

    private static RestTemplate restTemplateIgnoringHttps() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);

        return new RestTemplate(requestFactory);
    }

    public Optional<DocumentVerifiedContent> getVerifiedContent(MonFranceConnectAuthenticationRequest verificationRequest) {
        URI uri = verificationRequest.getUri(monFranceConnectBaseUrl);
        HttpEntity<String> entity = verificationRequest.getHttpEntity();
        return callMonFranceConnect(uri, entity)
                .flatMap(DocumentVerifiedContent::from);
    }

    private static Optional<ResponseEntity<String[]>> callMonFranceConnect(URI uri, HttpEntity<String> entity) {
        try {
            log.info("Calling MonFranceConnect at {} with body {}", uri, entity.getBody());
            ResponseEntity<String[]> response = restTemplateIgnoringHttps().exchange(uri, HttpMethod.POST, entity, String[].class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.of(response);
            }
            log.warn("MonFranceConnect responded with status {}", response.getStatusCode());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Error while calling MonFranceConnect (sentry id: {})", Sentry.captureException(e), e);
            return Optional.empty();
        }
    }
}
