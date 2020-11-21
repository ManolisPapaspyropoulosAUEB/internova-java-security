package controllers.security;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.*;
import play.api.Configuration;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import java.io.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.io.IOException;
import java.security.Key;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;
public class UsersControllers {
    protected Configuration configuration;
    private static final String ALGO = "AES";
    private static final byte[] keyValue =
            new byte[]{'T', 'h', 'e', 'B', 'e', 's', 't',
                    'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y'};
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;
    final static String uploadPath = "D:/developm/internova(Pr)/internova_JAVA_security/uploads/";
    @Inject
    public UsersControllers(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }
    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addUser(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {

                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode add_result = Json.newObject();
                                    String username = json.findPath("username").asText();
                                    String email = json.findPath("email").asText();
                                    String firstname = json.findPath("firstname").asText();
                                    String lastname = json.findPath("lastname").asText();
                                    String password = json.findPath("password").asText();
                                    String phone = json.findPath("telephone").asText();
                                    String position = json.findPath("position").asText();
                                    Integer status = json.findPath("status").asInt();
                                    String token = json.findPath("token").asText();
                                    String mobilePhone = json.findPath("mobilePhone").asText();
                                    String gender = json.findPath("gender").asText();
                                    String comments = json.findPath("comments").asText();
                                    Long roleId = json.findPath("roleId").asLong();
                                    Long orgId = json.findPath("orgId").asLong();
                                    Long depId = json.findPath("depId").asLong();
                                    //check if username is unique
                                    String sqlUsername = "select * from users where username='" + username + "'";
                                    List<UsersEntity> usersEntityList = (List<UsersEntity>)
                                            entityManager.createNativeQuery(sqlUsername, UsersEntity.class).getResultList();
                                    if (usersEntityList.size() > 0) {
                                        add_result.put("status", "error");
                                        add_result.put("message", "Το username χρησιμοποιειται απο αλλον user");
                                        return add_result;
                                    }
                                    //check if email is unique
                                    String sqlemail = "select * from users where email='" + email + "'";
                                    List<UsersEntity> usersEmailEntityList = (List<UsersEntity>)
                                            entityManager.createNativeQuery(sqlemail, UsersEntity.class).getResultList();
                                    if (usersEmailEntityList.size() > 0) {
                                        add_result.put("status", "error");
                                        add_result.put("message", "Το email χρησιμοποιειται απο αλλον user");
                                        return add_result;
                                    }
                                    UsersEntity user = new UsersEntity();
                                    user.setUsername(username);
                                    user.setEmail(email);
                                    user.setPosition(position);


                                    user.setFirstname(firstname);
                                    user.setLastname(lastname);
                                    user.setDepId(depId);
                                    try {
                                        user.setPassword(encrypt(password));
                                    } catch (Exception e) {
                                        add_result.put("status", "error");
                                        add_result.put("message", "Συστημικο προβλημα παρουσιαστηκε,παρακαλω επικοινωνηστε με τον administrator");
                                        return add_result;
                                    }
                                    user.setComments(comments);
                                    user.setGender(gender);
                                    user.setMobilePhone(mobilePhone);
                                    user.setPosition(position);
                                    user.setPhone(phone);
                                    user.setToken(token);
                                    user.setRoleId(roleId);
                                    user.setOrgId(orgId);
                                    user.setStatus(status);
                                    user.setCreationDate(new Date());
                                    entityManager.persist(user);
                                    add_result.put("status", "success");
                                    add_result.put("userId", user.getUserId());
                                    add_result.put("message", "Η καταχώρηση ολοκληρώθηκε με επιτυχία!");
                                    return add_result;
                                });
                            },
                            executionContext);


                    result = (ObjectNode) addFuture.get();
                    result.put("headers", request.getHeaders().asMap().toString());
                    //request.getHeaders().asMap().

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

    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateUser(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();

                    System.out.println(json);
                    CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode resultfuture = Json.newObject();
                                    String username = json.findPath("username").asText();
                                    String email = json.findPath("email").asText();
                                    String firstname = json.findPath("firstname").asText();
                                    String lastname = json.findPath("lastname").asText();
                                    String password = json.findPath("password").asText();
                                    String phone = json.findPath("telephone").asText();
                                    String position = json.findPath("position").asText();
                                    Integer status = json.findPath("status").asInt();
                                    String token = json.findPath("token").asText();
                                    String mobilePhone = json.findPath("mobilePhone").asText();
                                    String gender = json.findPath("gender").asText();
                                    String comments = json.findPath("comments").asText();
                                    Long id = json.findPath("userId").asLong();


//                                    Long roleId = json.findPath("selectedRole").findPath("id").asLong();
//                                    Long orgId = json.findPath("selectedOrganization").findPath("id").asLong();
//                                    Long depId = json.findPath("selectedDep").findPath("id").asLong();

                                    Long roleId = json.findPath("roleId").asLong();
                                    Long orgId = json.findPath("orgId").asLong();
                                    Long depId = json.findPath("depId").asLong();

                                    //check if username is unique
                                    String sqlUsername = "select * from users where username='" + username + "'" + "and user_id !=" + id;
                                    System.out.println(sqlUsername);
                                    List<UsersEntity> usersEntityList = (List<UsersEntity>)
                                            entityManager.createNativeQuery(sqlUsername, UsersEntity.class).getResultList();
                                    if (usersEntityList.size() > 0) {
                                        resultfuture.put("status", "error");
                                        resultfuture.put("message", "Το username χρησιμοποιειται απο αλλον user");
                                        return resultfuture;
                                    }
                                    //check if email is unique
                                    String sqlemail = "select * from users where email='" + email + "'" + "and user_id !=" + id;
                                    ;
                                    System.out.println(sqlemail);
                                    List<UsersEntity> usersEmailEntityList = (List<UsersEntity>)
                                            entityManager.createNativeQuery(sqlemail, UsersEntity.class).getResultList();
                                    if (usersEmailEntityList.size() > 0) {
                                        resultfuture.put("status", "error");
                                        resultfuture.put("message", "Το email χρησιμοποιειται απο αλλον user");
                                        return resultfuture;
                                    }
                                    UsersEntity user = entityManager.find(UsersEntity.class, id);
                                    user.setUsername(username);
                                    user.setEmail(email);
                                    user.setFirstname(firstname);
                                    user.setLastname(lastname);
                                    try {
                                        user.setPassword(encrypt(password));
                                    } catch (Exception e) {
                                        resultfuture.put("status", "error");
                                        resultfuture.put("message", "Συστημικο προβλημα παρουσιαστηκε,παρακαλω επικοινωνηστε με τον administrator");
                                        return resultfuture;
                                    }
                                    user.setComments(comments);
                                    user.setGender(gender);
                                    user.setMobilePhone(mobilePhone);
                                    user.setPosition(position);
                                    user.setPhone(phone);
                                    user.setPosition(position);
                                    user.setToken(token);
                                    user.setRoleId(roleId);
                                    user.setOrgId(orgId);
                                    user.setStatus(status);
                                    user.setRoleId(roleId);
                                    user.setOrgId(orgId);
                                    user.setDepId(depId);
                                    user.setCreationDate(new Date());
                                    entityManager.merge(user);
                                    resultfuture.put("status", "success");
                                    resultfuture.put("userId", user.getUserId());
                                    resultfuture.put("message", "Η ενημέρωση πραγματοποιήθηκε με επιτυχία!");
                                    return resultfuture;
                                });
                            },
                            executionContext);


                    result = (ObjectNode) updateFuture.get();
                    return ok(result);

                } catch (Exception e) {
                    ObjectNode result = Json.newObject();
                    e.printStackTrace();
                    result.put("status", "error");
                    result.put("message", "Προβλημα κατα την ενημερωση");
                    return ok(result);
                }
            }
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την ενημερωση");
            return ok(result);
        }
    }



    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result login(final Http.Request request) throws IOException {
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
                                        loginSQL = "select * from users u where u.email="+"'"+email+"'" +" and u.password="+"'"+encrypt(password)+"'";
                                        System.out.println(loginSQL);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    List<UsersEntity> usersEntityList = (List<UsersEntity>)  entityManager.createNativeQuery(loginSQL,UsersEntity.class).getResultList();
                                    if(usersEntityList.size()>0){
                                        result_future.put("status", "ok");
                                        result_future.put("id", usersEntityList.get(0).getUserId() );
                                        result_future.put("username", usersEntityList.get(0).getUsername() );
                                        result_future.put("firstName", usersEntityList.get(0).getFirstname() );
                                        result_future.put("lastName", usersEntityList.get(0).getLastname() );
                                        result_future.put("email", usersEntityList.get(0).getEmail() );
                                        try {
                                            result_future.put("token", encrypt(getSaltString()) );
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            result_future.put("status", "error");
                                            result_future.put("message", "Προβλημα κατα την ανάκτηση");
                                        }
                                    }else{
                                        String emailSql = "select * from users u where u.email="+"'"+email+"'";
                                        List<UsersEntity> usersEntityListEmailMatch = (List<UsersEntity>)  entityManager.createNativeQuery(emailSql,UsersEntity.class).getResultList();
                                        if(usersEntityListEmailMatch.size()>0){
                                            result_future.put("status", "error");
                                            result_future.put("message", "Λάνθασμένος κωδικός πρόσβασης");
                                        }else {
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
    public Result deleteUser(final Http.Request request) throws IOException {
        try {
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
                                    Long id = json.findPath("userId").asLong();
                                    UsersEntity o = entityManager.find(UsersEntity.class, id);
                                    if (o != null) {
                                        entityManager.remove(o);
                                        result_future.put("status", "success");
                                        result_future.put("message", "Η διαγραφή ολοκληρώθηκε με επιτυχία!");
                                        String filesSql = "select * from documents d where d.user_id="+id;
                                        List<DocumentsEntity> docsList = (List<DocumentsEntity>)  entityManager.createNativeQuery(filesSql,DocumentsEntity.class).getResultList();
                                        for(DocumentsEntity doc : docsList){
                                            entityManager.remove(doc);
                                            File dest = new File(uploadPath.concat(doc.getFullPath()));//
                                            dest.delete();
                                            entityManager.remove(doc);
                                        }
                                        File dest = new File(uploadPath.concat("D:/developm/internova(Pr)/internova_JAVA_security/uploads/"+id));
                                        dest.delete();
                                    } else {
                                        result_future.put("status", "error");
                                        result_future.put("message", "Δεν βρέθηκε σχετικός ρόλος");
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
                    result.put("message", "Προβλημα κατα την διαγραφή");
                    return ok(result);
                }
            }
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την διαγραφή");
            return ok(result);
        }
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result updatePassword(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    //password: "12345678"
                    //userId: "60"


                    String password = json.findPath("password").asText();
                    String userId = json.findPath("userId").asText();
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode result_future = Json.newObject();
                                    Long id = json.findPath("userId").asLong();
                                    UsersEntity user = entityManager.find(UsersEntity.class, id);
                                    try {
                                        user.setPassword(encrypt(password));
                                    } catch (Exception e) {
                                        result_future.put("status", "error");
                                        result_future.put("message", "Προβλημα κατα την ενημερωση");
                                        return result_future;
                                    }
                                    result_future.put("status", "success");
                                    result_future.put("message", "Η αλλαγή κωδικού πραγματοποιήθηκε με επιτυχία!");
                                    return result_future;
                                });
                            },
                            executionContext);
                    result = (ObjectNode) updateFuture.get();
                    return ok(result);

                } catch (Exception e) {
                    ObjectNode result = Json.newObject();
                    e.printStackTrace();
                    result.put("status", "error");
                    result.put("message", "Προβλημα κατα την διαγραφή");
                    return ok(result);
                }
            }
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την διαγραφή");
            return ok(result);
        }
    }



    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getUsers(final Http.Request request) throws IOException {
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
                                            String name = json.findPath("name").asText();
                                            String lastname = json.findPath("lastname").asText();
                                            String organization = json.findPath("organization").findPath("name").asText();
                                            String position = json.findPath("position").asText();
                                            String role = json.findPath("roleName").asText();
                                            String department = json.findPath("department").findPath("name").asText();
                                            String statusSearch = json.findPath("statusSearch").findPath("name").asText();
                                            String email = json.findPath("email").asText();
                                            String username = json.findPath("username").asText();
                                            String id = json.findPath("id").asText();
                                            String userId = json.findPath("userId").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlusers = "select * from users u where 1=1 ";
                                            if (!name.equalsIgnoreCase("") && name != null) {
                                                sqlusers += " and u.firstname like '%" + name + "%'";
                                            }
                                            if (!lastname.equalsIgnoreCase("") && lastname != null) {
                                                sqlusers += " and u.lastname like '%" + lastname + "%'";
                                            }
                                            if (!email.equalsIgnoreCase("") && email != null) {
                                                sqlusers += " and u.email like '%" + email + "%'";
                                            }

                                            if (!username.equalsIgnoreCase("") && username != null) {
                                                sqlusers += " and u.username like '%" + username + "%'";
                                            }
                                            if (!position.equalsIgnoreCase("") && position != null) {
                                                sqlusers += " and u.position like '%" + position + "%'";
                                            }
                                            if (!statusSearch.equalsIgnoreCase("") && statusSearch != null) {
                                                if (statusSearch.equalsIgnoreCase("Ανενεργός")) {
                                                    sqlusers += " and u.status =0";
                                                } else {
                                                    sqlusers += " and u.status =1";
                                                }
                                            }
                                            if (!organization.equalsIgnoreCase("") && organization != null && !organization.equalsIgnoreCase("null")) {
                                                sqlusers += " and u.org_id in (select organization_id from organizations alias where alias.name like '%" + organization + "%')";
                                            }
                                            if (!role.equalsIgnoreCase("") && role != null && !role.equalsIgnoreCase("null")) {
                                                sqlusers += " and u.role_id in (select role_id from roles alias where alias.name like '%" + role + "%'   )";
                                            }
                                            if (!department.equalsIgnoreCase("") && department != null && !department.equalsIgnoreCase("null")) {
                                                sqlusers += " and u.dep_id in (select id from departments alias where alias.department like '%" + department + "%'   )";
                                            }
                                            if (!userId.equalsIgnoreCase("") && userId != null) {
                                                sqlusers += " and u.user_id like '%" + userId + "%'";
                                            }
                                            if (!id.equalsIgnoreCase("") && id != null) {
                                                sqlusers += " and u.user_id =" + id;
                                            }
                                            List<UsersEntity> usersListAll
                                                    = (List<UsersEntity>) entityManager.createNativeQuery(
                                                    sqlusers, UsersEntity.class).getResultList();
                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                if(orderCol.equalsIgnoreCase("role")){
                                                    sqlusers +=  " order by (select name from roles r where r.role_id=u.role_id)"+ descAsc;
                                                }else if (orderCol.equalsIgnoreCase("organization")){
                                                    sqlusers +=  " order by (select name from organizations r where r.organization_id=u.org_id)"+ descAsc;
                                                }else{
                                                    sqlusers += " order by " + orderCol + " " + descAsc;
                                                }

                                            } else {
                                                sqlusers += " order by creation_date desc";
                                            }
                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlusers += " limit " + start + "," + limit;
                                            }
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> ufinalList = new ArrayList<HashMap<String, Object>>();
                                            List<UsersEntity> uList
                                                    = (List<UsersEntity>) entityManager.createNativeQuery(
                                                    sqlusers, UsersEntity.class).getResultList();
                                            for (UsersEntity j : uList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("firstname", j.getFirstname());
                                                sHmpam.put("lastname", j.getLastname());
                                                try {
                                                    sHmpam.put("password", decrypt(j.getPassword()));
                                                } catch (Exception e) {
                                                    returnList_future.put("status", "error");
                                                    returnList_future.put("message", "Πρόβλημα κατά την ανάκτηση δεδομένων,παρακαλώ επικοινωνήστε με τον διαχειριστή του συστήματος");
                                                    return returnList_future;
                                                }
                                                sHmpam.put("email", j.getEmail());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("telephone", j.getPhone());
                                                sHmpam.put("position", j.getPosition());
                                                sHmpam.put("statusUser", j.getStatus());
                                                sHmpam.put("userId", j.getUserId());
                                                sHmpam.put("comments", j.getComments());
                                                sHmpam.put("gender", j.getGender());
                                                sHmpam.put("mobilePhone", j.getMobilePhone());
                                                sHmpam.put("orgId", j.getOrgId());
                                                sHmpam.put("roleId", j.getRoleId());
                                                sHmpam.put("depId", j.getDepId());
                                                if (j.getOrgId() != null && j.getOrgId()!=0) {
                                                    HashMap<String, Object> orgMap = new HashMap<>();
                                                    OrganizationsEntity organizationsEntity = entityManager.find(OrganizationsEntity.class, j.getOrgId());
                                                    orgMap.put("name", organizationsEntity.getName());
                                                    orgMap.put("id", organizationsEntity.getOrganizationId());
                                                    sHmpam.put("organization", orgMap);
                                                    sHmpam.put("orgName", organizationsEntity.getName());
                                                }
                                                if (j.getRoleId() != null && j.getRoleId()!=0) {
                                                    HashMap<String, Object> roleMap = new HashMap<>();
                                                    RolesEntity rolesEntity = entityManager.find(RolesEntity.class, j.getRoleId());
                                                    roleMap.put("name", rolesEntity.getName());
                                                    roleMap.put("id", rolesEntity.getRoleId());
                                                    sHmpam.put("role", roleMap);
                                                    sHmpam.put("roleName", rolesEntity.getName());
                                                }
                                                if (j.getDepId() != null && j.getDepId()!=0) {
                                                    HashMap<String, Object> depMap = new HashMap<>();
                                                    DepartmentsEntity departmentsEntity = entityManager.find(DepartmentsEntity.class, j.getDepId());
                                                    depMap.put("name", departmentsEntity.getDepartment());
                                                    depMap.put("id", departmentsEntity.getId());
                                                    sHmpam.put("department", depMap);
                                                    sHmpam.put("depName", departmentsEntity.getDepartment());
                                                }
                                                sHmpam.put("token", j.getToken());
                                                sHmpam.put("username", j.getUsername());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                ufinalList.add(sHmpam);
                                            }
                                            returnList_future.put("data", ufinalList);
                                            returnList_future.put("total", usersListAll.size());
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


}
