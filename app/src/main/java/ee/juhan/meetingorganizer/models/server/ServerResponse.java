package ee.juhan.meetingorganizer.models.server;

public class ServerResponse {

	private ServerResult result;
	private String sid;
	private AccountDTO accountDTO;

	public ServerResponse(ServerResult result, String sid, AccountDTO accountDTO) {
		this.result = result;
		this.sid = sid;
		this.accountDTO = accountDTO;
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

	public AccountDTO getAccountDTO() {
		return accountDTO;
	}

	public void setAccountDTO(AccountDTO accountDTO) {
		this.accountDTO = accountDTO;
	}
}
