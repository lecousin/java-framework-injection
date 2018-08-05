package net.lecousin.framework.injection.tests;

import org.junit.Assert;
import org.junit.Test;

import net.lecousin.framework.concurrent.synch.ISynchronizationPoint;
import net.lecousin.framework.core.test.LCCoreAbstractTest;
import net.lecousin.framework.injection.InjectionContext;
import net.lecousin.framework.injection.InjectionXmlConfiguration;
import net.lecousin.framework.injection.test.attributes.AttributesContainer;
import net.lecousin.framework.injection.test.attributes.AttributesInterface;
import net.lecousin.framework.math.RangeInteger;

public class TestInjectionAttributes extends LCCoreAbstractTest {

	@Test(timeout=30000)
	public void test() throws Exception {
		InjectionContext ctx = new InjectionContext();
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-attributes.xml");
		cfg.blockThrow(0);
		AttributesContainer a = (AttributesContainer)ctx.getObject(AttributesInterface.class);
		Assert.assertEquals(12, a.b1);
		Assert.assertEquals(34, a.b2.byteValue());
		Assert.assertEquals(123, a.s1);
		Assert.assertEquals(456, a.s2.shortValue());
		Assert.assertEquals(1234, a.i1);
		Assert.assertEquals(5678, a.i2.intValue());
		Assert.assertEquals(12345, a.l1);
		Assert.assertEquals(54321, a.l2.longValue());
		Assert.assertTrue(a.bool1);
		Assert.assertFalse(a.bool2.booleanValue());
		Assert.assertNull(a.strNull);
		Assert.assertEquals("ok", a.str);
		Assert.assertEquals(4, a.integers.size());
		Assert.assertEquals(1, a.integers.get(0).intValue());
		Assert.assertEquals(23, a.integers.get(1).intValue());
		Assert.assertEquals(456, a.integers.get(2).intValue());
		Assert.assertEquals(7890, a.integers.get(3).intValue());
		Assert.assertEquals(4, a.strings.size());
		Assert.assertEquals("ok", a.strings.get(0));
		Assert.assertEquals("3", a.strings.get(1));
		Assert.assertNull(a.strings.get(2));
		Assert.assertEquals("ok again", a.strings.get(3));
		Assert.assertEquals(2, a.days);
		Assert.assertEquals(3, a.hours);
		Assert.assertEquals(10, a.minutes);
		Assert.assertEquals(180, a.seconds);
		Assert.assertEquals(2, a.ranges.size());
		Assert.assertEquals(new RangeInteger(10, 20), a.ranges.removeFirst());
		Assert.assertEquals(new RangeInteger(31, 39), a.ranges.removeFirst());
		Assert.assertEquals(new RangeInteger(51, 1664), a.range);
		Assert.assertEquals(51, a.custom.intValue());
		Assert.assertEquals(0, a.custom2.intValue());
	}
	
}
