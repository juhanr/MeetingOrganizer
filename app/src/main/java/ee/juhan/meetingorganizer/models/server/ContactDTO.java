package ee.juhan.meetingorganizer.models.server;

public class ContactDTO {

	private int accountId;
	private String name;
	private String email;
	private String phoneNumber;

	public ContactDTO() {

	}

	public ContactDTO(String name, String email, String phoneNumber) {
		this.name = name;
		this.email = email;
		this.phoneNumber = phoneNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }
		ContactDTO that = (ContactDTO) o;
		return phoneNumber.equals(that.phoneNumber);

	}

	public final int getAccountId() {
		return accountId;
	}

	public final void setAccountId(int accountId) {
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

	public final String getPhoneNumber() {
		return phoneNumber;
	}

	public final void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
}
