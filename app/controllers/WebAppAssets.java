package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.io.File;

public class WebAppAssets extends Controller {
    public Result at(String path, String filePath) {
        System.out.println("path "+path);
        System.out.println("filePath "+filePath);
        System.out.println(path + filePath);
        File file = new File(path + filePath);
        if(file.length() > 0) {
            return ok(file, true);
        }
        else {
            if(filePath.equals("getCollections")){
                return redirect("/getCollections");
            } else
                return ok(index.render());
        }
    }
}

