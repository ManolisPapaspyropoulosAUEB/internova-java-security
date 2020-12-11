package controllers.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.archives.CustomersSuppliersController;
import controllers.execution_context.DatabaseExecutionContext;
import models.AuditLogsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.*;
import play.mvc.Http.Request;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class Application extends Controller {

    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public Application(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }


    private static final Logger logger = LoggerFactory.getLogger(Application.class);


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public  Result ok(JsonNode content,final Http.Request request) throws IOException {
            JsonNode json = request.body().asJson();
            ((ObjectNode) json).remove("internovaSeller");
            ((ObjectNode) json).remove("billing");
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> auditLogFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode delete_result = Json.newObject();
                                    if(content.findPath("status").asText().equalsIgnoreCase("success") || content.findPath("status").asText().equalsIgnoreCase("ok")){
                                        System.out.println(content.findPath("DO_ID").asLong());
                                        AuditLogsEntity auditLogsEntity = new AuditLogsEntity();
                                        auditLogsEntity.setMessage("-");
                                        auditLogsEntity.setSystem(content.findPath("system").asText());
                                        auditLogsEntity.setMethod(request.toString());
                                        auditLogsEntity.setObjectId(content.findPath("DO_ID").asLong());
                                        auditLogsEntity.setUserId(content.findPath("user_id").asLong());
                                        auditLogsEntity.setCreationDate(new Date());
                                        entityManager.persist(auditLogsEntity);
                                    }
                                    return content;
                                });
                            },
                            executionContext);
                try {
                    result = (ObjectNode) auditLogFuture.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return ok(result);
            }
    }





















    private static int riskyCalculation() {
        System.out.println("!!riskyCalculation!!");
        return 10 / (new java.util.Random()).nextInt(2);
    }
}

class AccessLoggingAction extends Action.Simple {

    private static final Logger accessLogger = LoggerFactory.getLogger(AccessLoggingAction.class);

    public CompletionStage<Result> call(Http.Request request) {
        accessLogger.info(
                "method={} uri={} remote-address={}",
                request.method(),
                request.uri(),
                request.remoteAddress());

        return delegate.call(request);
    }
}