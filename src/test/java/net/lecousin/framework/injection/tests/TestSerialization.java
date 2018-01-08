package net.lecousin.framework.injection.tests;

import java.util.Collections;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.application.LCCore;
import net.lecousin.framework.concurrent.synch.ISynchronizationPoint;
import net.lecousin.framework.core.test.LCCoreAbstractTest;
import net.lecousin.framework.injection.Injection;
import net.lecousin.framework.injection.InjectionContext;
import net.lecousin.framework.injection.InjectionSerializationRule;
import net.lecousin.framework.injection.InjectionXmlConfiguration;
import net.lecousin.framework.injection.test.annotations.Impl3Dev;
import net.lecousin.framework.injection.test.annotations.Impl3Prod;
import net.lecousin.framework.injection.test.serialization.MyObject;
import net.lecousin.framework.io.IO.Seekable.SeekType;
import net.lecousin.framework.io.buffering.MemoryIO;
import net.lecousin.framework.io.serialization.TypeDefinition;
import net.lecousin.framework.xml.serialization.XMLDeserializer;
import net.lecousin.framework.xml.serialization.XMLSerializer;

import org.junit.Assert;
import org.junit.Test;

public class TestSerialization extends LCCoreAbstractTest {

	@Test(timeout=30000)
	public void test() throws Exception {
		Application app = LCCore.getApplication();
		// serialize with DEV
		app.setProperty("env", "DEV");
		InjectionContext ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		ISynchronizationPoint<Exception> cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-annotations.xml");
		cfg.blockThrow(0);
		MyObject o1 = new MyObject();
		o1.value = 111;
		Injection.inject(ctx, o1);
		Assert.assertTrue(o1.i3 instanceof Impl3Dev);
		MemoryIO io = new MemoryIO(10000, "test");
		new XMLSerializer(null, "test", null).serialize(o1, new TypeDefinition(MyObject.class), io, Collections.singletonList(new InjectionSerializationRule(ctx))).blockThrow(0);
		io.seekSync(SeekType.FROM_BEGINNING, 0);
		
		// deserialize with PROD
		app.setProperty("env", "PROD");
		ctx = app.getInstance(InjectionContext.class);
		ctx = new InjectionContext(ctx);
		cfg = InjectionXmlConfiguration.configure(ctx, "test-injection-annotations.xml");
		cfg.blockThrow(0);
		MyObject o2 = (MyObject)new XMLDeserializer(null, "test").deserialize(new TypeDefinition(MyObject.class), io, Collections.singletonList(new InjectionSerializationRule(ctx))).blockResult(0);
		io.close();
		Assert.assertEquals(111, o2.value);
		Assert.assertTrue(o2.i3 instanceof Impl3Prod);
	}
	
}
