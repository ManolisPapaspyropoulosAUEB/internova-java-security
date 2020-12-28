package controllers.MELLON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.UsersEntity;
import models.UsersMellonEntity;
import models.UsersMellonParkingHistoryEntity;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.Key;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;
public class UsersMellonController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;
    private static final String ALGO = "AES";
    private static final byte[] keyValue =
            new byte[]{'T', 'h', 'e', 'B', 'e', 's', 't',
                    'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y'};

    @Inject
    public UsersMellonController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result createUser(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                String email = json.findPath("email").asText();
                                String password = json.findPath("password").asText();
                                String firstName = json.findPath("firstName").asText();
                                String lastName = json.findPath("lastName").asText();
                                String imageUrl = json.findPath("imageUrl").asText();
                                Integer socialAuth = json.findPath("socialAuth").asInt();
                                String socialPlatform = json.findPath("socialPlatform").asText();
                                String googleId = json.findPath("googleId").asText();
                                String facebookId = json.findPath("facebookId").asText();
                                String address = json.findPath("address").asText();
                                String phone = json.findPath("phone").asText();
                                String vehicleType = json.findPath("vehicleType").asText();
                                String addressCity = json.findPath("addressCity").asText();
                                String postalCode = json.findPath("postalCode").asText();
                                String sqlUniqueEmail = "select * from users_mellon b where b.email=" + "'" + email + "'";
                                List<UsersMellonEntity> emailCheckList = entityManager.createNativeQuery(sqlUniqueEmail, UsersMellonEntity.class).getResultList();
                                if (emailCheckList.size() > 0) {
                                    add_result.put("status", "error");
                                    add_result.put("message", "Το email που δώσατε χρησιμοποιείτε ήδη,προσπαθήστε ξανά");
                                    return add_result;
                                }
                                UsersMellonEntity usersEntity = new UsersMellonEntity();
                                usersEntity.setFirstName(firstName);
                                usersEntity.setLastName(lastName);
                                usersEntity.setEmail(email);
                                usersEntity.setPassword(password);
                                try {
                                    usersEntity.setPassword(encrypt(password));
                                } catch (Exception e) {
                                    add_result.put("status", "error");
                                    add_result.put("message", "Συστημικο προβλημα παρουσιαστηκε,παρακαλω επικοινωνηστε με τον administrator");
                                    return add_result;
                                }
                                usersEntity.setStatus(1);
                                usersEntity.setAddress(address);
                                usersEntity.setVehicleType(vehicleType);
                                usersEntity.setPhone(phone);
                                usersEntity.setImageUrl(imageUrl);
                                usersEntity.setSocialAuth(socialAuth);
                                usersEntity.setSocialPlatform(socialPlatform);
                                usersEntity.setGoogleId(googleId);
                                usersEntity.setFacebookId(facebookId);
                                usersEntity.setPostalCode(postalCode);
                                usersEntity.setAddressCity(addressCity);

                                usersEntity.setBicycleInd(0);
                                usersEntity.setScooterInd(0);
                                usersEntity.setElectricBicycleInd(0);


                                entityManager.persist(usersEntity);
                                add_result.put("status", "success");
                                add_result.put("id", usersEntity.getId());
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
    public Result editUser(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                String email = json.findPath("email").asText();
                                String password = json.findPath("password").asText();
                                String firstName = json.findPath("firstName").asText();
                                String lastName = json.findPath("lastName").asText();
                                Long id = json.findPath("id").asLong();
                                String address = json.findPath("address").asText();
                                String phone = json.findPath("phone").asText();
                                String vehicleType = json.findPath("vehicleType").asText();
                                String addressCity = json.findPath("addressCity").asText();
                                String postalCode = json.findPath("postalCode").asText();

                                Integer bicycleInd = json.findPath("bicycleInd").asInt();
                                Integer scooterInd = json.findPath("scooterInd").asInt();
                                Integer electricBicycleInd = json.findPath("electricBicycleInd").asInt();


                                String sqlUniqueEmail = "select * from users_mellon b where b.email=" + "'" + email + "' and b.id!=" + id;
                                List<UsersMellonEntity> emailCheckList = entityManager.createNativeQuery(sqlUniqueEmail, UsersMellonEntity.class).getResultList();
                                if (emailCheckList.size() > 0) {
                                    add_result.put("status", "error");
                                    add_result.put("message", "Το email που δώσατε χρησιμοποιείτε ήδη,προσπαθήστε ξανά");
                                    return add_result;
                                }
                                UsersMellonEntity usersEntity = entityManager.find(UsersMellonEntity.class, id);
                                if (password != null && !password.equalsIgnoreCase("")) {
                                    try {
                                        usersEntity.setPassword(encrypt(password));
                                    } catch (Exception e) {
                                        add_result.put("status", "error");
                                        add_result.put("message", "Συστημικο προβλημα παρουσιαστηκε,παρακαλω επικοινωνηστε με τον administrator");
                                        return add_result;
                                    }
                                }
                                usersEntity.setFirstName(firstName);
                                usersEntity.setBicycleInd(bicycleInd);
                                usersEntity.setScooterInd(scooterInd);
                                usersEntity.setElectricBicycleInd(electricBicycleInd);
                                usersEntity.setFirstName(firstName);
                                usersEntity.setFirstName(firstName);
                                usersEntity.setLastName(lastName);
                                usersEntity.setEmail(email);
                                usersEntity.setAddress(address);
                                usersEntity.setVehicleType(vehicleType);
                                usersEntity.setPhone(phone);
                                usersEntity.setPostalCode(postalCode);
                                usersEntity.setAddressCity(addressCity);
                                entityManager.persist(usersEntity);
                                add_result.put("status", "success");
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


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getMellonUsers(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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
                                        String id = json.findPath("id").asText();
                                        String sqlUsersMellon = "select * from users_mellon us where 1=1 ";
                                        if (!id.equalsIgnoreCase("") && id != null) {
                                            sqlUsersMellon += " and us.id=" + id;
                                        }
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                        List<HashMap<String, Object>> mellonUsersList = new ArrayList<HashMap<String, Object>>();
                                        List<UsersMellonEntity> usersMellonEntityList
                                                = (List<UsersMellonEntity>) entityManager.createNativeQuery(
                                                sqlUsersMellon, UsersMellonEntity.class).getResultList();
                                        for (UsersMellonEntity j : usersMellonEntityList) {
                                            HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                            sHmpam.put("id", j.getId());
                                            sHmpam.put("address", j.getAddress());
                                            sHmpam.put("addressCity", j.getAddressCity());
                                            sHmpam.put("postalCode", j.getPostalCode());
                                            sHmpam.put("email", j.getEmail());
                                            sHmpam.put("firstName", j.getFirstName());
                                            sHmpam.put("lastName", j.getLastName());
                                            try {
                                                sHmpam.put("password", decrypt(j.getPassword()));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                returnList_future.put("status", "error");
                                                returnList_future.put("message", "Συστημικο προβλημα παρουσιαστηκε,παρακαλω επικοινωνηστε με τον administrator");
                                                return returnList_future;
                                            }
                                            sHmpam.put("phone", j.getPhone());
                                            sHmpam.put("socialAuth", j.getSocialAuth());
                                            sHmpam.put("socialPlatform", j.getSocialPlatform());
                                            sHmpam.put("status", j.getStatus());
                                            sHmpam.put("creationDate", j.getCreationDate());
                                            sHmpam.put("imageUrl", j.getImageUrl());
                                            sHmpam.put("googleId", j.getGoogleId());
                                            sHmpam.put("facebookId", j.getFacebookId());
                                            sHmpam.put("bicycleInd", j.getBicycleInd());
                                            sHmpam.put("scooterInd", j.getScooterInd());
                                            sHmpam.put("electricBicycleInd", j.getElectricBicycleInd());
                                            if((j.getGoogleId()!=null && !j.getGoogleId().equalsIgnoreCase("")) || (j.getFacebookId()!=null  && !j.getFacebookId().equalsIgnoreCase("") )){
                                                sHmpam.put("socialUser", true);
                                            }
                                            String vehicleType="";
                                            if(j.getBicycleInd()==1){
                                                vehicleType="bicycle,";
                                            }
                                            if(j.getScooterInd()==1){
                                                vehicleType+="scooter,";
                                            }
                                            if(j.getElectricBicycleInd()==1){
                                                vehicleType+="electric bicycle,";
                                            }
                                            if(!vehicleType.equalsIgnoreCase("")){
                                                vehicleType = removeLastChar(vehicleType);
                                            }

                                            sHmpam.put("vehicleType",vehicleType);
                                            String sqlChargeBycile = "select * from users_mellon_parking_history umph where umph.user_mellon_id="+j.getId()+
                                                    " and umph.end_time is null";
                                            List <UsersMellonParkingHistoryEntity> parkingHistoryEntityList =
                                                    entityManager.createNativeQuery(sqlChargeBycile,UsersMellonParkingHistoryEntity.class).getResultList();
                                            if(parkingHistoryEntityList.size()>0){
                                                String sqlDur = " SELECT " +
                                                        "  ((time_to_sec((TIMEDIFF(NOW(), umph.start_time))) / 60)*100000)" +
                                                        " FROM  users_mellon_parking_history umph where umph.id="+parkingHistoryEntityList.get(0).getId();
                                                BigDecimal duration = (BigDecimal) entityManager.createNativeQuery(sqlDur).getSingleResult();
                                                sHmpam.put("parkingExist",true);
                                                sHmpam.put("currentDuration",new Date().getTime() - parkingHistoryEntityList.get(0).getStartTime().getTime());
                                            }else{
                                                sHmpam.put("parkingExist",false);
                                                sHmpam.put("currentDuration",0);
                                            }
                                            if (j.getFirstName() != null
                                                    && !j.getFirstName().equalsIgnoreCase("")
                                                    && j.getEmail() != null
                                                    && !j.getEmail().equalsIgnoreCase("")
                                                    && j.getAddress() != null
                                                    && !j.getAddress().equalsIgnoreCase("")
                                                    && j.getPhone() != null
                                                    && !j.getPhone().equalsIgnoreCase("")) {
                                                sHmpam.put("firstTimeLogin", false);
                                            } else {
                                                sHmpam.put("firstTimeLogin", true);
                                            }
                                            mellonUsersList.add(sHmpam);
                                        }
                                        returnList_future.put("data", mellonUsersList);
                                        returnList_future.put("total", mellonUsersList.size());
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

    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result loginMellonUser(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                System.out.println(json);
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode result_future = Json.newObject();
                                String email = json.findPath("email").asText();
                                String password = json.findPath("password").asText();
                                String loginSQL = "";
                                try {
                                    loginSQL = "select * from users_mellon u where u.email=" + "'" + email + "'" + " and u.password=" + "'" + encrypt(password) + "'";
                                    System.out.println(loginSQL);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                List<UsersMellonEntity> usersEntityList = (List<UsersMellonEntity>) entityManager.createNativeQuery(loginSQL, UsersMellonEntity.class).getResultList();
                                if (usersEntityList.size() > 0) {
                                    result_future.put("status", "ok");
                                    result_future.put("id", usersEntityList.get(0).getId());
                                    result_future.put("firstName", usersEntityList.get(0).getFirstName());
                                    result_future.put("lastName", usersEntityList.get(0).getLastName());
                                    result_future.put("email", usersEntityList.get(0).getEmail());
                                    result_future.put("vehicleType", usersEntityList.get(0).getVehicleType());
                                    try {
                                        result_future.put("password", decrypt(usersEntityList.get(0).getPassword()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        result_future.put("status", "error");
                                        result_future.put("message", "Προβλημα κατα την ανάκτηση");
                                        return result_future;
                                    }
                                    try {
                                        result_future.put("token", encrypt(getSaltString()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        result_future.put("status", "error");
                                        result_future.put("message", "Προβλημα κατα την ανάκτηση");
                                        return result_future;
                                    }
                                } else {
                                    String emailSql = "select * from users u where u.email=" + "'" + email + "'";
                                    List<UsersEntity> usersEntityListEmailMatch = (List<UsersEntity>) entityManager.createNativeQuery(emailSql, UsersEntity.class).getResultList();
                                    if (usersEntityListEmailMatch.size() > 0) {
                                        result_future.put("status", "error");
                                        result_future.put("message", "Λάνθασμένος κωδικός πρόσβασης");
                                    } else {
                                        result_future.put("status", "error");
                                        result_future.put("message", "Δεν βρέθηκε χρήστης με αυτό το email");
                                    }
                                }
                                return result_future;
                            });
                        },
                        executionContext);
                result = (ObjectNode) addFuture.get();
                return ok(result);
            } catch (Exception e) {
                ObjectNode result = Json.newObject();
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Προβλημα κατα την ανάκτηση");
                return ok(result);
            }
        }
    }

    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result registerAndLoginWithSocialCAccount(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                System.out.println(json);
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode result_future = Json.newObject();
                                System.out.println(json);
                                String email = json.findPath("email").asText();
                                String sqlFindUserByEmail = "select * from users_mellon um where um.email='" + email + "'";
                                List<UsersMellonEntity> usersMellonEntityList = entityManager.createNativeQuery(sqlFindUserByEmail, UsersMellonEntity.class).getResultList();
                                if (usersMellonEntityList.size() > 0) {
                                    UsersMellonEntity mellonUser = entityManager.find(UsersMellonEntity.class, usersMellonEntityList.get(0).getId());
                                    mellonUser.setImageUrl(json.findPath("imageUrl").asText());
                                    mellonUser.setGoogleId(json.findPath("googleId").asText());
                                    mellonUser.setFacebookId(json.findPath("facebookId").asText());
                                    entityManager.merge(mellonUser);
                                    result_future.put("id", mellonUser.getId());
                                    result_future.put("imageUrl", mellonUser.getImageUrl());
                                    result_future.put("status", "success");
                                    result_future.put("firstTimeLogin", false);
                                    return result_future;
                                }
                                UsersMellonEntity newUserMellon = new UsersMellonEntity();
                                newUserMellon.setImageUrl(json.findPath("imageUrl").asText());
                                newUserMellon.setGoogleId(json.findPath("googleId").asText());
                                newUserMellon.setFacebookId(json.findPath("facebookId").asText());
                                newUserMellon.setCreationDate(new Date());
                                newUserMellon.setFirstName(json.findPath("firstName").asText());
                                newUserMellon.setLastName(json.findPath("lastName").asText());
                                newUserMellon.setEmail(json.findPath("email").asText());
                                newUserMellon.setStatus(1);
                                newUserMellon.setElectricBicycleInd(0);
                                newUserMellon.setScooterInd(0);
                                newUserMellon.setBicycleInd(0);
                                entityManager.persist(newUserMellon);
                                result_future.put("id", newUserMellon.getId());
                                result_future.put("imageUrl", newUserMellon.getImageUrl());
                                result_future.put("firstTimeLogin", true);
                                result_future.put("status", "success");
                                return result_future;
                            });
                        },
                        executionContext);
                result = (ObjectNode) addFuture.get();
                return ok(result);
            } catch (Exception e) {
                ObjectNode result = Json.newObject();
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Προβλημα κατα την ανάκτηση");
                return ok(result);
            }
        }

    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result createNewParkingEntryForUserMellon(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long userMellonId = json.findPath("userMellonId").asLong();
                                String barcode = json.findPath("barcode").asText();
                                String station = json.findPath("station").asText();
                                UsersMellonParkingHistoryEntity userParking = new UsersMellonParkingHistoryEntity();
                                userParking.setCreationDate(new Date());
                                userParking.setBarcode(barcode);
                                userParking.setStation(station);
                                userParking.setStartTime(new Date());
                                userParking.setUserMellonId(userMellonId);
                                entityManager.persist(userParking);
                                add_result.put("status", "success");
                                add_result.put("message", "Η διαδικασία πραγματοποίηθηκε με επιτυχία");
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


    static final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs
    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result editNewParkingEntryForUserMellon(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long userMellonId = json.findPath("userMellonId").asLong();
                                String barcode = json.findPath("barcode").asText();
                                String sqlCodes = "select * from users_mellon_parking_history umph where umph.barcode='" + barcode + "' and umph.user_mellon_id=" + userMellonId + " and umph.end_time is null";
                                List<UsersMellonParkingHistoryEntity> barcodeList = entityManager.createNativeQuery(sqlCodes, UsersMellonParkingHistoryEntity.class).getResultList();
                                if (barcodeList.size() > 0) {
                                    UsersMellonParkingHistoryEntity userParking = entityManager.find(UsersMellonParkingHistoryEntity.class, barcodeList.get(0).getId());
                                    userParking.setEndTime(new Date());
                                    String sqlDur = " SELECT " +
                                            " ROUND(time_to_sec((TIMEDIFF(NOW(), umph.start_time))) / 60) " +
                                            " FROM  users_mellon_parking_history umph where umph.id="+userParking.getId();
                                    BigDecimal duration = (BigDecimal) entityManager.createNativeQuery(sqlDur).getSingleResult();
                                    userParking.setDuration(duration.doubleValue());
                                    entityManager.merge(userParking);
                                    add_result.put("status", "success");
                                    add_result.put("message", "Η ενημερωση πραγματοποίηθηκε με επιτυχία,το ποδήλατο ξεκλειδώθηκε");
                                    return add_result;
                                } else {
                                    add_result.put("status", "error");
                                    add_result.put("message", "Ο κωδικός δεν ταιριάζει με το ποδηλατο σας,ξαναπροσπαθήστε");
                                    return add_result;
                                }

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
    public Result getParkinhHistoryByUser(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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
                                        String id = json.findPath("id").asText();
                                        String sqlUsersMellon = "select * from users_mellon_parking_history uph where 1=1 ";
                                        if (!id.equalsIgnoreCase("") && id != null) {
                                            sqlUsersMellon += " and uph.user_mellon_id=" + id;
                                        }
                                        sqlUsersMellon+=" order by uph.creation_date desc ";
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                        List<HashMap<String, Object>> parkingHistoryList = new ArrayList<HashMap<String, Object>>();
                                        List<UsersMellonParkingHistoryEntity> usersMellonParkingHistoryEntityList
                                                = (List<UsersMellonParkingHistoryEntity>) entityManager.createNativeQuery(
                                                sqlUsersMellon, UsersMellonParkingHistoryEntity.class).getResultList();
                                        for (UsersMellonParkingHistoryEntity j : usersMellonParkingHistoryEntityList) {
                                            HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                            sHmpam.put("id", j.getId());
                                            sHmpam.put("endTime", j.getEndTime());
                                            sHmpam.put("startTime", j.getStartTime());
                                            sHmpam.put("duration", j.getDuration());
                                            sHmpam.put("station", j.getStation());
                                            if(j.getDuration()!=null){
                                                sHmpam.put("cost", j.getDuration() * 0.10 + "€");
                                            }else{
                                                sHmpam.put("cost", "Σε εξέλιξη");
                                            }
                                            parkingHistoryList.add(sHmpam);
                                        }
                                        returnList_future.put("data", parkingHistoryList);
                                        returnList_future.put("total", parkingHistoryList.size());
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


    //------------------------------------------------------------------------------------------------------------------------------------------------------------------//
    @SuppressWarnings("Duplicates")
    protected String getSaltString() {
        String SALTCHARS = "abcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 6) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    @SuppressWarnings("Duplicates")
// Generates a random int with n digits
    public static int generateRandomDigits(int n) {
        int m = (int) Math.pow(10, n - 1);
        return m + new Random().nextInt(9 * m);
    }

    @SuppressWarnings("Duplicates")
    //encryption password user
    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }

    @SuppressWarnings("Duplicates")
    public static String encrypt(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());

        String encryptedValue = Base64.getEncoder().encodeToString(encVal);
        return encryptedValue;
    }

    @SuppressWarnings("Duplicates")
    public static String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = Base64.getDecoder().decode(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }


    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }


}
