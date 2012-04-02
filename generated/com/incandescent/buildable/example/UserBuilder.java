package com.incandescent.buildable.example;

import com.incandescent.buildable.Builder;
import java.lang.reflect.Field;


public class UserBuilder implements Builder<User> {

	public static UserBuilder aUser() {
		return new UserBuilder();
	}

	private UserBuilder() {}

	private java.lang.String name = "John Doe";
	public UserBuilder named(java.lang.String name) {
		this.name = name;
		return this;
	}

	private java.lang.String email = "johnDoe@acme.com";
	public UserBuilder withEmail(java.lang.String email) {
		this.email = email;
		return this;
	}

	private java.lang.String ssn = null;
	public UserBuilder withSsn(java.lang.String ssn) {
		this.ssn = ssn;
		return this;
	}

	private java.lang.Integer zipCode = 94114;
	public UserBuilder livingInZip(java.lang.Integer zipCode) {
		this.zipCode = zipCode;
		return this;
	}

	public User build() {
		try {
			final Class clazz = Class.forName(User.class.getCanonicalName());
			final User instance = (User) clazz.newInstance();

			final Field nameField = clazz.getDeclaredField("name");
			nameField.setAccessible(true);
			nameField.set(instance, name);
			nameField.setAccessible(false);

			final Field emailField = clazz.getDeclaredField("email");
			emailField.setAccessible(true);
			emailField.set(instance, email);
			emailField.setAccessible(false);

			final Field ssnField = clazz.getDeclaredField("ssn");
			ssnField.setAccessible(true);
			ssnField.set(instance, ssn);
			ssnField.setAccessible(false);

			final Field zipCodeField = clazz.getDeclaredField("zipCode");
			zipCodeField.setAccessible(true);
			zipCodeField.set(instance, zipCode);
			zipCodeField.setAccessible(false);

			return instance;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
		return null;
	}
}
