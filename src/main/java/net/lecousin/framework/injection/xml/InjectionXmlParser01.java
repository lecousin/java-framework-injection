package net.lecousin.framework.injection.xml;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.application.LCCore;
import net.lecousin.framework.injection.Factory;
import net.lecousin.framework.injection.Injectable;
import net.lecousin.framework.injection.InjectableWhen;
import net.lecousin.framework.injection.Injection;
import net.lecousin.framework.injection.InjectionContext;
import net.lecousin.framework.injection.ObjectAttribute;
import net.lecousin.framework.injection.ObjectMethod;
import net.lecousin.framework.injection.ObjectValue;
import net.lecousin.framework.injection.ObjectValueClass;
import net.lecousin.framework.injection.ObjectValueFromSingletonAttributeById;
import net.lecousin.framework.injection.ObjectValueFromSingletonAttributeByType;
import net.lecousin.framework.injection.ObjectValueFromString;
import net.lecousin.framework.injection.ObjectValueList;
import net.lecousin.framework.injection.ObjectValueMap;
import net.lecousin.framework.injection.ObjectValueRefId;
import net.lecousin.framework.injection.ObjectValueRefType;
import net.lecousin.framework.injection.Singleton;
import net.lecousin.framework.properties.Property;
import net.lecousin.framework.util.ClassUtil;
import net.lecousin.framework.util.UnprotectedStringBuffer;
import net.lecousin.framework.xml.XMLStreamEvents.ElementContext;
import net.lecousin.framework.xml.XMLStreamReader;

/** Parse for version 0.1. */
public class InjectionXmlParser01 implements InjectionXmlParser {
	
	@Override
	public void configureInjection(InjectionContext ctx, XMLStreamReader xml) throws Exception {
		Application app = LCCore.getApplication();
		ElementContext elem = xml.event.context.getFirst();
		while (xml.nextInnerElement(elem)) {
			if (xml.event.localName.equals("Singleton"))
				configureObjectProvider(ctx, app, xml, true);
			else if (xml.event.localName.equals("Factory"))
				configureObjectProvider(ctx, app, xml, false);
			else if (xml.event.localName.equals("property"))
				configureProperty(ctx, app, xml);
			else if (xml.event.localName.equals("scan-package"))
				configureScanPackage(ctx, app, xml);
			else if (xml.event.localName.equals("import"))
				configureImport(ctx, app, xml);
			else
				throw new Exception("Unexpected element " + xml.event.text.asString() + " in Injection");
		}
	}
	
	private static void configureProperty(InjectionContext ctx, Application app, XMLStreamReader xml) throws Exception {
		UnprotectedStringBuffer name = xml.getAttributeValueByLocalName("name");
		if (name == null) throw new Exception("Missing name of property");
		UnprotectedStringBuffer value = xml.getAttributeValueByLocalName("value");
		if (value == null) throw new Exception("Missing value of property");
		String val = Injection.resolveProperties(ctx, app, value.asString());
		ctx.setProperty(name.asString(), val);
		if (!xml.event.isClosed)
			xml.closeElement();
	}
	
