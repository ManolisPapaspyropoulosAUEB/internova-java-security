package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nexmo.client.NexmoClient;
import com.nexmo.client.NexmoClientException;
import com.nexmo.client.sms.SmsSubmissionResponse;
import com.nexmo.client.sms.SmsSubmissionResponseMessage;
import com.nexmo.client.sms.messages.TextMessage;
import com.typesafe.config.ConfigFactory;
import controllers.execution_context.DatabaseExecutionContext;
import controllers.procedures.ProceduresPrintController;
import models.BillingsEntity;
import models.OffersEntity;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.mail.*;
import play.api.db.Database;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.inject.Inject;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class MailerService {
    @Inject
    MailerClient mailerClient;
    private DatabaseExecutionContext executionContext;
    private JPAApi jpaApi;
    private final WSClient ws;
    private Database db;



    @Inject
    public MailerService(JPAApi jpaApi,MailerClient mailer, DatabaseExecutionContext executionContext,WSClient ws)
    {
        this.ws = ws;
        this.db = db;
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;

        this.mailerClient = mailer;
        MailcapCommandMap mc = (MailcapCommandMap) MailcapCommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }





    public void sendSms() throws IOException, NexmoClientException {

        NexmoClient client = new NexmoClient.Builder()
                .apiKey("2b5638e6")
                .apiSecret("pB049B9R1g6g5J7A")
                .build();

        String messageText = "Hello from Vonage SMS API";
        TextMessage message = new TextMessage("Vonage APIs", "306988953242", messageText);

        SmsSubmissionResponse response = client.getSmsClient().submitMessage(message);

        for (SmsSubmissionResponseMessage responseMessage : response.getMessages()) {
            System.out.println(responseMessage);
        }
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result sendEmailWS(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                String subject = json.findPath("subject").asText();
                                String from = json.findPath("from").asText();
                                String to = json.findPath("to").asText();
                                String bodyText = json.findPath("bodyText").asText();
                                String orderLoadingId = json.findPath("orderLoadingId").asText();
                                String offerSchedulesIds = json.findPath("offerSchedulesIds").asText();
                                String offerId = json.findPath("offerId").asText();
                                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                                Email email = new Email()
                                        .setSubject(subject)
                                        .setFrom(from)
                                        .addTo(to).addCc("manolis.papaspyropoulos@gmail.com").setBodyText(bodyText);
                                if(orderLoadingId!=null && !orderLoadingId.equalsIgnoreCase("") && !orderLoadingId.equalsIgnoreCase("null")){
                                    ProceduresPrintController proceduresPrintController =
                                            new ProceduresPrintController(db, jpaApi, executionContext);
                                    try {
                                        email.addAttachment("OderLoad.pdf",
                                                readStream(proceduresPrintController.
                                                        generateOrderLoadingReport(orderLoadingId)),
                                                "application/pdf", "OderLoad",
                                                EmailAttachment.INLINE);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (JRException e) {
                                        e.printStackTrace();
                                    }
                                    mailerClient.send(email);
                                    add_result.put("status", "success");
                                    add_result.put("message", "Το email αποστάλθηκε με επυτιχία!");
                                    return add_result;
                                }else if(offerSchedulesIds!=null && !offerSchedulesIds.equalsIgnoreCase("") && !offerSchedulesIds.equalsIgnoreCase("null")){
                                    OffersEntity offersEntity = entityManager.find(OffersEntity.class,Long.valueOf(offerId));
                                    entityManager.merge(offersEntity);
                                    offersEntity.setSendOfferDate(new Date());
                                    ProceduresPrintController proceduresPrintController =
                                            new ProceduresPrintController(db, jpaApi, executionContext);
                                    try {
                                        email.addAttachment("Offer.pdf",
                                                readStream(proceduresPrintController.
                                                        generateOfferReport(offerSchedulesIds,offerId)),
                                                "application/pdf", "Offer",
                                                EmailAttachment.INLINE);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (JRException e) {
                                        e.printStackTrace();
                                    }
                                    mailerClient.send(email);
                                    add_result.put("status", "success");
                                    add_result.put("message", "Το email αποστάλθηκε με επυτιχία!");
                                    return add_result;
                                }
                                add_result.put("status", "error");
                                add_result.put("message", "Δεν έχετε αποστείλει σωστά δεδομένα");
                                return add_result;
                            });
                        },
                        executionContext);
                result = (ObjectNode) addFuture.get();
                return ok(result);
            } catch (Exception e) {
                ObjectNode result = Json.newObject();
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Προβλημα κατα την καταχωρηση");
                return ok(result);
            }
        }
    }


    public byte[] readStream(ByteArrayInputStream bais) throws IOException {
        byte[] array = new byte[bais.available()];
        bais.read(array);

        return array;
    }


}