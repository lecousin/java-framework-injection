package net.lecousin.framework.injection.tests;

import net.lecousin.framework.core.test.LCCoreAbstractTest;
import net.lecousin.framework.injection.InjectionContext;
import net.lecousin.framework.injection.InjectionException;
import net.lecousin.framework.injection.InjectionSerializationRule;

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
	
}
