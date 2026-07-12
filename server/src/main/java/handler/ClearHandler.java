package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import dataaccess.DataAccessException;
import service.ClearService;

import java.util.Map;

public class ClearHandler {
    private final ClearService clearService;

    public ClearHandler(ClearService clearService){
        this.clearService = clearService;
    }

    public void clear(Context context){
        try{
            clearService.clear();
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of()));
        } catch (DataAccessException e){
            context.contentType("application/json");
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}