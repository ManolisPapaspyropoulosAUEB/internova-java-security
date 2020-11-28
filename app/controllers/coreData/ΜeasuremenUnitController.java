package controllers.coreData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.MeasurementUnitEntity;
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
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;
public class ΜeasuremenUnitController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;
    @Inject
    public ΜeasuremenUnitController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }
    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addMeasureUnit(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                String title = json.findPath("title").asText();
                                String comments = json.findPath("comments").asText();
                                Double zIndex = json.findPath("zIndex").asDouble();
                                Double xIndex = json.findPath("xIndex").asDouble();
                                Double yIndex = json.findPath("yIndex").asDouble();
                                Double volume = json.findPath("volume").asDouble();
                                Integer ftlIndicator = json.findPath("ftlIndicator").asInt();
                                String sqlUnique = "select * from measurement_unit mu where mu.title=" + "'" + title + "'";
                                List<MeasurementUnitEntity> measurementUnitEntityList = entityManager.createNativeQuery(sqlUnique, MeasurementUnitEntity.class).getResultList();
                                if (measurementUnitEntityList.size() > 0) {
                                    add_result.put("status", "error");
                                    add_result.put("message", "Αδυναμία καταχώρησης,Βρέθηκε εγγραφή με τον ίδιο τίτλο");
                                    return add_result;
                                }
                                MeasurementUnitEntity measurementUnitEntity = new MeasurementUnitEntity();
                                measurementUnitEntity.setCreationDate(new Date());
                                measurementUnitEntity.setTitle(title);
                                measurementUnitEntity.setComments(comments);
                                measurementUnitEntity.setzIndex(zIndex);
                                measurementUnitEntity.setxIndex(xIndex);
                                measurementUnitEntity.setyIndex(yIndex);
                                measurementUnitEntity.setVolume(volume);
                                entityManager.persist(measurementUnitEntity);
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
    public Result updateMeasureUnit(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode update_result = Json.newObject();
                                String title = json.findPath("title").asText();
                                Long id = json.findPath("id").asLong();
                                String comments = json.findPath("comments").asText();
                                Double zIndex = json.findPath("zIndex").asDouble();
                                Double xIndex = json.findPath("xIndex").asDouble();
                                Double yIndex = json.findPath("yIndex").asDouble();
                                Double volume = json.findPath("volume").asDouble();
                                String sqlUnique = "select * from measurement_unit mu where mu.title=" + "'" + title + "' and mu.id!=" + id;
                                List<MeasurementUnitEntity> measurementUnitEntityList = entityManager.createNativeQuery(sqlUnique, MeasurementUnitEntity.class).getResultList();
                                if (measurementUnitEntityList.size() > 0) {
                                    update_result.put("status", "error");
                                    update_result.put("message", "Αδυναμία καταχώρησης,Βρέθηκε εγγραφή με τον ίδιο τίτλο");
                                    return update_result;
                                }
                                MeasurementUnitEntity measurementUnitEntity =entityManager.find(MeasurementUnitEntity.class,id);
                                measurementUnitEntity.setUpdateDate(new Date());
                                measurementUnitEntity.setTitle(title);
                                measurementUnitEntity.setComments(comments);
                                measurementUnitEntity.setzIndex(zIndex);
                                measurementUnitEntity.setxIndex(xIndex);
                                measurementUnitEntity.setyIndex(yIndex);
                                measurementUnitEntity.setVolume(volume);
                                entityManager.merge(measurementUnitEntity);
                                update_result.put("status", "success");
                                update_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                return update_result;
                            });
                        },
                        executionContext);
                result = (ObjectNode) updateFuture.get();
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
    public Result deleteMeasureUnit(final Http.Request request) throws IOException {
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
                                //todo: tsekare an uparxei stis prosfores
                                // String existSql = "select * from ";
                                MeasurementUnitEntity measurementUnitEntity = entityManager.find(MeasurementUnitEntity.class, id);
                                entityManager.remove(measurementUnitEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η Διαγραφή πραγματοποίηθηκε με επιτυχία");
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
    public Result getMeasureUnits(final Http.Request request) throws IOException {
        ObjectNode result = Json.newObject();
        try {
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
                                            String descAsc = json.findPath("descAsc").asText();
                                            String title = json.findPath("title").asText();
                                            String zIndex = json.findPath("zIndex").asText();
                                            String yIndex = json.findPath("yIndex").asText();
                                            String xIndex = json.findPath("xIndex").asText();
                                            String volume = json.findPath("volume").asText();
                                            String comments = json.findPath("comments").asText();
                                            String creationDate = json.findPath("creationDate").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlMeasures = "select * from measurement_unit m where 1=1 ";
                                            if (!title.equalsIgnoreCase("") && title != null) {
                                                sqlMeasures += " and m.title like '%" + title + "%'";
                                            }
                                            if (!xIndex.equalsIgnoreCase("") && xIndex != null) {
                                                sqlMeasures += " and m.x_index like '%" + xIndex + "%'";
                                            }
                                            if (!zIndex.equalsIgnoreCase("") && zIndex != null) {
                                                sqlMeasures += " and m.z_index like '%" + zIndex + "%'";
                                            }
                                            if (!yIndex.equalsIgnoreCase("") && yIndex != null) {
                                                sqlMeasures += " and m.y_index like '%" + yIndex + "%'";
                                            }
                                            if (!volume.equalsIgnoreCase("") && volume != null) {
                                                sqlMeasures += " and m.volume like '%" + volume + "%'";
                                            }
                                            if (!comments.equalsIgnoreCase("") && comments != null) {
                                                sqlMeasures += " and m.comments like '%" + comments + "%'";
                                            }
                                            if (!creationDate.equalsIgnoreCase("") && creationDate != null) {
                                                sqlMeasures += " and SUBSTRING( b.creation_date, 1, 10)  = '" + creationDate + "'";
                                            }
                                            List<MeasurementUnitEntity> measurementUnitEntityListAll
                                                    = (List<MeasurementUnitEntity>) entityManager.createNativeQuery(
                                                    sqlMeasures, MeasurementUnitEntity.class).getResultList();

                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                sqlMeasures += " order by " + orderCol + " " + descAsc;
                                            } else {
                                                sqlMeasures += " order by creation_date desc";
                                            }
                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlMeasures += " limit " + start + "," + limit;
                                            }
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> measuresListAll = new ArrayList<HashMap<String, Object>>();
                                            List<MeasurementUnitEntity> measurementUnitEntityList
                                                    = (List<MeasurementUnitEntity>) entityManager.createNativeQuery(
                                                    sqlMeasures, MeasurementUnitEntity.class).getResultList();
                                            for (MeasurementUnitEntity j : measurementUnitEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("comments", j.getComments());
                                                sHmpam.put("title", j.getTitle());
                                                sHmpam.put("zIndex", j.getzIndex());
                                                sHmpam.put("yIndex", j.getyIndex());
                                                sHmpam.put("xIndex", j.getxIndex());
                                                sHmpam.put("volume", j.getVolume());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                measuresListAll.add(sHmpam);
                                            }
                                            returnList_future.put("data", measuresListAll);
                                            returnList_future.put("total", measurementUnitEntityListAll.size());
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
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων");
            return ok(result);
        }
    }






}
