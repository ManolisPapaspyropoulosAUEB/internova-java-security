package controllers.procedures;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.MeasurementUnitEntity;
import models.ScheduleEntity;
import models.SchedulePackagesEntity;
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

public class ScheduleController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public ScheduleController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addSchedule(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                JsonNode arrival = json.findPath("arrival");
                                JsonNode departure = json.findPath("departure");
                                String fromAddress = departure.findPath("fromAddress").asText();
                                String fromCity = departure.findPath("fromCity").asText();
                                String fromPostalCode = departure.findPath("fromPostalCode").asText();
                                String fromRegion = departure.findPath("fromRegion").asText();
                                String fromCountry = departure.findPath("fromCountry").asText();
                                Double fromLattitude = departure.findPath("fromLattitude").asDouble();
                                Double fromLongtitude = departure.findPath("fromLongtitude").asDouble();
                                String toAddress = arrival.findPath("toAddress").asText();
                                String toCity = arrival.findPath("toCity").asText();
                                String toPostalCode = arrival.findPath("toPostalCode").asText();
                                String toRegion = arrival.findPath("toRegion").asText();
                                String toCountry = arrival.findPath("toCountry").asText();
                                Double toLattitude = arrival.findPath("toLattitude").asDouble();
                                Double toLongtitude = arrival.findPath("toLongtitude").asDouble();
                                if(fromAddress.equalsIgnoreCase("")
                                        ||fromCountry.equalsIgnoreCase("")
                                        ||toAddress.equalsIgnoreCase("")
                                        ||toCountry.equalsIgnoreCase("")
                                ){
                                    add_result.put("status", "error");
                                    add_result.put("message", "Παρακαλώ συμπληρώστε όλα τα υποχρεωτικά πεδία");
                                    return add_result;

                                }
                                ScheduleEntity scheduleEntity = new ScheduleEntity();
                                scheduleEntity.setFromAddress(fromAddress);
                                scheduleEntity.setFromCity(fromCity);
                                scheduleEntity.setFromPostalCode(fromPostalCode);
                                scheduleEntity.setFromRegion(fromRegion);
                                scheduleEntity.setFromCountry(fromCountry);
                                scheduleEntity.setFromLongtitude(fromLongtitude);
                                scheduleEntity.setFromLattitude(fromLattitude);
                                scheduleEntity.setToAddress(toAddress);
                                scheduleEntity.setToCity(toCity);
                                scheduleEntity.setToPostalCode(toPostalCode);
                                scheduleEntity.setToRegion(toRegion);
                                scheduleEntity.setToCountry(toCountry);
                                scheduleEntity.setToLattitude(toLattitude);
                                scheduleEntity.setToLongtitude(toLongtitude);
                                scheduleEntity.setCreationDate(new Date());
                                if((fromCountry.equalsIgnoreCase("Ελλάδα") || fromCountry.equalsIgnoreCase("Greece") ) && !(fromCountry.equalsIgnoreCase("Ελλάδα") || fromCountry.equalsIgnoreCase("Greece"))){
                                    scheduleEntity.setType("Εξαγωγή");
                                }else if (!fromCountry.equalsIgnoreCase("Ελλάδα") && toCountry.equalsIgnoreCase("Ελλάδα")){
                                    scheduleEntity.setType("Εισαγωγή");
                                }else{
                                    scheduleEntity.setType("Εσωτερικού");
                                }
                                entityManager.persist(scheduleEntity);
                                add_result.put("status", "success");
                                add_result.put("id", scheduleEntity.getId());
                                add_result.put("type", scheduleEntity.getType());
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
    public Result updateSchedule(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode update_result = Json.newObject();
                                JsonNode arrival = json.findPath("arrival");
                                JsonNode departure = json.findPath("departure");
                                Long id = json.findPath("id").asLong();
                                String fromAddress = departure.findPath("fromAddress").asText();
                                String fromCity = departure.findPath("fromCity").asText();
                                String fromPostalCode = departure.findPath("fromPostalCode").asText();
                                String fromRegion = departure.findPath("fromRegion").asText();
                                String fromCountry = departure.findPath("fromCountry").asText();
                                Double fromLattitude = departure.findPath("fromLattitude").asDouble();
                                Double fromLongtitude = departure.findPath("fromLongtitude").asDouble();
                                String toAddress = arrival.findPath("toAddress").asText();
                                String toCity = arrival.findPath("toCity").asText();
                                String toPostalCode = arrival.findPath("toPostalCode").asText();
                                String toRegion = arrival.findPath("toRegion").asText();
                                String toCountry = arrival.findPath("toCountry").asText();
                                Double toLattitude = arrival.findPath("toLattitude").asDouble();
                                Double toLongtitude = arrival.findPath("toLattitude").asDouble();
                                ScheduleEntity scheduleEntity = entityManager.find(ScheduleEntity.class, id);
                                if(fromAddress.equalsIgnoreCase("")
                                        ||fromCountry.equalsIgnoreCase("")
                                        ||toAddress.equalsIgnoreCase("")
                                        ||toCountry.equalsIgnoreCase("")
                                ){
                                    update_result.put("status", "error");
                                    update_result.put("message", "Παρακαλώ συμπληρώστε όλα τα υποχρεωτικά πεδία");
                                    return update_result;
                                }
                                scheduleEntity.setFromAddress(fromAddress);
                                scheduleEntity.setFromCity(fromCity);
                                scheduleEntity.setFromPostalCode(fromPostalCode);
                                scheduleEntity.setFromRegion(fromRegion);
                                scheduleEntity.setFromCountry(fromCountry);
                                scheduleEntity.setFromLongtitude(fromLongtitude);
                                scheduleEntity.setFromLattitude(fromLattitude);
                                scheduleEntity.setToAddress(toAddress);
                                scheduleEntity.setToCity(toCity);
                                scheduleEntity.setToPostalCode(toPostalCode);
                                scheduleEntity.setToRegion(toRegion);
                                scheduleEntity.setToCountry(toCountry);
                                scheduleEntity.setToLattitude(toLattitude);
                                scheduleEntity.setToLongtitude(toLongtitude);
                                scheduleEntity.setUpdateDate(new Date());
                                if(fromCountry.equalsIgnoreCase("Ελλάδα") && !toCountry.equalsIgnoreCase("Ελλάδα")){
                                    scheduleEntity.setType("Εξαγωγή");
                                }else if (!fromCountry.equalsIgnoreCase("Ελλάδα") && toCountry.equalsIgnoreCase("Ελλάδα")){
                                    scheduleEntity.setType("Εισαγωγή");
                                }else{
                                    scheduleEntity.setType("Εσωτερικού");
                                }
                                entityManager.merge(scheduleEntity);
                                update_result.put("status", "success");
                                update_result.put("message", "Η ενημέρωση πραγματοποίηθηκε με επιτυχία");
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
    public Result deleteSchedule(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode update_result = Json.newObject();
                                Long id = json.findPath("id").asLong();
                                ScheduleEntity scheduleEntity = entityManager.find(ScheduleEntity.class, id);
                                String sqlExist = " select * from schedule_packages sp where sp.schedule_id="+id;
                                List<SchedulePackagesEntity> schedulePackagesEntityList = entityManager.createNativeQuery(sqlExist,SchedulePackagesEntity.class).getResultList();
                                if(schedulePackagesEntityList.size()>0){
                                    update_result.put("status", "error");
                                    update_result.put("message", "Βρέθηκαν συνδεδεμένες εγγραφές");
                                    return update_result;
                                }
                                entityManager.remove(scheduleEntity);
                                update_result.put("status", "success");
                                update_result.put("message", "Η Διαγραφή πραγματοποίηθηκε με επιτυχία");
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
    public Result getSchedules(final Http.Request request) throws IOException {
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
                                            String id = json.findPath("id").asText();
                                            String fromAddress = json.findPath("fromAddress").asText();
                                            String fromCity = json.findPath("fromCity").asText();
                                            String fromPostalCode = json.findPath("fromPostalCode").asText();
                                            String fromRegion = json.findPath("fromRegion").asText();
                                            String fromCountry = json.findPath("fromCountry").asText();
                                            String toAddress = json.findPath("toAddress").asText();
                                            String toCity = json.findPath("toCity").asText();
                                            String toPostalCode = json.findPath("toPostalCode").asText();
                                            String toRegion = json.findPath("toRegion").asText();
                                            String type = json.findPath("type").asText();
                                            String toCountry = json.findPath("toCountry").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlMeasures = "select * from schedule s where 1=1 ";
                                            if (!id.equalsIgnoreCase("") && id != null) {
                                                sqlMeasures += " and s.id =" + id + "";
                                            }
                                            if (!fromAddress.equalsIgnoreCase("") && fromAddress != null) {
                                                sqlMeasures += " and s.from_address like '%" + fromAddress + "%'";
                                            }
                                            if (!type.equalsIgnoreCase("") && type != null) {
                                                sqlMeasures += " and s.type like '%" + type + "%'";
                                            }
                                            if (!fromCity.equalsIgnoreCase("") && fromCity != null) {
                                                sqlMeasures += " and s.from_city like '%" + fromCity + "%'";
                                            }
                                            if (!fromPostalCode.equalsIgnoreCase("") && fromPostalCode != null) {
                                                sqlMeasures += " and s.from_postalCode like '%" + fromPostalCode + "%'";
                                            }
                                            if (!fromRegion.equalsIgnoreCase("") && fromRegion != null) {
                                                sqlMeasures += " and s.from_region like '%" + fromRegion + "%'";
                                            }
                                            if (!fromCountry.equalsIgnoreCase("") && fromCountry != null) {
                                                sqlMeasures += " and s.from_country like '%" + fromCountry + "%'";
                                            }
                                            if (!toAddress.equalsIgnoreCase("") && toAddress != null) {
                                                sqlMeasures += " and s.to_address like '%" + toAddress + "%'";
                                            }
                                            if (!toCity.equalsIgnoreCase("") && toCity != null) {
                                                sqlMeasures += " and s.to_city like '%" + toCity + "%'";
                                            }
                                            if (!toPostalCode.equalsIgnoreCase("") && toPostalCode != null) {
                                                sqlMeasures += " and s.to_postal_code like '%" + toPostalCode + "%'";
                                            }
                                            if (!toRegion.equalsIgnoreCase("") && toRegion != null) {
                                                sqlMeasures += " and s.to_region like '%" + toRegion + "%'";
                                            }
                                            if (!toCountry.equalsIgnoreCase("") && toCountry != null) {
                                                sqlMeasures += " and s.to_country like '%" + toCountry + "%'";
                                            }

                                            List<ScheduleEntity> scheduleEntityListAll
                                                    = (List<ScheduleEntity>) entityManager.createNativeQuery(
                                                    sqlMeasures, ScheduleEntity.class).getResultList();

                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                sqlMeasures += " order by " + orderCol + " " + descAsc;
                                            } else {
                                                sqlMeasures += " order by s.creation_date desc";
                                            }
                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlMeasures += " limit " + start + "," + limit;
                                            }
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> schedList = new ArrayList<HashMap<String, Object>>();
                                            List<ScheduleEntity> scheduleEntityList
                                                    = (List<ScheduleEntity>) entityManager.createNativeQuery(
                                                    sqlMeasures, ScheduleEntity.class).getResultList();
                                            for (ScheduleEntity j : scheduleEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("id", j.getId());
                                                HashMap<String, Object> departure = new HashMap<String, Object>();
                                                HashMap<String, Object> arrival = new HashMap<String, Object>();
                                                departure.put("fromAddress", j.getFromAddress());
                                                departure.put("fromCity", j.getFromCity());
                                                departure.put("fromCountry", j.getFromCountry());
                                                departure.put("fromRegion", j.getFromRegion());
                                                departure.put("fromPostalCode", j.getFromPostalCode());
                                                departure.put("fromLongtitude", j.getFromLongtitude());
                                                departure.put("fromLattitude", j.getFromLattitude());
                                                arrival.put("toAddress", j.getToAddress());
                                                arrival.put("toCity", j.getToCity());
                                                arrival.put("toCountry", j.getToCountry());
                                                arrival.put("toRegion", j.getToRegion());
                                                arrival.put("toPostalCode", j.getToPostalCode());
                                                arrival.put("toLongtitude", j.getToLongtitude());
                                                arrival.put("toLattitude", j.getToLattitude());
                                                sHmpam.put("departure", departure);
                                                sHmpam.put("arrival", arrival);
                                                sHmpam.put("expanded",false);
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                sHmpam.put("type", j.getType());
                                                String fullTracked = "select * from schedule_packages sp where sp.measurement_unit_id=23 and sp.schedule_id="+j.getId();
                                                List <SchedulePackagesEntity> spList = entityManager.createNativeQuery(fullTracked,SchedulePackagesEntity.class).getResultList();
                                                if(spList.size()>0){
                                                    sHmpam.put("fullTracked",true);
                                                }else{
                                                    sHmpam.put("fullTracked", false);

                                                }
//                                                sHmpam.put("schedulePackageList",splFinalList);
                                                schedList.add(sHmpam);
                                            }
                                            returnList_future.put("data", schedList);
                                            returnList_future.put("total", scheduleEntityListAll.size());
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



    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getSchedulesMeasueresUnitsByScheduleId(final Http.Request request) throws IOException {
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
                                            String id = json.findPath("scheduleId").asText();
                                            String measureUnitLabel = json.findPath("measureUnitLabel").asText();

                                            String measureuUitPrice = json.findPath("measureuUitPrice").asText();
                                            String measureTo = json.findPath("measureTo").asText();
                                            String measureFrom = json.findPath("measureFrom").asText();


                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sql = "select * from schedule_packages sp where 1=1 ";
                                            if (!id.equalsIgnoreCase("") && id != null) {
                                                sql += " and sp.schedule_id =" + id + "";
                                            }
                                            if (!measureUnitLabel.equalsIgnoreCase("") && measureUnitLabel != null) {
                                                sql += " and sp.measurement_unit_id in " +
                                                        " (select id from measurement_unit mu where mu.title like '%"+measureUnitLabel+"%' ) ";
                                            }
                                            if (!measureTo.equalsIgnoreCase("") && measureTo != null) {
                                                sql += " and sp.to like '%" + measureTo + "%'";
                                            }
                                            if (!measureFrom.equalsIgnoreCase("") && measureFrom != null) {
                                                sql += " and sp.from like '%" + measureFrom + "%'";
                                            }
                                            if (!measureuUitPrice.equalsIgnoreCase("") && measureuUitPrice != null) {
                                                sql += " and sp.unit_price like '%" + measureuUitPrice + "%'";
                                            }




                                            List<SchedulePackagesEntity> schedulePackagesEntityListaLL
                                                    = (List<SchedulePackagesEntity>) entityManager.createNativeQuery(
                                                    sql, SchedulePackagesEntity.class).getResultList();



                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                if(orderCol.equalsIgnoreCase("measureUnitLabel")){
                                                    sql +=  " order by (select title from measurement_unit r where r.id=sp.measurement_unit_id)"+ descAsc;
                                                }else{
                                                    sql += " order by " + orderCol + " " + descAsc;
                                                }
                                            } else {
                                                sql += " order by sp.creation_date desc";
                                            }
                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sql += " limit " + start + "," + limit;
                                            }

                                            System.out.println(sql);
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();

                                            List<HashMap<String, Object>> schedList = new ArrayList<HashMap<String, Object>>();
                                            List<SchedulePackagesEntity> scheduleEntityList
                                                    = (List<SchedulePackagesEntity>) entityManager.createNativeQuery(
                                                    sql, SchedulePackagesEntity.class).getResultList();
                                            for(SchedulePackagesEntity sp : scheduleEntityList){
                                                HashMap<String, Object> spmap = new HashMap<String, Object>();
                                                spmap.put("from", sp.getFromUnit().toString());
                                                spmap.put("to", sp.getToUnit().toString());
                                                spmap.put("scheduleId", sp.getScheduleId());
                                                spmap.put("shdulesPackageId", sp.getId());
                                                spmap.put("id", sp.getId());
                                                spmap.put("measurementUnitId", sp.getMeasurementUnitId());
                                                MeasurementUnitEntity measurementUnit = entityManager.find(MeasurementUnitEntity.class,sp.getMeasurementUnitId());
                                                spmap.put("measurementUnit_id", measurementUnit.getId());
                                                spmap.put("measurementUnit_title", measurementUnit.getTitle());
                                                spmap.put("measurementUnit", measurementUnit);
                                                spmap.put("unitPrice", sp.getUnitPrice().toString());
                                                spmap.put("updateDate", sp.getUpdateDate());
                                                spmap.put("summary", sp.getSummary());
                                                spmap.put("creationDate", sp.getCreationDate());
                                                spmap.put("measureUnitLabel", entityManager.find(MeasurementUnitEntity.class,sp.getMeasurementUnitId()).getTitle());
                                                schedList.add(spmap);
                                            }
                                            //searchMeasureSearch
                                            returnList_future.put("data", schedList);
                                            returnList_future.put("total", schedulePackagesEntityListaLL.size());
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




    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addScheduleMeasureUnit(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                JsonNode measurementUnit = json.findPath("measurementUnit");
                                ((ObjectNode)json).remove("measurementUnit");
                                Double unitPrice = json.findPath("unitPrice").asDouble();
                                Integer to = json.findPath("to").asInt();
                                Integer from = json.findPath("from").asInt();
                                Long scheduleId = json.findPath("scheduleId").asLong();
                                SchedulePackagesEntity schedulePackagesEntity = new SchedulePackagesEntity();
                                schedulePackagesEntity.setCreationDate(new Date());
                                schedulePackagesEntity.setScheduleId(scheduleId);
                                schedulePackagesEntity.setUnitPrice(unitPrice);
                                schedulePackagesEntity.setToUnit(to);
                                schedulePackagesEntity.setFromUnit(from);
                                schedulePackagesEntity.setMeasurementUnitId(measurementUnit.findPath("id").asLong());
                                entityManager.persist(schedulePackagesEntity);
                                add_result.put("status", "success");
                                add_result.put("id", schedulePackagesEntity.getId());
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


//deleteScheduleMeasureUnit deleteAllScheduleMeasureUnitByScheduleId


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteAllScheduleMeasureUnitByScheduleId(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        ((ObjectNode) json).remove("measurementUnit");
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long scheduleId = json.findPath("scheduleId").asLong();
                                Long expectId = json.findPath("expectId").asLong();
                                System.out.println(scheduleId);
                                String sqlS = "select * from schedule_packages sp where sp.schedule_id="+scheduleId;
                                if(expectId!=null && expectId!=0){
                                    sqlS+=" and sp.id!="+expectId;
                                }
                                System.out.println(sqlS);
                                List<SchedulePackagesEntity> spList = entityManager.createNativeQuery(sqlS,SchedulePackagesEntity.class).getResultList();
                                for(SchedulePackagesEntity s : spList){
                                    entityManager.remove(s);
                                }

                                add_result.put("status", "success");
                                add_result.put("message", "Η διαγραφή πραγματοποίηθηκε με επιτυχία");
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
    public Result deleteScheduleMeasureUnit(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        ((ObjectNode) json).remove("measurementUnit");
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long id = json.findPath("id").asLong();
                                System.out.println(json);
                                SchedulePackagesEntity schedulePackagesEntity = entityManager.find(SchedulePackagesEntity.class,id);
                                entityManager.remove(schedulePackagesEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η διαγραφή πραγματοποίηθηκε με επιτυχία");
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
    public Result updateScheduleMeasureUnit(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                JsonNode measurementUnit = json.findPath("measurementUnit");
                                ((ObjectNode)json).remove("measurementUnit");
                                Double unitPrice = json.findPath("unitPrice").asDouble();
                                Integer to = json.findPath("to").asInt();
                                Integer from = json.findPath("from").asInt();
                                Long scheduleId = json.findPath("scheduleId").asLong();
                                Long id = json.findPath("id").asLong();
                                SchedulePackagesEntity schedulePackagesEntity = entityManager.find(SchedulePackagesEntity.class,id);
                                schedulePackagesEntity.setCreationDate(new Date());
                                schedulePackagesEntity.setScheduleId(scheduleId);
                                schedulePackagesEntity.setUnitPrice(unitPrice);
                                schedulePackagesEntity.setToUnit(to);
                                schedulePackagesEntity.setFromUnit(from);
                                schedulePackagesEntity.setMeasurementUnitId(measurementUnit.findPath("id").asLong());
                                entityManager.persist(schedulePackagesEntity);
                                add_result.put("status", "success");
                                add_result.put("id", schedulePackagesEntity.getId());
                                add_result.put("measurementUnit_id", schedulePackagesEntity.getMeasurementUnitId());
                                add_result.put("measurementUnit_title", measurementUnit.findPath("title").asText());
                                add_result.put("message", "Η ενημέρωση πραγματοποίηθηκε με επιτυχία");
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








}