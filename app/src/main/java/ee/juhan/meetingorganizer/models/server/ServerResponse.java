package ee.juhan.meetingorganizer.models.server;

public class ServerResponse {

	private ServerResult result;
	private String sid;
	private Account account;

	public ServerResponse(ServerResult result, String sid, Account account) {
		this.result = result;
		this.sid = sid;
		this.account = account;
	}

	public ServerResponse(ServerResult result) {
		this.result = result;
	}

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

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}
}
