package net.lecousin.framework.injection.tests;

import net.lecousin.framework.core.test.LCCoreAbstractTest;
import net.lecousin.framework.injection.InitMethod;
import net.lecousin.framework.injection.Injection;
import net.lecousin.framework.injection.InjectionContext;
import net.lecousin.framework.injection.InjectionException;
import net.lecousin.framework.injection.InjectionSerializationRule;
import net.lecousin.framework.injection.ObjectMethod;
import net.lecousin.framework.injection.ObjectValueFromString;

import java.util.Arrays;

import org.junit.Test;

public class TestBasics extends LCCoreAbstractTest {

	@SuppressWarnings("unused")
	@Test(timeout=30000)
	public void testInjectionException() {
		new InjectionException("test");
		new InjectionException("test", new Exception());
	}

	@Test(timeout=30000)
	public void testInjectionRule() {
		InjectionContext context = new InjectionContext();
		InjectionSerializationRule rule = new InjectionSerializationRule(context);
		rule.isEquivalent(rule);
		rule.isEquivalent(null);
	}
	
	public static class NotInstantiable1 {
		
		private NotInstantiable1() {
		}
		
		public NotInstantiable1(int i) {
		}
		
	}
	
	public static class NotInstantiable2 {
		
		private NotInstantiable2() {
		}
		
		public NotInstantiable2(String s) {
			throw new Error("an error");
		}
		
	}
	
	public static abstract class NotInstantiable3 {
		
		private NotInstantiable3() {
		}
		
		public NotInstantiable3(String s) {
		}
		
	}
	
	@Test(timeout=30000)
	public void testNotInstantiable() {
		InjectionContext context = new InjectionContext();
		try {
			Injection.create(context, NotInstantiable1.class, null, null, null);
			throw new AssertionError("Should throw an error for class NotInstantiable");
		} catch (InjectionException e) {
			// ok
		}
		try {
			Injection.create(context, NotInstantiable1.class, Arrays.asList(new ObjectValueFromString("not a number")), null, null);
			throw new AssertionError("Should throw an error for class NotInstantiable");
		} catch (InjectionException e) {
			// ok
		}
		try {
			Injection.create(context, NotInstantiable2.class, Arrays.asList(new ObjectValueFromString("not a number")), null, null);
			throw new AssertionError("Should throw an error for class NotInstantiable");
		} catch (InjectionException e) {
			// ok
		}
		try {
			Injection.create(context, NotInstantiable3.class, Arrays.asList(new ObjectValueFromString("not a number")), null, null);
			throw new AssertionError("Should throw an error for class NotInstantiable");
		} catch (InjectionException e) {
			// ok
		}
	}
	
	public static class InitMethodError {
		
		@InitMethod
		public void initWithError() {
			throw new Error("error");
		}

		public void initWithError2() {
			throw new Error("error");
		}
		
	}

	public static class InitMethodError2 {
		
		public void initWithError2() {
			throw new Error("error");
		}
		
	}
	
	public static class InitMethodError3 {
		
		@InitMethod
		public void notAValidInit(int i) {
			throw new Error("Should not be called");
		}
		
	}

	@Test(timeout=30000)
	public void testInitMethodError() throws InjectionException {
		InjectionContext context = new InjectionContext();
		try {
			Injection.create(context, InitMethodError.class, null, null, null);
			throw new AssertionError("Should throw an error for class InitMethodError");
		} catch (InjectionException e) {
			// ok
		}
		try {
			Injection.create(context, InitMethodError2.class, null, new ObjectMethod("initWithError2", null), null);
			throw new AssertionError("Should throw an error for class InitMethodError2");
		} catch (InjectionException e) {
			// ok
		}
		Injection.create(context, InitMethodError3.class, null, null, null);
	}
	
}
