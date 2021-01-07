package controllers.MELLON;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.UsersMellonStationsEntity;
import models.UsersMellonVehiclesEntity;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class VehiclesController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;


    @Inject
    public VehiclesController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addVehicle(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long station = json.findPath("station").asLong();
                                String ofType = json.findPath("ofType").asText();
                                String barcode = json.findPath("barcode").asText();
                                UsersMellonVehiclesEntity billingsEntity = new UsersMellonVehiclesEntity();
                                billingsEntity.setCreationDate(new Date());
                                billingsEntity.setStation(station);
                                billingsEntity.setOfType(ofType);//Scooter  Electric bycicle  ,Bicycle
                                billingsEntity.setBarcode(barcode);
                                entityManager.persist(billingsEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
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


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateVehicle(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long station = json.findPath("station").asLong();
                                Long id = json.findPath("id").asLong();
                                String ofType = json.findPath("ofType").asText();
                                String barcode = json.findPath("barcode").asText();
                                UsersMellonVehiclesEntity billingsEntity = entityManager.find(UsersMellonVehiclesEntity.class, id);
                                billingsEntity.setCreationDate(new Date());
                                billingsEntity.setStation(station);
                                billingsEntity.setOfType(ofType);//Scooter  Electric bycicle  ,Bicycle
                                billingsEntity.setBarcode(barcode);
                                entityManager.merge(billingsEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η ενημερωση πραγματοποίηθηκε με επιτυχία");
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


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteVehicle(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long id = json.findPath("id").asLong();
                                UsersMellonVehiclesEntity billingsEntity = entityManager.find(UsersMellonVehiclesEntity.class, id);
                                entityManager.remove(id);
                                add_result.put("status", "success");
                                add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
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


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getAllVehicles(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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
                            return jpaApi.withTransaction(
                                    entityManager -> {
                                        String orderCol = json.findPath("orderCol").asText();

                                        String sqlroles = "select * from users_mellon_vehicles ";

                                        List<UsersMellonVehiclesEntity> rolesListAll
                                                = (List<UsersMellonVehiclesEntity>) entityManager.createNativeQuery(
                                                sqlroles, UsersMellonVehiclesEntity.class).getResultList();
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                        List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                        List<UsersMellonVehiclesEntity> orgsList
                                                = (List<UsersMellonVehiclesEntity>) entityManager.createNativeQuery(
                                                sqlroles, UsersMellonVehiclesEntity.class).getResultList();
                                        for (UsersMellonVehiclesEntity j : orgsList) {
                                            HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                            sHmpam.put("id", j.getId());
                                            sHmpam.put("lat", entityManager.find(UsersMellonStationsEntity.class, j.getStation()).getLat());
                                            sHmpam.put("longt", entityManager.find(UsersMellonStationsEntity.class, j.getStation()).getLongt());
                                            sHmpam.put("barcode", j.getBarcode());
                                            sHmpam.put("ofType", j.getOfType());
                                            sHmpam.put("status", j.getStatus());
                                            sHmpam.put("stationName", entityManager.find(UsersMellonStationsEntity.class, j.getStation()).getStationName());
                                            sHmpam.put("creationDate", j.getCreationDate());
                                            serversList.add(sHmpam);
                                        }
                                        returnList_future.put("data", serversList);
                                        returnList_future.put("total", rolesListAll.size());
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
