package uk.gov.hmcts.reform.managecase.service;

import com.google.common.collect.Lists;
import java.util.List;
import javax.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.managecase.ApplicationParams;
import uk.gov.hmcts.reform.managecase.domain.notify.EmailNotificationRequest;
import uk.gov.hmcts.reform.managecase.domain.notify.EmailNotificationRequestStatus;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class NotifyServiceTest {

    private static final String CASE_ID = "12345678";

    private static final String TEST_EMAIL = "test@hmcts.net";

    private static final String EMAIL_TEMPLATE_ID = "TestEmailTemplateId";

    @Mock
    private ApplicationParams appParams;

    @Mock
    private NotificationClient notificationClient;

    private NotifyService notifyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        this.notifyService = new NotifyService(appParams, notificationClient);
    }

    @Test
    @DisplayName("should fail when null email notification request list is passed")
    void shouldThrowValidationExceptionWhenCaseIdsIsNull() {
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> this.notifyService.senEmail(null)
        );

        assertThat(exception.getMessage())
            .isEqualTo("At least one email notification request is required to send notification");
    }

    @Test
    @DisplayName("should fail when emptu email notification request list is passed")
    void shouldThrowValidationExceptionWhenCaseIdsIsEmpty() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            this.notifyService.senEmail(Lists.newArrayList()));

        assertThat(exception.getMessage())
            .isEqualTo("At least one email notification request is required to send notification");
    }

    @Test
    @DisplayName("should invoke notification client sendEmail")
    void shouldInvokeNotificationClientSendNotification() throws NotificationClientException {
        given(appParams.getEmailTemplateId()).willReturn(EMAIL_TEMPLATE_ID);
        SendEmailResponse sendEmailResponse = mock(SendEmailResponse.class);
        given(this.notificationClient
                  .sendEmail(
                      anyString(),
                      anyString(),
                      anyMap(),
                      anyString()
                  ))
            .willReturn(sendEmailResponse);
        EmailNotificationRequest request = new EmailNotificationRequest(CASE_ID, TEST_EMAIL);

        List<EmailNotificationRequestStatus> responses = this.notifyService.senEmail(Lists.newArrayList(request));
        assertNotNull(responses, "response object should not be null");
        assertEquals(1, responses.size(), "response size is not equal");
        verify(this.notificationClient).sendEmail(
            anyString(),
            anyString(),
            anyMap(),
            anyString()
        );
    }

    @Test
    @DisplayName("should invoke notification client sendEmail for more than one case id")
    void shouldInvokeNotificationClientSendNotificationForMoreThanOneCaseId() throws NotificationClientException {
        given(appParams.getEmailTemplateId()).willReturn(EMAIL_TEMPLATE_ID);
        SendEmailResponse sendEmailResponse = mock(SendEmailResponse.class);
        given(this.notificationClient
                  .sendEmail(
                      anyString(),
                      anyString(),
                      anyMap(),
                      anyString(),
                      anyString()
                  ))
            .willReturn(sendEmailResponse);

        EmailNotificationRequest request1 = new EmailNotificationRequest(CASE_ID, TEST_EMAIL);
        EmailNotificationRequest request2 = new EmailNotificationRequest("12345679", TEST_EMAIL);

        List<EmailNotificationRequestStatus> responses = this.notifyService
            .senEmail(Lists.newArrayList(request1, request2));

        assertNotNull(responses, "response object should not be null");
        assertEquals(2, responses.size(), "response size is not equal");
        verify(this.notificationClient, times(2)).sendEmail(
            anyString(),
            anyString(),
            anyMap(),
            anyString()
        );
    }

    @Test
    @DisplayName("should invoke notification client sendEmail for multiple case id and email addresses")
    void shouldInvokeNotificationClientSendNotificationForMoreThanOneCaseIdAndMoreThanOneEmailAddress()
        throws NotificationClientException {
        given(appParams.getEmailTemplateId()).willReturn(EMAIL_TEMPLATE_ID);
        SendEmailResponse sendEmailResponse = mock(SendEmailResponse.class);
        given(this.notificationClient
                  .sendEmail(
                      anyString(),
                      anyString(),
                      anyMap(),
                      anyString()
                  ))
            .willReturn(sendEmailResponse);

        EmailNotificationRequest request1 = new EmailNotificationRequest(CASE_ID, TEST_EMAIL);
        EmailNotificationRequest request2 = new EmailNotificationRequest("12345679", TEST_EMAIL);
        EmailNotificationRequest request3 = new EmailNotificationRequest(CASE_ID, "test2@hmcts.net");
        EmailNotificationRequest request4 = new EmailNotificationRequest("12345679", "test2@hmcts.net");

        List<EmailNotificationRequestStatus> responses = this.notifyService.senEmail(
            Lists.newArrayList(request1, request2, request3, request4));

        assertNotNull(responses, "response object should not be null");
        assertEquals(4, responses.size(), "response size is not equal");
        verify(this.notificationClient, times(4)).sendEmail(
            anyString(),
            anyString(),
            anyMap(),
            anyString()
        );
    }
}