	private static void configureObjectProvider(InjectionContext ctx, Application app, XMLStreamReader xml, boolean isSingleton)
	throws Exception {
		UnprotectedStringBuffer id = xml.getAttributeValueByLocalName("id");
		String typeStr = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("type"));
		Class<?> type = typeStr != null ? app.getClassLoader().loadClass(typeStr) : null;
		Class<?> cl = null;
		List<ObjectAttribute> attributes = null;
		ObjectMethod initMethod = null;
		ObjectMethod destroyMethod = null;
		ElementContext elem = xml.event.context.getFirst();
		while (xml.nextInnerElement(elem)) {
			if (xml.event.localName.equals("class")) {
				UnprotectedStringBuffer n = xml.getAttributeValueByLocalName("name");
				if (n == null) throw new Exception("Missing attribute name on element class");
				String name = Injection.resolveProperties(ctx, app, n.asString());
				boolean eligible = true;
				List<ObjectAttribute> attrs = new LinkedList<>();
				ObjectMethod init = null;
				ObjectMethod destroy = null;
				ElementContext elemClass = xml.event.context.getFirst();
				while (xml.nextInnerElement(elemClass)) {
					if (xml.event.localName.equals("property")) {
						UnprotectedStringBuffer pname = xml.getAttributeValueByLocalName("name");
						if (pname == null) throw new Exception("Missing attribute name on element property");
						String pvalue = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("value"));
						if (pvalue == null) throw new Exception("Missing attribute value on property " + pname);
						eligible &= pvalue.equals(Injection.getProperty(app, ctx, pname.asString()));
					} else if (xml.event.localName.equals("attribute")) {
						attrs.add(readObjectAttribute(ctx, xml, app));
					} else if (xml.event.localName.equals("init-method")) {
						if (init != null) throw new Exception("Only one init-method can be specified for class " + name);
						init = readObjectMethod(ctx, xml, app);
					} else if (isSingleton && xml.event.localName.equals("destroy-method")) {
						if (destroy != null)
							throw new Exception("Only one destroy-method can be specified for class " + name);
						destroy = readObjectMethod(ctx, xml, app);
						if (!destroy.getParameters().isEmpty())
							throw new Exception("destroy-method cannot have parameters");
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
				if (destroy != null) {
					List<Method> list = ClassUtil.getMethods(cl, destroy.getName(), 0);
					if (list.isEmpty())
						throw new Exception("Destroy method " + destroy.getName()
							+ " does not exist on class " + cl.getName());
				}
				initMethod = init;
				destroyMethod = destroy;
				attributes = attrs;
				break;
			}
			throw new Exception("Unexpected element " + xml.event.text.asString());
		}
		if (cl == null || type == null)
			return;
		if (isSingleton)
			ctx.add(new Singleton(ctx, type, Injection.create(ctx, cl, initMethod, attributes),
				id != null ? id.asString() : null, destroyMethod));
		else
			ctx.add(new Factory(ctx, type, cl, initMethod, attributes, id != null ? id.asString() : null));
	}
	
	private static void configureScanPackage(InjectionContext ctx, Application app, XMLStreamReader xml) throws Exception {
		String pkgName = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("package"));
		if (pkgName == null) throw new Exception("Missing package attribute on element scan-package");
		boolean singletons = true;
		UnprotectedStringBuffer s = xml.getAttributeValueByLocalName("singleton");
		if (s != null && s.toLowerCase().equals("false"))
			singletons = false;
		boolean eligible = true;
		ElementContext elem = xml.event.context.getFirst();
		while (xml.nextInnerElement(elem)) {
			if (xml.event.localName.equals("property")) {
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

	private static void configureImport(InjectionContext ctx, Application app, XMLStreamReader xml) throws Exception {
		String filename = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("file"));
		if (filename == null) throw new Exception("Missing file attribute on element import");
		boolean eligible = true;
		ElementContext elem = xml.event.context.getFirst();
		while (xml.nextInnerElement(elem)) {
			if (xml.event.localName.equals("property")) {
				UnprotectedStringBuffer pname = xml.getAttributeValueByLocalName("name");
				if (pname == null) throw new Exception("Missing attribute name on element property");
				String pvalue = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("value"));
				if (pvalue == null) throw new Exception("Missing attribute value on property " + pname);
				eligible &= pvalue.equals(Injection.getProperty(app, ctx, pname.asString()));
			} else
				throw new Exception("Unexpected element " + xml.event.text.asString());
		}
		if (!eligible) return;
		InjectionXmlConfiguration.configure(ctx, filename).blockThrow(0);
	}
	
	/** Read an attribute from the given XML which must be on the attribute element. */
	public static ObjectAttribute readObjectAttribute(InjectionContext ctx, XMLStreamReader xml, Application app) throws Exception {
		UnprotectedStringBuffer name = xml.getAttributeValueByLocalName("name");
		if (name == null) throw new Exception("Missing attribute name on element 'attribute'");
		ObjectValue value = readObjectValue(ctx, xml, app);
		return new ObjectAttribute(name.asString(), value);
	}
	
