package net.lecousin.framework.injection;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.application.LCCore;
import net.lecousin.framework.concurrent.Task;
import net.lecousin.framework.concurrent.synch.ISynchronizationPoint;
import net.lecousin.framework.concurrent.synch.SynchronizationPoint;
import net.lecousin.framework.exception.NoException;
import net.lecousin.framework.io.IO;
import net.lecousin.framework.io.buffering.PreBufferedReadable;
import net.lecousin.framework.io.provider.IOProvider;
import net.lecousin.framework.properties.Property;
import net.lecousin.framework.util.ClassUtil;
import net.lecousin.framework.util.UnprotectedStringBuffer;
import net.lecousin.framework.xml.XMLStreamEvents;
import net.lecousin.framework.xml.XMLStreamEvents.ElementContext;
import net.lecousin.framework.xml.XMLStreamReader;

/** Utility class to configure an InjectionContext from an XML file. */
public final class InjectionXmlConfiguration {
	
	private InjectionXmlConfiguration() { /* no instance */ }
	
	public static final String NAMESPACE_URI_0_1 = "http://code.lecousin.net/java/net.lecousin.framework.injection/0.1";
	
	/** Configure the given InjectionContext with the XML file. */
	@SuppressWarnings("resource")
	public static ISynchronizationPoint<Exception> configure(InjectionContext ctx, String filename) {
		IOProvider.Readable provider = LCCore.getApplication().getClassLoader().getIOProvider(filename);
		if (provider == null)
			return new SynchronizationPoint<>(new FileNotFoundException(filename));
		try {
			return configure(ctx, provider.provideIOReadable(Task.PRIORITY_RATHER_IMPORTANT));
		} catch (IOException e) {
			return new SynchronizationPoint<>(e);
		}
	}
	
	/** Configure the given InjectionContext with the XML file. */
	@SuppressWarnings("resource")
	public static ISynchronizationPoint<Exception> configure(InjectionContext ctx, IO.Readable xml) {
		IO.Readable.Buffered bio;
		if (xml instanceof IO.Readable.Buffered)
			bio = (IO.Readable.Buffered)xml;
		else
			bio = new PreBufferedReadable(xml, 16384, Task.PRIORITY_RATHER_IMPORTANT, 16384, Task.PRIORITY_NORMAL, 4);
		SynchronizationPoint<Exception> result = new SynchronizationPoint<>();
		XMLStreamReader.start(bio, 15000).listenInline(
			(reader) -> {
				new Task.Cpu<Void, NoException>("Parsing Injection XML file", Task.PRIORITY_NORMAL) {
					@Override
					public Void run() {
						try {
							configure(ctx, reader);
							result.unblock();
						} catch (Exception e) {
							result.error(e);
						} finally {
							xml.closeAsync();
						}
						return null;
					}
				}.start();
			},
			(error) -> {
				xml.closeAsync();
				result.error(error);
			},
			(cancel) -> {
				xml.closeAsync();
				result.cancel(cancel);
			}
		);
		return result;
	}

	private static void configure(InjectionContext ctx, XMLStreamReader xml) throws Exception {
		while (!XMLStreamEvents.Event.Type.START_ELEMENT.equals(xml.event.type)) {
			try { xml.next(); }
			catch (EOFException e) {
				throw new Exception("Invalid XML: no root element");
			}
		}
		configureInjection(ctx, xml);
	}
	
	/** Configure the given context with the given XML which must be on an Injection element. */
	public static void configureInjection(InjectionContext ctx, XMLStreamReader xml) throws Exception {
		if (!xml.event.localName.equals("Injection"))
			throw new Exception("Invalid XML: root element must be Injection");
		if (xml.event.namespaceURI.equals(NAMESPACE_URI_0_1))
			configureInjection01(ctx, xml);
		else
			throw new Exception("Unknown injection namespace: " + xml.event.namespaceURI);
	}
	
	private static void configureInjection01(InjectionContext ctx, XMLStreamReader xml) throws Exception {
		Application app = LCCore.getApplication();
		ElementContext elem = xml.event.context.getFirst();
		while (xml.nextInnerElement(elem)) {
			if (xml.event.text.equals("Singleton"))
				configureObjectProvider01(ctx, app, xml, true);
			else if (xml.event.text.equals("Factory"))
				configureObjectProvider01(ctx, app, xml, false);
			else if (xml.event.text.equals("property"))
				configureProperty01(ctx, app, xml);
			else if (xml.event.text.equals("scan-package"))
				configureScanPackage01(ctx, app, xml);
			else if (xml.event.text.equals("import"))
				configureImport01(ctx, app, xml);
			else
				throw new Exception("Unexpected element " + xml.event.text.asString() + " in Injection");
		}
	}
	
