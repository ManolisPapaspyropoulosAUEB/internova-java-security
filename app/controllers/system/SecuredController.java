package controllers.system;

import com.fasterxml.jackson.databind.JsonNode;
import play.api.mvc.Codec;
import play.mvc.Controller;


import play.mvc.Result;

import javax.persistence.EntityManager;

/**
 * Created by aioannidis on 27/11/2014.
 * This controller is the parent of all of our controllers.
 * This class runs all of its methods with an Action handler called
 * SecurityActionHandler and secures the request-response process.
 */
public abstract class SecuredController extends Controller {

//    static Codec utf8 = Codec.javaSupported("utf-8");

    public static Result ok(JsonNode content) {

        System.out.println(content);


        //-------
        return ok(content);
    }
}