	/** Read an ObjectValue from the given XML. */
	public static ObjectValue readObjectValue(InjectionContext ctx, XMLStreamReader xml, Application app) throws Exception {
		String s = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("value"));
		if (s != null)
			return new ObjectValueFromString(s);
		
		s = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("ref"));
		if (s != null)
			return new ObjectValueRefId(s);
		
		s = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("refType"));
		if (s != null)
			return new ObjectValueRefType(app.getClassLoader().loadClass(s));

		s = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("class"));
		if (s != null) {
			Class<?> cl = app.getClassLoader().loadClass(s);
			List<ObjectValue> params = new LinkedList<>();
			List<ObjectAttribute> attrs = new LinkedList<>();
			if (!xml.event.isClosed) {
				ElementContext elem = xml.event.context.getFirst();
				while (xml.nextInnerElement(elem)) {
					if (xml.event.localName.equals("parameter")) {
						params.add(readObjectValue(ctx, xml, app));
					} else if (xml.event.localName.equals("attribute")) {
						attrs.add(readObjectAttribute(ctx, xml, app));
					} else
						throw new Exception("Unexpected element " + xml.event.text.asString());
				}
			}
			return new ObjectValueClass(cl, params, attrs);
		}
		
		if (xml.event.isClosed)
			throw new Exception("Invalid element " + xml.event.text.asString() + ": no value specified");
		
		ElementContext elem = xml.event.context.getFirst();
		while (xml.nextInnerElement(elem)) {
			if (xml.event.localName.equals("from")) {
				String attrName = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("attribute"));
				if (attrName == null)
					throw new Exception("Missing attribute 'attribute' on element 'from'");
				s = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("ref"));
				if (s != null) {
					xml.closeElement();
					return new ObjectValueFromSingletonAttributeById(attrName, s);
				}
				s = Injection.resolveProperties(ctx, app, xml.getAttributeValueByLocalName("refType"));
				if (s != null) {
					xml.closeElement();
					return new ObjectValueFromSingletonAttributeByType(attrName, app.getClassLoader().loadClass(s));
				}
				throw new Exception("Missing attribute 'ref' or 'refType' on element 'from'");
			} else if (xml.event.localName.equals("list")) {
				ElementContext elemList = xml.event.context.getFirst();
				List<ObjectValue> list = new LinkedList<>();
				while (xml.nextInnerElement(elemList)) {
					if (xml.event.localName.equals("element")) {
						list.add(readObjectValue(ctx, xml, app));
					} else 
						throw new Exception("Unexpected element " + xml.event.text.asString() + " in list");
				}
				return new ObjectValueList(list);
			} else if (xml.event.localName.equals("map")) {
				ElementContext elemList = xml.event.context.getFirst();
				List<ObjectAttribute> list = new LinkedList<>();
				while (xml.nextInnerElement(elemList)) {
					if (xml.event.localName.equals("entry")) {
						list.add(readObjectAttribute(ctx, xml, app));
					} else 
						throw new Exception("Unexpected element " + xml.event.text.asString() + " in map");
				}
				return new ObjectValueMap(list);
			} else
				throw new Exception("Unexpected element " + xml.event.text.asString() + " in " + elem.localName);
		}
		
		throw new Exception("Invalid element " + xml.event.text.asString() + ": no value specified");
	}
	
	/** Read an object method from an XML stream. */
	public static ObjectMethod readObjectMethod(InjectionContext ctx, XMLStreamReader xml, Application app) throws Exception {
		UnprotectedStringBuffer name = xml.getAttributeValueByLocalName("name");
		if (name == null) throw new Exception("Missing attribute name on element '" + xml.event.text.asString() + "'");
		List<ObjectValue> params = new LinkedList<>();
		if (!xml.event.isClosed) {
			ElementContext elem = xml.event.context.getFirst();
			while (xml.nextInnerElement(elem)) {
				if (xml.event.localName.equals("parameter")) {
					params.add(readObjectValue(ctx, xml, app));
				} else
					throw new Exception("Unexpected element " + xml.event.text.asString());
			}
		}
		return new ObjectMethod(name.asString(), params);
	}

}
