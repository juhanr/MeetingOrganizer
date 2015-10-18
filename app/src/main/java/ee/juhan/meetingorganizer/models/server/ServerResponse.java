package ee.juhan.meetingorganizer.models.server;

public class ServerResponse {

	private ServerResult result;
	private String sid;
	private Integer userId;

	public final ServerResult getResult() {
		return result;
	}

	public final void setResult(ServerResult result) {
		this.result = result;
	}

	public final String getSid() {
		return sid;
	}

	public final void setSid(String sid) {
		this.sid = sid;
	}

	public final Integer getUserId() {
		return userId;
	}

	public final void setUserId(Integer userId) {
		this.userId = userId;
	}
}
