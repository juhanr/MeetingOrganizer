package ee.juhan.meetingorganizer.models.server;

public class ServerResponse {

    private ServerResult result;
    private String sid;
    private Integer userId;

    public ServerResponse(ServerResult result, String sid, Integer userId) {
        this.result = result;
        this.sid = sid;
        this.userId = userId;
    }

    public ServerResponse(ServerResult result) {
        this.result = result;
    }

    public ServerResult getResult() {
        return result;
    }

    public void setResult(ServerResult result) {
        this.result = result;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
