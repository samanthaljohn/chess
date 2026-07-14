package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import dataaccess.DataAccessException;
import service.ClearService;

import java.util.Map;

public class ClearHandler extends ErrorHandler{
    private final ClearService clearService;

    public ClearHandler(ClearService clearService){
        this.clearService = clearService;
    }

    public void clear(Context context){
        try{
            clearService.clear();
            context.json(Map.of());
        } catch (DataAccessException e){
            handleError(context, 500, "Error: " + e.getMessage());
        }
    }
}