package ee.juhan.meetingorganizer.models.server;

public class Account {

	private int accountId;
	private String name;
	private String email;
	private String password;
	private String phoneNumber;

	public Account() {}

	public Account(String name, String email, String password, String phoneNumber) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.phoneNumber = phoneNumber;
	}

	public Account(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final String getEmail() {
		return email;
	}

	public final void setEmail(String email) {
		this.email = email;
	}

	public final String getPassword() {
		return password;
	}

	public final void setPassword(String password) {
		this.password = password;
	}

	public final String getPhoneNumber() {
		return phoneNumber;
	}

	public final void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

}
