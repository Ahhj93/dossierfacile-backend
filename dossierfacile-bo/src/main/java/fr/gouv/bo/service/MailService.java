package fr.gouv.bo.service;

import com.google.common.base.Strings;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.entity.User;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sendinblue.ApiException;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailTo;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final TransactionalEmailsApi apiInstance;
    @Value("${mailjet.template.id.message.notification}")
    private Long templateIDMessageNotification;
    @Value("${mailjet.template.id.account.deleted}")
    private Long templateIdAccountDeleted;
    @Value("${mailjet.template.id.dossier.validated}")
    private Long templateIDDossierValidated;

    @Async
    public void sendEmailAccountDeleted(User user) {
        Map<String, String> variables = new HashMap<>();
        variables.put("PRENOM", user.getFirstName());
        variables.put("NOM", Strings.isNullOrEmpty(user.getPreferredName()) ? user.getLastName() : user.getPreferredName());

        SendSmtpEmailTo sendSmtpEmailTo = new SendSmtpEmailTo();
        sendSmtpEmailTo.setEmail(user.getEmail());
        if (!Strings.isNullOrEmpty(user.getFullName())) {
            sendSmtpEmailTo.setName(user.getFullName());
        }

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.templateId(templateIdAccountDeleted);
        sendSmtpEmail.params(variables);
        sendSmtpEmail.to(Collections.singletonList(sendSmtpEmailTo));

        try {
            apiInstance.sendTransacEmail(sendSmtpEmail);
        } catch (ApiException e) {
            log.error("Email api exception", e);
        }
    }

    @Async
    public void sendMailNotificationAfterDeny(User user) {
        Map<String, String> variables = new HashMap<>();
        variables.put("PRENOM", user.getFirstName());
        variables.put("NOM", Strings.isNullOrEmpty(user.getPreferredName()) ? user.getLastName() : user.getPreferredName());

        SendSmtpEmailTo sendSmtpEmailTo = new SendSmtpEmailTo();
        sendSmtpEmailTo.setEmail(user.getEmail());
        if (!Strings.isNullOrEmpty(user.getFullName())) {
            sendSmtpEmailTo.setName(user.getFullName());
        }

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.templateId(templateIDMessageNotification);
        sendSmtpEmail.params(variables);
        sendSmtpEmail.to(Collections.singletonList(sendSmtpEmailTo));

        try {
            apiInstance.sendTransacEmail(sendSmtpEmail);
        } catch (ApiException e) {
            log.error("Email api exception", e);
        }
    }

    @Async
    public void sendEmailToTenantAfterValidateAllDocuments(Tenant tenant) {
        Map<String, String> variables = new HashMap<>();
        variables.put("PRENOM", tenant.getFirstName());
        variables.put("NOM", Strings.isNullOrEmpty(tenant.getPreferredName()) ? tenant.getLastName() : tenant.getPreferredName());

        SendSmtpEmailTo sendSmtpEmailTo = new SendSmtpEmailTo();
        sendSmtpEmailTo.setEmail(tenant.getEmail());
        if (!Strings.isNullOrEmpty(tenant.getFullName())) {
            sendSmtpEmailTo.setName(tenant.getFullName());
        }

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.templateId(templateIDDossierValidated);
        sendSmtpEmail.params(variables);
        sendSmtpEmail.to(Collections.singletonList(sendSmtpEmailTo));

        try {
            apiInstance.sendTransacEmail(sendSmtpEmail);
        } catch (ApiException e) {
            log.error("Email api exception", e);
        }
    }
}