	private static void configureProperty01(InjectionContext ctx, Application app, XMLStreamReader xml) throws Exception {
		UnprotectedStringBuffer name = xml.getAttributeValueByLocalName("name");
		if (name == null) throw new Exception("Missing name of property");
		UnprotectedStringBuffer value = xml.getAttributeValueByLocalName("value");
		if (value == null) throw new Exception("Missing value of property");
		String val = Injection.resolveProperties(ctx, app, value.asString());
		ctx.setProperty(name.asString(), val);
		if (!xml.event.isClosed)
			xml.closeElement();
	}
	
	private static void configureObjectProvider01(InjectionContext ctx, Application app, XMLStreamReader xml, boolean isSingleton)
	throws Exception {
		UnprotectedStringBuffer id = xml.getAttributeValueByLocalName("id");
		String typeStr = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("type"));
		Class<?> type = typeStr != null ? app.getClassLoader().loadClass(typeStr) : null;
		Class<?> cl = null;
		List<ObjectAttribute> attributes = null;
		ObjectMethod initMethod = null;
		ElementContext elem = xml.event.context.getFirst();
		while (xml.nextInnerElement(elem)) {
			if (xml.event.text.equals("class")) {
				UnprotectedStringBuffer n = xml.getAttributeValueByLocalName("name");
				if (n == null) throw new Exception("Missing attribute name on element class");
				String name = Injection.resolveProperties(ctx, app, n.asString());
				boolean eligible = true;
				List<ObjectAttribute> attrs = new LinkedList<>();
				ObjectMethod init = null;
				ElementContext elemClass = xml.event.context.getFirst();
				while (xml.nextInnerElement(elemClass)) {
					if (xml.event.text.equals("property")) {
						UnprotectedStringBuffer pname = xml.getAttributeValueByLocalName("name");
						if (pname == null) throw new Exception("Missing attribute name on element property");
						String pvalue = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("value"));
						if (pvalue == null) throw new Exception("Missing attribute value on property " + pname);
						eligible &= pvalue.equals(Injection.getProperty(app, ctx, pname.asString()));
					} else if (xml.event.text.equals("attribute")) {
						attrs.add(readObjectAttribute01(ctx, xml, app));
					} else if (xml.event.text.equals("init-method")) {
						if (init != null) throw new Exception("Only one init-method can be specified for class " + name);
						init = readObjectMethod01(xml);
					} else
						throw new Exception("Unexpected element " + xml.event.text.asString());
				}
				if (!eligible) continue;
				try { cl = app.getClassLoader().loadClass(name); }
				catch (Throwable t) { continue; }
				InjectableWhen when = cl.getAnnotation(InjectableWhen.class);
				if (when != null) {
					for (Property p : when.value()) {
						String pname = p.name();
						String pvalue = p.value();
						pvalue = Injection.resolveProperties(ctx, app, pvalue);
						if (!pvalue.equals(Injection.getProperty(app, ctx, pname))) {
							eligible = false;
							break;
						}
					}
				}
				if (!eligible) continue;
				if (type == null) {
					Injectable it = cl.getAnnotation(Injectable.class);
					if (it == null)
						type = cl;
					else
						type = it.value();
				}
				if (init != null) {
					List<Method> list = ClassUtil.getMethods(cl, init.getName(), init.getParameters().size());
					if (list.isEmpty())
						throw new Exception("Init method " + init.getName() + " does not exist on class " + cl.getName());
					if (list.size() > 1)
						throw new Exception("Init method " + init.getName() + " is ambiguous on class " + cl.getName());
				}
				initMethod = init;
				attributes = attrs;
				break;
			}
			throw new Exception("Unexpected element " + xml.event.text.asString());
		}
		if (cl == null || type == null)
			return;
		if (isSingleton)
			ctx.add(new Singleton(type, Injection.create(ctx, cl, initMethod, attributes), id != null ? id.asString() : null));
		else
			ctx.add(new Factory(type, cl, initMethod, attributes, id != null ? id.asString() : null));
	}
	
	private static void configureScanPackage01(InjectionContext ctx, Application app, XMLStreamReader xml) throws Exception {
		String pkgName = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("package"));
		if (pkgName == null) throw new Exception("Missing package attribute on element scan-package");
		boolean singletons = true;
		UnprotectedStringBuffer s = xml.getAttributeValueByLocalName("singleton");
		if (s != null && s.toLowerCase().equals("false"))
			singletons = false;
		boolean eligible = true;
		ElementContext elem = xml.event.context.getFirst();
		while (xml.nextInnerElement(elem)) {
			if (xml.event.text.equals("property")) {
				UnprotectedStringBuffer pname = xml.getAttributeValueByLocalName("name");
				if (pname == null) throw new Exception("Missing attribute name on element property");
				String pvalue = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("value"));
				if (pvalue == null) throw new Exception("Missing attribute value on property " + pname);
				eligible &= pvalue.equals(Injection.getProperty(app, ctx, pname.asString()));
			} else
				throw new Exception("Unexpected element " + xml.event.text.asString());
		}
		if (!eligible) return;
		Injection.scanPackage(ctx, app, pkgName, singletons);
	}

	private static void configureImport01(InjectionContext ctx, Application app, XMLStreamReader xml) throws Exception {
		String filename = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("file"));
		if (filename == null) throw new Exception("Missing file attribute on element import");
		boolean eligible = true;
		ElementContext elem = xml.event.context.getFirst();
		while (xml.nextInnerElement(elem)) {
			if (xml.event.text.equals("property")) {
				UnprotectedStringBuffer pname = xml.getAttributeValueByLocalName("name");
				if (pname == null) throw new Exception("Missing attribute name on element property");
				String pvalue = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("value"));
				if (pvalue == null) throw new Exception("Missing attribute value on property " + pname);
				eligible &= pvalue.equals(Injection.getProperty(app, ctx, pname.asString()));
			} else
				throw new Exception("Unexpected element " + xml.event.text.asString());
		}
		if (!eligible) return;
		configure(ctx, filename).blockThrow(0);
	}
	
	/** Read an attribute from the given XML which must be on the attribute element. */
	public static ObjectAttribute readObjectAttribute01(InjectionContext ctx, XMLStreamReader xml, Application app) throws Exception {
		UnprotectedStringBuffer name = xml.getAttributeValueByLocalName("name");
		if (name == null) throw new Exception("Missing attribute name on element 'attribute'");
		String value = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("value"));
		String ref = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("ref"));
		String clazz = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("class"));
		String fromSingleton = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("fromSingleton"));
		if (value != null) {
			if (ref != null) throw new Exception("Attributes value and ref are exclusive on element 'attribute'");
			if (clazz != null) throw new Exception("Attributes value and class are exclusive on element 'attribute'");
			if (fromSingleton != null) throw new Exception("Attributes value and fromSingleton are exclusive on element 'attribute'");
			if (!xml.event.isClosed) xml.closeElement();
			return new ObjectAttribute(name.asString(), value, null, null, null);
		}
		if (ref != null) {
			if (clazz != null) throw new Exception("Attributes ref and class are exclusive on element 'attribute'");
			if (fromSingleton != null) throw new Exception("Attributes ref and fromSingleton are exclusive on element 'attribute'");
			if (!xml.event.isClosed) xml.closeElement();
			return new ObjectAttribute(name.asString(), null, ref, null, null);
		}
		if (clazz != null) {
			if (fromSingleton != null) throw new Exception("Attributes class and fromSingleton are exclusive on element 'attribute'");
			Class<?> cl = app.getClassLoader().loadClass(clazz);
			List<String> params = new LinkedList<>();
			List<ObjectAttribute> attrs = new LinkedList<>();
			if (!xml.event.isClosed) {
				ElementContext elem = xml.event.context.getFirst();
				while (xml.nextInnerElement(elem)) {
					if (xml.event.text.equals("parameter")) {
						UnprotectedStringBuffer val = xml.getAttributeValueByLocalName("value");
						if (val == null) throw new Exception("Missing attribute value on element parameter");
						params.add(Injection.resolveProperties(ctx, app, val.asString()));
					} else if (xml.event.text.equals("attribute")) {
						attrs.add(readObjectAttribute01(ctx, xml, app));
					} else
						throw new Exception("Unexpected element " + xml.event.text.asString());
				}
			}
			return new ObjectAttribute(name.asString(), cl, params, attrs);
		}
		String fromAttr = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("fromAttribute"));
		if (fromAttr == null) throw new Exception("Missing fromAttribute with fromSingleton on element 'attribute'");
		return new ObjectAttribute(name.asString(), null, null, fromSingleton, fromAttr);
	}
	
	/** Read an object method from an XML stream (version 0-1). */
	public static ObjectMethod readObjectMethod01(XMLStreamReader xml) throws Exception {
		UnprotectedStringBuffer name = xml.getAttributeValueByLocalName("name");
		if (name == null) throw new Exception("Missing attribute name on element '" + xml.event.text.asString() + "'");
		List<String> params = new LinkedList<>();
		if (!xml.event.isClosed) {
			ElementContext elem = xml.event.context.getFirst();
			while (xml.nextInnerElement(elem)) {
				if (xml.event.text.equals("parameter")) {
					UnprotectedStringBuffer val = xml.getAttributeValueByLocalName("value");
					if (val == null) throw new Exception("Missing attribute value on element parameter");
					params.add(val.asString());
				} else
					throw new Exception("Unexpected element " + xml.event.text.asString());
			}
		}
		return new ObjectMethod(name.asString(), params);
	}

}
