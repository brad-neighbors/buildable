package com.incandescent.buildable.example;

import com.incandescent.buildable.Builder;
import java.lang.reflect.Field;


public class AccountBuilder implements Builder<Account> {

	public static AccountBuilder anAccount() {
		return new AccountBuilder();
	}

	private AccountBuilder() {}

	private java.lang.String name = null;
	public AccountBuilder named(java.lang.String name) {
		this.name = name;
		return this;
	}

	public Account build() {
		try {
			final Class clazz = Class.forName(Account.class.getCanonicalName());
			final Account instance = (Account) clazz.newInstance();

			final Field nameField = clazz.getDeclaredField("name");
			nameField.setAccessible(true);
			nameField.set(instance, name);
			nameField.setAccessible(false);

			return instance;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
		return null;
	}
}
