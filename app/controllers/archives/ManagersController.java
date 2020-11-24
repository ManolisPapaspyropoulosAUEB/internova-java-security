package controllers.archives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.*;
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

public class ManagersController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public ManagersController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addManager(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                System.out.println(json);
                                ObjectNode result_add = Json.newObject();
                                String firstname = json.findPath("firstname").asText();
                                String lastname = json.findPath("lastname").asText();
                                String position = json.findPath("position").asText();
                                String telephone = json.findPath("telephone").asText();
                                String email = json.findPath("email").asText();
                                String system = json.findPath("system").asText();
                                Long system_id = json.findPath("system_id").asLong();
                                Long selectedManagerId = json.findPath("selectedManagerId").asLong();
                                if (1 == 0) {
                                    ManagersEntity manager = entityManager.find(ManagersEntity.class, selectedManagerId);
                                    manager.setSystem(system);
                                    manager.setSystemId(system_id);
                                    entityManager.persist(manager);
                                } else {
                                    ManagersEntity manager = new ManagersEntity();
                                    manager.setCreationDate(new Date());
                                    manager.setFirstName(firstname);
                                    manager.setLastName(lastname);
                                    manager.setPosition(position);
                                    manager.setTelephone(telephone);
                                    manager.setEmail(email);
                                    if (system != null && !system.equalsIgnoreCase("")) {
                                        manager.setSystem(system);
                                    }
                                    if (system_id != null && system_id != 0) {
                                        manager.setSystemId(system_id);
                                    }
                                    entityManager.persist(manager);
                                }
                                result_add.put("status", "success");
                                result_add.put("message", "Η καταχώρηση ολοκληρώθηκε με επιτυχία!");
                                return result_add;
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
    public Result updateManager(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode result_add = Json.newObject();
                                String firstname = json.findPath("firstname").asText();
                                String lastname = json.findPath("lastname").asText();
                                String position = json.findPath("position").asText();
                                String telephone = json.findPath("telephone").asText();
                                String email = json.findPath("email").asText();
                                String system = json.findPath("system").asText();
                                Long systemId = json.findPath("systemId").asLong();
                                Long id = json.findPath("id").asLong();
                                ManagersEntity manager = entityManager.find(ManagersEntity.class, id);
                                manager.setUpdateDate(new Date());
                                manager.setFirstName(firstname);
                                manager.setLastName(lastname);
                                manager.setPosition(position);
                                manager.setTelephone(telephone);
                                manager.setEmail(email);
                                manager.setSystem(system);
                                manager.setSystemId(systemId);
                                entityManager.merge(manager);
                                result_add.put("status", "success");
                                result_add.put("message", "Η ενημέρωση ολοκληρώθηκε με επιτυχία!");
                                return result_add;
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
    public Result deleteManager(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode result_add = Json.newObject();
                                    Long id = json.findPath("id").asLong();
                                    ManagersEntity manager = entityManager.find(ManagersEntity.class, id);
                                    if(manager.getSystemId()!=null && manager.getSystemId()!=0){
                                        result_add.put("status", "error");
                                        result_add.put("message", "Ο συγκεκριμένος υπεύθυνος είναι συνδεδεμένος με κάποια Ετερεία/σύστημα");
                                    }else{
                                        entityManager.remove(manager);
                                        result_add.put("status", "success");
                                        result_add.put("message", "Η Διαγραφή ολοκληρώθηκε με επιτυχία!");
                                    }
                                    return result_add;
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
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την καταχωρηση");
            return ok(result);
        }
    }


    //getManagersExpect


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getManagersExpect(final Http.Request request) throws IOException {
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
                                            String system = json.findPath("system").asText();
                                            String system_id = json.findPath("system_id").asText();
                                            String sqlManagers = "select * from managers manager" + " where 1=1 and" +
                                                    " (manager.system != " + "'" + system + "'" + " or manager.system is null ) " +
                                                    "and  (manager.system_id!=" + system_id + " or manager.system_id is null)";
                                            //systemBrandName
                                            if (system.equalsIgnoreCase("-")) {

                                            }
                                            System.out.println(sqlManagers);


                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<ManagersEntity> posList
                                                    = (List<ManagersEntity>) entityManager.createNativeQuery(
                                                    sqlManagers, ManagersEntity.class).getResultList();
                                            for (ManagersEntity j : posList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("firstname", j.getFirstName());
                                                sHmpam.put("lastname", j.getLastName());
                                                sHmpam.put("fullName", j.getFirstName() + " " + j.getLastName());
                                                sHmpam.put("email", j.getEmail());
                                                sHmpam.put("position", j.getPosition());
                                                sHmpam.put("systemId", j.getSystemId());
                                                sHmpam.put("system_id", j.getSystemId());
                                                sHmpam.put("telephone", j.getTelephone());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                sHmpam.put("system", j.getSystem());
                                                if (j.getSystemId() != null) {
                                                    switch (j.getSystem()) {
                                                        case "Εργοστάσιο":
                                                            sHmpam.put("systemBrandName", entityManager.find(FactoriesEntity.class, j.getSystemId()).getBrandName());

                                                            break;
                                                        case "Αποθήκη":
                                                            sHmpam.put("systemBrandName", entityManager.find(WarehousesEntity.class, j.getSystemId()));
                                                            break;
                                                        default:
                                                            // code block
                                                    }
                                                } else {
                                                    sHmpam.put("systemBrandName", "-");
                                                    sHmpam.put("system", "-");
                                                }

                                                serversList.add(sHmpam);
                                            }
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
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων");
            return ok(result);
        }
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getManagers(final Http.Request request) throws IOException {
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
                                            String system = json.findPath("system").asText();
                                            String systemSearch = json.findPath("systemSearch").asText();
                                            String system_id = json.findPath("system_id").asText();
                                            String id = json.findPath("id").asText();
                                            String firstName = json.findPath("firstName").asText();
                                            String email = json.findPath("email").asText();
                                            String lastName = json.findPath("lastName").asText();
                                            String position = json.findPath("position").asText();
                                            String systemBrandName = json.findPath("systemBrandName").asText();
                                            String telephone = json.findPath("telephone").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlManagers = "select * from managers manager where 1=1 ";
                                            if (!firstName.equalsIgnoreCase("") && firstName != null) {
                                                sqlManagers += " and manager.first_name like '%" + firstName + "%'";
                                            }
                                            if (!email.equalsIgnoreCase("") && email != null) {
                                                sqlManagers += " and manager.email like '%" + email + "%'";
                                            }
                                            if (!lastName.equalsIgnoreCase("") && lastName != null) {
                                                sqlManagers += " and manager.last_name like '%" + lastName + "%'";
                                            }
                                            if (!position.equalsIgnoreCase("") && position != null) {
                                                sqlManagers += " and manager.position like '%" + position + "%'";
                                            }
                                            if (!telephone.equalsIgnoreCase("") && telephone != null) {
                                                sqlManagers += " and manager.telephone like '%" + telephone + "%'";
                                            }
                                            if (!system.equalsIgnoreCase("") && system != null) {
                                                sqlManagers += " and manager.system = '" + system + "'";
                                            }
                                            if (!systemSearch.equalsIgnoreCase("") && systemSearch != null) {
                                                sqlManagers += " and manager.system like '%" + systemSearch + "%'";
                                            }
                                            if (!system_id.equalsIgnoreCase("") && system_id != null) {
                                                sqlManagers += " and manager.system_id = '" + system_id + "'";
                                            }
                                            if (!id.equalsIgnoreCase("") && id != null) {
                                                sqlManagers += " and manager.id=" + id;
                                            }
                                            if (!id.equalsIgnoreCase("") && id != null) {
                                                sqlManagers += " and manager.id=" + id;
                                            }
                                            if (!systemBrandName.equalsIgnoreCase("") && systemBrandName != null) {
                                                sqlManagers += " and " +
                                                        " (manager.system_id in " +
                                                        " (select id " +
                                                        " from factories f" +
                                                        " where f.brand_name like '%" + systemBrandName + "%'" +
                                                        " union" +
                                                        " select id" +
                                                        " from warehouses w" +
                                                        " where w.brand_name like '%" + systemBrandName + "%'))";
                                            }

                                            List<ManagersEntity> posListAll
                                                    = (List<ManagersEntity>) entityManager.createNativeQuery(
                                                    sqlManagers, ManagersEntity.class).getResultList();
                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {

                                                if (orderCol.equalsIgnoreCase("systemBrandName")) {
                                                    sqlManagers += " order by " +
                                                            "\n"+" CASE manager.system " +
                                                            "\n"+"when 'Αποθήκη' then " +
                                                            "\n"+"     (select brand_name from warehouses w where w.id=manager.system_id) " +
                                                            "\n"+"when 'Εργοστάσιο' then " +
                                                            "\n"+"     (select brand_name from factories f where f.id=manager.system_id) " +
                                                            "\n"+"when 'Πελάτες-Προμηθευτές' then " +
                                                            "\n"+"     (select brand_name from customers_suppliers cs where cs.id=manager.system_id)  " +
                                                            "\n"+"when 'Προσφορά' then " +
                                                            "\n"+"      (" +
                                                            "\n"+"        select cs.brand_name " +
                                                            "\n"+"        from offers offer " +
                                                            "\n"+"        join customers_suppliers cs on " +
                                                            "\n"+"        (cs.id=offer.customer_id) " +
                                                            "\n"+"        where (offer.id=manager.system_id)" +
                                                            "\n"+"      )" +
                                                            "\n"+"else '-' " +
                                                            "\n"+"  END  " + descAsc;
                                                } else {
                                                    sqlManagers += " order by " + orderCol + " " + descAsc;
                                                }
                                            } else {
                                                sqlManagers += " order by creation_date desc";
                                            }

                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlManagers += " limit " + start + "," + limit;
                                            }
                                            System.out.println(sqlManagers);
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<ManagersEntity> posList
                                                    = (List<ManagersEntity>) entityManager.createNativeQuery(
                                                    sqlManagers, ManagersEntity.class).getResultList();
                                            for (ManagersEntity j : posList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("firstname", j.getFirstName());
                                                sHmpam.put("lastname", j.getLastName());
                                                sHmpam.put("fullName", j.getFirstName() + " " + j.getLastName());
                                                sHmpam.put("email", j.getEmail());
                                                sHmpam.put("position", j.getPosition());
                                                sHmpam.put("systemId", j.getSystemId());
                                                sHmpam.put("system_id", j.getSystemId());
                                                sHmpam.put("telephone", j.getTelephone());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                sHmpam.put("system", j.getSystem());
                                                if (j.getSystemId() != null && j.getSystemId() != 0) {
                                                    if (j.getSystem().equalsIgnoreCase("Εργοστάσιο")) {
                                                        sHmpam.put("systemBrandName",
                                                                entityManager.find(FactoriesEntity.class,
                                                                        j.getSystemId()).getBrandName());
                                                    } else if (j.getSystem().equalsIgnoreCase("Αποθήκη")) {
                                                        sHmpam.put("systemBrandName",
                                                                entityManager.find(WarehousesEntity.class,
                                                                        j.getSystemId()).getBrandName());
                                                    } else if (j.getSystem().equalsIgnoreCase("Προσφορά")) {
                                                        sHmpam.put("systemBrandName",
                                                                entityManager.find(CustomersSuppliersEntity.class, entityManager.find(OffersEntity.class,
                                                                        j.getSystemId()).getCustomerId()).getBrandName());
                                                    } else if (j.getSystem().equalsIgnoreCase("Πελάτες-Προμηθευτές")) {
                                                        sHmpam.put("systemBrandName",
                                                                entityManager.find(CustomersSuppliersEntity.class,
                                                                        j.getSystemId()).getBrandName());
                                                    }
                                                } else {
                                                    sHmpam.put("systemBrandName", "-");
                                                    sHmpam.put("system", "-");
                                                }
                                                serversList.add(sHmpam);
                                            }
                                            returnList_future.put("data", serversList);
                                            returnList_future.put("total", posListAll.size());
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
