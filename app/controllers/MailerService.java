package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nexmo.client.NexmoClient;
import com.nexmo.client.NexmoClientException;
import com.nexmo.client.sms.SmsSubmissionResponse;
import com.nexmo.client.sms.SmsSubmissionResponseMessage;
import com.nexmo.client.sms.messages.TextMessage;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import play.libs.Json;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class MailerService {
    @Inject
    MailerClient mailerClient;



    @Inject
    public MailerService(MailerClient mailer)
    {
        this.mailerClient = mailer;
        MailcapCommandMap mc = (MailcapCommandMap) MailcapCommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }


    public void sendEmail() {



        Email email = new Email()
                .setSubject("Simple email")
                .setFrom("manolis.papaspyropoulos@gmail.com")
                .addTo("manolis.papaspyropoulos@gmail.com").setBodyText("A text message");
        mailerClient.send(email);


    }

//    public void sendEmail() {
//        String cid = "1234";
//        Email email = new Email()
//                .setSubject("EMAIL offer")
//                .setFrom("manolis.papaspyropoulos@gmail.com")
//                .addTo("manolis.papaspyropoulos@gmail.com")
//                .addTo("mpapaspyropoulos@synergic.gr")
//                //.addTo("maria_kukou@hotmail.com")
//                // adds attachment
//                // .addAttachment("attachment.pdf", new File("/some/path/attachment.pdf"))
//                // .addAttachment("data.txt", "data".getBytes(), "text/plain", "Simple data", EmailAttachment.INLINE)
//                .addAttachment("wd.html", new File("D:\\developm\\wd.html"), cid)
//                .setBodyText("A text message")
//                .setBodyHtml("<html><body><p>An <b>html</b> message with cid <img src=\"cid:" + cid + "\"></p></body></html>");
//        mailerClient.send(email);
//    }


    public static void sendEmail2() throws EmailException {


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
                    ObjectNode add_result = Json.newObject();
                    sendEmail();
                    System.out.println("hey");
                    add_result.put("status", "success");
                    add_result.put("message", "success");
                    return add_result;
                });
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
//


}