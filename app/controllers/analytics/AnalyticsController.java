package controllers.analytics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.MailerService;
import controllers.execution_context.DatabaseExecutionContext;
import controllers.system.Application;

import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class AnalyticsController extends Application {
    private JPAApi jpaApi;
    private MailerService ms;
    private DatabaseExecutionContext executionContext;

    @Inject
    public AnalyticsController(MailerService ms, JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext);
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
        this.ms = ms;
    }









    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getAnalytics(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
        ObjectNode result = Json.newObject();
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            if (json == null) {
                result.put("status", "error");
                result.put("message", "Δεν εχετε αποστειλει εγκυρα δεδομενα.");
                return ok(result);
            } else {
                ObjectMapper ow = new ObjectMapper();
                HashMap<String, Object> returnList = new HashMap<String, Object>();
                String jsonResult = "";
                CompletableFuture<HashMap<String, Object>> getFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                String billingName = json.findPath("billingName").asText();
                                String startDate = json.findPath("startDate").asText();
                                String endDate = json.findPath("endDate").asText();
                                String selectedModeChart = json.findPath("selectedModeChart").asText();

                                String sqlroles = "select * from billings b where 1=1 ";
                                if (!billingName.equalsIgnoreCase("") && billingName != null) {
                                    sqlroles += " and b.name like '%" + billingName + "%'";
                                }

                                HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();


                                HashMap<String, Object> plotMap = new HashMap<String, Object>();
                                plotMap.put("day","Δευτέρα 5/6");
                                plotMap.put("value",4200);
                                serversList.add(plotMap);
                                plotMap = new HashMap<String, Object>();
                                plotMap.put("day","Τρίτη 6/6");
                                plotMap.put("value",343);
                                serversList.add(plotMap);
                                plotMap = new HashMap<String, Object>();
                                plotMap.put("day","Τετάρτη 7/6");
                                plotMap.put("value",532);
                                serversList.add(plotMap);
                                plotMap = new HashMap<String, Object>();
                                plotMap.put("day","Πεμπτη 8/6");
                                plotMap.put("value",3153);
                                serversList.add(plotMap);
                                plotMap = new HashMap<String, Object>();
                                plotMap.put("day","Παρασκευή 9/6");
                                plotMap.put("value",6435);
                                serversList.add(plotMap);
                                plotMap = new HashMap<String, Object>();
                                plotMap.put("day","Σάββατο 10/6");
                                plotMap.put("value",532);
                                serversList.add(plotMap);
                                plotMap = new HashMap<String, Object>();
                                plotMap.put("day","Κυριακή 11/6");
                                plotMap.put("value",643);
                                serversList.add(plotMap);
                                returnList_future.put("data", serversList);
                                returnList_future.put("status", "success");
                                returnList_future.put("message", "success");
                                return returnList_future;
                            });
                        },
                        executionContext);
                returnList = getFuture.get();
                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                ow.setDateFormat(myDateFormat);
                try {
                    jsonResult = ow.writeValueAsString(returnList);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.put("status", "error");
                    result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων ");
                    return ok(result);
                }
                return ok(jsonResult);
            }
        }

    }








}
