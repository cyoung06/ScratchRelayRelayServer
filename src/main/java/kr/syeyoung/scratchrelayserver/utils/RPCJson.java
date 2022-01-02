package kr.syeyoung.scratchrelayserver.utils;

import org.json.JSONObject;

import java.util.Objects;

public class RPCJson {
    public static JSONObject createResponse(JSONObject object, JSONObject result) {
        return new JSONObject().put("jsonrpc", "2.0").put("id", Objects.requireNonNull(object).get("id")).put("result", result == null ? JSONObject.NULL : result);
    }

    public static JSONObject createRequest(String method, JSONObject params) {
        return new JSONObject().put("jsonrpc", "2.0").put("method",method).put("params", params);
    }
}
