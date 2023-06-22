package fr.dossierfacile.common.service;

import fr.dossierfacile.common.entity.ObjectStorageProvider;
import fr.dossierfacile.common.entity.StorageFile;
import fr.dossierfacile.common.exceptions.RetryableOperationException;
import fr.dossierfacile.common.repository.StorageFileRepository;
import fr.dossierfacile.common.service.interfaces.FileStorageProviderService;
import fr.dossierfacile.common.service.interfaces.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.ProviderNotFoundException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!mockOvh")
public class FileStorageServiceImpl implements FileStorageService {
    @Qualifier("ovhFileStorageProvider")
    private FileStorageProviderService ovhFileStorageService;
    @Qualifier("outscaleFileStorageProvider")
    private FileStorageProviderService outscaleFileStorageService;

    @Autowired
    private StorageFileRepository storageFileRepository;

    @Value("#{'${storage.provider.list}'.split(',')}")
    private List<ObjectStorageProvider> providers;

    private FileStorageProviderService getStorageService(ObjectStorageProvider storageProvider) {
        return switch (storageProvider) {
            case OVH -> ovhFileStorageService;
            case THREEDS_OUTSCALE -> outscaleFileStorageService;
            default -> throw new ProviderNotFoundException();
        };
    }

    @Override
    public void delete(StorageFile storageFile) {
        if (storageFile == null) {
            return;
        }
        getStorageService(storageFile.getProvider()).delete(storageFile.getPath());
    }

    @Override
    public InputStream download(StorageFile storageFile) throws IOException {
        return getStorageService(storageFile.getProvider())
                .download(storageFile.getPath(), storageFile.getEncryptionKey());
    }

    @Override
    public StorageFile upload(InputStream inputStream, StorageFile storageFile) throws IOException {
        if (inputStream == null) {
            return null;
        }
        if (storageFile == null) {
            log.warn("fallback to new storage file if file is null");
            storageFile = StorageFile.builder().name("undefined").build();
        }

        if (StringUtils.isBlank(storageFile.getPath())) {
            storageFile.setPath(UUID.randomUUID().toString());
        }

        for (ObjectStorageProvider provider : providers) {
            try {
                getStorageService(provider)
                        .upload(storageFile.getPath(), inputStream, storageFile.getEncryptionKey(), storageFile.getContentType());

                storageFile.setProvider(provider);
                break;
            } catch (RetryableOperationException e) {
                log.warn("Provider " + provider + " Failed - Retry with the next provider if exists.", e);
            }
        }
        if (storageFile.getProvider() == null)
            throw new IOException("Unable to upload the file");

        return storageFileRepository.save(storageFile);
    }

}